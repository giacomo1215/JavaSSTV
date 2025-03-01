// ImageToSound.java
import javax.sound.sampled.*;
import java.awt.image.BufferedImage;

public class ImageToSound {
    public static final float SAMPLE_RATE = 11025;        // Standard SSTV sample rate
    private static final double SYNC_PULSE_FREQ = 1200.0; // Hz
    private static final double BLACK_FREQ = 1500.0;      // Hz
    private static final double WHITE_FREQ = 2300.0;      // Hz

    // Color channel frequencies
    private static final double RED_FREQ = 2100.0;    // Red channel max
    private static final double GREEN_FREQ = 1900.0;  // Green channel max
    private static final double BLUE_FREQ = 1700.0;   // Blue channel max
    
    // Scottie DX timing constants (in milliseconds)
    private static final double SYNC_PULSE_MS = 9.0;
    private static final double PORCH_MS = 1.5;
    private static final double COLOR_SCAN_MS = 138.240;
    private static final double SCAN_SEPARATOR_MS = 1.5;
    private static final long LINE_TIME_MS = 445;  // Total scan line time

    private static SourceDataLine audioLine;
    
    private static void initAudio() throws LineUnavailableException {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
        audioLine = AudioSystem.getSourceDataLine(format);
        audioLine.open(format);
        audioLine.start();
    }

        public static void playImageTones(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        try {
            initAudio();
            
            // VIS code for Scottie DX
            generateTone(1900, 300);  // Leader tone
            generateTone(1200, 10);   // Break
            generateTone(1900, 300);  // Leader tone
            
            for (int y = 0; y < height; y++) {
                // Sync pulse
                generateTone(SYNC_PULSE_FREQ, (int)SYNC_PULSE_MS);
                generateTone(BLACK_FREQ, (int)PORCH_MS);
                
                // Scan red line
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    int r = (pixel >> 16) & 0xFF;
                    double frequency = map(r, 0, 255, BLACK_FREQ, RED_FREQ);
                    generateTone(frequency, (int)(COLOR_SCAN_MS / width));
                }
                generateTone(BLACK_FREQ, (int)SCAN_SEPARATOR_MS);
                
                // Scan green line
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    int g = (pixel >> 8) & 0xFF;
                    double frequency = map(g, 0, 255, BLACK_FREQ, GREEN_FREQ);
                    generateTone(frequency, (int)(COLOR_SCAN_MS / width));
                }
                generateTone(BLACK_FREQ, (int)SCAN_SEPARATOR_MS);
                
                // Scan blue line
                for (int x = 0; x < width; x++) {
                    int pixel = img.getRGB(x, y);
                    int b = pixel & 0xFF;
                    double frequency = map(b, 0, 255, BLACK_FREQ, BLUE_FREQ);
                    generateTone(frequency, (int)(COLOR_SCAN_MS / width));
                }
                
                // Wait for remaining line time if needed
                long remainingTime = LINE_TIME_MS - 
                    ((long)SYNC_PULSE_MS + (long)PORCH_MS + 
                     (long)(COLOR_SCAN_MS * 3) + (long)(SCAN_SEPARATOR_MS * 2));
                if (remainingTime > 0) {
                    generateTone(BLACK_FREQ, (int)remainingTime);
                }
            }
            
            // End transmission
            generateTone(BLACK_FREQ, 300);
            audioLine.drain();
            audioLine.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double map(double value, double inMin, double inMax, double outMin, double outMax) {
        return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
    }

    private static void generateTone(double freq, int durationMs) {
        byte[] buffer = new byte[(int)(SAMPLE_RATE * durationMs / 1000)];
        for (int i = 0; i < buffer.length; i++) {
            double angle = 2.0 * Math.PI * i * freq / SAMPLE_RATE;
            buffer[i] = (byte)(Math.sin(angle) * 127);
        }
        audioLine.write(buffer, 0, buffer.length);
    }
}