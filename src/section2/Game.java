package section2;

import javax.swing.*;
import java.awt.*;

public class Game extends JFrame {
    public static void main(String[] args) {
        new Game();
    }

    Game() {
        setTitle("Kart Racing Game");
        GamePanel game = new GamePanel();
        // when debug mode is on, colliders are drawn along with the images
        game.debugMode = true;
        setContentPane(game);

        setSize(new Dimension(850, 700));
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
    }
}
