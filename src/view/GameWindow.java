package view;

import presenter.KontrakPresenter;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.ArrayList;

/**
 * Filename  : GameWindow.java
 * Package   : view
 * Description:
 * The Main GUI Container.
 * Features:
 * 1. CardLayout to switch between MENU and GAME views.
 * 2. Menu Panel: Displays High Score Table (Database) & Username Input.
 * 3. Game Panel: Renders the gameplay loop graphics.
 * 4. Handles User Input (Keyboard/Mouse) -> Delegates to Presenter.
 *
 * Programmer: MochammadAzkaBasria
 * Date      : 2025-12-24
 */
public class GameWindow extends JFrame implements KontrakView, KeyListener, MouseListener, MouseMotionListener {

    private KontrakPresenter presenter;
    private SoundManager soundManager;

    // --- LAYOUT COMPONENTS ---
    private JPanel cardPanel;
    private CardLayout cardLayout;

    // 1. Menu Panel Components
    private JPanel menuPanel;
    private JTable scoreTable;
    private DefaultTableModel tableModel;
    private JTextField usernameField;
    private JButton startButton;

    // 2. Game Panel Components
    private GamePanel gameCanvas;

    // Local Data for Rendering (Updated by Presenter)
    private Player player;
    private List<Alien> aliens = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();

    // Input States
    private boolean up, down, left, right;

    /**
     * Constructor: GameWindow
     * Sets up the main frame, layout, sounds, and listeners.
     */
    public GameWindow() {
        setTitle("Hide and Seek: The Challenge");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // A. Setup Card Layout (Container for Menu & Game)
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // B. Setup Menu Panel (High Score & Input)
        setupMenuPanel();
        cardPanel.add(menuPanel, "MENU");

        // C. Setup Game Panel (Canvas)
        gameCanvas = new GamePanel();
        gameCanvas.setBackground(Color.BLACK);
        gameCanvas.setFocusable(true); // Essential for KeyListener
        gameCanvas.addKeyListener(this);
        gameCanvas.addMouseListener(this);
        gameCanvas.addMouseMotionListener(this);
        cardPanel.add(gameCanvas, "GAME");

        this.add(cardPanel);

        // D. Initialize Audio
        initSounds();

        setVisible(true);
    }

