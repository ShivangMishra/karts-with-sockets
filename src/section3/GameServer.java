package section3;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

/**
 * A Game Server for Racing Kart game.
 * It can handle multiple clients in separate threads.
 */
public class GameServer extends JFrame {
    private static final int DEFAULT_PORT = 12345;

    /**
     * Port on which the server is listening
     */
    private int serverPort;

    /**
     * To allow the section3.GameServer to run as a stand alone application
     *
     * @param args
     */
    public static void main(String[] args) {
        new GameServer();
    }

    /**
     * All the client threads. Each clients' requests are handled in a separate client thread.
     */
    ArrayList<ClientThread> clientThreads;

    HashMap<String, String> karts;

    /**
     * ActionListener to handle events from buttons.
     */
    private ActionListener actionListener;

    /**
     * Port text box
     */
    JTextField portT;
    /**
     * Button to execute the server
     */
    JButton exec;

    /**
     * Button to show the data of all clients stored on the server
     */
    JButton showDataButton;

    /**
     * Button to close the server and disconnect all the clients.
     */
    JButton end;

    /**
     * To toggle 'finalize' property
     */
    JCheckBox finalizeCheckBox;

    /**
     * The server activity is shown here.
     */
    JTextArea transcript;

    /**
     * whether the server should be finalized when the last client disconnects
     */
    private boolean finalize;

    /**
     * Server socket to listen for client sockets.
     */
    ServerSocket serverSocket;

    /**
     * Number of connected clients
     */
    int nClients;

    /**
     * is the server running?
     */
    boolean running;

    /**
     * Initializes a section3.Game server and displays the GUI window.
     */
    public GameServer() {
        initActionListener();
        clientThreads = new ArrayList<>();
        karts = new HashMap<>();
        JPanel mainPanel = new JPanel();
        JLabel logoLabel;
        try {
            ImageIcon logoIcon = new ImageIcon("RacingLogoServer.png");
            logoLabel = new JLabel(logoIcon);
        } catch (Exception e) {
            logoLabel = new JLabel("<Racing logo image>");
        }

        mainPanel.add(logoLabel, BorderLayout.NORTH);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Contains all the buttons and text fields

        // Text fields and labels
        JLabel portL = new JLabel("Port :");
        btnP.add(portL);
        portT = new JTextField(DEFAULT_PORT + "");
        btnP.add(portT);

        exec = new JButton("Execute");
        exec.addActionListener(actionListener);
        btnP.add(exec);

        showDataButton = new JButton("Show Karts Data");
        showDataButton.addActionListener(actionListener);
        showDataButton.setEnabled(false);
        btnP.add(showDataButton);

        finalizeCheckBox = new JCheckBox("Finalize");
        finalizeCheckBox.addActionListener(actionListener);
        btnP.add(finalizeCheckBox);

        end = new JButton("End");
        end.setEnabled(false);
        end.addActionListener(actionListener);
        btnP.add(end);

        mainPanel.add(btnP);

        transcript = new JTextArea();
        transcript = new JTextArea(7, 40);
        transcript.setLineWrap(true);
        transcript.setWrapStyleWord(true);
        transcript.setEditable(false);

        mainPanel.add(new JScrollPane(transcript));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Server");
        setContentPane(mainPanel);
        setSize(new Dimension(560, 350));
        setResizable(false);
        setVisible(true);

    }

    /**
     * Executes the server.
     * The serverSocket is initialized and starts listening on the serverPort in a new thread.
     */
    private void exec() {
        try {
            serverPort = Integer.parseInt(portT.getText().trim());
        } catch (NumberFormatException e) {
            showError("Invalid port" + portT.getText());
            return;
        }
        try {
            serverSocket = new ServerSocket(serverPort);

        } catch (Exception e) {
            showError("Failed to initialize server socket");
        }
        running = true;
        new Thread(() -> {
            //Listen for new connections while the server is running.
            while (running) {
                try {
                    Socket socket = serverSocket.accept();
                    ClientThread clientThread = new ClientThread(socket);
                    clientThreads.add(clientThread);
                    clientThread.start();
                    showDataButton.setEnabled(true);
                } catch (Exception e) {
                    showError(e.getMessage());
                }
            }
        }).start();

        log("Listening on Port : " + serverPort);
        exec.setEnabled(false);
        end.setEnabled(true);
    }

