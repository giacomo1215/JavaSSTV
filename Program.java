import java.nio.Buffer;
import java.util.Random;
import javax.swing.*;
import javax.xml.transform.Source;
import javax.sound.sampled.*;

/**
 * SSTV (Slow-Scan Television) Image to Audio Converter
 * 
 * This program demonstrates the basic principles of SSTV by converting a simple color
 * image into audio signals. It generates a random color pattern and converts each pixel
 * into corresponding audio frequencies, similar to how traditional SSTV works.
 */
public class Program {

    public static final float SAMPLE_RATE = 3845;

    public static void main(String[] args) {
        Random random = new Random();
        int width = 16;  // Width of the test image
        int height = 16; // Height of the test image

        // Create a 2D array to store the color information for each pixel
        Color[][] image = new Color[width][height];
        
        // Generate random colors for each pixel and convert to audio
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                // Create a random RGB color
                Color color = new Color(
                    (int)random.nextInt(256),  // Red value (0-255)
                    (int)random.nextInt(256),  // Green value (0-255)
                    (int)random.nextInt(256)   // Blue value (0-255)
                );
                image[j][i] = color;
                // Convert the color to an audio tone and play it
                generateTone(color.getDecimal(), 60);
            }
        }

        // Create and display the GUI window showing the generated image
        JFrame frame = new JFrame("SSTV Image Preview");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ImagePanel(image, 50)); 
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Generates and plays an audio tone at the specified frequency.
     * 
     * This method creates a simple sine wave at the given frequency and plays it
     * through the system's default audio output device. The audio is generated
     * using PCM encoding with 8-bit samples.
     * 
     * @param freq The frequency of the tone to generate, in Hz
     * @param durationMs The duration of the tone in milliseconds
     */
    public static void generateTone(double freq, int durationMs) {
        try {
            // Calculate buffer size based on sample rate and duration
            byte[] buffer = new byte[(int)(SAMPLE_RATE * durationMs / 1000)];
            
            // Configure audio format: 44100Hz sample rate, 8-bit depth, mono, signed, big endian
            AudioFormat format = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            
            // Get audio output line
            SourceDataLine line = AudioSystem.getSourceDataLine(format);
            line.open(format);
            line.start();

            // Generate sine wave samples
            for (int i = 0; i < buffer.length; i++) {
                double angle = 2.0 * Math.PI * i * freq / SAMPLE_RATE;
                // Convert sine wave to 8-bit PCM
                buffer[i] = (byte) (Math.sin(angle) * 127);
            }

            // Play the generated audio
            line.write(buffer, 0, buffer.length);
            line.drain();
            line.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}