    /**
     * Initializes the Menu UI Components.
     */
    private void setupMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());

        // 1. Header Title
        JLabel titleLabel = new JLabel("Hide and Seek: Hall of Fame", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        menuPanel.add(titleLabel, BorderLayout.NORTH);

        // 2. High Score Table
        String[] columnNames = {"Username", "Total Score", "Dodged", "Ammo Left"};
        tableModel = new DefaultTableModel(columnNames, 0);
        scoreTable = new JTable(tableModel);
        scoreTable.setEnabled(false); // Read-only table
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        menuPanel.add(scrollPane, BorderLayout.CENTER);

        // 3. Bottom Controls (Input Username & Start Button)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        bottomPanel.add(new JLabel("Enter Username: "));
        usernameField = new JTextField(15);
        bottomPanel.add(usernameField);

        startButton = new JButton("START GAME");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(50, 200, 50));
        startButton.setForeground(Color.WHITE);

        // Action Listener for Start Button
        startButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username to record your score!", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                if (presenter != null) {
                    presenter.startGame(username); // Start game via Presenter
                }
            }
        });
        bottomPanel.add(startButton);

        menuPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    /**
     * Load audio resources.
     * Ensure the 'assets' folder exists in your project root.
     */
    private void initSounds() {
        soundManager = new SoundManager();
        // Load sounds (Uncommented and ready)
        soundManager.loadSound("BGM", "assets/bgm.wav");
        soundManager.loadSound("SHOOT", "assets/shoot.wav");
        soundManager.loadSound("SHOOT_ENEMY", "assets/shoot.wav"); // Re-using shoot.wav, can be changed
        // soundManager.loadSound("EXPLOSION", "assets/explosion.wav");
        // soundManager.loadSound("GAMEOVER", "assets/gameover.wav");
    }

    /**
     * Inject Presenter reference.
     */
    public void setPresenter(KontrakPresenter presenter) {
        this.presenter = presenter;
    }

    // --- IMPLEMENTATION OF KONTRAKVIEW ---

    @Override
    public void updateScoreTable(Object[][] data) {
        // Clear existing data
        tableModel.setRowCount(0);
        // Add new data from Database
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    @Override
    public void showMenu() {
        // Switch view to MENU
        cardLayout.show(cardPanel, "MENU");
        // Request fresh data for the table
        if (presenter != null) {
            presenter.loadData();
        }
    }

    @Override
    public void showGame() {
        // Switch view to GAME
        cardLayout.show(cardPanel, "GAME");
        gameCanvas.requestFocus(); // Important: Grab focus for Keyboard Input
    }

    @Override
    public void showGameOver(int finalScore) {
        // Show Dialog
        JOptionPane.showMessageDialog(this, "GAME OVER\nFinal Score: " + finalScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        // Go back to Menu
        showMenu();
    }

    @Override
    public void updateGraphics(Player player, List<Alien> aliens, List<Bullet> bullets, List<Obstacle> obstacles) {
        // Update local references
        this.player = player;
        this.aliens = aliens;
        this.bullets = bullets;
        this.obstacles = obstacles;

        // Trigger repaint on EDT (Thread Safe)
        SwingUtilities.invokeLater(() -> gameCanvas.repaint());
    }

    @Override
    public void playSound(String type) {
        if (soundManager != null) {
            if (type.equals("BGM")) {
                soundManager.loop(type);
            } else {
                soundManager.play(type);
            }
        }
    }

    // --- INNER CLASS: GAME PANEL (RENDERING ENGINE) ---
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            // Enable smooth edges (Antialiasing)
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // 1. Draw Obstacles (Rocks)
            g2d.setColor(Color.DARK_GRAY);
            for (Obstacle obs : obstacles) {
                g2d.fillRect((int)obs.getX(), (int)obs.getY(), obs.getWidth(), obs.getHeight());
                // Draw Border
                g2d.setColor(Color.GRAY);
                g2d.drawRect((int)obs.getX(), (int)obs.getY(), obs.getWidth(), obs.getHeight());
                g2d.setColor(Color.DARK_GRAY);
            }

            // 2. Draw Bullets
            for (Bullet b : bullets) {
                if (b.isPlayerBullet()) {
                    g2d.setColor(Color.YELLOW);
                } else {
                    g2d.setColor(Color.RED);
                }
                g2d.fillOval((int)b.getX(), (int)b.getY(), b.getWidth(), b.getHeight());
            }

            // 3. Draw Aliens
            for (Alien a : aliens) {
                if (a.getType() == Alien.Type.CHASER) {
                    g2d.setColor(new Color(0, 200, 0)); // Green
                } else {
                    g2d.setColor(new Color(0, 150, 50)); // Darker Green
                }
                g2d.fillOval((int)a.getX(), (int)a.getY(), a.getWidth(), a.getHeight());
            }

            // 4. Draw Player (With Rotation)
            if (player != null) {
                AffineTransform old = g2d.getTransform();

                // Calculate center for rotation
                double centerX = player.getX() + player.getWidth() / 2.0;
                double centerY = player.getY() + player.getHeight() / 2.0;

                g2d.translate(centerX, centerY);
                g2d.rotate(player.getRotation());
                g2d.translate(-centerX, -centerY);

                // Body
                g2d.setColor(Color.CYAN);
                g2d.fillOval((int)player.getX(), (int)player.getY(), player.getWidth(), player.getHeight());

                // Gun Indicator
                g2d.setColor(Color.WHITE);
                g2d.fillRect((int)centerX, (int)centerY - 2, 25, 4);

                // Restore rotation context
                g2d.setTransform(old);
            }

            // 5. Draw HUD (Heads Up Display)
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 14));
            if (presenter != null) {
                g2d.drawString("Score: " + presenter.getScore(), 10, 20);
                g2d.drawString("Ammo: " + presenter.getAmmo(), 10, 40);
                g2d.drawString("Current Player: " + usernameField.getText(), 10, 60);
            }
        }
    }

    // --- INPUT LISTENERS (Delegated to Presenter) ---

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = true;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = true;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = true;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = true;

        if (presenter != null) presenter.movePlayer(up, down, left, right);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();
        if (key == KeyEvent.VK_W || key == KeyEvent.VK_UP) up = false;
        if (key == KeyEvent.VK_S || key == KeyEvent.VK_DOWN) down = false;
        if (key == KeyEvent.VK_A || key == KeyEvent.VK_LEFT) left = false;
        if (key == KeyEvent.VK_D || key == KeyEvent.VK_RIGHT) right = false;

        if (presenter != null) presenter.movePlayer(up, down, left, right);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        if (presenter != null) {
            presenter.rotatePlayer(e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (presenter != null && SwingUtilities.isLeftMouseButton(e)) {
            presenter.shoot();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // Handle rotation even while dragging (e.g., holding shoot button)
        mouseMoved(e);
    }

    // Unused Listener Methods
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseReleased(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}