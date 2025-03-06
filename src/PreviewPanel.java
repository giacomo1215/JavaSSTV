package src;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class PreviewPanel extends JPanel {
    private BufferedImage image;
    private final int scale;

    public PreviewPanel(int width, int height, int scale) {
        this.scale = scale;
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        setPreferredSize(new Dimension(width * scale, height * scale));
    }

    public void setPixel(int x, int y, int color) {
        image.setRGB(x, y, color);
        // Only repaint the affected pixel area
        repaint(x * scale, y * scale, scale, scale);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // Scale up the image to make it visible
        g.drawImage(image, 0, 0, image.getWidth() * scale, image.getHeight() * scale, null);
    }

    public BufferedImage getImage() {
        return image;
    }

    public void resetImage() {
        image = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        repaint();
    }
}
