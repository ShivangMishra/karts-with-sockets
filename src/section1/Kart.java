package section1;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;

/**
 * A simple Kart object in the game. Encapsulates kart movement and rendering
 */
public class Kart {
    private int centerX, centerY;
    private final int width;
    private final int height;
    private int v;
    private int direction;

    static int totalKarts;

    private final Color color1;
    private final Color color2;

    Image[] images;
    public Kart(Point center, Dimension size, int direction, String imgRoot) {
        images = new Image[16];
        for(int i = 0; i < images.length; i++) {
            try {
                images[i] = ImageIO.read(getClass().getClassLoader().getResource(imgRoot + i + ".png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.centerX = center.x;
        this.centerY = center.y;
        this.width = size.width;
        this.height = size.height;
        this.direction = direction;
        if (totalKarts++ == 0) {
            this.color1 = Color.RED;
            this.color2 = Color.BLUE;
        } else {
            this.color1 = Color.BLUE;
            this.color2 = Color.RED;
        }
    }

    public void render(Graphics g) {
        Rectangle bounds = getBounds();
        if(images!= null && images[direction] != null) {

            g.drawImage(images[direction], bounds.x, bounds.y,null);
            return;
        }
        g.setColor(color1);
        g.fillRect(bounds.x, bounds.y, width, height);
        g.setColor(color2);
        int x2 = centerX + (int) (width/2 * Math.cos(direction * Math.PI / 8));
        int y2 = centerY + (int) (width/2 * Math.sin(direction * Math.PI / 8));
        g.fillRect(x2, y2, 10, 10);
    }

    public void steerLeft() {
        direction += 1;
        if(direction > 15)
            direction = 0;
    }

    public void steerRight() {
        direction -= 1;
        if(direction < 0)
            direction = 15;
    }

    public void accelerate() {
        v += 10;
        if (v > 100)
            v = 100;
    }

    public void decelerate() {
        v -= 10;
        if (v < 0)
            v = 0;
    }

    public void update(double dt) {
        centerX += v * Math.cos(direction * Math.PI / 8) * dt;
        centerY -= v * Math.sin(direction * Math.PI / 8) * dt;
    }

    public Rectangle getBounds() {
        return new Rectangle(centerX - width / 2, centerY - width / 2, width, height);
    }
}
