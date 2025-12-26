package model;

/**
 * Filename  : Benefit.java
 * Package   : model
 * Description:
 * An Entity class (POJO) representing the player's game data structure
 * (Score, Missed Bullets, Ammo). Contains only private fields and
 * getter/setter methods.
 *
 * Programmer: [Your Name]
 * Date      : 2025-12-24
 */
public class Benefit {

    // Attributes matching database columns
    private String username;
    private int skor;
    private int peluruMeleset;
    private int sisaPeluru;

    /**
     * Constructor: Benefit
     * Default constructor.
     */
    public Benefit() {
    }

    // --- GETTERS & SETTERS ---

    /**
     * Retrieves the player's username.
     * @return The username string.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the player's username.
     * @param username The new username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Retrieves the total score.
     * @return Current score value.
     */
    public int getSkor() {
        return skor;
    }

    /**
     * Sets the total score.
     * @param skor The new score value.
     */
    public void setSkor(int skor) {
        this.skor = skor;
    }

    /**
     * Retrieves the count of enemy bullets dodged.
     * @return The number of missed bullets.
     */
    public int getPeluruMeleset() {
        return peluruMeleset;
    }

    /**
     * Sets the count of enemy bullets dodged.
     * @param peluruMeleset The new count value.
     */
    public void setPeluruMeleset(int peluruMeleset) {
        this.peluruMeleset = peluruMeleset;
    }

    /**
     * Retrieves the player's remaining ammo.
     * @return The remaining ammo count.
     */
    public int getSisaPeluru() {
        return sisaPeluru;
    }

    /**
     * Sets the player's remaining ammo.
     * @param sisaPeluru The new ammo count.
     */
    public void setSisaPeluru(int sisaPeluru) {
        this.sisaPeluru = sisaPeluru;
    }
}