package presenter;

import model.*;
import view.KontrakView;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
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
 * 1. FIX: Obstacle Hitbox Offset Removed (Memperbaiki masalah tembus tembok).
 * 2. FIX: Gun Muzzle Offset (Peluru keluar dari posisi senjata yang benar).
 */
public class GamePresenter implements KontrakPresenter, Runnable {

    private KontrakView view;

    // Data Models
    private Player player;
    private List<Alien> aliens;
    private List<Bullet> bullets;
    private List<Obstacle> obstacles;
    private List<PowerUp> powerUps;
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
    private boolean isMousePressed = false;

    // Timers
    private long lastArShotTime = 0;

    public GamePresenter(KontrakView view) {
        this.view = view;
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
        player.setAmmo(savedAmmo > 0 ? savedAmmo : 50);

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
        int maxObstacles = 6;
        int attempts = 0;
        int margin = 100;

        while (obstacles.size() < maxObstacles && attempts < 3000) {
            double ox = margin + random.nextInt(WIDTH - (2 * margin) - 60);
            double oy = margin + random.nextInt(HEIGHT - (2 * margin) - 60);
            int w = 60;
            int h = 60;

            boolean valid = true;
            if (Math.hypot(ox - player.getX(), oy - player.getY()) < 200) valid = false;

            if (valid) {
                Rectangle newRect = new Rectangle((int)ox, (int)oy, w, h);
                for (Obstacle existing : obstacles) {
                    int gap = 100;
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
                view.updateGraphics(player, aliens, bullets, obstacles, powerUps);
            }

            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
    }

    @Override
    public void updateGame() {
        if (!isRunning) return;

        handlePlayerMovement();
        player.checkWeaponTimer();

        spawnLogic();
        updateAliens();
        updatePowerUps();
        updateBullets();

        if (isMousePressed && player.getCurrentWeapon() == Player.WeaponType.ASSAULT_RIFLE) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastArShotTime > 50) { // Rate of fire AR
                fireWeapon();
                lastArShotTime = currentTime;
            }
        }
    }

    // --- SPAWNING LOGIC ---
    private void spawnLogic() {
        if (enemiesToSpawnInWave <= 0 && aliens.isEmpty()) {
            currentWave++;
            player.setScore(player.getScore() + 500);
            startNextWave();
            return;
        }

        if (enemiesToSpawnInWave > 0 && aliens.size() < 8 && random.nextInt(100) < 2) {
            spawnOneAlien();
        }

        if (powerUps.size() < 1 && random.nextInt(2000) < 2) {
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

        Alien.Loadout loadout = Alien.Loadout.DEFAULT;
        int chance = random.nextInt(100);

        if (currentWave >= 1) {
            if (chance < 20) loadout = Alien.Loadout.SHOTGUN;
            else if (chance < 40) loadout = Alien.Loadout.ASSAULT_RIFLE;
            else loadout = Alien.Loadout.DEFAULT;
        } else {
            if (chance < 5) loadout = Alien.Loadout.ASSAULT_RIFLE;
        }

        Alien alien = new Alien(spawnX, spawnY, type, loadout);

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
            if (p.isExpired()) {
                powerUps.remove(p);
                continue;
            }
            // Tabrakan PowerUp diperketat sedikit (inset 5 pixel)
            Rectangle pRect = p.getBounds();
            Rectangle shrunkPowerUp = new Rectangle(pRect.x + 5, pRect.y + 5, pRect.width - 10, pRect.height - 10);

            if (player.getBounds().intersects(shrunkPowerUp)) {
                if (p.getType() == PowerUp.Type.ASSAULT_RIFLE) {
                    player.setWeapon(Player.WeaponType.ASSAULT_RIFLE, 30);
                    view.playSound("RELOAD");
                } else {
                    player.setWeapon(Player.WeaponType.SHOTGUN, 30);
                    view.playSound("RELOAD");
                }
                powerUps.remove(p);
            }
        }
    }

