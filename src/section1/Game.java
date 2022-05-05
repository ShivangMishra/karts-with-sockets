package section1;

import javax.swing.*;
import java.awt.*;

/**
 * Racing karts Game Application.
 */
public class Game extends JFrame {
    /**
     * Allows the program to run as a stand-alone application
     */
    public static void main(String[] args) {
        new Game();
    }

    Game() {
        setTitle("Racing Game: Part 1");
        setContentPane(new GamePanel());
        setSize(new Dimension(850, 650));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
