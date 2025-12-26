package model;

/**
 * Filename  : Alien.java
 * Package   : model
 * Description:
 * Represents the enemy character.
 * Supports omni-directional movement (360 degrees) via velocity vectors.
 * Includes shooting cooldown logic.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class Alien extends GameObject {

    /**
     * Enum defining the behavior type of the Alien.
     * CHASER: Moves directly towards the player.
     * ZIGZAG: Moves diagonally or erratically.
     */
    public enum Type {
        CHASER,
        ZIGZAG
    }

    private Type type;

    // Velocity vectors for 360-degree movement
    private double velX;
    private double velY;

    // Timestamp of the last shot fired to manage attack speed (cooldown)
    private long lastShotTime;

    /**
     * Constructor: Alien
     * Spawns an alien at the specified coordinates with a fixed size (30x30).
     * * @param x Initial X coordinate.
     * @param y Initial Y coordinate.
     * @param type The behavior type of the alien.
     */
    public Alien(double x, double y, Type type) {
        super(x, y, 30, 30);
        this.type = type;
        this.lastShotTime = System.currentTimeMillis();
    }

    // --- GETTERS & SETTERS ---

    /**
     * @return The behavior type of the alien.
     */
    public Type getType() {
        return type;
    }

    /**
     * @return Velocity on the X-axis.
     */
    public double getVelX() {
        return velX;
    }

    /**
     * Sets velocity on the X-axis.
     * @param velX Velocity value.
     */
    public void setVelX(double velX) {
        this.velX = velX;
    }

    /**
     * @return Velocity on the Y-axis.
     */
    public double getVelY() {
        return velY;
    }

    /**
     * Sets velocity on the Y-axis.
     * @param velY Velocity value.
     */
    public void setVelY(double velY) {
        this.velY = velY;
    }

    /**
     * @return Timestamp (ms) of the last shot fired.
     */
    public long getLastShotTime() {
        return lastShotTime;
    }

    /**
     * Updates the timestamp of the last shot.
     * @param time Current system time in milliseconds.
     */
    public void setLastShotTime(long time) {
        this.lastShotTime = time;
    }
}