    private void updateAliens() {
        for (Alien alien : aliens) {
            alien.setX(alien.getX() + alien.getVelX());
            alien.setY(alien.getY() + alien.getVelY());

            // Bounce Logic
            int aw = alien.getWidth(); int ah = alien.getHeight();
            if (alien.getX() < 0 || alien.getX() > WIDTH - aw) {
                alien.setX(Math.max(0, Math.min(WIDTH-aw, alien.getX())));
                alien.setVelX(alien.getVelX() * -1);
            }
            if (alien.getY() < 0 || alien.getY() > HEIGHT - ah) {
                alien.setY(Math.max(0, Math.min(HEIGHT-ah, alien.getY())));
                alien.setVelY(alien.getVelY() * -1);
            }

            Rectangle alienRect = alien.getBounds();
            for (Obstacle obs : obstacles) {
                // Gunakan Helper method untuk cek collision yang lebih presisi
                if (checkRectCollision(alienRect, obs, 5)) {
                    alien.setVelX(alien.getVelX() * -1);
                    alien.setVelY(alien.getVelY() * -1);
                }
            }

            updateAlienShootingLogic(alien);
        }
    }

    private void updateAlienShootingLogic(Alien alien) {
        long currentTime = System.currentTimeMillis();

        if (alien.isBursting()) {
            if (currentTime - alien.getLastBurstTime() > 100) {
                double dx = player.getX() - alien.getX();
                double dy = player.getY() - alien.getY();
                createArBullet(alien, Math.atan2(dy, dx));

                alien.setBurstShotsFired(alien.getBurstShotsFired() + 1);
                alien.setLastBurstTime(currentTime);

                if (alien.getBurstShotsFired() >= 5) {
                    alien.setBursting(false);
                }
            }
        }
        else {
            long timeSinceLastShot = currentTime - alien.getLastShotTime();
            long cooldown = 2000;
            if (alien.getLoadout() == Alien.Loadout.ASSAULT_RIFLE) cooldown = 1500;
            if (alien.getLoadout() == Alien.Loadout.SHOTGUN) cooldown = 2500;

            if (timeSinceLastShot > cooldown) {
                if (isLineOfSightClear(alien, player)) {
                    if (alien.getLoadout() == Alien.Loadout.ASSAULT_RIFLE) {
                        alien.setBursting(true);
                        alien.setBurstShotsFired(1);
                        alien.setLastBurstTime(currentTime);
                        double dx = player.getX() - alien.getX();
                        double dy = player.getY() - alien.getY();
                        createArBullet(alien, Math.atan2(dy, dx));
                    } else {
                        shootFromAlien(alien);
                    }
                    alien.setLastShotTime(currentTime);
                }
            }
        }
    }

    private boolean isLineOfSightClear(Alien alien, Player player) {
        Line2D line = new Line2D.Double(alien.getX()+30, alien.getY()+30, player.getX()+30, player.getY()+30);
        for (Obstacle obs : obstacles) {
            // Kita pakai bounds yang diperkecil untuk Line of Sight juga
            Rectangle rect = obs.getBounds();
            Rectangle shrunk = new Rectangle(rect.x + 10, rect.y + 10, rect.width - 20, rect.height - 20);
            if (line.intersects(shrunk)) return false;
        }
        return true;
    }

    private void shootFromAlien(Alien alien) {
        double dx = player.getX() - alien.getX();
        double dy = player.getY() - alien.getY();
        double angle = Math.atan2(dy, dx);
        double spawnX = alien.getX() + alien.getWidth()/2.0;
        double spawnY = alien.getY() + alien.getHeight()/2.0;

        if (alien.getLoadout() == Alien.Loadout.SHOTGUN) {
            int pellets = 5;
            double spread = Math.toRadians(45);
            double startAngle = angle - (spread/2);
            double step = spread / (pellets-1);

            for (int i=0; i<pellets; i++) {
                double a = startAngle + (i*step);
                double speed = 6.0;
                double vx = Math.cos(a) * speed;
                double vy = Math.sin(a) * speed;
                bullets.add(new Bullet(spawnX, spawnY, vx, vy, Color.ORANGE, "BULLET_ENEMY_SHOTGUN"));
            }
            view.playSound("SHOOT_ENEMY");
        } else {
            double jitter = Math.toRadians((random.nextDouble()*20)-10);
            double a = angle + jitter;
            double speed = 5.0;
            double vx = Math.cos(a) * speed;
            double vy = Math.sin(a) * speed;
            bullets.add(new Bullet(spawnX, spawnY, vx, vy, Color.RED, "BULLET_ENEMY_PISTOL"));
            view.playSound("SHOOT_ENEMY");
        }
    }

