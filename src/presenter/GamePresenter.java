package presenter;

import model.*;
import view.KontrakView;

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
 * Combines all features:
 * 1. Menu Logic (Load Data & Username).
 * 2. Intelligent AI (Line of Sight & Arena Trap).
 * 3. Advanced Physics (Collision, varied Obstacles).
 * 4. Game Rules (Ammo Benefit on Miss/Wall hit).
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class GamePresenter implements KontrakPresenter, Runnable {

    private KontrakView view;

    // Data Models
    private Player player;
    private List<Alien> aliens;
    private List<Bullet> bullets;
    private List<Obstacle> obstacles;
    private TabelBenefit tabelBenefit;

    // Game State
    private boolean isRunning = false;
    private Thread gameThread;
    private Random random = new Random();
    private int missedBulletsSession = 0;

    // Player Identity
    private String currentUsername = "Player";

    // Constants
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // Input States
    private boolean isUp, isDown, isLeft, isRight;

    public GamePresenter(KontrakView view) {
        this.view = view;
        // Gunakan Thread-Safe List untuk entitas dinamis
        this.aliens = new CopyOnWriteArrayList<>();
        this.bullets = new CopyOnWriteArrayList<>();
        this.obstacles = new ArrayList<>();

        try {
            tabelBenefit = new TabelBenefit();
        } catch (Exception e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }
    }

    // --- MENU LOGIC: Load High Score ---
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

            Object[][] data = new Object[rowList.size()][4];
            for (int i = 0; i < rowList.size(); i++) {
                data[i] = rowList.get(i);
            }

            view.updateScoreTable(data);
            tabelBenefit.closeResult();

        } catch (Exception e) {
            System.err.println("Error loading data: " + e.getMessage());
        }
    }

    // --- GAME START LOGIC ---
    @Override
    public void startGame(String username) {
        // 1. Validasi Username
        if (username == null || username.trim().isEmpty()) {
            this.currentUsername = "Player";
        } else {
            this.currentUsername = username;
        }

        // 2. PERBAIKAN UTAMA: Daftarkan user ke database SEBELUM game mulai
        if (tabelBenefit != null) {
            tabelBenefit.registerPlayer(this.currentUsername);

            // 3. Refresh tabel skor di Menu agar user melihat namanya muncul di list
            loadData();
        }

        player = new Player(WIDTH / 2.0, HEIGHT / 2.0);

        int savedAmmo = 0;
        if (tabelBenefit != null) {
            savedAmmo = tabelBenefit.getAmmoByUsername(currentUsername);
        }
        player.setAmmo(savedAmmo);
        aliens.clear();
        bullets.clear();
        obstacles.clear();
        missedBulletsSession = 0;

        isUp = false; isDown = false; isLeft = false; isRight = false;

        generateLevel();

        // 5. Mulai Thread
        isRunning = true;
        gameThread = new Thread(this);
        gameThread.start();

        view.showGame();
        view.playSound("BGM");
    }

    // --- LEVEL GENERATION (7 Batu, Variasi Ukuran, Jarak Aman) ---
    private void generateLevel() {
        int maxObstacles = 7;
        int attempts = 0;
        int margin = 100; // Margin aman agar musuh bisa masuk

        while (obstacles.size() < maxObstacles && attempts < 2000) {
            double ox = margin + random.nextInt(WIDTH - (2 * margin) - 50);
            double oy = margin + random.nextInt(HEIGHT - (2 * margin) - 50);

            // Variasi Ukuran (40-90px)
            int w = 40 + random.nextInt(50);
            int h = 40 + random.nextInt(50);

            boolean valid = true;

            // 1. Cek Jarak dari Player
            double distToPlayer = Math.hypot(ox - player.getX(), oy - player.getY());
            if (distToPlayer < 150) valid = false;

            // 2. Cek Jarak antar Batu (Buffer 60px)
            if (valid) {
                Rectangle newRect = new Rectangle((int)ox, (int)oy, w, h);
                for (Obstacle existing : obstacles) {
                    Rectangle expandedExisting = new Rectangle(
                            (int)existing.getX() - 60,
                            (int)existing.getY() - 60,
                            existing.getWidth() + 120,
                            existing.getHeight() + 120
                    );

                    if (newRect.intersects(expandedExisting)) {
                        valid = false;
                        break;
                    }
                }
            }

            if (valid) {
                obstacles.add(new Obstacle(ox, oy, w, h));
            }
            attempts++;
        }
    }

    // --- GAME LOOP ---
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

            if (isRunning) view.updateGraphics(player, aliens, bullets, obstacles);

            try { Thread.sleep(2); } catch (InterruptedException e) {}
        }
    }

    @Override
    public void updateGame() {
        if (!isRunning) return;

        handlePlayerMovement();
        spawnAliensLogic();
        updateAliens();
        updateBullets();
    }

    // --- PHYSICS: Player Movement & Collision ---
    private void handlePlayerMovement() {
        double speed = player.getSpeed();

        // Gerakan X
        double nextX = player.getX();
        if (isLeft) nextX -= speed;
        if (isRight) nextX += speed;
        if (nextX < 0) nextX = 0;
        if (nextX > WIDTH - 35) nextX = WIDTH - 35;

        if (!checkPlayerCollision(nextX, player.getY())) player.setX(nextX);

        // Gerakan Y
        double nextY = player.getY();
        if (isUp) nextY -= speed;
        if (isDown) nextY += speed;
        if (nextY < 0) nextY = 0;
        if (nextY > HEIGHT - 35) nextY = HEIGHT - 35;

        if (!checkPlayerCollision(player.getX(), nextY)) player.setY(nextY);
    }

    private boolean checkPlayerCollision(double x, double y) {
        Rectangle futureBounds = new Rectangle((int)x, (int)y, 30, 30);
        for (Obstacle obs : obstacles) {
            if (futureBounds.intersects(obs.getBounds())) return true;
        }
        return false;
    }

    // --- AI: Alien Spawn & Logic ---
    private void spawnAliensLogic() {
        if (aliens.size() < 6 && random.nextInt(100) < 2) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double spawnRadius = 500;

            double spawnX = (WIDTH / 2.0) + Math.cos(angle) * spawnRadius;
            double spawnY = (HEIGHT / 2.0) + Math.sin(angle) * spawnRadius;

            Alien.Type type = random.nextBoolean() ? Alien.Type.CHASER : Alien.Type.ZIGZAG;
            Alien alien = new Alien(spawnX, spawnY, type);

            double dx = player.getX() - spawnX;
            double dy = player.getY() - spawnY;
            double distance = Math.sqrt(dx * dx + dy * dy);
            double speed = 1.0;

            alien.setVelX((dx / distance) * speed);
            alien.setVelY((dy / distance) * speed);

            aliens.add(alien);
        }
    }

    private void updateAliens() {
        for (Alien alien : aliens) {
            double prevX = alien.getX();
            double prevY = alien.getY();

            alien.setX(alien.getX() + alien.getVelX());
            alien.setY(alien.getY() + alien.getVelY());

            // 1. TRAP LOGIC (Tidak bisa keluar layar jika sudah masuk)
            int aw = alien.getWidth();
            int ah = alien.getHeight();
            boolean wasInsideX = (prevX >= 0 && prevX <= WIDTH - aw);
            boolean wasInsideY = (prevY >= 0 && prevY <= HEIGHT - ah);

            if (wasInsideX) {
                if (alien.getX() < 0) {
                    alien.setX(0);
                    alien.setVelX(alien.getVelX() * -1);
                } else if (alien.getX() > WIDTH - aw) {
                    alien.setX(WIDTH - aw);
                    alien.setVelX(alien.getVelX() * -1);
                }
            }
            if (wasInsideY) {
                if (alien.getY() < 0) {
                    alien.setY(0);
                    alien.setVelY(alien.getVelY() * -1);
                } else if (alien.getY() > HEIGHT - ah) {
                    alien.setY(HEIGHT - ah);
                    alien.setVelY(alien.getVelY() * -1);
                }
            }

            // 2. OBSTACLE BOUNCE
            Rectangle alienRect = alien.getBounds();
            for (Obstacle obs : obstacles) {
                if (alienRect.intersects(obs.getBounds())) {
                    alien.setVelX(alien.getVelX() * -1);
                    alien.setVelY(alien.getVelY() * -1);
                }
            }

            // 3. SHOOTING LOGIC (With Line of Sight)
            long currentTime = System.currentTimeMillis();
            if (currentTime - alien.getLastShotTime() > 2000) {
                if (isLineOfSightClear(alien, player)) {
                    shootFromAlien(alien);
                    alien.setLastShotTime(currentTime);
                }
            }
        }
    }

    // Helper: Cek Garis Pandang (Raycasting)
    private boolean isLineOfSightClear(Alien alien, Player player) {
        double ax = alien.getX() + 15;
        double ay = alien.getY() + 15;
        double px = player.getX() + 15;
        double py = player.getY() + 15;

        Line2D line = new Line2D.Double(ax, ay, px, py);

        for (Obstacle obs : obstacles) {
            if (line.intersects(obs.getBounds())) {
                return false; // Terhalang Tembok
            }
        }
        return true; // Bersih
    }

    private void shootFromAlien(Alien alien) {
        double dx = player.getX() - alien.getX();
        double dy = player.getY() - alien.getY();
        double angleToPlayer = Math.atan2(dy, dx);
        double jitter = Math.toRadians((random.nextDouble() * 30) - 15);

        bullets.add(new Bullet(
                alien.getX() + 15, alien.getY() + 15,
                angleToPlayer + jitter,
                false
        ));
        view.playSound("SHOOT_ENEMY");
    }

    // --- PHYSICS: Bullets ---
    private void updateBullets() {
        for (Bullet b : bullets) {
            b.setX(b.getX() + Math.cos(b.getAngle()) * b.getSpeed());
            b.setY(b.getY() + Math.sin(b.getAngle()) * b.getSpeed());

            // Hapus jika keluar layar
            if (b.getX() < 0 || b.getX() > WIDTH || b.getY() < 0 || b.getY() > HEIGHT) {
                bullets.remove(b);
                if (!b.isPlayerBullet()) handleMissedBullet();
                continue;
            }
            checkCollisionSafe(b);
        }
    }

    private void checkCollisionSafe(Bullet b) {
        Rectangle bulletRect = b.getBounds();

        // 1. Cek Kena Batu
        for (Obstacle obs : obstacles) {
            if (bulletRect.intersects(obs.getBounds())) {
                bullets.remove(b);
                // Jika peluru musuh kena batu -> Player dapat poin miss
                if (!b.isPlayerBullet()) {
                    handleMissedBullet();
                }
                return;
            }
        }

        // 2. Cek Kena Entitas
        if (b.isPlayerBullet()) {
            for (Alien a : aliens) {
                if (bulletRect.intersects(a.getBounds())) {
                    aliens.remove(a);
                    bullets.remove(b);
                    player.setScore(player.getScore() + 100);
                    view.playSound("EXPLOSION");
                    return;
                }
            }
        } else {
            Rectangle playerRect = new Rectangle((int)player.getX() + 10, (int)player.getY() + 10, 10, 10);
            if (bulletRect.intersects(playerRect)) gameOver();
        }
    }

    private void handleMissedBullet() {
        missedBulletsSession++;
        player.setAmmo(player.getAmmo() + 1);
        view.playSound("RELOAD");
    }

    // --- GAME OVER & SAVE ---
    private void gameOver() {
        isRunning = false;
        view.playSound("GAMEOVER");
        if (tabelBenefit != null) {
            // Simpan data menggunakan Username yang diinput di awal
            tabelBenefit.saveGameData(currentUsername, player.getScore(), missedBulletsSession, player.getAmmo());
        }
        view.showGameOver(player.getScore());
    }

    // --- INPUTS (Delegated from View) ---
    @Override public void startGame() { startGame("Player"); } // Default Fallback

    @Override
    public void movePlayer(boolean up, boolean down, boolean left, boolean right) {
        this.isUp = up;
        this.isDown = down;
        this.isLeft = left;
        this.isRight = right;
    }

    @Override
    public void rotatePlayer(int mouseX, int mouseY) {
        if (!isRunning) return;
        double dx = mouseX - (player.getX() + 15);
        double dy = mouseY - (player.getY() + 15);
        player.setRotation(Math.atan2(dy, dx));
    }

    @Override
    public void shoot() {
        if (!isRunning) return;
        if (player.getAmmo() > 0) {
            bullets.add(new Bullet(
                    player.getX() + 15, player.getY() + 15,
                    player.getRotation(),
                    true
            ));
            player.setAmmo(player.getAmmo() - 1);
            view.playSound("SHOOT");
        }
    }

    @Override public int getScore() { return player.getScore(); }
    @Override public int getAmmo() { return player.getAmmo(); }
}