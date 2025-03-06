package src.com.sstv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;

public class SSTVImageEncoder {
    private static final int SAMPLE_RATE = 44100;
    private static final int LINE_DURATION_MS = 445;  // Scottie DX line duration
    
    public static void encodeImage(String filename) throws Exception {
        BufferedImage img = ImageIO.read(new File(filename));
        int width = img.getWidth();
        int height = img.getHeight();
        
        ByteArrayOutputStream audioBuffer = new ByteArrayOutputStream();

        // Add VIS Header (Scottie DX: 01101000 / 0x68)
        int[] visCode = {0, 1, 1, 0, 1, 0, 0, 0};  // Correct 8-bit VIS
        renderFSK(audioBuffer, visCode, 30);

        // Vertical Sync (9ms 1200Hz + 1.5ms 1500Hz)
        renderTone(audioBuffer, 1200, 9);
        renderTone(audioBuffer, 1500, 1.5f);

        for (int y = 0; y < height; y++) {
            long lineStart = System.nanoTime();
            
            // Horizontal Sync
            renderTone(audioBuffer, 1200, 9);
            renderTone(audioBuffer, 1500, 1.5f);

            // Green Channel (138.24ms)
            renderColorChannel(audioBuffer, img, y, width, 1); // Green offset
            renderTone(audioBuffer, 1500, 1.5f);  // Separator
            
            // Blue Channel (138.24ms)
            renderColorChannel(audioBuffer, img, y, width, 0); // Blue offset
            renderTone(audioBuffer, 1500, 1.5f);  // Separator
            
            // Red Channel (138.24ms)
            renderColorChannel(audioBuffer, img, y, width, 2); // Red offset
            renderTone(audioBuffer, 1500, 1.5f);  // Line porch

            // Calculate remaining time for line duration
            double elapsedMs = (System.nanoTime() - lineStart) / 1e6;
            if (elapsedMs < LINE_DURATION_MS) {
                renderSilence(audioBuffer, LINE_DURATION_MS - elapsedMs);
            }
        }
        
        // Play entire audio in one operation
        Sound.playBuffer(audioBuffer.toByteArray());
    }

    private static void renderColorChannel(ByteArrayOutputStream buffer, 
            BufferedImage img, int y, int width, int colorOffset) {
        double[] frequencies = new double[width];
        for (int x = 0; x < width; x++) {
            int rgb = img.getRGB(x, y);
            int value = (rgb >> (8 * (2 - colorOffset))) & 0xFF; // 2=Red,1=Green,0=Blue
            frequencies[x] = 1500 + (value / 255.0) * 800;
        }
        renderScanLine(buffer, frequencies, 138.24f);
    }

    private static void renderTone(ByteArrayOutputStream buffer, 
            double freq, float durationMs) {
        int samples = (int)(durationMs * SAMPLE_RATE / 1000);
        for (int i = 0; i < samples; i++) {
            short sample = (short)(Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) 
                    * Short.MAX_VALUE);
            buffer.write((byte)(sample & 0xFF));
            buffer.write((byte)(sample >> 8 & 0xFF));
        }
    }

    private static void renderScanLine(ByteArrayOutputStream buffer,
            double[] frequencies, float durationMs) {
        int totalSamples = (int)(durationMs * SAMPLE_RATE / 1000);
        int samplesPerPixel = totalSamples / frequencies.length;
        
        for (int i = 0; i < totalSamples; i++) {
            int px = Math.min(i / samplesPerPixel, frequencies.length - 1);
            double freq = frequencies[px];
            short sample = (short)(Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) 
                    * Short.MAX_VALUE);
            buffer.write((byte)(sample & 0xFF));
            buffer.write((byte)(sample >> 8 & 0xFF));
        }
    }

    private static void renderSilence(ByteArrayOutputStream buffer, 
            double durationMs) {
        int silenceSamples = (int)(durationMs * SAMPLE_RATE / 1000);
        for (int i = 0; i < silenceSamples; i++) {
            buffer.write(0);
            buffer.write(0);
        }
    }

    private static void renderFSK(ByteArrayOutputStream buffer, 
            int[] bits, int bitDurationMs) {
        int samplesPerBit = (int)(bitDurationMs * SAMPLE_RATE / 1000);
        for (int bit : bits) {
            double freq = bit == 0 ? 1100 : 1300;
            for (int i = 0; i < samplesPerBit; i++) {
                short sample = (short)(Math.sin(2 * Math.PI * freq * i / SAMPLE_RATE) 
                        * Short.MAX_VALUE);
                buffer.write((byte)(sample & 0xFF));
                buffer.write((byte)(sample >> 8 & 0xFF));
            }
        }
    }
}