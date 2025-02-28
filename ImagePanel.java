import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private final Color[][] image;
    private final int pixelSize;

    public ImagePanel(Color[][] image, int pixelSize) {
        this.image = image;
        this.pixelSize = pixelSize;
        setPreferredSize(new Dimension(
            image.length * pixelSize, 
            image[0].length * pixelSize
        ));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int x = 0; x < image.length; x++) {
            for (int y = 0; y < image[x].length; y++) {
                Color color = image[x][y];
                g.setColor(new java.awt.Color(
                    color.getR(), color.getG(), color.getB()
                ));
                g.fillRect(x * pixelSize, y * pixelSize, pixelSize, pixelSize);
            }
        }
    }
}