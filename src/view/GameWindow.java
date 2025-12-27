package view;

import presenter.GamePresenter;
import presenter.KontrakPresenter;
import model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.ArrayList;

public class GameWindow extends JFrame implements KontrakView, KeyListener, MouseListener, MouseMotionListener {

    private GamePresenter presenter;
    private SoundManager soundManager;
    private ImageManager imageManager;

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

    // Local Data for Rendering
    private Player player;
    private List<Alien> aliens = new ArrayList<>();
    private List<Bullet> bullets = new ArrayList<>();
    private List<Obstacle> obstacles = new ArrayList<>();
    private List<PowerUp> powerUps = new ArrayList<>();

    // Input States
    private boolean up, down, left, right;

    public GameWindow() {
        setTitle("Hide and Seek: Metal Slug Edition");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        setLocationRelativeTo(null);

        // A. Setup Card Layout
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);

        // B. Setup Menu Panel
        setupMenuPanel();
        cardPanel.add(menuPanel, "MENU");

        // C. Setup Game Panel
        gameCanvas = new GamePanel();
        gameCanvas.setFocusable(true);

        // Listeners
        gameCanvas.addKeyListener(this);
        gameCanvas.addMouseListener(this);
        gameCanvas.addMouseMotionListener(this);

        cardPanel.add(gameCanvas, "GAME");

        this.add(cardPanel);

        // D. Initialize Audio & Images
        initSounds();
        initImages();

