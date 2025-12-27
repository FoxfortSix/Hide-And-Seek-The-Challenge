package presenter;

import model.*;
import view.KontrakView;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Filename  : GamePresenter.java
 * Package   : presenter
 * Description:
 * The COMPLETE "Brain" of the application.
 * Updated Features:
 * 1. Metal Slug Style PowerUps (AR & Shotgun).
 * 2. Player Weapon System (Spray for AR, Spread for Shotgun).
 * 3. Intelligent Enemy AI (Burst AR, Spread Shotgun).
 * 4. Wave System.
 */
public class GamePresenter implements KontrakPresenter, Runnable {

    private KontrakView view;

    // Data Models
    private Player player;
    private List<Alien> aliens;
    private List<Bullet> bullets;
    private List<Obstacle> obstacles;
    private List<PowerUp> powerUps; // List untuk item PowerUp
    private TabelBenefit tabelBenefit;

    // Game State
    private boolean isRunning = false;
    private Thread gameThread;
    private Random random = new Random();
    private int missedBulletsSession = 0;

    // --- WAVE SYSTEM STATES ---
    private int currentWave = 1;
    private int enemiesToSpawnInWave = 0;
    private final int BASE_ENEMIES = 5;
    private final int WAVE_MULTIPLIER = 2;

    // Player Identity
    private String currentUsername = "Player";

    // Constants
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // Input States
    private boolean isUp, isDown, isLeft, isRight;
    private boolean isMousePressed = false; // Deteksi apakah mouse sedang ditahan

    // Timers
    private long lastArShotTime = 0; // Untuk membatasi kecepatan tembak AR Player

