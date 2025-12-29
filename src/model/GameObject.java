package model;

import java.awt.Rectangle;

/**
 * Abstract base class for all entities in the game world.
 * <p>
 * The {@code GameObject} class defines the fundamental spatial properties
 * shared by every interactive or renderable entity, including position
 * and size. Concrete game objects such as players, enemies, projectiles,
 * and obstacles inherit from this class.
 * </p>
 *
 * <p>
 * Responsibilities of this class include:
 * <ul>
 *     <li>Storing world coordinates</li>
 *     <li>Defining object dimensions</li>
 *     <li>Providing a standard collision bounding box</li>
 * </ul>
 * </p>
 *
 * <p>
 * Subclasses are expected to implement behavior-specific logic such as
 * movement, rendering, and interaction handling.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public abstract class GameObject {

    /**
     * Horizontal position in world coordinates.
     * <p>
     * Declared as {@code protected} to allow direct access by subclasses
     * where performance or simplicity is required.
     * </p>
     */
    protected double x;

    /**
     * Vertical position in world coordinates.
     */
    protected double y;

    /**
     * Width of the object's collision and render area.
     */
    protected int width;

    /**
     * Height of the object's collision and render area.
     */
    protected int height;

    /**
     * Constructs a new {@code GameObject} with the specified
     * position and dimensions.
     *
     * @param x      the initial horizontal coordinate
     * @param y      the initial vertical coordinate
     * @param width  the width of the object
     * @param height the height of the object
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the current horizontal position.
     *
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Updates the horizontal position.
     *
     * @param x the new x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the current vertical position.
     *
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Updates the vertical position.
     *
     * @param y the new y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns the width of the object.
     *
     * @return the object width
     */
    public int getWidth() {
        return width;
    }

    /**
     * Returns the height of the object.
     *
     * @return the object height
     */
    public int getHeight() {
        return height;
    }

    /**
     * Returns the collision bounds of the object.
     * <p>
     * This method generates a rectangular bounding box based on the
     * current position and dimensions. It is primarily used by
     * collision detection systems to evaluate intersections
     * between game entities.
     * </p>
     *
     * @return a {@link Rectangle} representing the object's bounds
     */
    public Rectangle getBounds() {
        return new Rectangle((int) x, (int) y, width, height);
    }
}
