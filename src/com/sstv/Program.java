package src.com.sstv;

import javax.swing.*;
import javax.sound.sampled.*;

public class Program {
    public static void main(String[] args) {
        JFrame frame = new JFrame("SSTV Encoder/Decoder");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);

        JPanel panel = new JPanel();
        JButton encodeButton = new JButton("Broadcast Image");
        JButton decodeButton = new JButton("Receive Signal");

        encodeButton.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                try {
                    SSTVImageEncoder.encodeImage(fileChooser.getSelectedFile().getPath());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        decodeButton.addActionListener(e -> new Thread(() -> {
            try {
                SSTVDecoder decoder = new SSTVDecoder();
                decoder.startDecoding();
            } catch (LineUnavailableException ex) {
                ex.printStackTrace();
            }
        }).start());

        panel.add(encodeButton);
        panel.add(decodeButton);
        frame.add(panel);
        frame.setVisible(true);
    }
}