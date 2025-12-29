package model;

/**
 * Represents the player-controlled entity in the game world.
 * <p>
 * The {@code Player} class extends {@link GameObject} and encapsulates
 * player-specific state such as movement speed, score tracking,
 * ammunition, rotation, and weapon power-up management.
 * </p>
 *
 * <p>
 * This class is primarily responsible for:
 * <ul>
 *     <li>Maintaining player statistics and movement attributes</li>
 *     <li>Tracking the currently equipped weapon</li>
 *     <li>Managing time-limited weapon power-ups</li>
 * </ul>
 * </p>
 *
 * <p>
 * Actual input handling, shooting logic, and rendering are expected
 * to be managed by external controller or presenter components.
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class Player extends GameObject {

    /**
     * Defines the weapon types available to the player.
     */
    public enum WeaponType {

        /** Standard single-shot pistol weapon. */
        DEFAULT,

        /** Automatic weapon with rapid-fire capability. */
        ASSAULT_RIFLE,

        /** Weapon that fires multiple projectiles in a spread pattern. */
        SHOTGUN
    }

    /** Movement speed of the player. */
    private double speed = 3.5;

    /** Player's accumulated score. */
    private int score = 0;

    /** Remaining ammunition available to the player. */
    private int ammo = 0;

    /**
     * Rotation angle of the player in radians.
     * Used to align rendering and shooting direction.
     */
    private double rotation;

    /**
     * Currently equipped weapon type.
     */
    private WeaponType currentWeapon = WeaponType.DEFAULT;

    /**
     * System timestamp indicating when the current weapon power-up expires.
     * A value of zero indicates no active power-up.
     */
    private long weaponPowerUpEndTime = 0;

    /**
     * Constructs a new {@code Player} instance.
     * <p>
     * The player is initialized with a fixed collision size
     * suitable for character representation.
     * </p>
     *
     * @param x the initial horizontal position
     * @param y the initial vertical position
     */
    public Player(double x, double y) {
        super(x, y, 30, 30);
    }

    /**
     * Activates a weapon power-up for a limited duration.
     * <p>
     * This method assigns a new weapon type to the player and
     * calculates the expiration time based on the current system
     * clock.
     * </p>
     *
     * @param type            the weapon type to activate
     * @param durationSeconds duration of the power-up in seconds
     */
    public void setWeapon(WeaponType type, int durationSeconds) {
        this.currentWeapon = type;
        this.weaponPowerUpEndTime =
                System.currentTimeMillis() + (durationSeconds * 1000L);
    }

    /**
     * Checks whether the active weapon power-up has expired.
     * <p>
     * This method is expected to be invoked during the game loop.
     * When the power-up duration ends, the weapon automatically
     * reverts to {@link WeaponType#DEFAULT}.
     * </p>
     */
    public void checkWeaponTimer() {
        if (currentWeapon != WeaponType.DEFAULT) {
            if (System.currentTimeMillis() > weaponPowerUpEndTime) {
                currentWeapon = WeaponType.DEFAULT;
            }
        }
    }

    /**
     * Returns the remaining time of the current weapon power-up.
     * <p>
     * This value is typically used by the user interface
     * to display a countdown timer on the HUD.
     * </p>
     *
     * @return remaining time in seconds, or {@code 0} if no power-up is active
     */
    public int getWeaponTimeLeft() {
        if (currentWeapon == WeaponType.DEFAULT) {
            return 0;
        }

        long timeLeftMillis =
                weaponPowerUpEndTime - System.currentTimeMillis();

        if (timeLeftMillis < 0) {
            return 0;
        }

        return (int) (timeLeftMillis / 1000);
    }

    /**
     * Returns the currently equipped weapon type.
     *
     * @return the active weapon type
     */
    public WeaponType getCurrentWeapon() {
        return currentWeapon;
    }

    /**
     * Returns the player's movement speed.
     *
     * @return movement speed value
     */
    public double getSpeed() {
        return speed;
    }

    /**
     * Updates the player's movement speed.
     *
     * @param speed the new speed value
     */
    public void setSpeed(double speed) {
        this.speed = speed;
    }

    /**
     * Returns the player's current score.
     *
     * @return score value
     */
    public int getScore() {
        return score;
    }

    /**
     * Updates the player's score.
     *
     * @param score the new score value
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Returns the remaining ammunition count.
     *
     * @return remaining ammo
     */
    public int getAmmo() {
        return ammo;
    }

    /**
     * Updates the remaining ammunition count.
     *
     * @param ammo the new ammo value
     */
    public void setAmmo(int ammo) {
        this.ammo = ammo;
    }

    /**
     * Returns the current rotation angle.
     *
     * @return rotation angle in radians
     */
    public double getRotation() {
        return rotation;
    }

    /**
     * Updates the rotation angle.
     *
     * @param rotation the new rotation angle in radians
     */
    public void setRotation(double rotation) {
        this.rotation = rotation;
    }
}