    private void createArBullet(Alien alien, double angle) {
        double jitter = Math.toRadians((random.nextDouble()*6)-3);
        double a = angle + jitter;
        double speed = 7.0;
        double vx = Math.cos(a) * speed;
        double vy = Math.sin(a) * speed;
        double spawnX = alien.getX() + alien.getWidth()/2.0;
        double spawnY = alien.getY() + alien.getHeight()/2.0;

        bullets.add(new Bullet(spawnX, spawnY, vx, vy, Color.RED, "BULLET_ENEMY_AR"));
        view.playSound("SHOOT_ENEMY");
    }

    private void updateBullets() {
        for (Bullet b : bullets) {
            b.update();

            if (b.getX() < 0 || b.getX() > WIDTH || b.getY() < 0 || b.getY() > HEIGHT) {
                bullets.remove(b);
                if (!b.getImageKey().contains("PLAYER")) handleMissedBullet();
                continue;
            }
            checkCollisionSafe(b);
        }
    }

    private void checkCollisionSafe(Bullet b) {
        Rectangle br = b.getBounds();

        // Obstacle Collision
        for (Obstacle obs : obstacles) {
            if (checkRectCollision(br, obs, 5)) {
                bullets.remove(b);
                if (!b.getImageKey().contains("PLAYER")) handleMissedBullet();
                return;
            }
        }

        // Hit Entity Logic
        if (b.getImageKey().contains("PLAYER")) {
            for (Alien a : aliens) {
                if (br.intersects(a.getBounds())) {
                    aliens.remove(a);
                    bullets.remove(b);
                    player.setScore(player.getScore() + 100);
                    view.playSound("EXPLOSION");
                    return;
                }
            }
        } else {
            if (br.intersects(player.getBounds())) {
                gameOver();
            }
        }
    }

    private void handleMissedBullet() {
        missedBulletsSession++;
        player.setAmmo(player.getAmmo() + 1);
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

        nextX = Math.max(0, Math.min(WIDTH-60, nextX));
        if (!checkPlayerCollision(nextX, player.getY())) player.setX(nextX);

        double nextY = player.getY();
        if (isUp) nextY -= speed; if (isDown) nextY += speed;

        nextY = Math.max(0, Math.min(HEIGHT-60, nextY));
        if (!checkPlayerCollision(player.getX(), nextY)) player.setY(nextY);
    }

