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
 * Acts as the central controller ("Brain") of the application, bridging the Model and View.
 * <p>
 * This class implements {@link KontrakPresenter} and {@link Runnable} to manage the game loop,
 * game state updates, entity spawning, collision detection, and user input handling.
 * </p>
 *
 * <p>
 * Key Responsibilities:
 * <ul>
 *     <li><b>Game Loop:</b> Manages the main game loop thread with a fixed time step.</li>
 *     <li><b>Entity Management:</b> Updates and renders Players, Aliens, Bullets, Obstacles, and PowerUps.</li>
 *     <li><b>Collision Detection:</b> Handles interactions between game entities (e.g., bullets hitting aliens, player hitting obstacles).</li>
 *     <li><b>Wave System:</b> Controls the progression of game waves and enemy spawning.</li>
 *     <li><b>Data Persistence:</b> Communicates with {@link TabelBenefit} to save and load high scores.</li>
 * </ul>
 * </p>
 *
 * <p>
 * <b>Recent Updates:</b>
 * <ul>
 *     <li>FIX: Obstacle Hitbox Offset Removed (Fixed wall clipping issue).</li>
 *     <li>FIX: Gun Muzzle Offset (Bullet spawns from correct weapon position).</li>
 * </ul>
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class GamePresenter implements KontrakPresenter, Runnable {

    /** The view interface for rendering the game. */
    private KontrakView view;

    // --- Data Models ---

    /** The main player character. */
    private Player player;
    /** Thread-safe list of active alien enemies. */
    private List<Alien> aliens;
    /** Thread-safe list of active bullets. */
    private List<Bullet> bullets;
    /** List of obstacles/walls in the current level. */
    private List<Obstacle> obstacles;
    /** Thread-safe list of active power-ups. */
    private List<PowerUp> powerUps;
    /** Database handling class for high scores. */
    private TabelBenefit tabelBenefit;

    // --- Game State ---

    /** Flag indicating if the game loop is currently running. */
    private boolean isRunning = false;
    /** The main thread running the game loop. */
    private Thread gameThread;
    /** Random number generator for spawning and RNG logic. */
    private Random random = new Random();
    /** Counter for bullets missed in the current session (used for stats). */
    private int missedBulletsSession = 0;

    // --- Wave System States ---

    /** The current game wave number. */
    private int currentWave = 1;
    /** Number of enemies left to spawn in the current wave. */
    private int enemiesToSpawnInWave = 0;
    private final int BASE_ENEMIES = 5;
    private final int WAVE_MULTIPLIER = 2;

    // --- Player Identity ---

    /** Display name of the current player. */
    private String currentUsername = "Player";

    // --- Constants ---

    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    // --- Input States ---

    private boolean isUp, isDown, isLeft, isRight;
    private boolean isMousePressed = false;

    // --- Timers ---

    /** Timestamp of the last Assault Rifle shot to control fire rate. */
    private long lastArShotTime = 0;

    /**
     * Constructs a new {@code GamePresenter} with the specified view.
     * <p>
     * Initializes all game object lists (aliens, bullets, obstacles, etc.) and
     * attempts to establish a connection to the database via {@link TabelBenefit}.
     * </p>
     *
     * @param view the view interface for UI updates
     */
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

    /**
     * Loads high score data from the database and updates the view.
     * <p>
     * Retrieves player statistics including username, score, missed bullets, and
     * remaining ammo from {@link TabelBenefit}, converting the result set into
     * a format suitable for the view's table.
     * </p>
     */
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

    /**
     * Helper method to convert an ArrayList of Objects to a 2D Object array.
     *
     * @param list the list of row data
     * @return a 2D array representation of the data
     */
    private Object[][] convertListtoObject(ArrayList<Object[]> list){
        Object[][] data = new Object[list.size()][4];
        for(int i=0; i<list.size(); i++) data[i] = list.get(i);
        return data;
    }

    // --- GAME START LOGIC ---

    /**
     * Starts the game session for the specified user.
     * <p>
     * Resets game state, registers the player in the database (if connected),
     * initializes the player entity, resets wave progress, generates the level,
     * and starts the game loop thread.
     * </p>
     *
     * @param username the name of the player
     */
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

        // Initialize Player at the center of the screen
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

    /**
     * Prepares the next wave of enemies.
     * <p>
     * Calculates the number of enemies to spawn based on the current wave number.
     * </p>
     */
    private void startNextWave() {
        enemiesToSpawnInWave = BASE_ENEMIES + ((currentWave - 1) * WAVE_MULTIPLIER);
        System.out.println("Starting Wave " + currentWave + " with " + enemiesToSpawnInWave + " enemies.");
    }

    /**
     * Generates a random level layout by placing obstacles.
     * <p>
     * Places a set number of obstacles at random positions, ensuring they do not
     * overlap with the player or each other.
     * </p>
     */
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

    /**
     * The main game loop driven by the thread.
     * <p>
     * Implements a fixed time-step loop (approx 60 ticks per second).
     * Updates game logic and renders graphics each frame.
     * </p>
     */
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

    /**
     * Updates the game state for a single frame.
     * <p>
     * Handles player movement, weapon timers, entity spawning, and updates for
     * all active game objects (Aliens, PowerUps, Bullets). Also handles automatic
     * firing if the mouse is held down for auto-weapons.
     * </p>
     */
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

    /**
     * Manages entity spawning logic.
     * <p>
     * Handles spawning of enemies (Aliens) based on wave progress and chance,
     * as well as random PowerUp spawning. Checks for wave completion to advance.
     * </p>
     */
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

    /**
     * Spawns a single alien enemy at a random position outside the screen center.
     * <p>
     * Determines the alien type (Chaser/Zigzag) and loadout (Weapon) based on
     * probability and current wave difficulty.
     * </p>
     */
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

    /**
     * Updates all active PowerUps.
     * <p>
     * Handles expiration and collision with the player.
     * </p>
     */
    private void updatePowerUps() {
        for (PowerUp p : powerUps) {
            if (p.isExpired()) {
                powerUps.remove(p);
                continue;
            }
            // PowerUp collision tightened slightly (inset 5 pixel)
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

    /**
     * Updates all active Aliens.
     * <p>
     * Handles movement, screen boundary bouncing, obstacle collision,
     * and shooting logic for each alien.
     * </p>
     */
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
                // Use Helper method for more precise collision checking
                if (checkRectCollision(alienRect, obs, 5)) {
                    alien.setVelX(alien.getVelX() * -1);
                    alien.setVelY(alien.getVelY() * -1);
                }
            }

            updateAlienShootingLogic(alien);
        }
    }

    /**
     * Manages shooting behavior for an individual alien.
     * <p>
     * Handles burst firing for Assault Rifle aliens and single shots for others.
     * Checks line of sight before firing.
     * </p>
     *
     * @param alien the alien entity to update
     */
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

    /**
     * Checks if there is a clear line of sight between an alien and the player.
     *
     * @param alien  the source alien
     * @param player the target player
     * @return {@code true} if no obstacles block the line of sight, {@code false} otherwise
     */
    private boolean isLineOfSightClear(Alien alien, Player player) {
        Line2D line = new Line2D.Double(alien.getX()+30, alien.getY()+30, player.getX()+30, player.getY()+30);
        for (Obstacle obs : obstacles) {
            // We use shrunk bounds for Line of Sight as well
            Rectangle rect = obs.getBounds();
            Rectangle shrunk = new Rectangle(rect.x + 10, rect.y + 10, rect.width - 20, rect.height - 20);
            if (line.intersects(shrunk)) return false;
        }
        return true;
    }

    /**
     * Executes a shot from an alien towards the player.
     * <p>
     * Calculates the trajectory and handles different weapon spread patterns (e.g. Shotgun).
     * </p>
     *
     * @param alien the shooting alien
     */
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

    /**
     * Creates a single bullet from an alien with an Assault Rifle.
     *
     * @param alien the shooting alien
     * @param angle the base angle of the shot
     */
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

    /**
     * Updates all active bullets.
     * <p>
     * Handles bullet movement, boundary checks (removing off-screen bullets),
     * and collision detection with entities or obstacles.
     * </p>
     */
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

    /**
     * Checks collisions for a specific bullet against obstacles and entities.
     *
     * @param b the bullet to check
     */
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

    /**
     * Handles logic for when a player's bullet misses.
     * <p>
     * Increments the missed bullet session counter (for stats) and
     * potentially adds default ammo back (game mechanic).
     * </p>
     */
    private void handleMissedBullet() {
        missedBulletsSession++;
        player.setAmmo(player.getAmmo() + 1);
        view.playSound("RELOAD");
    }

    /**
     * Ends the game session.
     * <p>
     * Stops the game loop, saves game data to the database, and displays the Game Over screen.
     * </p>
     */
    private void gameOver() {
        isRunning = false;
        view.playSound("GAMEOVER");
        if (tabelBenefit != null) {
            tabelBenefit.saveGameData(currentUsername, player.getScore(), missedBulletsSession, player.getAmmo());
        }
        view.showGameOver(player.getScore());
    }

    // --- PLAYER INPUT & MOVEMENT ---

    /**
     * Handles player movement logic based on current input states (isUp, isDown, etc.).
     * <p>
     * Calculates next position and checks for collisions before applying movement.
     * </p>
     */
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
     * Checks if the player would collide with any obstacles at the given position.
     * <p>
     * PERBAIKAN 1: FIX OFFSET HITBOX.
     * Uses coordinates as the top-left of the 30x30 hitbox without additional offset,
     * fixing previous issues where the player could clip through walls.
     * </p>
     *
     * @param x the potential X coordinate
     * @param y the potential Y coordinate
     * @return {@code true} if a collision occurs, {@code false} otherwise
     */
    private boolean checkPlayerCollision(double x, double y) {
        // Use original position (without +15) because Player.java already defines
        // x,y as top-left point of 30x30 hitbox.
        Rectangle pRect = new Rectangle((int)x, (int)y, 30, 30);

        for (Obstacle obs : obstacles) {
            // Use 10 pixel padding so visual can slightly overlap wall (to be less rigid)
            if (checkRectCollision(pRect, obs, 10)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Generic wrapper to check collision between an entity rectangle and an obstacle.
     *
     * @param entityRect the bounding box of the entity
     * @param obs        the obstacle to check against
     * @param padding    padding to shrink the obstacle's effective hitbox (makes movement smoother)
     * @return {@code true} if they intersect
     */
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

    /**
     * Updates player movement flags based on user input.
     */
    @Override
    public void movePlayer(boolean up, boolean down, boolean left, boolean right) {
        this.isUp = up; this.isDown = down; this.isLeft = left; this.isRight = right;
    }

    /**
     * Updates the player's rotation to face the mouse cursor.
     *
     * @param mouseX the X coordinate of the mouse
     * @param mouseY the Y coordinate of the mouse
     */
    @Override
    public void rotatePlayer(int mouseX, int mouseY) {
        if (!isRunning) return;
        // Player center for angle calculation (+30 because visual hitbox is 60)
        double dx = mouseX - (player.getX() + 30);
        double dy = mouseY - (player.getY() + 30);
        player.setRotation(Math.atan2(dy, dx));
    }

    // --- PLAYER SHOOTING HANDLING ---

    /**
     * Initiates shooting or sets the flag for continuous firing.
     */
    public void startShooting() {
        if (!isRunning) return;
        isMousePressed = true;
        if (player.getCurrentWeapon() == Player.WeaponType.DEFAULT ||
                player.getCurrentWeapon() == Player.WeaponType.SHOTGUN) {
            fireWeapon();
        }
    }

    /**
     * Stops user-initiated shooting.
     */
    public void stopShooting() {
        isMousePressed = false;
    }

    /**
     * Placeholder for interface requirement.
     * Actual shooting logic is handled via {@link #startShooting()} and Internal update loops.
     */
    @Override public void shoot() {}

    /**
     * Calculates the exact position of the gun muzzle for bullet spawning.
     * <p>
     * PERBAIKAN 2: GUN OFFSET.
     * Computes the spawn point based on player rotation to ensure bullets
     * appear to come from the weapon (right side) rather than the center of the body.
     * </p>
     *
     * @return a {@link Point2D.Double} representing the muzzle coordinates
     */
    private Point2D.Double getGunMuzzlePosition() {
        double angle = player.getRotation();

        // Player Center (Visual)
        double centerX = player.getX() + 30;
        double centerY = player.getY() + 30;

        // OFFSET SETTING (Adjust these numbers to match the image)
        double forwardOffset = 25.0; // Muzzle forward distance from center
        double rightOffset = 18.0;   // Muzzle right distance from center (right hand)

        // Vector Rotation Formula:
        // SpawnX = CenterX + (Forward * cos) + (Right * cos(90+angle))
        // SpawnY = CenterY + (Forward * sin) + (Right * sin(90+angle))
        // cos(90+a) = -sin(a), sin(90+a) = cos(a)

        double spawnX = centerX + (forwardOffset * Math.cos(angle)) - (rightOffset * Math.sin(angle));
        double spawnY = centerY + (forwardOffset * Math.sin(angle)) + (rightOffset * Math.cos(angle));

        return new Point2D.Double(spawnX, spawnY);
    }

    /**
     * Fires the player's current weapon.
     * <p>
     * Handles different weapon types (Shotgun, Assault Rifle, Pistol),
     * ammo consumption, spread/jitter, and sound effects.
     * </p>
     */
    private void fireWeapon() {
        Player.WeaponType weapon = player.getCurrentWeapon();
        Point2D.Double muzzle = getGunMuzzlePosition(); // Get the corrected spawn position

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

    /**
     * Fires the default pistol weapon.
     */
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

    /**
     * Overload of {@link #startGame(String)} that defaults to "Player".
     */
    @Override public void startGame() { startGame("Player"); }
    @Override public int getScore() { return player.getScore(); }
    @Override public int getAmmo() { return player.getAmmo(); }

    /**
     * Gets the current wave number.
     * @return current wave
     */
    public int getWave() { return currentWave; }
}