package src;
import javax.sound.sampled.*;
import java.io.File;
import javax.imageio.ImageIO;

public class SoundToImage {
    private static final int WIDTH = 320;             // Image width
    private static final int HEIGHT = 256;            // Scottie DX height
    private static final float SAMPLE_RATE = 11025;   // SSTV Sample rate
    private static final int BUFFER_SIZE = 1024;      // Buffer size
    
    // Scottie DX frequencies
    private static final double SYNC_FREQ = 1200.0;   // Sync pulse frequency
    private static final double BLACK_FREQ = 1500.0;  // Black level
    private static final double WHITE_FREQ = 2300.0;  // White level
    
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
    
    // Buffer calculations
    private static final int SAMPLES_PER_MS = (int) (SAMPLE_RATE / 1000);
    private static final int SYNC_SAMPLES = ((int) (SYNC_PULSE_MS * SAMPLES_PER_MS) / 2) * 2;  // Ensure even
    private static final int PORCH_SAMPLES = ((int) (PORCH_MS * SAMPLES_PER_MS) / 2) * 2;      // Ensure even
    private static final int COLOR_SAMPLES = ((int) (COLOR_SCAN_MS * SAMPLES_PER_MS) / 2) * 2;  // Ensure even

    

    private static PreviewPanel previewPanel;

    public static void setPreviewPanel(PreviewPanel panel) {
        previewPanel = panel;
    }

    private static void waitForFrequency(TargetDataLine line, double targetFreq, int samples) {
        // Ensure buffer size is even
        byte[] buffer = new byte[samples + (samples % 2)];
        line.read(buffer, 0, buffer.length);
        double freq = detectFrequency(buffer);
        while (Math.abs(freq - targetFreq) > 50) {
            line.read(buffer, 0, buffer.length);
            freq = detectFrequency(buffer);
        }
    }

    private static int[] readColorLine(TargetDataLine line, double baseFreq) {
        int[] colorLine = new int[WIDTH];
        byte[] buffer = new byte[((COLOR_SAMPLES / WIDTH) / 2) * 2];
        
        for (int x = 0; x < WIDTH; x++) {
            line.read(buffer, 0, buffer.length);
            double freq = detectFrequency(buffer);
            colorLine[x] = normalizeColor(freq, baseFreq);
        }
        
        return colorLine;
    }

    private static int normalizeColor(double freq, double baseFreq) {
        double normalized = (freq - BLACK_FREQ) / (baseFreq - BLACK_FREQ);
        int value = (int)(normalized * 255);
        return Math.max(0, Math.min(255, value));
    }

    public static void listen() throws Exception {
        if (previewPanel != null) {
            previewPanel.resetImage();
        }

        TargetDataLine microphone = AudioSystem.getTargetDataLine(new AudioFormat(SAMPLE_RATE, 16, 1, true, true));
        microphone.open();
        microphone.start();

        int y = 0;
        long lineStartTime;

        while (y < HEIGHT) {
            lineStartTime = System.currentTimeMillis();
            
            // Wait for sync pulse
            waitForFrequency(microphone, SYNC_FREQ, SYNC_SAMPLES);
            
            // Process color channels
            int[] redLine = readColorLine(microphone, RED_FREQ);
            int[] greenLine = readColorLine(microphone, GREEN_FREQ);
            int[] blueLine = readColorLine(microphone, BLUE_FREQ);
            
            // Update image line
            for (int x = 0; x < WIDTH; x++) {
                int rgb = (redLine[x] << 16) | (greenLine[x] << 8) | blueLine[x];
                if (previewPanel != null) {
                    previewPanel.setPixel(x, y, rgb);
                }
            }
            
            y++;
            
            // Wait for remaining line time
            long elapsed = System.currentTimeMillis() - lineStartTime;
            if (elapsed < LINE_TIME_MS) {
                Thread.sleep(LINE_TIME_MS - elapsed);
            }
        }

        microphone.stop();
        microphone.close();
        
        if (previewPanel != null) {
            ImageIO.write(previewPanel.getImage(), "png", new File("output.png"));
        }
    }

    private static double detectFrequency(byte[] buffer) {
        int N = buffer.length;
        double maxMagnitude = 0;
        double dominantFreq = 0;

        // Only scan frequencies in the expected range
        for (double targetFreq = BLACK_FREQ; targetFreq <= WHITE_FREQ; targetFreq += 10) {
            double real = 0, imag = 0;
            double coeff = 2 * Math.PI * targetFreq / SAMPLE_RATE;
            
            for (int i = 0; i < N; i++) {
                real += buffer[i] * Math.cos(coeff * i);
                imag += buffer[i] * Math.sin(coeff * i);
            }

            double magnitude = Math.sqrt(real * real + imag * imag);
            if (magnitude > maxMagnitude) {
                maxMagnitude = magnitude;
                dominantFreq = targetFreq;
            }
        }

        return dominantFreq;
    }
}