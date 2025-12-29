import presenter.GamePresenter;
import view.GameWindow;

import javax.swing.SwingUtilities;

/**
 * Filename  : Main.java
 * Description:
 * The entry point of the application.
 * <p>
 * Responsible for assembling the MVP (Model-View-Presenter) components:
 * <ol>
 *     <li>Creates the View ({@link GameWindow}).</li>
 *     <li>Creates the Presenter ({@link GamePresenter}) and injects the View into it.</li>
 *     <li>Injects the Presenter into the View (for handling inputs).</li>
 *     <li>Starts the application flow by showing the menu.</li>
 * </ol>
 * </p>
 *
 * @author Mochammad Azka Basria
 * @version 1.0
 */
public class Main {

    /**
     * Main method to launch the application.
     * <p>
     * Uses {@link SwingUtilities#invokeLater(Runnable)} to ensure thread safety
     * when initializing the Swing GUI.
     * </p>
     *
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // 1. Instantiate the View (GUI)
                // At this point, the window opens but has no logic connected yet.
                GameWindow view = new GameWindow();

                // 2. Instantiate the Presenter (Logic)
                // We pass the View to the Presenter so the logic knows where to display output.
                GamePresenter presenter = new GamePresenter(view);

                // 3. Connect Presenter to View
                // We pass the Presenter to the View so the GUI knows where to send user inputs (keys/mouse).
                view.setPresenter(presenter);

                // 4. Start the Application
                // Show the initial menu or start screen.
                view.showMenu();

                System.out.println("Application started successfully.");

            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Failed to start application: " + e.getMessage());
            }
        });
    }
}