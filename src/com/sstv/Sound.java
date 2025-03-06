package src.com.sstv;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;

public class Sound {
    private static final int SAMPLE_RATE = 11025;   // bom
    private double frequency;                       // Hz
    private int duration;                           // ms

    // Public constructor
    public Sound(double frequency, int duration) {
        this.frequency  = frequency;
        this.duration   = duration;
    }

    // Getter methods
    public double getFrequency() { return frequency; }
    public int getDuration() { return duration; }

    // Public methods
    public void playTone() throws LineUnavailableException {
        int numSamples = (int)((duration / 1000.0) * SAMPLE_RATE);  // I get the number of samples
        byte[] buffer = new byte[numSamples * 2];                   // 16-bit PCM, so 2 bytes per sample

        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE);
            
            // Little-endian order
            buffer[2 * i] = (byte) (sample & 0xFF);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }
}