    /**
     * Shows each client's kart data.
     */
    private void showKartsData() {
        log("\nsection3.Game All clients data : ");
        log("\n");
        for (String key : karts.keySet()) {
            log(karts.get(key) + "\n");
        }
    }

    /**
     * Disconnects all the clients and closes the serverSocket.
     */
    private void end() {
        for (ClientThread client : clientThreads) {
            try {
                log("Disconnecting Player : " + client.id);
                client.socket.close();
                client.running = false;
            } catch (Exception e) {
                showError("Failed to disconnect client " + client.id + ": " + e.getMessage());
            }
        }
        try {
            serverSocket.close();
        } catch (Exception e) {
            showError("Failed to close the Server : " + e.getMessage());
        }
        log("Server closed.");
        running = false;

        exec.setEnabled(true);
        end.setEnabled(false);
    }

    /**
     * Initializes the action listener.
     */
    private void initActionListener() {
        actionListener = e -> {
            if (e.getSource() == exec) {
                exec();
            } else if (e.getSource() == showDataButton) {
                showKartsData();
            } else if (e.getSource() == end) {
                end();
            } else if (e.getSource() == finalizeCheckBox) {
                finalize = finalizeCheckBox.isSelected();
            }
        };
    }

    /**
     * Logs a given message to the transcript.
     *
     * @param message
     */
    synchronized void log(String message) {
        transcript.append(message + "\n");
    }

    /**
     * Shows an error dialog.
     *
     * @param errorMessage message to be shown in the dialog
     */
    synchronized void showError(String errorMessage) {
        JOptionPane.showMessageDialog(this, errorMessage, "Error !", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Handles interaction of the server with a client
     */
    private class ClientThread extends Thread {

        /**
         * is this client running ?
         */
        boolean running;

        /**
         * Socket of this client
         */
        private final Socket socket;

        /**
         * To read data from the socket.
         */
        private BufferedReader in;

        /**
         * To write data to the socket
         */
        private PrintWriter out;

        /**
         * Id of this client.
         * By storing the client's id in its thread, we don't need to rely on the client's requests to infer its id.
         */
        String id;

        /**
         * Initializes the in, out and sends an identificatin to the connected client.
         *
         * @param socket Socket associated with this client.
         */
        private ClientThread(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream());
                id = nClients++ + ""; //client id start from 0.
                log("New Client Connected. Identification : " + id);
                out.println(id);
                out.flush();
                running = true;
            } catch (Exception e) {
                showError("I/O Error occurred : " + e.getMessage());
            }
        }

        /**
         * Continuously listens to the client's.
         * when the client stops, the client's data is stored.
         * If it was the last client and finalize is true, the server closes.
         */
        public void run() {
            String line;
            while (running) {
                try {
                    line = in.readLine();
                    handleClientRequest(line);

                } catch (Exception e) {
                    showError("Failed to load client request" + e.getMessage());
                    break;
                }
            }
            log("Client " + id + " left the game");

            if (--nClients == 0 && finalize) {
                log("Last client closed.");
                end();
            }
        }

        /**
         * Handles a client request as per the protocols described in the assignment document.
         *
         * @param request the client request
         */
        private synchronized void handleClientRequest(String request) {
            if (request == null) {
                log("Client " + id + " closed");
                return;
            }
            log(request);
            StringTokenizer st = new StringTokenizer(request.trim(), "#");

            String protocol = st.nextToken();

            if (protocol.equals("P0")) {
                try {
                    socket.close();
                    log("Client closed : " + id);
                    log("Game Over !");
                    for(var client: clientThreads) {
                        if(client.id != id) {
                            client.out.println("P0#" + id);
                        }
                    }
                    running = false;
                } catch (Exception e) {
                    showError("Failed to close socket");
                }
            } else if (protocol.equals("P1")) {
                String data = st.nextToken();
                int numKarts = karts.size() - 1;
                karts.put(id, data.trim());
                log("Kart Data received from : " + id );
                log("Number of karts '" + (numKarts) + "' sent to client " + id);
                out.println(numKarts + "");
                out.flush();
                if (numKarts < 1)
                    return;
                for (var client : clientThreads) {
                    if (!client.id.equals(id)) {
                        out.println(client.id + "?" + karts.get(client.id));
                        out.flush();
                        log("Kart " + client.id + " Data " + karts.get(client.id) + " sent to : " + id + "\n");
                    }
                }
                log("----------------------");

            }
        }
    }
}
