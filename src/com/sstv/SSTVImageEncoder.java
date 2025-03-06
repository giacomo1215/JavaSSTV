package src.com.sstv;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SSTVImageEncoder {
    public static void encodeImage(String filename) throws Exception {
        BufferedImage img = ImageIO.read(new File(filename));

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int pixel = img.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = (pixel) & 0xFF;
                Color color = new Color(r, g, b);
                SSTVEncoder.encodePixel(color);
            }
        }
    }
}