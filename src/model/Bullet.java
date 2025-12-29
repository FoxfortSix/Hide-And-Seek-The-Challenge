package model;

import java.awt.Color;
import java.awt.Rectangle;

/**
 * Represents a projectile entity fired by a player or enemy.
 * <p>
 * The {@code Bullet} class extends {@link GameObject} and encapsulates
 * movement, rendering, and collision-related data for a single projectile.
 * </p>
 *
 * <p>
 * Core responsibilities of this class include:
 * <ul>
 *     <li>Tracking bullet position and velocity</li>
 *     <li>Providing sprite lookup information via an image key</li>
 *     <li>Supplying rotation data so the sprite aligns with movement direction</li>
 *     <li>Exposing a collision hitbox for physics and hit detection</li>
 * </ul>
 * </p>
 *
 * <p>
 * Rendering systems may choose to use {@code imageKey} to load a sprite.
 * If sprite loading fails, the {@code color} field can be used as a fallback
 * visual representation.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class Bullet extends GameObject {

    /** Horizontal velocity component of the bullet. */
    private double velX;

    /** Vertical velocity component of the bullet. */
    private double velY;

    /**
     * Fallback color used when the sprite image
     * cannot be loaded or rendered.
     */
    private Color color;

    /**
     * Key used to retrieve the bullet sprite
     * from an image or asset manager.
     */
    private String imageKey;

    /**
     * Constructs a new {@code Bullet} instance.
     * <p>
     * The bullet is initialized with a rectangular hitbox sized
     * specifically to support sprite rotation without distortion.
     * </p>
     *
     * @param x        the initial horizontal position
     * @param y        the initial vertical position
     * @param velX     the horizontal velocity
     * @param velY     the vertical velocity
     * @param color    fallback color if sprite rendering fails
     * @param imageKey identifier used to resolve the bullet sprite
     */
    public Bullet(double x, double y, double velX, double velY, Color color, String imageKey) {
        super(x, y, 20, 10);
        this.velX = velX;
        this.velY = velY;
        this.color = color;
        this.imageKey = imageKey;
    }

    /**
     * Updates the bullet's position based on its velocity.
     * <p>
     * This method is typically invoked once per game tick
     * by the main game loop.
     * </p>
     */
    public void update() {
        x += velX;
        y += velY;
    }

    /**
     * Returns the image key used for sprite lookup.
     *
     * @return the image key string
     */
    public String getImageKey() {
        return imageKey;
    }

    /**
     * Returns the fallback rendering color.
     *
     * @return the bullet color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Calculates the rotation angle of the bullet in radians.
     * <p>
     * The rotation is derived from the velocity vector so that
     * the rendered sprite always faces the direction of movement.
     * </p>
     *
     * @return the rotation angle in radians
     */
    public double getRotation() {
        return Math.atan2(velY, velX);
    }

    /**
     * Returns the rectangular collision bounds of the bullet.
     * <p>
     * This bounding box is used by collision detection systems
     * to determine intersections with other game objects.
     * </p>
     *
     * @return a {@link Rectangle} representing the hitbox
     */
    @Override
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }
}
