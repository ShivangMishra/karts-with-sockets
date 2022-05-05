package section1;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * Handles Game logic and rendering
 */
public class GamePanel extends JPanel implements ActionListener {

    /**
     * A simple kart that can be controlled by the player
     */
    private final Kart kart;

    /**
     * Used for game loop
     */
    private final Timer timer;

    public GamePanel() {

        setFocusable(true);
        Point kartPos = new Point(400, 250);
        String imgRoot = "resources\\karts\\kart1\\";
        kart = new Kart(kartPos, new Dimension(50, 50), 0, imgRoot);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT -> kart.steerLeft();
                    case KeyEvent.VK_RIGHT -> kart.steerRight();
                    case KeyEvent.VK_UP -> kart.accelerate();
                    case KeyEvent.VK_DOWN -> kart.decelerate();
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

        kart.render(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        kart.update(0.3);
        repaint();
    }
}