    public GamePresenter(KontrakView view) {
        this.view = view;
        // Menggunakan CopyOnWriteArrayList untuk menghindari ConcurrentModificationException
        this.aliens = new CopyOnWriteArrayList<>();
        this.bullets = new CopyOnWriteArrayList<>();
        this.powerUps = new CopyOnWriteArrayList<>();
        this.obstacles = new ArrayList<>();

        try {
            tabelBenefit = new TabelBenefit();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    // --- MENU & DATA LOGIC ---
    @Override
    public void loadData() {
        if (tabelBenefit == null) return;
        try {
            tabelBenefit.getBenefit();
            ResultSet rs = tabelBenefit.getResult();
            ArrayList<Object[]> rowList = new ArrayList<>();
            while (rs.next()) {
                rowList.add(new Object[] {
                        rs.getString("username"),
                        rs.getInt("skor"),
                        rs.getInt("peluru_meleset"),
                        rs.getInt("sisa_peluru")
                });
            }
            view.updateScoreTable(convertListtoObject(rowList));
            tabelBenefit.closeResult();
        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    private Object[][] convertListtoObject(ArrayList<Object[]> list){
        Object[][] data = new Object[list.size()][4];
        for(int i=0; i<list.size(); i++) data[i] = list.get(i);
        return data;
    }

    // --- GAME START LOGIC ---
    @Override
    public void startGame(String username) {
        if (username == null || username.trim().isEmpty()) {
            this.currentUsername = "Player";
        } else {
            this.currentUsername = username;
        }

        if (tabelBenefit != null) {
            tabelBenefit.registerPlayer(this.currentUsername);
            loadData();
        }

        // Init Player di tengah layar
        player = new Player(WIDTH / 2.0, HEIGHT / 2.0);

        int savedAmmo = 0;
        if (tabelBenefit != null) {
            savedAmmo = tabelBenefit.getAmmoByUsername(currentUsername);
        }
        player.setAmmo(savedAmmo > 0 ? savedAmmo : 50); // Minimal 50 ammo

        // Reset Game Objects
        aliens.clear();
        bullets.clear();
        obstacles.clear();
        powerUps.clear();
        missedBulletsSession = 0;

        // Reset Inputs
        isUp = false; isDown = false; isLeft = false; isRight = false;
        isMousePressed = false;

        // Reset Wave
        currentWave = 1;
        startNextWave();

        generateLevel();

        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();

        view.showGame();
        view.playSound("BGM");
    }

    private void startNextWave() {
        enemiesToSpawnInWave = BASE_ENEMIES + ((currentWave - 1) * WAVE_MULTIPLIER);
        System.out.println("Starting Wave " + currentWave + " with " + enemiesToSpawnInWave + " enemies.");
    }

    private void generateLevel() {
        int maxObstacles = 6; // Saya kurangi sedikit jadi 6 agar tidak terlalu penuh/gagal spawn karena gap besar
        int attempts = 0;
        int margin = 100;

        while (obstacles.size() < maxObstacles && attempts < 3000) { // Tambah attempts biar komputer lebih sabar mencari tempat kosong
            double ox = margin + random.nextInt(WIDTH - (2 * margin) - 60);
            double oy = margin + random.nextInt(HEIGHT - (2 * margin) - 60);

            // Ukuran Tetap
            int w = 60;
            int h = 60;

            boolean valid = true;
            // Jarak aman dari Player (agar tidak spawn di muka player)
            if (Math.hypot(ox - player.getX(), oy - player.getY()) < 200) valid = false;

            if (valid) {
                Rectangle newRect = new Rectangle((int)ox, (int)oy, w, h);
                for (Obstacle existing : obstacles) {

                    // --- MODIFIKASI GAP DI SINI ---
                    // Kita buat 'zona terlarang' di sekitar obstacle yang sudah ada.
                    // Semakin besar 'gap', semakin jauh jaraknya.
                    int gap = 100; // Jarak minimal antar obstacle (pixel)

                    Rectangle expanded = new Rectangle(
                            (int)existing.getX() - gap,
                            (int)existing.getY() - gap,
                            existing.getWidth() + (gap * 2),
                            existing.getHeight() + (gap * 2)
                    );

                    if (newRect.intersects(expanded)) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) obstacles.add(new Obstacle(ox, oy, w, h));
            attempts++;
        }
    }

    // --- MAIN GAME LOOP ---
    @Override
    public void run() {
        long lastTime = System.nanoTime();
        double amountOfTicks = 60.0;
        double ns = 1000000000 / amountOfTicks;
        double delta = 0;

        while (isRunning) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;

            while (delta >= 1) {
                updateGame();
                delta--;
            }

            if (isRunning) {
                // Update tampilan (Mengirim semua list termasuk PowerUps)
                view.updateGraphics(player, aliens, bullets, obstacles, powerUps);
            }

            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
    }

    @Override
    public void updateGame() {
        if (!isRunning) return;

        handlePlayerMovement();

        // Cek apakah durasi senjata spesial player (30 detik) sudah habis
        player.checkWeaponTimer();

        spawnLogic();
        updateAliens();
        updatePowerUps();
        updateBullets();

        // --- PLAYER SHOOTING LOGIC (CONTINUOUS / SPRAY) ---
        // Jika mouse ditahan DAN senjata adalah AR, tembak terus menerus
        if (isMousePressed && player.getCurrentWeapon() == Player.WeaponType.ASSAULT_RIFLE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastArShotTime > 50) {
                fireWeapon();
                lastArShotTime = currentTime;
            }
        }
    }

    // --- SPAWNING LOGIC ---
    private void spawnLogic() {
        // 1. Wave Check
        if (enemiesToSpawnInWave <= 0 && aliens.isEmpty()) {
            currentWave++;
            player.setScore(player.getScore() + 500); // Bonus Wave Clear
            startNextWave();
            return;
        }

        // 2. Spawn Aliens
        if (enemiesToSpawnInWave > 0 && aliens.size() < 8 && random.nextInt(100) < 2) {
            spawnOneAlien();
        }

        // 3. Spawn PowerUps (Metal Slug Style)
        // Maksimal 2 powerup di layar, chance kecil (0.5% per tick)
        if (powerUps.size() < 1 && random.nextInt(2000) < 2) {
            // Saya juga ubah powerUps.size() < 1 supaya cuma boleh ada 1 powerup di layar

            double x = 50 + random.nextInt(WIDTH - 100);
            double y = 50 + random.nextInt(HEIGHT - 100);

            PowerUp.Type type = random.nextBoolean() ? PowerUp.Type.ASSAULT_RIFLE : PowerUp.Type.SHOTGUN;
            powerUps.add(new PowerUp(x, y, type));
        }
    }

    private void spawnOneAlien() {
        double angle = random.nextDouble() * 2 * Math.PI;
        double spawnRadius = 500;
        double spawnX = (WIDTH / 2.0) + Math.cos(angle) * spawnRadius;
        double spawnY = (HEIGHT / 2.0) + Math.sin(angle) * spawnRadius;

        Alien.Type type = random.nextBoolean() ? Alien.Type.CHASER : Alien.Type.ZIGZAG;

        // Tentukan Loadout Musuh berdasarkan Wave
        Alien.Loadout loadout = Alien.Loadout.DEFAULT;
        int chance = random.nextInt(100);

        if (currentWave >= 1) {
            // SETTING BARU: Mengurangi frekuensi musuh bersenjata
            if (chance < 20) {
                loadout = Alien.Loadout.SHOTGUN;        // 20% Peluang
            } else if (chance < 40) {
                loadout = Alien.Loadout.ASSAULT_RIFLE;  // 20% Peluang
            } else {
                loadout = Alien.Loadout.DEFAULT;        // 60% Peluang (Mayoritas)
            }
        } else {
            // Wave 1: Sangat jarang ada AR (misal 5% saja)
            if (chance < 5) loadout = Alien.Loadout.ASSAULT_RIFLE;
        }

        Alien alien = new Alien(spawnX, spawnY, type, loadout);

        // Set kecepatan awal
        double dx = player.getX() - spawnX;
        double dy = player.getY() - spawnY;
        double distance = Math.sqrt(dx*dx + dy*dy);
        double speed = 1.0 + (currentWave * 0.1);

        alien.setVelX((dx / distance) * speed);
        alien.setVelY((dy / distance) * speed);

        aliens.add(alien);
        enemiesToSpawnInWave--;
    }

    // --- UPDATES & PHYSICS ---

    private void updatePowerUps() {
        for (PowerUp p : powerUps) {
            // 1. Cek Durasi (Hilang setelah 10 detik)
            if (p.isExpired()) {
                powerUps.remove(p);
                continue;
            }

            // 2. Cek Tabrakan dengan Player (Ambil Senjata)
            if (player.getBounds().intersects(p.getBounds())) {
                if (p.getType() == PowerUp.Type.ASSAULT_RIFLE) {
                    player.setWeapon(Player.WeaponType.ASSAULT_RIFLE, 30); // 30 Detik AR
                    view.playSound("RELOAD"); // Suara Reload/PowerUp
                } else {
                    player.setWeapon(Player.WeaponType.SHOTGUN, 30); // 30 Detik Shotgun
                    view.playSound("RELOAD");
                }
                powerUps.remove(p); // Hapus powerup setelah diambil
            }
        }
    }

    private void updateAliens() {
        for (Alien alien : aliens) {
            double prevX = alien.getX();
            double prevY = alien.getY();

            alien.setX(alien.getX() + alien.getVelX());
            alien.setY(alien.getY() + alien.getVelY());

            // Bounce Logic (Layar)
            int aw = alien.getWidth(); int ah = alien.getHeight();
            if (alien.getX() < 0 || alien.getX() > WIDTH - aw) {
                alien.setX(Math.max(0, Math.min(WIDTH-aw, alien.getX())));
                alien.setVelX(alien.getVelX() * -1);
            }
            if (alien.getY() < 0 || alien.getY() > HEIGHT - ah) {
                alien.setY(Math.max(0, Math.min(HEIGHT-ah, alien.getY())));
                alien.setVelY(alien.getVelY() * -1);
            }

            // Bounce Logic (Obstacles)
            Rectangle alienRect = alien.getBounds();
            for (Obstacle obs : obstacles) {
                if (alienRect.intersects(obs.getBounds())) {
                    alien.setVelX(alien.getVelX() * -1);
                    alien.setVelY(alien.getVelY() * -1);
                }
            }

            // Enemy Shooting Logic
            updateAlienShootingLogic(alien);
        }
    }

    private void updateAlienShootingLogic(Alien alien) {
        long currentTime = System.currentTimeMillis();

        // 1. Logic Burst (AR Enemy)
        if (alien.isBursting()) {
            if (currentTime - alien.getLastBurstTime() > 100) { // Jeda burst 0.1s
                double dx = player.getX() - alien.getX();
                double dy = player.getY() - alien.getY();
                createArBullet(alien, Math.atan2(dy, dx)); // Tembak AR Bullet

                alien.setBurstShotsFired(alien.getBurstShotsFired() + 1);
                alien.setLastBurstTime(currentTime);

                if (alien.getBurstShotsFired() >= 5) {
                    alien.setBursting(false); // Selesai burst
                }
            }
        }
        // 2. Logic Cooldown Normal
        else {
            long timeSinceLastShot = currentTime - alien.getLastShotTime();
            long cooldown = 2000;
            if (alien.getLoadout() == Alien.Loadout.ASSAULT_RIFLE) cooldown = 1500;
            if (alien.getLoadout() == Alien.Loadout.SHOTGUN) cooldown = 2500;

            if (timeSinceLastShot > cooldown) {
                if (isLineOfSightClear(alien, player)) {
                    if (alien.getLoadout() == Alien.Loadout.ASSAULT_RIFLE) {
                        // Mulai Burst AR
                        alien.setBursting(true);
                        alien.setBurstShotsFired(1);
                        alien.setLastBurstTime(currentTime);
                        // Tembakan pertama
                        double dx = player.getX() - alien.getX();
                        double dy = player.getY() - alien.getY();
                        createArBullet(alien, Math.atan2(dy, dx));
                    } else {
                        // Shotgun atau Default
                        shootFromAlien(alien);
                    }
                    alien.setLastShotTime(currentTime);
                }
            }
        }
    }

    private boolean isLineOfSightClear(Alien alien, Player player) {
        Line2D line = new Line2D.Double(alien.getX()+15, alien.getY()+15, player.getX()+15, player.getY()+15);
        for (Obstacle obs : obstacles) {
            if (line.intersects(obs.getBounds())) return false;
        }
        return true;
    }

    private void shootFromAlien(Alien alien) {
        double dx = player.getX() - alien.getX();
        double dy = player.getY() - alien.getY();
        double angle = Math.atan2(dy, dx);

        if (alien.getLoadout() == Alien.Loadout.SHOTGUN) {
            // Musuh Shotgun: 5 Peluru menyebar
            int pellets = 5;
            double spread = Math.toRadians(45);
            double startAngle = angle - (spread/2);
            double step = spread / (pellets-1);

            for (int i=0; i<pellets; i++) {
                double a = startAngle + (i*step);
                Bullet b = new Bullet(alien.getX()+15, alien.getY()+15, a, false, new Color(255, 200, 0)); // Kuning Gelap
                b.setSpeed(6.0);
                bullets.add(b);
            }
            view.playSound("SHOOT_ENEMY");
        } else {
            // Musuh Default
            double jitter = Math.toRadians((random.nextDouble()*20)-10);
            bullets.add(new Bullet(alien.getX()+15, alien.getY()+15, angle+jitter, false, Color.RED));
            view.playSound("SHOOT_ENEMY");
        }
    }

    private void createArBullet(Alien alien, double angle) {
        double jitter = Math.toRadians((random.nextDouble()*6)-3);
        Bullet b = new Bullet(alien.getX()+15, alien.getY()+15, angle+jitter, false, new Color(255, 69, 0)); // Orange/Merah Terang
        b.setSpeed(6.0);
        bullets.add(b);
        view.playSound("SHOOT_ENEMY");
    }

    private void updateBullets() {
        for (Bullet b : bullets) {
            b.setX(b.getX() + Math.cos(b.getAngle()) * b.getSpeed());
            b.setY(b.getY() + Math.sin(b.getAngle()) * b.getSpeed());

            if (b.getX() < 0 || b.getX() > WIDTH || b.getY() < 0 || b.getY() > HEIGHT) {
                bullets.remove(b);
                if (!b.isPlayerBullet()) handleMissedBullet();
                continue;
            }
            checkCollisionSafe(b);
        }
    }

    private void checkCollisionSafe(Bullet b) {
        Rectangle br = b.getBounds();

        // Obstacle Collision
        for (Obstacle obs : obstacles) {
            if (br.intersects(obs.getBounds())) {
                bullets.remove(b);
                if (!b.isPlayerBullet()) handleMissedBullet();
                return;
            }
        }

        // Hit Entity
        if (b.isPlayerBullet()) {
            for (Alien a : aliens) {
                if (br.intersects(a.getBounds())) {
                    aliens.remove(a);
                    bullets.remove(b);
                    player.setScore(player.getScore() + 100);
                    view.playSound("EXPLOSION"); // Pastikan file suara ada (optional)
                    return;
                }
            }
        } else {
            // Enemy hitting Player
            if (br.intersects(player.getBounds())) {
                gameOver();
            }
        }
    }

    private void handleMissedBullet() {
        missedBulletsSession++;
        player.setAmmo(player.getAmmo() + 1); // Refund peluru musuh yang meleset
        view.playSound("RELOAD");
    }

    private void gameOver() {
        isRunning = false;
        view.playSound("GAMEOVER");
        if (tabelBenefit != null) {
            tabelBenefit.saveGameData(currentUsername, player.getScore(), missedBulletsSession, player.getAmmo());
        }
        view.showGameOver(player.getScore());
    }

    // --- PLAYER INPUT & MOVEMENT ---

    private void handlePlayerMovement() {
        double speed = player.getSpeed();
        double nextX = player.getX();
        if (isLeft) nextX -= speed; if (isRight) nextX += speed;

        nextX = Math.max(0, Math.min(WIDTH-40, nextX)); // Clamp X
        if (!checkPlayerCollision(nextX, player.getY())) player.setX(nextX);

        double nextY = player.getY();
        if (isUp) nextY -= speed; if (isDown) nextY += speed;

        nextY = Math.max(0, Math.min(HEIGHT-70, nextY)); // Clamp Y
        if (!checkPlayerCollision(player.getX(), nextY)) player.setY(nextY);
    }

    private boolean checkPlayerCollision(double x, double y) {
        Rectangle pRect = new Rectangle((int)x, (int)y, 30, 30);
        for (Obstacle obs : obstacles) {
            if (pRect.intersects(obs.getBounds())) return true;
        }
        return false;
    }

    @Override
    public void movePlayer(boolean up, boolean down, boolean left, boolean right) {
        this.isUp = up; this.isDown = down; this.isLeft = left; this.isRight = right;
    }

    @Override
    public void rotatePlayer(int mouseX, int mouseY) {
        if (!isRunning) return;
        double dx = mouseX - (player.getX() + 15);
        double dy = mouseY - (player.getY() + 15);
        player.setRotation(Math.atan2(dy, dx));
    }

    // --- PLAYER SHOOTING HANDLING ---

    // Dipanggil saat Mouse Ditekan (Pressed)
    public void startShooting() {
        if (!isRunning) return;
        isMousePressed = true;

        // Jika Senjata Semi-Auto (Default/Shotgun), tembak sekali saat klik
        if (player.getCurrentWeapon() == Player.WeaponType.DEFAULT ||
                player.getCurrentWeapon() == Player.WeaponType.SHOTGUN) {
            fireWeapon();
        }
        // Jika AR, akan ditangani di updateGame() (Spray)
    }

    // Dipanggil saat Mouse Dilepas (Released)
    public void stopShooting() {
        isMousePressed = false;
    }

    // Deprecated from interface (diganti logic startShooting)
    @Override public void shoot() {}

    // Core Shooting Logic Player
    private void fireWeapon() {
        Player.WeaponType weapon = player.getCurrentWeapon();

        if (weapon == Player.WeaponType.SHOTGUN) {
            // LOGIKA SHOTGUN PLAYER
            if (player.getAmmo() >= 5) { // Butuh 5 ammo
                int pellets = 5;
                double spread = Math.toRadians(30); // Sebaran Player lebih sempit (30 derajat)
                double startAngle = player.getRotation() - (spread/2);
                double step = spread / (pellets-1);

                for (int i=0; i<pellets; i++) {
                    double a = startAngle + (i*step);
                    Bullet b = new Bullet(player.getX()+15, player.getY()+15, a, true, Color.YELLOW);
                    b.setSpeed(5.0);
                    bullets.add(b);
                }
                player.setAmmo(player.getAmmo() - 5);
                view.playSound("SHOOT");
            } else if (player.getAmmo() > 0) {
                // Sisa peluru dikit? Tembak biasa
                fireDefaultBullet();
            }

        } else if (weapon == Player.WeaponType.ASSAULT_RIFLE) {
            // LOGIKA AR PLAYER (Spray)
            if (player.getAmmo() > 0) {
                double jitter = Math.toRadians((random.nextDouble()*4)-2); // Sedikit goyang
                Bullet b = new Bullet(player.getX()+15, player.getY()+15, player.getRotation()+jitter, true, Color.ORANGE);
                b.setSpeed(12.0); // Peluru Player AR Cepat
                bullets.add(b);
                player.setAmmo(player.getAmmo() - 1);
                view.playSound("SHOOT"); // Idealnya suara loop
            }

        } else {
            // LOGIKA DEFAULT (PISTOL)
            if (player.getAmmo() > 0) {
                fireDefaultBullet();
            }
        }
    }

    private void fireDefaultBullet() {
        Bullet b = new Bullet(player.getX()+15, player.getY()+15, player.getRotation(), true, Color.YELLOW);
        bullets.add(b);
        player.setAmmo(player.getAmmo() - 1);
        view.playSound("SHOOT");
    }

    // --- GETTERS ---
    @Override public void startGame() { startGame("Player"); }
    @Override public int getScore() { return player.getScore(); }
    @Override public int getAmmo() { return player.getAmmo(); }
    public int getWave() { return currentWave; }
}