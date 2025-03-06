package src.com.sstv;

import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class SSTVImageEncoder {
    public static void encodeImage(String filename) throws Exception {
        BufferedImage img = ImageIO.read(new File(filename));
        int width = img.getWidth();
        int height = img.getHeight();

        // Transmit VIS header (Scottie DX: 0100000000)
        int[] visCode = {0, 1, 0, 0, 0, 0, 0, 0, 0, 0}; // Example VIS for Scottie 2
        Sound visSound = new Sound(1100, 1300, 30);
        visSound.playFSK(visCode, 30);

        // Vertical sync and porch
        SSTVEncoder.playSyncPulse(9);    // 9ms sync
        SSTVEncoder.playPorch(1);        // 1.5ms porch (approximated to 1ms)

        for (int y = 0; y < height; y++) {
            long lineStart = System.currentTimeMillis();

            // Horizontal sync and separator
            SSTVEncoder.playSyncPulse(9);
            SSTVEncoder.playPorch(1);

            // Green scan
            double[] greenFreqs = new double[width];
            for (int x = 0; x < width; x++) {
                int g = (img.getRGB(x, y) >> 8) & 0xFF;
                greenFreqs[x] = SSTVEncoder.rgbToFrequency(g);
            }
            SSTVEncoder.playScanLine(greenFreqs, 138);

            SSTVEncoder.playPorch(1); // Separator

            // Blue scan
            double[] blueFreqs = new double[width];
            for (int x = 0; x < width; x++) {
                int b = img.getRGB(x, y) & 0xFF;
                blueFreqs[x] = SSTVEncoder.rgbToFrequency(b);
            }
            SSTVEncoder.playScanLine(blueFreqs, 138);

            SSTVEncoder.playPorch(1); // Separator

            // Red scan
            double[] redFreqs = new double[width];
            for (int x = 0; x < width; x++) {
                int r = (img.getRGB(x, y) >> 16) & 0xFF;
                redFreqs[x] = SSTVEncoder.rgbToFrequency(r);
            }
            SSTVEncoder.playScanLine(redFreqs, 138);

            SSTVEncoder.playPorch(1); // Line porch

            // Maintain line timing (445ms)
            long elapsed = System.currentTimeMillis() - lineStart;
            if (elapsed < 445) {
                Thread.sleep(445 - elapsed);
            }
        }
    }
}