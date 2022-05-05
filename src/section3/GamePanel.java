package section3;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;

/**
 * GamePanel encapsulates the game logic including communication with the server
 */
public class GamePanel extends JPanel implements ActionListener {

    /**
     * Used for the game loop
     */
    private Timer timer;

    /**
     * Stores all the karts. karts[id] is the local player's kart
     */
    Kart[] karts;

    /**
     * When debug mode is toggled on, the game objects' colliders are drawn,
     * and any logs or errors are shown directly to the user.
     * When debug mode is toggled off, only sprites(images) are drawn and logs/errors are hidden
     */
    private boolean debugMode;

    /**
     * Frames Per Second, i.e., number of times the game is updated and rendered per second
     */
    private final int FPS = 30;

    //Variables related to the game map
    private Rectangle innerBounds = new Rectangle(150, 200, 550, 300);
    private Rectangle midBounds = new Rectangle(100, 150, 650, 400);
    private Rectangle outerBounds = new Rectangle(50, 100, 750, 500);
    private int trackWidth = 100;
    private Point startPoint = new Point(425, 500);

    //kart image folders
    private final String KART1_PATH = "karts/kart1/";
    private final String KART2_PATH = "karts/kart2/";

    //map image
    private final String mapImgPath = "map.png";
    private Image mapImg;

    /**
     * ID of this client. The server identifies each client with a particular id.
     * Also, in the karts array, the kart at index id is this client's kart
     */
    private int id;

    /**
     * Socket of this client
     */
    private Socket socket;

    //Socket IO
    private BufferedReader in;
    private PrintWriter out;

    /**
     * Whether this client is connected to a server
     */
    private boolean connected;

    /**
     * Default Constructor - loads map image and initializes everything
     */
    protected GamePanel() {
        setFocusable(true); // otherwise, we can't capture key events

        // load map image
        try {
            mapImg = ImageIO.read(getClass().getClassLoader().getResource(mapImgPath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        init();


        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> karts[id].steerLeft();
                    case KeyEvent.VK_RIGHT -> karts[id].steerRight();
                    case KeyEvent.VK_UP -> karts[id].accelerate();
                    case KeyEvent.VK_DOWN -> karts[id].decelerate();
                }
            }
        });

