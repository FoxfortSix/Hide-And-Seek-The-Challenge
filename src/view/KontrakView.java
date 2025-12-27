package view;

import model.Alien;
import model.Bullet;
import model.Obstacle;
import model.Player;
import model.PowerUp;
import presenter.KontrakPresenter; // Pastikan import ini ada

import java.util.List;

/**
 * Filename  : KontrakView.java
 * Package   : view
 * Description:
 * FINAL Interface defining the methods that the View must implement.
 */
public interface KontrakView {

    /**
     * Menghubungkan View dengan Presenter.
     */
    void setPresenter(KontrakPresenter presenter);

    /**
     * Updates the High Score table in the Menu.
     */
    void updateScoreTable(Object[][] data);

    /**
     * Updates the graphical representation of the game.
     * Called by the Presenter every frame.
     * Menggunakan 5 parameter (termasuk PowerUps).
     */
    void updateGraphics(Player player, List<Alien> aliens, List<Bullet> bullets, List<Obstacle> obstacles, List<PowerUp> powerUps);

    /**
     * Plays a sound effect or background music.
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
     */
    void showGameOver(int finalScore);
}