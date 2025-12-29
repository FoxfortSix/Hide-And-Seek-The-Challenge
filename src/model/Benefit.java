package model;

/**
 * Represents a player's benefit-related game data.
 * <p>
 * The {@code Benefit} class is a Plain Old Java Object (POJO) used to
 * encapsulate player-related statistical data generated during gameplay.
 * This includes score tracking, ammunition usage, and shooting accuracy.
 * </p>
 *
 * <p>
 * This class is designed strictly as a data container and follows the
 * JavaBean convention:
 * <ul>
 *     <li>All fields are private</li>
 *     <li>A public no-argument constructor is provided</li>
 *     <li>Each field is accessed through getter and setter methods</li>
 * </ul>
 * </p>
 *
 * <p>
 * Typical usage includes:
 * <ul>
 *     <li>Persisting player statistics to a database</li>
 *     <li>Transferring player data between application layers</li>
 *     <li>Displaying end-of-game or in-game statistics</li>
 * </ul>
 * </p>
 *
 * @author Mochammad Azka Basria
 */
public class Benefit {

    /** The unique username identifying the player. */
    private String username;

    /** The total score achieved by the player. */
    private int skor;

    /** The number of bullets that failed to hit any target. */
    private int peluruMeleset;

    /** The remaining ammunition available to the player. */
    private int sisaPeluru;

    /**
     * Constructs a new {@code Benefit} instance.
     * <p>
     * This default constructor initializes an empty data object.
     * Field values are expected to be populated using setter methods.
     * </p>
     */
    public Benefit() {
    }

    /**
     * Returns the player's username.
     *
     * @return the username associated with this record
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the player's username.
     * <p>
     * This value is typically used as a unique identifier when
     * storing or retrieving player statistics.
     * </p>
     *
     * @param username the username to assign
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the player's total score.
     *
     * @return the current score value
     */
    public int getSkor() {
        return skor;
    }

    /**
     * Updates the player's total score.
     *
     * @param skor the new score value
     */
    public void setSkor(int skor) {
        this.skor = skor;
    }

    /**
     * Returns the number of bullets that did not hit a target.
     * <p>
     * This value can be used to evaluate player accuracy
     * or calculate performance metrics.
     * </p>
     *
     * @return the count of missed bullets
     */
    public int getPeluruMeleset() {
        return peluruMeleset;
    }

    /**
     * Sets the number of missed bullets.
     *
     * @param peluruMeleset the new missed bullet count
     */
    public void setPeluruMeleset(int peluruMeleset) {
        this.peluruMeleset = peluruMeleset;
    }

    /**
     * Returns the amount of ammunition remaining.
     *
     * @return the remaining ammunition count
     */
    public int getSisaPeluru() {
        return sisaPeluru;
    }

    /**
     * Sets the remaining ammunition count.
     *
     * @param sisaPeluru the new ammunition value
     */
    public void setSisaPeluru(int sisaPeluru) {
        this.sisaPeluru = sisaPeluru;
    }
}
