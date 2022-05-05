package section3;

import javax.swing.*;
import java.awt.*;

/**
 * Main class for Game Client. Allows the player to connect to a server and play a racing game.
 * Even when the player is not connected to the server, they can still play with their kart(like a practice game)
 */
public class GameClient extends JFrame {

    private final JButton connectButton, disconnectButton;

    public static void main(String[] args) {
        new GameClient();
    }

    private final JTextField serverField;
    private final JTextField portField;

    private GamePanel game;

    final int DEFAULT_PORT = 12345;
    final String DEFAULT_SERVER = "localhost";

    public GameClient() {
        setTitle("Kart Racing: Client");
        game = new GamePanel();
        //setContentPane(game);
        add(game, BorderLayout.CENTER);

        JLabel serverLabel = new JLabel("Server: ");
        serverField = new JTextField(10);
        serverField.setText(DEFAULT_SERVER);

        JLabel portLabel = new JLabel("Port: ");
        portField = new JTextField(10);
        portField.setText(DEFAULT_PORT + "");

        connectButton = new JButton("Connect");
        connectButton.addActionListener(e -> connect());

        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(e -> disconnect());
        disconnectButton.setEnabled(false);

        JCheckBox debugCheckBox = new JCheckBox("Debug Mode");
        debugCheckBox.addActionListener(e -> game.setDebugMode(debugCheckBox.isSelected()));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        topPanel.add(serverLabel);
        topPanel.add(serverField);
        topPanel.add(portLabel);
        topPanel.add(portField);
        topPanel.add(connectButton);
        topPanel.add(disconnectButton);
        topPanel.add(debugCheckBox);
        topPanel.setBorder(BorderFactory.createLineBorder(Color.BLUE));
        add(topPanel, BorderLayout.NORTH);

        setSize(new Dimension(850, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }

    /**
     * Disconnect from the server
     */
    private void disconnect() {
        game.disconnect();
        disconnectButton.setEnabled(false);
        connectButton.setEnabled(true);
    }

    /**
     * Connects the game to the server by passing the server and port
     * to the game's connect function
     */
    private void connect() {
        String server = serverField.getText().trim();
        int port = -1;
        try {
            port = Integer.parseInt(portField.getText().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid port");
        }
        game.init();
        if (game.connect(server, port)) {
            connectButton.setEnabled(false);
            disconnectButton.setEnabled(true);
        } else {
            JOptionPane.showMessageDialog(this, "Failed to connect", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