        // to request focus whenever mouse hovers on the game panel
        // this is necessary when the window has other elements such as text fields
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                requestFocusInWindow();
            }
        });
    }

    /**
     * Initializes the karts and the timer.
     * This method allows the same game panel to be reused after a game ends
     */
    protected void init() {
        // Position the two karts at the start line
        Point kart1Pos = new Point(startPoint.x + trackWidth / 4, startPoint.y + trackWidth / 4);
        Point kart2Pos = new Point(startPoint.x + trackWidth / 4, startPoint.y + trackWidth / 4 + trackWidth / 2);

        Kart kart1 = new Kart(kart1Pos, new Dimension(35, 25), 0, KART1_PATH);
        Kart kart2 = new Kart(kart2Pos, new Dimension(35, 25), 0, KART2_PATH);

        /* my game supports more than two players just as easily as two players
         We only need to assign a new kart in this array.
         The rest is taken care of already according to the current rules.*/
        karts = new Kart[]{kart1, kart2};

        requestFocusInWindow();


        /* delay = (number of millis in 1s) / (frames per second)
         but not always guaranteed to give the specified fps (depends on many factors)*/
        if(timer != null)
            timer.stop();
        timer = new Timer(1000 / FPS, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderMap(g);
        for (var kart : karts) {
            kart.render(g);
        }
        if (debugMode) {
            drawColliders(g);
        }
    }

    private void renderMap(Graphics g) {
        if (mapImg != null) {
            g.drawImage(mapImg, 0, 0, null);
            return;
        }

        // Draw the map using simple shapes if the image can't be loaded.

        Color c1 = Color.GREEN;
        g.setColor(c1);
        g.fillRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height); // grass
        Color c2 = Color.BLACK;
        g.setColor(c2);
        g.drawRect(outerBounds.x, outerBounds.y, outerBounds.width, outerBounds.height); // outer edge
        g.drawRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height); // inner edge
        Color c3 = Color.YELLOW;
        g.setColor(c3);
        g.drawRect(midBounds.x, midBounds.y, midBounds.width, midBounds.height); // mid-lane marker
        Color c4 = Color.WHITE;
        g.setColor(c4);
        g.drawLine(startPoint.x, startPoint.y, startPoint.x, startPoint.y + trackWidth); // start line

    }

    /**
     * Draws all the colliders in the map including the colliders of karts.
     * Helpful in debugging collision detection and fine tuning image and collision boxes of objects
     *
     * @param g
     */
    private void drawColliders(Graphics g) {
        g.setColor(Color.RED);
        g.drawRect(outerBounds.x, outerBounds.y, outerBounds.width, outerBounds.height); // outer edge
        g.drawRect(innerBounds.x, innerBounds.y, innerBounds.width, innerBounds.height); // inner edge

        for (Kart kart : karts) kart.drawColliders(g);
    }

    /**
     * Game Loop is handled in this function, handles the events fired by the timer
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        checkCollisions();
        karts[id].update(0.3);
        if (connected)
            requestNextFrame();
        repaint();
    }

    /**
     * Sends the local player's kart data to the server
     * and Requests the data of all the other karts from the server for the next frame.
     */
    private synchronized void requestNextFrame() {

        //send own kart data
        out.println("P1#" + karts[id].encode());
        out.flush();

        try {
            // first the server sends the number of karts(excluding our kart)
            // for example if we are playing with one other player, server will send 1 instead of 2.
            // Then it will send the data of the other player's kart.

            String line = in.readLine().trim();
            if (line.length() > 1 && line.substring(0, 2).equals("P0")) {
                String message = "The client " + line.substring(2) + " left." +
                        " You can continue playing alone or disconnect and restart the game.";
                JOptionPane.showMessageDialog(this, message);

            }
            int numKarts = Integer.parseInt(line);
            log("number of other karts = " + numKarts + "\n");


            if (numKarts < 1) {
                log("No other player connected !");
                return;
                //no other player is connected, don't wait for the server to send kart data
            }

            // receive the karts data and update the karts
            for (int i = 1; i <= numKarts; i++) {

                String response = in.readLine();
                System.out.println(response);
                // server sends the data in the form - <Client Index>?<Client's kart data>
                int idx = Integer.parseInt(response.substring(0, response.indexOf('?')));
                String kartData = response.substring(response.indexOf('?') + 1);
                karts[idx].decode(kartData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Connect to the specified server and port
     *
     * @return whether or not the connection was successful
     */
    protected boolean connect(String server, int port) {
        try {
            socket = new Socket(server, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream());
            id = Integer.parseInt(in.readLine().trim());

            log("Connected to Server.\nIdentification received : " + id + '\n');
            connected = true;
            requestFocusInWindow();
            return true;
        } catch (Exception e) {
            showError("Failed to connect : " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Game Over
     */
    void gameOver() {
        timer.stop();
        log("section3.Game Over !!");
        disconnect();
        JOptionPane.showMessageDialog(null, "Game Over !");
    }

    /**
     * Disconnects from the server
     */
    protected void disconnect() {
        connected = false;
        try {
            //First notify the server that we are going to disconnect
            if(out == null)
                return;
            out.println("P0#" + karts[id].encode());
            out.flush();

            socket.close();//actually disconnect from the server
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks all the collisions.
     * Collisions are checked using bounding boxes
     */
    private void checkCollisions() {

        // if own kart collides with any other kart, game over.
        // in my game's current version, collision of any two karts means 'game over' for all players
        // this can be changed, but it is out of this project's current scope.
        for (int i = 0; i < karts.length; i++) {
            if (i == id)
                continue; // don't check collision with self
            if (karts[id].getShape().intersects(karts[i].getShape().getBounds())) {
                collisionEffect();
                gameOver();
                return;
            }
        }
        checkKartCollision(karts[id], innerBounds, outerBounds);
    }

    /**
     * Checks kart's collision with the game objects in the map other than karts.
     *
     * @param kart        the kart which is to be checked
     * @param innerBounds inner bounds of the map
     * @param outerBounds outer bounds of the map
     */
    private void checkKartCollision(Kart kart, Rectangle innerBounds, Rectangle outerBounds) {

        Shape kartBounds = kart.getShape();
        if (kartBounds.intersects(innerBounds)) {
            kart.stop();

            if (!kart.isStuck()) {
                collisionEffect();
                System.out.println("Inner collision");
                kart.setStuck(true);
            }
        } else if (!outerBounds.contains(kartBounds.getBounds())) {
            kart.stop();
            if (!kart.isStuck()) {
                collisionEffect();
                System.out.println("Outer collision");
                kart.setStuck(true);
            }
        } else {
            kart.setStuck(false);
        }

    }

    /**
     * PLays the collision sound effect
     */
    private void collisionEffect() {

        try {
            String soundPath = "collision.wav";
            Clip collisionClip = AudioSystem.getClip();
            collisionClip.open(AudioSystem.getAudioInputStream(getClass().getClassLoader().getResource(soundPath)));
            collisionClip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @return whether the client is connected to a server or not
     */
    public boolean isConnected() {
        return connected;
    }

    /**
     * Set the debug mode
     *
     * @param debugMode debug mode
     */
    protected void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }

    /**
     * Similar to log, but used exclusively for error messages.
     * Currently it shows the error message in a message dialog
     * No error message is shown if the debug mode is toggled off
     *
     * @param s
     */
    private void showError(String s) {
        if (debugMode)
            JOptionPane.showMessageDialog(this, s, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /**
     * Logs the given message as it is.
     * Currently it simply prints the message to standard output
     * Does not log if the debug mode is toggled off.
     */
    private void log(String message) {
        if (debugMode)
            System.out.print(message);
    }
}
