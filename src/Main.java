import presenter.GamePresenter;
import presenter.KontrakPresenter;
import view.GameWindow;
import view.KontrakView;

import javax.swing.SwingUtilities;

/**
 * Filename  : Main.java
 * Description:
 * The entry point of the application.
 * Responsible for assembling the MVP components:dd
 * 1. Creates the View.
 * 2. Creates the Presenter and injects the View into it.
 * 3. Injects the Presenter into the View (for handling inputs).
 * 4. Starts the application flow.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class Main {

    /**
     * Main method to launch the application.
     * Uses SwingUtilities.invokeLater to ensure thread safety for the GUI.
     * * @param args Command line arguments (not used).
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