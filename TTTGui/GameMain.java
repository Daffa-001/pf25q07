import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L;

    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.WHITE;
    public static final Color COLOR_BG_STATUS = new Color(204, 204, 204);
    public static final Color COLOR_CROSS = new Color(239, 105, 80);
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225);
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);
    private static String rPass;

    private Board board;
    private State currentState;
    private Seed currentPlayer;
    private JLabel statusBar;
    private Image backgroundImage;

    private String playerX;
    private String playerO;

    public GameMain(String playerX, String playerO) {
        this.playerX = playerX;
        this.playerO = playerO;

        // Load background image from file
        try {
            backgroundImage = ImageIO.read(new File("src/TTTGui/forGame.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int mouseX = e.getX();
                int mouseY = e.getY();
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        currentState = board.stepGame(currentPlayer, row, col);
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                } else {
                    newGame();
                }
                repaint();
            }
        });

        statusBar = new JLabel() {
            @Override
            protected void paintComponent(Graphics g) {
                // Custom background with transparency
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)); // 60% opacity
                g2.setColor(new Color(0, 0, 0)); // dark background
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        statusBar.setFont(new Font("SansSerif", Font.BOLD, 16));
        statusBar.setForeground(Color.WHITE);
        statusBar.setOpaque(false); // kita lukis sendiri background-nya
        statusBar.setPreferredSize(new Dimension(300, 40));
        statusBar.setHorizontalAlignment(JLabel.CENTER);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));


        super.setLayout(new BorderLayout());
        super.add(statusBar, BorderLayout.PAGE_END);
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        initGame();
        newGame();
    }

    public void initGame() {
        board = new Board();
    }

    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED;
            }
        }
        currentPlayer = Seed.CROSS;
        currentState = State.PLAYING;
        SoundPlayer.play("start.wav");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }

        board.paint(g);

        if (currentState == State.PLAYING) {
            statusBar.setForeground(Color.BLACK);
            statusBar.setText((currentPlayer == Seed.CROSS) ? "X's Turn" : "O's Turn");
        } else if (currentState == State.DRAW) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("It's a Draw! Click to play again.");
            SoundPlayer.play("draw.wav");
            updateStatsDraw();
        } else if (currentState == State.CROSS_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'X' Won! Click to play again.");
            SoundPlayer.play("x_win.wav");
            updateStats(playerX, playerO);
        } else if (currentState == State.NOUGHT_WON) {
            statusBar.setForeground(Color.RED);
            statusBar.setText("'O' Won! Click to play again.");
            SoundPlayer.play("o_win.wav");
            updateStats(playerO, playerX);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LoginPanel(frame));
            frame.pack();
            frame.setSize(450, 250);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }

    static String retrievePassword(String un) throws ClassNotFoundException {
        String host, port, databaseName, userName, password;
        host = "mysql-tictactoe-daffaagungpratama2005-3812.c.aivencloud.com";
        port = "12692";
        databaseName = "tictactoe";
        userName = "avnadmin";
        password = "AVNS_W4NwkPUexvpy82N-4wE";
        Class.forName("com.mysql.cj.jdbc.Driver");
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require",
                userName, password);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(
                     "SELECT password FROM users WHERE username = '" + un + "'")) {

            if (resultSet.next()) {
                return resultSet.getString("password");
            } else {
                System.out.println("Username not found.");
            }

        } catch (SQLException e) {
            System.out.println("Connection failure.");
            e.printStackTrace();
        }

        return null;
    }

    private void updateStats(String winner, String loser) {
        String host = "mysql-tictactoe-daffaagungpratama2005-3812.c.aivencloud.com";
        String port = "12692";
        String databaseName = "tictactoe";
        String userName = "avnadmin";
        String password = "AVNS_W4NwkPUexvpy82N-4wE";
        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require",
                userName, password)) {

            String winQuery = "UPDATE users SET win = win + 1, play = play + 1 WHERE username = ?";
            try (PreparedStatement winStmt = connection.prepareStatement(winQuery)) {
                winStmt.setString(1, winner);
                winStmt.executeUpdate();
            }

            String loseQuery = "UPDATE users SET lose = lose + 1, play = play + 1 WHERE username = ?";
            try (PreparedStatement loseStmt = connection.prepareStatement(loseQuery)) {
                loseStmt.setString(1, loser);
                loseStmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    private void updateStatsDraw() {
        String host = "mysql-tictactoe-daffaagungpratama2005-3812.c.aivencloud.com";
        String port = "12692";
        String databaseName = "tictactoe";
        String userName = "avnadmin";
        String password = "AVNS_W4NwkPUexvpy82N-4wE";

        try (Connection connection = DriverManager.getConnection(
                "jdbc:mysql://" + host + ":" + port + "/" + databaseName + "?sslmode=require",
                userName, password)) {

            String playQuery = "UPDATE users SET play = play + 1 WHERE username = ?";
            try (PreparedStatement ps = connection.prepareStatement(playQuery)) {
                ps.setString(1, playerX);
                ps.executeUpdate();
                ps.setString(1, playerO);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}