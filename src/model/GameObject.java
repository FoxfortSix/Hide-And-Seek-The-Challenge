package model;

import java.awt.Rectangle;

/**
 * Filename  : GameObject.java
 * Package   : model
 * Description:
 * The abstract base class for all game entities (Player, Alien, Bullet, Obstacle).
 * It holds common properties like position (x, y) and dimensions (width, height).
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public abstract class GameObject {

    // Protected fields so subclasses can access them directly if needed
    protected double x;
    protected double y;
    protected int width;
    protected int height;

    /**
     * Constructor: GameObject
     * Initializes the position and size of the object.
     * * @param x Initial X coordinate.
     * @param y Initial Y coordinate.
     * @param width Width of the object.
     * @param height Height of the object.
     */
    public GameObject(double x, double y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // --- GETTERS & SETTERS ---

    /**
     * @return The current X coordinate.
     */
    public double getX() {
        return x;
    }

    /**
     * @param x The new X coordinate.
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * @return The current Y coordinate.
     */
    public double getY() {
        return y;
    }

    /**
     * @param y The new Y coordinate.
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * @return The width of the object.
     */
    public int getWidth() {
        return width;
    }

    /**
     * @return The height of the object.
     */
    public int getHeight() {
        return height;
    }

    /**
     * Helper method for collision detection.
     * Creates a Rectangle object based on current position and size.
     * This is used by the Presenter to check for intersections.
     * * @return A Rectangle representing the object's bounds.
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }
}