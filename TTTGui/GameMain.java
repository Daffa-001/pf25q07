import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.sql.*;
import java.util.Scanner;
/**
 * Tic-Tac-Toe: Two-player Graphic version with better OO design.
 * The Board and Cell classes are separated in their own classes.
 */

public class GameMain extends JPanel {
    private static final long serialVersionUID = 1L; // to prevent serializable warning

    // Define named constants for the drawing graphics
    public static final String TITLE = "Tic Tac Toe";
    public static final Color COLOR_BG = Color.WHITE;
    public static final Color COLOR_BG_STATUS = new Color(204, 204, 204);
    public static final Color COLOR_CROSS = new Color(239, 105, 80);  // Red #EF6950
    public static final Color COLOR_NOUGHT = new Color(64, 154, 225); // Blue #409AE1
    public static final Font FONT_STATUS = new Font("OCR A Extended", Font.PLAIN, 14);
    private static String rPass;

    // Define game objects
    private Board board;         // the game board
    private State currentState;  // the current state of the game
    private Seed currentPlayer;  // the current player
    private JLabel statusBar;    // for displaying status message

    /**
     * Constructor to setup the UI and game components
     */
    private String playerX;
    private String playerO;

    public GameMain(String playerX, String playerO) {
        this.playerX = playerX;
        this.playerO = playerO;
        // This JPanel fires MouseEvent
        super.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {  // mouse-clicked handler
                int mouseX = e.getX();
                int mouseY = e.getY();
                // Get the row and column clicked
                int row = mouseY / Cell.SIZE;
                int col = mouseX / Cell.SIZE;

                if (currentState == State.PLAYING) {
                    if (row >= 0 && row < Board.ROWS && col >= 0 && col < Board.COLS
                            && board.cells[row][col].content == Seed.NO_SEED) {
                        // Update cells[][] and return the new game state after the move
                        currentState = board.stepGame(currentPlayer, row, col);
                        // Switch player
                        currentPlayer = (currentPlayer == Seed.CROSS) ? Seed.NOUGHT : Seed.CROSS;
                    }
                } else {        // game over
                    newGame();  // restart the game
                }
                // Refresh the drawing canvas
                repaint();  // Callback paintComponent().
            }
        });

        // Setup the status bar (JLabel) to display status message
        statusBar = new JLabel();
        statusBar.setFont(FONT_STATUS);
        statusBar.setBackground(COLOR_BG_STATUS);
        statusBar.setOpaque(true);
        statusBar.setPreferredSize(new Dimension(300, 30));
        statusBar.setHorizontalAlignment(JLabel.LEFT);
        statusBar.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 12));

        super.setLayout(new BorderLayout());
        super.add(statusBar, BorderLayout.PAGE_END); // same as SOUTH
        super.setPreferredSize(new Dimension(Board.CANVAS_WIDTH, Board.CANVAS_HEIGHT + 30));
        // account for statusBar in height
        super.setBorder(BorderFactory.createLineBorder(COLOR_BG_STATUS, 2, false));

        // Set up Game
        initGame();
        newGame();
    }

    /**
     * Initialize the game (run once)
     */
    public void initGame() {
        board = new Board();  // allocate the game-board
    }

    /**
     * Reset the game-board contents and the current-state, ready for new game
     */
    public void newGame() {
        for (int row = 0; row < Board.ROWS; ++row) {
            for (int col = 0; col < Board.COLS; ++col) {
                board.cells[row][col].content = Seed.NO_SEED; // all cells empty
            }
        }
        currentPlayer = Seed.CROSS;    // cross plays first
        currentState = State.PLAYING;  // ready to play
        SoundPlayer.play("start.wav");
    }

    /**
     * Custom painting codes on this JPanel
     */
    @Override
    public void paintComponent(Graphics g) {  // Callback via repaint()
        super.paintComponent(g);
        setBackground(COLOR_BG); // set its background color

        board.paint(g);  // ask the game board to paint itself

        // Print status-bar message
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

    /**
     * The entry "main" method
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(TITLE);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new LoginPanel(frame)); // show login panel first
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