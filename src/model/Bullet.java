package model;

/**
 * Filename  : Bullet.java
 * Package   : model
 * Description:
 * Represents a projectile fired by either the Player or an Alien.
 * Moves in a straight line defined by an angle.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class Bullet extends GameObject {

    // The angle of trajectory in radians
    private double angle;

    // Constant speed of the bullet
    private double speed = 7.0;

    // Flag to distinguish between player bullets and enemy bullets
    private boolean isPlayerBullet;

    /**
     * Constructor: Bullet
     * * @param x Initial X coordinate.
     * @param y Initial Y coordinate.
     * @param angle Direction of travel (radians).
     * @param isPlayerBullet True if fired by player, False if fired by enemy.
     */
    public Bullet(double x, double y, double angle, boolean isPlayerBullet) {
        super(x, y, 8, 8); // Bullets are small (8x8)
        this.angle = angle;
        this.isPlayerBullet = isPlayerBullet;
    }

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
     * @return True if the bullet belongs to the Player.
     */
    public boolean isPlayerBullet() {
        return isPlayerBullet;
    }
}