    /**
     * PERBAIKAN 1: FIX OFFSET HITBOX
     * Sebelumnya ada '+ 15' yang membuat hitbox bergeser ke kanan bawah,
     * sehingga sisi kiri player bisa menembus tembok.
     * Sekarang kita gunakan 'x' dan 'y' murni karena hitbox (30x30)
     * sudah otomatis di-center oleh renderer visual (60x60).
     */
    private boolean checkPlayerCollision(double x, double y) {
        // Gunakan posisi asli (tanpa +15) karena Player.java sudah mendefinisikan
        // x,y sebagai titik kiri-atas dari hitbox 30x30.
        Rectangle pRect = new Rectangle((int)x, (int)y, 30, 30);

        for (Obstacle obs : obstacles) {
            // Gunakan padding 10 pixel agar visual bisa sedikit overlap tembok (agar tidak kaku)
            if (checkRectCollision(pRect, obs, 10)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkRectCollision(Rectangle entityRect, Obstacle obs, int padding) {
        Rectangle original = obs.getBounds();
        Rectangle tighterBox = new Rectangle(
                original.x + padding,
                original.y + padding,
                Math.max(1, original.width - (padding * 2)),
                Math.max(1, original.height - (padding * 2))
        );
        return entityRect.intersects(tighterBox);
    }

    @Override
    public void movePlayer(boolean up, boolean down, boolean left, boolean right) {
        this.isUp = up; this.isDown = down; this.isLeft = left; this.isRight = right;
    }

    @Override
    public void rotatePlayer(int mouseX, int mouseY) {
        if (!isRunning) return;
        // Pusat player untuk kalkulasi sudut (+30 karena hitbox visual 60)
        double dx = mouseX - (player.getX() + 30);
        double dy = mouseY - (player.getY() + 30);
        player.setRotation(Math.atan2(dy, dx));
    }

    // --- PLAYER SHOOTING HANDLING ---

    public void startShooting() {
        if (!isRunning) return;
        isMousePressed = true;
        if (player.getCurrentWeapon() == Player.WeaponType.DEFAULT ||
                player.getCurrentWeapon() == Player.WeaponType.SHOTGUN) {
            fireWeapon();
        }
    }

    public void stopShooting() {
        isMousePressed = false;
    }

    @Override public void shoot() {}

    /**
     * PERBAIKAN 2: GUN OFFSET
     * Menghitung posisi spawn peluru agar keluar dari moncong senjata (kanan).
     */
    private Point2D.Double getGunMuzzlePosition() {
        double angle = player.getRotation();

        // Pusat Player (Visual)
        double centerX = player.getX() + 30;
        double centerY = player.getY() + 30;

        // OFFSET SETTING (Sesuaikan angka ini agar pas dengan gambar)
        double forwardOffset = 25.0; // Jarak moncong ke depan dari pusat
        double rightOffset = 18.0;   // Jarak moncong ke kanan dari pusat (tangan kanan)

        // Rumus Rotasi Vektor:
        // SpawnX = PusatX + (Maju * cos) + (Kanan * cos(90+sudut))
        // SpawnY = PusatY + (Maju * sin) + (Kanan * sin(90+sudut))
        // cos(90+a) = -sin(a), sin(90+a) = cos(a)

        double spawnX = centerX + (forwardOffset * Math.cos(angle)) - (rightOffset * Math.sin(angle));
        double spawnY = centerY + (forwardOffset * Math.sin(angle)) + (rightOffset * Math.cos(angle));

        return new Point2D.Double(spawnX, spawnY);
    }

    private void fireWeapon() {
        Player.WeaponType weapon = player.getCurrentWeapon();
        Point2D.Double muzzle = getGunMuzzlePosition(); // Ambil posisi spawn yang sudah dikoreksi

        if (weapon == Player.WeaponType.SHOTGUN) {
            if (player.getAmmo() >= 5) {
                int pellets = 5;
                double spread = Math.toRadians(30);
                double startAngle = player.getRotation() - (spread/2);
                double step = spread / (pellets-1);

                for (int i=0; i<pellets; i++) {
                    double a = startAngle + (i*step);
                    double speed = 8.0;
                    double vx = Math.cos(a) * speed;
                    double vy = Math.sin(a) * speed;
                    bullets.add(new Bullet(muzzle.x, muzzle.y, vx, vy, Color.YELLOW, "BULLET_PLAYER_SHOTGUN"));
                }
                player.setAmmo(player.getAmmo() - 5);
                view.playSound("SHOOT");
            } else if (player.getAmmo() > 0) {
                fireDefaultBullet();
            }

        } else if (weapon == Player.WeaponType.ASSAULT_RIFLE) {
            if (player.getAmmo() > 0) {
                double jitter = Math.toRadians((random.nextDouble()*4)-2);
                double a = player.getRotation() + jitter;
                double speed = 12.0;
                double vx = Math.cos(a) * speed;
                double vy = Math.sin(a) * speed;
                bullets.add(new Bullet(muzzle.x, muzzle.y, vx, vy, Color.ORANGE, "BULLET_PLAYER_AR"));
                player.setAmmo(player.getAmmo() - 1);
                view.playSound("SHOOT");
            }

        } else {
            if (player.getAmmo() > 0) {
                fireDefaultBullet();
            }
        }
    }

    private void fireDefaultBullet() {
        Point2D.Double muzzle = getGunMuzzlePosition();
        double a = player.getRotation();
        double speed = 10.0;
        double vx = Math.cos(a) * speed;
        double vy = Math.sin(a) * speed;
        bullets.add(new Bullet(muzzle.x, muzzle.y, vx, vy, Color.YELLOW, "BULLET_PLAYER_PISTOL"));
        player.setAmmo(player.getAmmo() - 1);
        view.playSound("SHOOT");
    }

    // --- GETTERS ---
    @Override public void startGame() { startGame("Player"); }
    @Override public int getScore() { return player.getScore(); }
    @Override public int getAmmo() { return player.getAmmo(); }
    public int getWave() { return currentWave; }
}