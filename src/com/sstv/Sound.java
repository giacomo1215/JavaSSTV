package src.com.sstv;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.*;

public class Sound {
    private static final int SAMPLE_RATE = 44100;   // Sample rate in Hz (low-quality for reduced processing load)
    private static SourceDataLine line;             // 
    private double frequency;                       // Frequency of the tone in Hz
    private double startFreq;                       // Frequency of the start tone in Hz (FSK)
    private double endFreq;                         // Frequency of the end tone in Hz   (FSK)
    private int duration;                           // Duration of the tone in milliseconds

    /**
     * Constructor to initialize a sound object with a specific frequency and duration.
     * @param frequency The frequency of the tone in Hz.
     * @param duration The duration of the tone in milliseconds.
     */
    public Sound(double frequency, int duration) {
        this.frequency = frequency;
        this.duration = duration;
    }

    /**
     * Constructor to initialize a sound object in FSK with specific frequencies and duration
     * @param startFreq The frequency at the start of the tone in Hz
     * @param endFreq The frequency ad the end of the tone in Hz
     * @param duration The duration of the tone in milliseconds
     */
    public Sound(double startFreq, double endFreq, int duration) {
        this.frequency = startFreq;     // I set the pure sine frequency to avoid null references
        this.startFreq = startFreq;
        this.endFreq = endFreq;
        this.duration = duration;
    }

    // Getter methods to retrieve the frequency and duration values
    public double getFrequency() { return frequency; }
    public int getDuration() { return duration; }

    public static void renderToBuffer(ByteArrayOutputStream buffer, double freq, int durationMs) {
        int samples = (int)(durationMs / 1000.0 * SAMPLE_RATE);
        for (int i = 0; i < samples; i++) {
            double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
            short sample = (short)(Math.sin(angle) * Short.MAX_VALUE);
            buffer.write((byte)(sample & 0xFF));
            buffer.write((byte)((sample >> 8) & 0xFF));
        }
    }

    /**
     * Generates and plays a sine wave tone with the specified frequency and duration.
     * Uses 16-bit PCM encoding and plays the sound via Java's audio system.
     * @throws LineUnavailableException if the audio line cannot be opened.
     */
    public void playTone() throws LineUnavailableException {
        int numSamples = (int) ((duration / 1000.0) * SAMPLE_RATE); // Convert duration to number of audio samples
        byte[] buffer = new byte[numSamples * 2];  // 16-bit PCM (2 bytes per sample)

        // Generate sine wave samples
        for (int i = 0; i < numSamples; i++) {
            double angle = 2.0 * Math.PI * frequency * i / SAMPLE_RATE; // Calculate phase angle
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE); // Convert sine wave to 16-bit sample
            
            // Store the sample in little-endian format (least significant byte first)
            buffer[2 * i] = (byte) (sample & 0xFF);        // Lower 8 bits
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF); // Upper 8 bits
        }

        // Define the audio format (Mono, 16-bit, Little-endian)
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        // Open an audio line and play the generated sound
        line = AudioSystem.getSourceDataLine(format);
        line.open(format);  // Open the line with the specified format
        line.start();       // Start playback
        line.write(buffer, 0, buffer.length); // Send audio data to the speaker
        line.drain();       // Ensure all audio is played before closing
        line.close();       // Close the audio line
    }

    /**
     * Plays an FSK-modulated signal.
     * @throws LineUnavailableException if audio line cannot be opened.
     */
    public void playFSK() throws LineUnavailableException {
        if (startFreq == 0 || endFreq == 0) {
            throw new IllegalStateException("FSK frequencies not set.");
        }

        int numSamples = (int) ((duration / 1000.0) * SAMPLE_RATE);
        byte[] buffer = new byte[numSamples * 2];

        for (int i = 0; i < numSamples; i++) {
            // Linearly interpolate the frequency between startFreq and endFreq
            double freq = startFreq + (endFreq - startFreq) * (i / (double) numSamples);
            double angle = 2.0 * Math.PI * freq * i / SAMPLE_RATE;
            double amplitude = (i < numSamples * 0.05 || i > numSamples * 0.95) ? (i / (numSamples * 0.05)) : 1.0;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE * amplitude);

            // Store in little-endian format
            buffer[2 * i] = (byte) (sample & 0xFF);
            buffer[2 * i + 1] = (byte) ((sample >> 8) & 0xFF);
        }

        // Define the audio format (Mono, 16-bit, Little-endian)
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);

        // Open an audio line and play the generated sound
        line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }

    public void playScanLine(double[] frequencies, int scanDurationMs) throws LineUnavailableException {
        int totalSamples = (int) ((scanDurationMs / 1000.0) * SAMPLE_RATE);
        int samplesPerPixel = totalSamples / frequencies.length;
        byte[] buffer = new byte[totalSamples * 2];
        double angle = 0.0;
    
        for (int i = 0; i < totalSamples; i++) {
            int pixelIndex = Math.min(i / samplesPerPixel, frequencies.length - 1);
            double freq = frequencies[pixelIndex];
            angle += 2.0 * Math.PI * freq / SAMPLE_RATE;
            short sample = (short) (Math.sin(angle) * Short.MAX_VALUE);
            buffer[i * 2] = (byte) (sample & 0xFF);
            buffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xFF);
        }
    
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }

    public static void playBuffer(byte[] buffer) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(44100, 16, 1, true, false);
        SourceDataLine line = AudioSystem.getSourceDataLine(format);
        line.open(format);
        line.start();
        line.write(buffer, 0, buffer.length);
        line.drain();
        line.close();
    }
}