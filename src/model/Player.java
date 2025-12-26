package model;

/**
 * Filename  : Player.java
 * Package   : model
 * Description:
 * Represents the player character.
 * Stores specific attributes like rotation angle (for aiming), movement speed,
 * current score, and available ammo.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class Player extends GameObject {

    // Angle in radians, used to render the player facing the mouse cursor
    private double rotation;

    // Movement speed
    private double speed = 3.0;

    // Game Status
    private int score = 0;
    private int ammo = 0; // Starts with 0 ammo as per requirements

    /**
     * Constructor: Player
     * Spawns the player at the specified coordinates with a fixed size (30x30).
     * * @param startX Initial X coordinate.
     * @param startY Initial Y coordinate.
     */
    public Player(double startX, double startY) {
        super(startX, startY, 30, 30);
        this.rotation = 0;
    }

    // --- GETTERS & SETTERS ---

    /**
     * @return The rotation angle in radians.
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Sets the player's facing angle.
     * @param rotation Angle in radians.
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    /**
     * @return The movement speed of the player.
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * @return Current accumulated score.
     */
    public int getScore() {
        return score;
    }

    /**
     * Updates the score.
     * @param score New score value.
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * @return Current ammunition count.
     */
    public int getAmmo() {
        return ammo;
    }

    /**
     * Updates the ammo count.
     * @param ammo New ammo value.
     */
    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }
}