package model;

import java.awt.Color;

/**
 * Represents a temporary weapon power-up entity in the game world.
 * <p>
 * The {@code PowerUp} class extends {@link GameObject} and models
 * collectible items that grant the player enhanced weapons for
 * a limited duration.
 * </p>
 *
 * <p>
 * Each power-up:
 * <ul>
 *     <li>Has a specific {@link Type} defining the weapon granted</li>
 *     <li>Exists for a limited time before disappearing automatically</li>
 *     <li>Provides visual cues through color and letter representation</li>
 * </ul>
 * </p>
 *
 * <p>
 * Collision detection systems are expected to determine when the
 * player collects a power-up and apply its effect accordingly.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class PowerUp extends GameObject {

    /**
     * Defines the type of weapon enhancement provided by the power-up.
     */
    public enum Type {

        /** Grants an assault rifle weapon power-up. */
        ASSAULT_RIFLE,

        /** Grants a shotgun weapon power-up. */
        SHOTGUN
    }

    /** The weapon type associated with this power-up. */
    private Type type;

    /**
     * Timestamp marking the moment this power-up was created.
     * Used to determine expiration.
     */
    private long creationTime;

    /**
     * Lifetime duration of the power-up in milliseconds.
     * After this time elapses, the power-up is removed from the game world.
     */
    private final long LIFETIME = 10000;

    /**
     * Constructs a new {@code PowerUp} instance.
     * <p>
     * The power-up is initialized with a fixed collision size
     * and records its creation time for expiration tracking.
     * </p>
     *
     * @param x    the horizontal position in world coordinates
     * @param y    the vertical position in world coordinates
     * @param type the weapon type granted by this power-up
     */
    public PowerUp(double x, double y, Type type) {
        super(x, y, 30, 30);
        this.type = type;
        this.creationTime = System.currentTimeMillis();
    }

    /**
     * Returns the type of this power-up.
     *
     * @return the power-up type
     */
    public Type getType() {
        return type;
    }

    /**
     * Determines whether the power-up has exceeded its lifetime.
     * <p>
     * This method is typically called during the game loop to
     * remove expired power-ups from the world.
     * </p>
     *
     * @return {@code true} if the power-up has expired, {@code false} otherwise
     */
    public boolean isExpired() {
        return (System.currentTimeMillis() - creationTime) > LIFETIME;
    }

    /**
     * Returns the display color associated with this power-up.
     * <p>
     * The color provides a visual distinction between different
     * weapon types.
     * </p>
     *
     * @return the color representing this power-up
     */
    public Color getColor() {
        return (type == Type.ASSAULT_RIFLE)
                ? Color.RED
                : new Color(255, 200, 0);
    }

    /**
     * Returns the letter symbol representing this power-up.
     * <p>
     * This value is commonly rendered on top of the power-up icon
     * to indicate its weapon type to the player.
     * </p>
     *
     * @return a single-character string identifier
     */
    public String getLetter() {
        return (type == Type.ASSAULT_RIFLE) ? "A" : "S";
    }
}
