package section2;

import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.geom.AffineTransform;

/**
 * A simple kart object - Encapsulates the kart's movement and rendering
 */
public class Kart {
    /**
     * Kart center coordinates
     */
    private int centerX, centerY;

    /**
     * dimensions of the kart
     */
    private int sizeX, sizeY;

    /**
     * Kart speed
     */
    private int speed;

    /**
     * Kart direction - can take values : 0 to 15
     */
    private int direction;
    private boolean stuck;

    /**
     * @return Whether the kart is stuck in a collision or not
     */
    public boolean isStuck() {
        return stuck;
    }

    /**
     * set whether the kart is stuck in a collision or not
     */
    public void setStuck(boolean stuck) {
        this.stuck = stuck;
    }

    /**
     * Kart images for each of the 16 directions(0 to 15)
     */
    private Image[] images;

    /**
     * @param center      center of the kart
     * @param dim         dimension of the kart
     * @param direction   initial direction
     * @param imgRootPath Path to the folder containing images for each direction
     */
    public Kart(Point center, Dimension dim, int direction, String imgRootPath) {
        images = new Image[16];
        for (int i = 0; i < images.length; i++) {
            try {
                images[i] = ImageIO.read(getClass().getClassLoader().getResource(imgRootPath + i + ".png"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.centerX = center.x;
        this.centerY = center.y;
        this.sizeX = dim.width;
        this.sizeY = dim.height;
        this.direction = direction;
    }

    /**
     * Renders/draws the kart with the given graphics context
     *
     * @param g
     */
    public void render(Graphics g) {
        if (images != null && images[direction] != null) {
            g.drawImage(images[direction], centerX - 25, centerY - 22, null);
        } else {
            System.out.println("Kart image not loaded");
        }
    }

    /**
     * Steers towards left
     */
    public void steerLeft() {
        direction += 1;

        if (direction > 15)
            direction = 0;

    }

    /**
     * Steers towards right
     */
    public void steerRight() {
        direction -= 1;
        if (direction < 0)
            direction = 15;

    }

    /**
     * Accelerates the kart
     */
    public void accelerate() {
        speed += 10;
        if (speed > 100)
            speed = 100;
    }

    /**
     * Decelerates the kart.
     * Negative speed is not allowed. So the kart stops instead of moving backwards.
     */
    public void decelerate() {
        speed -= 10;
        if (speed < 0)
            speed = 0;
    }

    /**
     * Updates the kart position
     *
     * @param dt time interval, can be used for simulating speeds as in the real world
     */
    public void update(double dt) {
        centerX += speed * Math.cos(direction * Math.PI / 8) * dt;
        centerY -= speed * Math.sin(direction * Math.PI / 8) * dt;
    }

    /**
     * Stops the kart
     */
    public void stop() {
        speed = 0;
    }

    /**
     * Returns the shape of the kart.
     * The bounding box of the returned shape can be used for collision detection
     *
     * @return shape of the kart
     */
    public Shape getShape() {
        Rectangle bounds = new Rectangle(centerX - sizeX / 2, centerY - sizeY / 2, sizeX, sizeY);
        AffineTransform tx = new AffineTransform();
        // rotate the shape using AffineTransform for accuracy
        tx.rotate(-Math.PI / 8 * direction, centerX, centerY);
        return tx.createTransformedShape(bounds);
    }

    /**
     * Draws the colliders of the kart
     *
     * @param g graphics context to be used for drawing
     */
    public void drawColliders(Graphics g) {
        Shape bounds = getShape();
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(Color.GREEN);
        g2d.draw(bounds);
    }
}
