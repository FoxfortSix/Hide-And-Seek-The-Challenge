package model;

/**
 * Represents an alien enemy entity in the game world.
 * <p>
 * The {@code Alien} class extends {@link GameObject} and introduces
 * behavior-related attributes such as movement velocity, attack timing,
 * firing patterns, and weapon loadouts.
 * </p>
 *
 * <p>
 * Each alien has:
 * <ul>
 *     <li>A movement {@link Type} that defines its navigation behavior</li>
 *     <li>A {@link Loadout} that determines its firing pattern and cooldown</li>
 *     <li>State tracking for burst firing and shot timing</li>
 * </ul>
 * </p>
 *
 * <p>
 * This class acts primarily as a data holder. The actual movement,
 * shooting logic, and AI behavior are expected to be handled by
 * game systems or controllers that consume this model.
 * </p>
 *
 * @author Mochammad Azka Basria
 */

public class Alien extends GameObject {

    /**
     * Defines the movement behavior of an alien.
     */
    public enum Type {

        /**
         * Alien moves directly toward the target in a straight line.
         */
        CHASER,

        /**
         * Alien moves using a zigzag or oscillating pattern.
         */
        ZIGZAG
    }

    /**
     * Defines the weapon configuration carried by the alien.
     * <p>
     * Each loadout implies different firing mechanics such as
     * bullet count, firing spread, and cooldown duration.
     * </p>
     */
    public enum Loadout {

        /**
         * Default weapon with a single-shot firing behavior.
         */
        DEFAULT,

        /**
         * Rapid-fire weapon with a short cooldown between shots.
         */
        ASSAULT_RIFLE,

        /**
         * Fires multiple projectiles in a spread pattern
         * with a longer cooldown.
         */
        SHOTGUN
    }

    /** Movement behavior type of the alien. */
    private Type type;

    /** Weapon loadout currently equipped by the alien. */
    private Loadout loadout;

    /** Horizontal velocity component. */
    private double velX;

    /** Vertical velocity component. */
    private double velY;

    /**
     * Timestamp of the last fired shot.
     * Used to enforce weapon cooldowns.
     */
    private long lastShotTime;

    /**
     * Indicates whether the alien is currently performing a burst fire.
     */
    private boolean isBursting = false;

    /**
     * Number of shots fired during the current burst sequence.
     */
    private int burstShotsFired = 0;

    /**
     * Timestamp of the last burst shot fired.
     * Used to space shots inside a burst.
     */
    private long lastBurstTime = 0;

    /**
     * Constructs a new {@code Alien} instance with the specified
     * position, movement type, and weapon loadout.
     * <p>
     * The alien is initialized with a fixed size and its shooting
     * timer is set to the current system time.
     * </p>
     *
     * @param x       the initial horizontal position
     * @param y       the initial vertical position
     * @param type    the movement behavior type
     * @param loadout the weapon loadout carried by the alien
     */
    public Alien(double x, double y, Type type, Loadout loadout) {
        super(x, y, 30, 30);
        this.type = type;
        this.loadout = loadout;
        this.lastShotTime = System.currentTimeMillis();
    }

    /**
     * Returns the movement behavior type of this alien.
     *
     * @return the alien movement type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the current weapon loadout.
     *
     * @return the equipped loadout
     */
    public Loadout getLoadout() {
        return loadout;
    }

    /**
     * Sets a new weapon loadout for the alien.
     *
     * @param loadout the new loadout to assign
     */
    public void setLoadout(Loadout loadout) {
        this.loadout = loadout;
    }

    /**
     * Returns the horizontal velocity.
     *
     * @return horizontal velocity value
     */
    public double getVelX() {
        return velX;
    }

    /**
     * Sets the horizontal velocity.
     *
     * @param velX the new horizontal velocity
     */
    public void setVelX(double velX) {
        this.velX = velX;
    }

    /**
     * Returns the vertical velocity.
     *
     * @return vertical velocity value
     */
    public double getVelY() {
        return velY;
    }

    /**
     * Sets the vertical velocity.
     *
     * @param velY the new vertical velocity
     */
    public void setVelY(double velY) {
        this.velY = velY;
    }

    /**
     * Returns the timestamp of the last fired shot.
     *
     * @return last shot time in milliseconds
     */
    public long getLastShotTime() {
        return lastShotTime;
    }

    /**
     * Updates the timestamp of the last fired shot.
     *
     * @param lastShotTime the new shot timestamp
     */
    public void setLastShotTime(long lastShotTime) {
        this.lastShotTime = lastShotTime;
    }

    /**
     * Indicates whether the alien is currently in a burst-firing state.
     *
     * @return {@code true} if bursting, {@code false} otherwise
     */
    public boolean isBursting() {
        return isBursting;
    }

    /**
     * Sets the burst-firing state.
     *
     * @param bursting {@code true} to enable burst mode
     */
    public void setBursting(boolean bursting) {
        isBursting = bursting;
    }

    /**
     * Returns the number of shots fired in the current burst.
     *
     * @return burst shot count
     */
    public int getBurstShotsFired() {
        return burstShotsFired;
    }

    /**
     * Sets the number of shots fired in the current burst.
     *
     * @param count number of shots fired
     */
    public void setBurstShotsFired(int count) {
        this.burstShotsFired = count;
    }

    /**
     * Returns the timestamp of the last burst shot.
     *
     * @return last burst shot time in milliseconds
     */
    public long getLastBurstTime() {
        return lastBurstTime;
    }

    /**
     * Updates the timestamp of the last burst shot.
     *
     * @param time new burst timestamp
     */
    public void setLastBurstTime(long time) {
        this.lastBurstTime = time;
    }
}
