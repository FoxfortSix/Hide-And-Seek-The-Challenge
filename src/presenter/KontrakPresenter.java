package presenter;

/**
 * Filename  : KontrakPresenter.java
 * Package   : presenter
 * Description:
 * FINAL Interface defining the methods available to the View.
 * Handles user input commands, game start logic, and data retrieval.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public interface KontrakPresenter {

    /**
     * Loads high score data from the database to be displayed in the Menu.
     */
    void loadData();

    /**
     * Starts the game session with a specific username.
     * * @param username The player's name input from the Menu.
     */
    void startGame(String username);

    /**
     * Default start game method (Fallback).
     */
    void startGame();

    /**
     * Main game logic update.
     */
    void updateGame();

    // --- INPUT COMMANDS ---

    /**
     * Handles directional movement input from the user.
     * * @param up True if UP key is pressed.
     * @param down True if DOWN key is pressed.
     * @param left True if LEFT key is pressed.
     * @param right True if RIGHT key is pressed.
     */
    void movePlayer(boolean up, boolean down, boolean left, boolean right);

    /**
     * Updates the player's rotation to face the mouse cursor.
     * * @param mouseX X-coordinate of the mouse cursor.
     * @param mouseY Y-coordinate of the mouse cursor.
     */
    void rotatePlayer(int mouseX, int mouseY);

    /**
     * Triggers the player to fire a bullet.
     */
    void shoot();

    // --- DATA RETRIEVAL (For UI Display) ---

    /**
     * @return Current score.
     */
    int getScore();

    /**
     * @return Current ammo count.
     */
    int getAmmo();
}