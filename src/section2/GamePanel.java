package section2;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Handles Game Logic and rendering
 */
public class GamePanel extends JPanel implements ActionListener {

    // Karts
    private final Kart kart1;
    private final Kart kart2;

    /**
     * Used for game loop
     */
    private final Timer timer;

    private final Point startPoint = new Point(425, 500);
    private final int trackWidth = 100;

    Rectangle innerBounds = new Rectangle(150, 200, 550, 300);
    Rectangle outerBounds = new Rectangle(50, 100, 750, 500);
    Rectangle midBounds = new Rectangle(100, 150, 650, 400);
    Dimension kartDim = new Dimension(35, 25);


    // Game images
    private final String mapImgPath = "map.png";
    private final String imgRoot1 = "karts/kart1/";
    private final String imgRoot2 = "karts/kart2/";
    private Image mapImg;

    /**
     * When debug mode is toggled on, the game objects' colliders are drawn
     */
    protected boolean debugMode;

    public GamePanel() {

        setFocusable(true);

        try {
            mapImg = ImageIO.read(getClass().getClassLoader().getResource(mapImgPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Point kart1Pos = new Point(startPoint.x + 25, startPoint.y + 25);
        Point kart2Pos = new Point(startPoint.x + 25, startPoint.y + 25 + 50);
        kart1 = new Kart(kart1Pos, kartDim, 0, imgRoot1);
        kart2 = new Kart(kart2Pos, kartDim, 0, imgRoot2);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> kart1.steerLeft();
                    case KeyEvent.VK_RIGHT -> kart1.steerRight();
                    case KeyEvent.VK_UP -> kart1.accelerate();
                    case KeyEvent.VK_DOWN -> kart1.decelerate();

                    case KeyEvent.VK_A -> kart2.steerLeft();
                    case KeyEvent.VK_D -> kart2.steerRight();
                    case KeyEvent.VK_W -> kart2.accelerate();
                    case KeyEvent.VK_S -> kart2.decelerate();

                }
            }
        });
        requestFocusInWindow();
        timer = new Timer(30, this);
        timer.start();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderMap(g);
        kart1.render(g);
        kart2.render(g);
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

        kart1.drawColliders(g);
        kart2.drawColliders(g);
    }


    /**
     * Handles events fired by the timer
     * Updates the karts and redraws each frame
     *
     * @param e
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        checkCollisions();
        kart1.update(0.3);
        kart2.update(0.3);
        repaint();
    }

    /**
     * Checks all the collisions.
     * Collisions are checked using bounding boxes
     */
    private void checkCollisions() {

        // if own kart collides with any other kart, game over.
        // in my game's current version, collision of any two karts means 'game over' for all players
        // this can be changed, but it is out of this project's current scope.
        if (kart1.getShape().intersects(kart2.getShape().getBounds())) {
            collisionEffect();
            gameOver();
            return;
        }

        checkKartCollision(kart1, innerBounds, outerBounds);
        checkKartCollision(kart2, innerBounds, outerBounds);
    }

    private void gameOver() {
        timer.stop();
        JOptionPane.showMessageDialog(this, "Game Over !");
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
}
