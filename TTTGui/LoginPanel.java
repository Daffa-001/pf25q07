import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

/*
The Java class for the GUI-based login input panel.
Uses the same kind of GUI as the main tic tac toe game (JPanel)
Contains text fields for two players to enter usernames and passwords.
On clicking the “Start Game” button:
It checks credentials via the database using GameMain.retrievePassword().
If both players are valid, it replaces the login panel with the TicTacToe screen.
 */


public class LoginPanel extends JPanel {
    private JTextField usernameField1 = new JTextField(15);
    private JPasswordField passwordField1 = new JPasswordField(15);
    private JTextField usernameField2 = new JTextField(15);
    private JPasswordField passwordField2 = new JPasswordField(15);
    private JButton loginButton = new JButton("Start Game");
    private String[] players = new String[2];
    private JFrame parentFrame;

    public LoginPanel(JFrame frame) {
        this.parentFrame = frame;
        setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        setLayout(new GridLayout(6, 2, 5, 5));
        add(new JLabel("Player 1 Username:"));
        add(usernameField1);
        add(new JLabel("Player 1 Password:"));
        add(passwordField1);
        add(new JLabel("Player 2 Username:"));
        add(usernameField2);
        add(new JLabel("Player 2 Password:"));
        add(passwordField2);
        add(new JLabel()); // empty cell
        add(loginButton);

        loginButton.addActionListener(e -> attemptLogin());
    }

    private void attemptLogin() {
        try {
            String user1 = usernameField1.getText();
            String pass1 = new String(passwordField1.getPassword());
            String correct1 = GameMain.retrievePassword(user1);

            String user2 = usernameField2.getText();
            String pass2 = new String(passwordField2.getPassword());
            String correct2 = GameMain.retrievePassword(user2);

            if (correct1 != null && pass1.equals(correct1) &&
                    correct2 != null && pass2.equals(correct2)) {

                players[0] = user1;
                players[1] = user2;

                // Load Game Panel
                GameMain gamePanel = new GameMain(players[0], players[1]);
                parentFrame.setContentPane(gamePanel);
                parentFrame.pack();
                parentFrame.setLocationRelativeTo(null);
            } else {
                JOptionPane.showMessageDialog(this, "Incorrect username or password.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error.");
        }
    }
}
