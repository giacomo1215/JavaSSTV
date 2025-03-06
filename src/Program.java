package src;
import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Program {
    private static BufferedImage image;

    public static void main(String[] args) {

        // Initializing window
        JFrame frame = new JFrame("Image to Sound");
        frame.setSize(500, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Declaring buttons
        JLabel imageLabel = new JLabel("No Image Selected", SwingConstants.CENTER);
        JButton uploadButton = new JButton("UPLOAD");
        JButton playButton = new JButton("BROADCAST");
        JButton listenButton = new JButton("RECEIVE");

        // Adding buttons
        frame.add(uploadButton, BorderLayout.NORTH);
        frame.add(imageLabel, BorderLayout.CENTER);
        frame.add(playButton, BorderLayout.SOUTH);
        frame.add(listenButton, BorderLayout.WEST);

        // Add image button
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

        // Play sound from image
        playButton.addActionListener(e -> {
            if (image != null) {
                new Thread(() -> ImageToSound.playImageTones(image)).start();
            } else {
                JOptionPane.showMessageDialog(frame, "No image loaded!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Add preview panel
        PreviewPanel previewPanel = new PreviewPanel(320, 240, 2);
        frame.add(previewPanel, BorderLayout.EAST);
        SoundToImage.setPreviewPanel(previewPanel);

        // Decode sound into image
        listenButton.addActionListener(e -> {
            new Thread(() -> {
                try {
                    SoundToImage.listen();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }).start();
        });

        // Open the window
        frame.setVisible(true);
    }
}