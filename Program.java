import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Program {
    private static BufferedImage image;
    public static final float SAMPLE_RATE = 3845;

    public static void main(String[] args) {
        JFrame frame = new JFrame("Image to Sound");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        JButton uploadButton = new JButton("Upload Image");
        JButton playButton = new JButton("Play Tones");

        frame.add(uploadButton, BorderLayout.NORTH);
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(playButton, BorderLayout.SOUTH);

        uploadButton.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                File file = chooser.getSelectedFile();
                try {
                    image = ImageIO.read(file);
                    ImageIcon icon = new ImageIcon(image.getScaledInstance(200, 200, Image.SCALE_SMOOTH));
                    imageLabel.setIcon(icon);
                    imageLabel.setText("");  // Remove text when an image is loaded
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });

        playButton.addActionListener(e -> {
            if (image != null) {
                playImageTones(image);
            }
        });

        frame.setVisible(true);
    }

    public static void playImageTones(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        for (int y = 0; y < height; y += 10) {  // Skip pixels for efficiency
            for (int x = 0; x < width; x += 10) {
                int pixel = img.getRGB(x, y);
                int r = (pixel >> 16) & 0xFF;
                int g = (pixel >> 8) & 0xFF;
                int b = pixel & 0xFF;
                
                int decimalColor = (r << 16) | (g << 8) | b;
                double frequency = map(decimalColor, 0, 16777215, 100, 5000);
                generateTone(frequency, 100);  // Play for 100ms
            }
        }
    }


    public static double map(int value, int inMin, int inMax, int outMin, int outMax) {
        return outMin + (double) (value - inMin) * (outMax - outMin) / (inMax - inMin);
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