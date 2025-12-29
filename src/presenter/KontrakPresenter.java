package presenter;

/**
 * Filename  : KontrakPresenter.java
 * Package   : presenter
 * Description:
 * Public Interface defining the contract between the View and the Presenter.
 * <p>
 * This interface outlines the methods necessary for the View to communicate usage
 * inputs (movement, shooting) and lifecycle events (start game) to the Presenter,
 * as well as methods for retrieving game state data (score, ammo).
 * </p>
 *
 * @author Mochammad Azka Basria
 * @version 1.0
 */
public interface KontrakPresenter {

    /**
     * Loads high score data from the database to be displayed in the Menu.
     * <p>
     * Typically called when the application starts or returns to the main menu.
     * </p>
     */
    void loadData();

    /**
     * Starts the game session with a specific username.
     *
     * @param username The player's name input from the Menu.
     */
    void startGame(String username);

    /**
     * Default start game method (Fallback).
     * <p>
     * Starts the game with a default username (e.g., "Player").
     * </p>
     */
    void startGame();

    /**
     * Main game logic update.
     * <p>
     * Should be called periodically by the game loop to update game state.
     * </p>
     */
    void updateGame();

    // --- INPUT COMMANDS ---

    /**
     * Handles directional movement input from the user.
     *
     * @param up    True if UP key is pressed.
     * @param down  True if DOWN key is pressed.
     * @param left  True if LEFT key is pressed.
     * @param right True if RIGHT key is pressed.
     */
    void movePlayer(boolean up, boolean down, boolean left, boolean right);

    /**
     * Updates the player's rotation to face the mouse cursor.
     *
     * @param mouseX X-coordinate of the mouse cursor.
     * @param mouseY Y-coordinate of the mouse cursor.
     */
    void rotatePlayer(int mouseX, int mouseY);

    /**
     * Triggers the player to fire a bullet.
     * <p>
     * Logic for weapon type, fire rate, and ammo consumption is handled by the implementation.
     * </p>
     */
    void shoot();

    // --- DATA RETRIEVAL (For UI Display) ---

    /**
     * Gets the current player score.
     *
     * @return Current score.
     */
    int getScore();

    /**
     * Gets the current remaining ammo.
     *
     * @return Current ammo count.
     */
    int getAmmo();

    /**
     * Gets the current wave number.
     *
     * @return Current wave.
     */
    int getWave();
}