        setVisible(true);
    }

    private void setupMenuPanel() {
        menuPanel = new JPanel(new BorderLayout());

        JLabel titleLabel = new JLabel("Hide and Seek: Hall of Fame", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        menuPanel.add(titleLabel, BorderLayout.NORTH);

        String[] columnNames = {"Username", "Total Score", "Dodged", "Ammo Left"};
        tableModel = new DefaultTableModel(columnNames, 0);
        scoreTable = new JTable(tableModel);
        scoreTable.setEnabled(false);
        JScrollPane scrollPane = new JScrollPane(scoreTable);
        menuPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 20, 10));

        bottomPanel.add(new JLabel("Enter Username: "));
        usernameField = new JTextField(15);
        bottomPanel.add(usernameField);

        startButton = new JButton("START MISSION");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.setBackground(new Color(200, 50, 50));
        startButton.setForeground(Color.WHITE);

        startButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username!", "Warning", JOptionPane.WARNING_MESSAGE);
            } else {
                if (presenter != null) {
                    presenter.startGame(username);
                }
            }
        });
        bottomPanel.add(startButton);

        menuPanel.add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initSounds() {
        soundManager = new SoundManager();
        // soundManager.loadSound("BGM", "assets/bgm.wav");
    }

    private void initImages() {
        imageManager = new ImageManager();

        // 1. BACKGROUND -> Tile.png
        imageManager.loadImage("BG", "assets/img/Tile.png");

        // 2. OBSTACLE -> Box.png (PERUBAHAN DI SINI)
        imageManager.loadImage("OBSTACLE", "assets/img/Box.png");

        // Catatan: PowerUp tidak diload gambarnya karena kita akan pakai kotak warna manual.

        // 3. Player Skins
        imageManager.loadImage("PLAYER_DEFAULT", "assets/img/PlayerHandgun.png");
        imageManager.loadImage("PLAYER_AR", "assets/img/PlayerAR.png");
        imageManager.loadImage("PLAYER_SHOTGUN", "assets/img/PlayerShotgun.png");

        // 4. Enemy Skins
        imageManager.loadImage("ALIEN_DEFAULT", "assets/img/EnemyHandgun.png");
        imageManager.loadImage("ALIEN_AR", "assets/img/EnemyAR.png");
        imageManager.loadImage("ALIEN_SHOTGUN", "assets/img/EnemyShotgun.png");
    }

    @Override
    public void setPresenter(KontrakPresenter presenter) {
        if (presenter instanceof GamePresenter) {
            this.presenter = (GamePresenter) presenter;
        }
    }

    @Override
    public void updateGraphics(Player player, List<Alien> aliens, List<Bullet> bullets, List<Obstacle> obstacles, List<PowerUp> powerUps) {
        this.player = player;
        this.aliens = aliens;
        this.bullets = bullets;
        this.obstacles = obstacles;
        this.powerUps = powerUps;
        SwingUtilities.invokeLater(() -> gameCanvas.repaint());
    }

    @Override
    public void updateScoreTable(Object[][] data) {
        tableModel.setRowCount(0);
        for (Object[] row : data) {
            tableModel.addRow(row);
        }
    }

    @Override
    public void showMenu() {
        cardLayout.show(cardPanel, "MENU");
        SwingUtilities.invokeLater(() -> {
            if (presenter != null) presenter.loadData();
        });
    }

    @Override
    public void showGame() {
        cardLayout.show(cardPanel, "GAME");
        gameCanvas.requestFocus();
    }

    @Override
    public void showGameOver(int finalScore) {
        JOptionPane.showMessageDialog(this, "MISSION FAILED\nFinal Score: " + finalScore, "Game Over", JOptionPane.INFORMATION_MESSAGE);
        showMenu();
    }

    @Override
    public void playSound(String type) {
        if (soundManager != null) {
            if (type.equals("BGM")) soundManager.loop(type);
            else soundManager.play(type);
        }
    }

    // --- GAME PANEL (RENDERING ENGINE) ---
    private class GamePanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // ---------------------------------------------------------
            // 1. DRAW BACKGROUND (Tile.png - Tiling)
            // ---------------------------------------------------------
            BufferedImage bgImg = imageManager.getImage("BG");
            if (bgImg != null) {
                int tileWidth = bgImg.getWidth();
                int tileHeight = bgImg.getHeight();
                for (int y = 0; y < getHeight(); y += tileHeight) {
                    for (int x = 0; x < getWidth(); x += tileWidth) {
                        g2d.drawImage(bgImg, x, y, null);
                    }
                }
            } else {
                g2d.setColor(Color.BLACK);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }

            // ---------------------------------------------------------
            // 2. Draw Obstacles (Box.png)
            // ---------------------------------------------------------
            BufferedImage obsImg = imageManager.getImage("OBSTACLE"); // Ini sekarang Box.png
            for (Obstacle obs : obstacles) {
                if (obsImg != null) {
                    g2d.drawImage(obsImg, (int)obs.getX(), (int)obs.getY(), obs.getWidth(), obs.getHeight(), null);
                } else {
                    g2d.setColor(Color.DARK_GRAY);
                    g2d.fillRect((int)obs.getX(), (int)obs.getY(), obs.getWidth(), obs.getHeight());
                }
            }

            // ---------------------------------------------------------
            // 3. Draw PowerUps (Manual Drawing - Tanpa Asset)
            // ---------------------------------------------------------
            for (PowerUp p : powerUps) {
                // Kotak Warna
                g2d.setColor(p.getColor());
                g2d.fillRect((int)p.getX(), (int)p.getY(), p.getWidth(), p.getHeight());

                // Border Putih
                g2d.setColor(Color.WHITE);
                g2d.setStroke(new BasicStroke(2));
                g2d.drawRect((int)p.getX(), (int)p.getY(), p.getWidth(), p.getHeight());

                // Huruf (A / S)
                g2d.setFont(new Font("Arial", Font.BOLD, 20));
                g2d.drawString(p.getLetter(), (int)p.getX() + 8, (int)p.getY() + 22);
            }

            // 4. Draw Bullets
            for (Bullet b : bullets) {
                g2d.setColor(b.getColor());
                g2d.fillOval((int)b.getX(), (int)b.getY(), b.getWidth(), b.getHeight());
            }

            // 5. Draw Aliens
            for (Alien a : aliens) {
                BufferedImage alienImg;
                if (a.getLoadout() == Alien.Loadout.ASSAULT_RIFLE) alienImg = imageManager.getImage("ALIEN_AR");
                else if (a.getLoadout() == Alien.Loadout.SHOTGUN) alienImg = imageManager.getImage("ALIEN_SHOTGUN");
                else alienImg = imageManager.getImage("ALIEN_DEFAULT");

                if (alienImg != null) {
                    g2d.drawImage(alienImg, (int)a.getX(), (int)a.getY(), a.getWidth(), a.getHeight(), null);
                } else {
                    g2d.setColor(Color.GREEN);
                    g2d.fillOval((int)a.getX(), (int)a.getY(), a.getWidth(), a.getHeight());
                }
            }

            // 6. Draw Player
            if (player != null) {
                BufferedImage playerImg;
                if (player.getCurrentWeapon() == Player.WeaponType.ASSAULT_RIFLE) playerImg = imageManager.getImage("PLAYER_AR");
                else if (player.getCurrentWeapon() == Player.WeaponType.SHOTGUN) playerImg = imageManager.getImage("PLAYER_SHOTGUN");
                else playerImg = imageManager.getImage("PLAYER_DEFAULT");

                AffineTransform old = g2d.getTransform();
                double centerX = player.getX() + player.getWidth() / 2.0;
                double centerY = player.getY() + player.getHeight() / 2.0;

                g2d.translate(centerX, centerY);
                g2d.rotate(player.getRotation());
                g2d.translate(-centerX, -centerY);

                if (playerImg != null) {
                    g2d.drawImage(playerImg, (int)player.getX(), (int)player.getY(), player.getWidth(), player.getHeight(), null);
                } else {
                    g2d.setColor(Color.CYAN);
                    g2d.fillOval((int)player.getX(), (int)player.getY(), player.getWidth(), player.getHeight());
                }
                g2d.setTransform(old);
            }

            // 7. Draw HUD
            drawHUD(g2d);
        }

        private void drawHUD(Graphics2D g2d) {
            if (presenter != null && player != null) {
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 14));
                g2d.drawString("Score: " + presenter.getScore(), 10, 20);
                g2d.drawString("Ammo: " + presenter.getAmmo(), 10, 40);
                g2d.setColor(Color.YELLOW);
                g2d.drawString("WAVE: " + presenter.getWave(), 10, 60);

                if (player.getCurrentWeapon() != Player.WeaponType.DEFAULT) {
                    String wName = (player.getCurrentWeapon() == Player.WeaponType.ASSAULT_RIFLE) ? "HEAVY MACHINE GUN" : "SHOTGUN";
                    if (player.getWeaponTimeLeft() <= 5 && (System.currentTimeMillis() / 250) % 2 == 0) {
                        g2d.setColor(Color.RED);
                    } else {
                        g2d.setColor(Color.GREEN);
                    }
                    g2d.drawString(wName + " (" + player.getWeaponTimeLeft() + "s)", 10, 80);
                }
                g2d.setColor(Color.WHITE);
                g2d.drawString("Player: " + usernameField.getText(), 10, 100);
            }
        }
    }

    // --- INPUT LISTENERS ---
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
        if (presenter != null) presenter.rotatePlayer(e.getX(), e.getY());
    }
    @Override
    public void mouseDragged(MouseEvent e) { mouseMoved(e); }
    @Override
    public void mousePressed(MouseEvent e) {
        if (presenter != null && SwingUtilities.isLeftMouseButton(e)) presenter.startShooting();
    }
    @Override
    public void mouseReleased(MouseEvent e) {
        if (presenter != null && SwingUtilities.isLeftMouseButton(e)) presenter.stopShooting();
    }
    @Override public void keyTyped(KeyEvent e) {}
    @Override public void mouseClicked(MouseEvent e) {}
    @Override public void mouseEntered(MouseEvent e) {}
    @Override public void mouseExited(MouseEvent e) {}
}