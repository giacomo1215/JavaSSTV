package src.com.sstv;

import javax.sound.sampled.LineUnavailableException;

public class SSTVEncoder {
    private static final int SAMPLE_RATE = 44100;

    // Play a sync pulse (1200 Hz for specified duration)
    public static void playSyncPulse(int durationMs) throws LineUnavailableException {
        new Sound(1200, durationMs).playTone();
    }

    // Play a porch (1500 Hz for specified duration)
    public static void playPorch(int durationMs) throws LineUnavailableException {
        new Sound(1500, durationMs).playTone();
    }

    // Play a scan line for a color channel
    public static void playScanLine(double[] frequencies, int durationMs) throws LineUnavailableException {
        Sound sound = new Sound(0, 0, 0); // Dummy initialization
        sound.playScanLine(frequencies, durationMs);
    }

    // Convert RGB component to SSTV frequency (1500-2300 Hz)
    public static double rgbToFrequency(int component) {
        return 1500.0 + (component / 255.0) * 800.0;
    }
}