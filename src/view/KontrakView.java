package view;

import model.Alien;
import model.Bullet;
import model.Obstacle;
import model.Player;
import java.util.List;

/**
 * Filename  : KontrakView.java
 * Package   : view
 * Description:
 * FINAL Interface defining the methods that the View must implement.
 * Includes methods for Game Rendering, Audio, and Menu/High Score management.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public interface KontrakView {

    /**
     * Updates the High Score table in the Menu.
     * * @param data A 2D array of objects containing database records.
     */
    void updateScoreTable(Object[][] data);

    /**
     * Updates the graphical representation of the game.
     * Called by the Presenter every frame.
     * * @param player The current state of the player.
     * @param aliens The list of active aliens.
     * @param bullets The list of active bullets.
     * @param obstacles The list of static obstacles.
     */
    void updateGraphics(Player player, List<Alien> aliens, List<Bullet> bullets, List<Obstacle> obstacles);

    /**
     * Plays a sound effect or background music.
     * * @param type The key/name of the sound file (e.g., "SHOOT", "BGM").
     */
    void playSound(String type);

    /**
     * Switches the view to the Main Menu (High Score Table).
     */
    void showMenu();

    /**
     * Switches the view to the Gameplay screen.
     */
    void showGame();

    /**
     * Displays the Game Over screen/dialog.
     * * @param finalScore The final score achieved by the player.
     */
    void showGameOver(int finalScore);
}