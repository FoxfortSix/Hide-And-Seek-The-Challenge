package model;

import java.awt.Color;

/**
 * Filename  : Bullet.java
 * Package   : model
 * Description:
 * Represents a projectile fired by either the Player or an Alien.
 * Moves in a straight line defined by an angle.
 * Updated: Added setSpeed method for Weapon Loadouts and Color support.
 */
public class Bullet extends GameObject {

    // The angle of trajectory in radians
    private double angle;

    // Constant speed of the bullet (Default 7.0)
    private double speed = 7.0;

    // Flag to distinguish between player bullets and enemy bullets
    private boolean isPlayerBullet;

    // Color of the bullet (Red for AR, Yellow for Shotgun, etc.)
    private Color color;

    /**
     * Constructor: Bullet
     * @param x Initial X coordinate.
     * @param y Initial Y coordinate.
     * @param angle Direction of travel (radians).
     * @param isPlayerBullet True if fired by player, False if fired by enemy.
     * @param color The color of the bullet.
     */
    // PERBAIKAN UTAMA ADA DI SINI: Menambahkan parameter "Color color"
    public Bullet(double x, double y, double angle, boolean isPlayerBullet, Color color) {
        super(x, y, 8, 8); // Bullets are small (8x8)
        this.angle = angle;
        this.isPlayerBullet = isPlayerBullet;
        this.color = color; // Menyimpan warna yang dikirim dari GamePresenter
    }

    public Color getColor() { return color; }

    /**
     * @return The trajectory angle in radians.
     */
    public double getAngle() {
        return angle;
    }

    /**
     * @return The movement speed of the bullet.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Sets the speed of the bullet.
     * Used for different weapon types (e.g., Shotgun vs Rifle).
     * @param speed The new speed value.
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * @return True if the bullet belongs to the Player.
     */
    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
}