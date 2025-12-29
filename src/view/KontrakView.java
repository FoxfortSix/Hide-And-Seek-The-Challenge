package view;

import model.Alien;
import model.Bullet;
import model.Obstacle;
import model.Player;
import model.PowerUp;
import presenter.KontrakPresenter;

import java.util.List;

/**
 * Filename  : KontrakView.java
 * Package   : view
 * Description:
 * Public Interface defining the methods that the View must implement.
 * <p>
 * This interface allows the Presenter to update the View without knowing the specific
 * implementation details (e.g., Swing, JavaFX). It handles UI updates for scores,
 * graphics rendering, and screen transitions.
 * </p>
 *
 * @author Mochammad Azka Basria
 * @version 1.0
 */
public interface KontrakView {

    /**
     * Connects the View with the Presenter.
     *
     * @param presenter The presenter instance handling game logic.
     */
    void setPresenter(KontrakPresenter presenter);

    /**
     * Updates the High Score table in the Menu.
     *
     * @param data A 2D array of objects containing score data to be displayed in the JTable.
     */
    void updateScoreTable(Object[][] data);

    /**
     * Updates the graphical representation of the game.
     * <p>
     * Called by the Presenter every frame to render game entities.
     * Uses 5 parameters including PowerUps.
     * </p>
     *
     * @param player    The current player object.
     * @param aliens    List of active aliens.
     * @param bullets   List of active bullets.
     * @param obstacles List of level obstacles.
     * @param powerUps  List of active power-ups.
     */
    void updateGraphics(Player player, List<Alien> aliens, List<Bullet> bullets, List<Obstacle> obstacles, List<PowerUp> powerUps);

    /**
     * Plays a sound effect or background music.
     *
     * @param type The key/ID of the sound to play (e.g., "SHOOT", "BGM").
     */
    void playSound(String type);

    /**
     * Switches the view to the Main Menu.
     */
    void showMenu();

    /**
     * Switches the view to the Gameplay screen.
     */
    void showGame();

    /**
     * Displays the Game Over screen/dialog.
     *
     * @param finalScore The final score achieved by the player.
     */
    void showGameOver(int finalScore);
}