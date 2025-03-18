package src.com.sstv;

import javax.swing.*;
import javax.sound.sampled.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;

public class SSTVDecoder {
    // Constants for SSTV decoding
    private static final int SAMPLE_RATE = 44100; // Audio sample rate
    private static final int VIS_CODE = 0x3C;     // Scottie DX VIS code (0b00111100)
    private static final int WIDTH = 320;         // Image width
    private static final int HEIGHT = 256;        // Image height

    // Add Scottie DX timing constants
    private static final double SYNC_MS = 9.0;
    private static final double PORCH_MS = 1.5;
    private static final double SCAN_MS = 345.6;
    private static final double LINE_MS = 508.3;
    
    // Decoding states for finite state machine
    private enum DecodeState { VIS, SYNC, PORCH, LINE }
    
    private final PreviewPanel previewPanel;                        // GUI component for preview
    private final Queue<Short> audioBuffer = new LinkedList<>();    // Buffer for storing audio samples
    private DecodeState state = DecodeState.VIS;                    // Current decoding state
    
    // Image construction variables
    private int currentLine = 0;                                    // Current image line being decoded
    private int[] greenPixels = new int[WIDTH];                     // Green channel buffer
    private int[] bluePixels = new int[WIDTH];                      // Blue channel buffer
    private int[] redPixels = new int[WIDTH];                       // Red channel buffer
    private int colorChannel = 0;                                   // Current color channel (0=G, 1=B, 2=R)

    public SSTVDecoder() {
        previewPanel = new PreviewPanel(WIDTH, HEIGHT, 2);    // Initialize preview panel
        JFrame frame = new JFrame("SSTV Decoder");            // Set up main window
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(previewPanel);
        frame.pack();
        frame.setVisible(true);
    }

    public void startDecoding() throws LineUnavailableException {
        // Set up audio input format
        AudioFormat format = new AudioFormat(SAMPLE_RATE, 16, 1, true, false);
        TargetDataLine line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();

        // Continuous audio processing loop
        byte[] buffer = new byte[4096];
        while (true) {
            int bytesRead = line.read(buffer, 0, buffer.length);
            processAudio(Arrays.copyOf(buffer, bytesRead));
        }
    }

    // Convert byte array to 16-bit samples and add to buffer
    private void processAudio(byte[] bytes) {
        for (int i = 0; i < bytes.length; i += 2) {
            short sample = (short) ((bytes[i] & 0xFF) | (bytes[i+1] << 8));
            audioBuffer.add(sample);
        }
        processBuffer();
    }

    // Main decoding state machine
    private void processBuffer() {
        while (audioBuffer.size() > 2048) { // Maintain manageable buffer size
            switch (state) {
                case VIS:
                    if (checkVisHeader()) {
                        state = DecodeState.SYNC;
                        previewPanel.resetImage();
                        currentLine = 0;
                    }
                    break;
                    
                case SYNC:
                    if (detectTone(1200, 9)) { // 9ms 1200Hz sync pulse
                        state = DecodeState.PORCH;
                    }
                    break;
                    
                case PORCH:
                    if (detectTone(1500, 1.5)) { // 1.5ms 1500Hz porch
                        state = DecodeState.LINE;
                        colorChannel = 0;
                    }
                    break;
                    
                case LINE:
                    if (processLine()) {
                        state = DecodeState.SYNC;
                        if (++currentLine >= HEIGHT) { // End of image
                            state = DecodeState.VIS;
                            currentLine = 0;
                        }
                    }
                    break;
            }
        }
    }

    // VIS header detection using FSK decoding
    private boolean checkVisHeader() {
        int bitDuration = 30; // ms per bit
        int samplesPerBit = (int)(bitDuration * SAMPLE_RATE / 1000);
        int requiredSamples = samplesPerBit * 10; // 8 bits + start/stop
        
        if (audioBuffer.size() < requiredSamples) return false;
        
        // Decode VIS bits (simplified without proper synchronization)
        int visValue = 0;
        Iterator<Short> it = audioBuffer.iterator();
        
        // Skip start bit (assuming 1100Hz)
        for(int i=0; i<samplesPerBit; i++) it.next();
        
        // Decode 8 data bits (LSB first)
        for(int bit=0; bit<8; bit++) {
            double[] samples = new double[samplesPerBit];
            for(int i=0; i<samplesPerBit; i++) {
                samples[i] = it.next() / 32768.0;
            }
            double power1200 = calculatePower(samples, 1200);
            double power1100 = calculatePower(samples, 1100);
            
            visValue |= (power1100 > power1200 ? 1 : 0) << bit;
        }
        
        // Check against expected VIS code
        if(visValue == VIS_CODE) {
            removeSamples(requiredSamples);
            return true;
        }
        return false;
    }

    // Tone detection using Goertzel algorithm
    private boolean detectTone(double targetFreq, double durationMs) {
        int samplesNeeded = (int)(durationMs * SAMPLE_RATE / 1000);
        if(audioBuffer.size() < samplesNeeded) return false;
        
        // Extract samples without removing from buffer
        double[] samples = new double[samplesNeeded];
        Iterator<Short> it = audioBuffer.iterator();
        for(int i=0; i<samplesNeeded; i++) {
            samples[i] = it.next() / 32768.0;
        }
        
        double power = calculatePower(samples, targetFreq);
        boolean detected = power > 0.2; // Empirical threshold
        
        if(detected) removeSamples(samplesNeeded);
        return detected;
    }

    // Process one image line (three color channels)
    private boolean processLine() {
        // Decode current color channel
        int[] targetArray = null;
        switch(colorChannel) {
            case 0: targetArray = greenPixels; break;
            case 1: targetArray = bluePixels; break;
            case 2: targetArray = redPixels; break;
        }
        
        if(!decodeColorChannel(SCAN_MS, targetArray)) 
            return false;

        if(++colorChannel < 3) 
            return false; // Need more channels

        // Combine all channels when all three are decoded
        for(int x=0; x<WIDTH; x++) {
            int rgb = (redPixels[x] << 16) | (greenPixels[x] << 8) | bluePixels[x];
            previewPanel.setPixel(x, currentLine, rgb);
        }
        return true;
    }

    // Decode one color channel into specified buffer
    private boolean decodeColorChannel(double durationMs, int[] target) {
        int samplesNeeded = (int)(durationMs * SAMPLE_RATE / 1000);
        if(audioBuffer.size() < samplesNeeded) return false;
        
        int samplesPerPixel = samplesNeeded / WIDTH;
        for(int x=0; x<WIDTH; x++) {
            target[x] = decodePixel(samplesPerPixel);
        }
        removeSamples(samplesNeeded);
        return true;
    }

    // Decode single pixel value from audio samples
    private int decodePixel(int samplesPerPixel) {
        int samplesToUse = Math.min(samplesPerPixel, audioBuffer.size());
        if(samplesToUse == 0) return 0;
        
        double[] samples = new double[samplesToUse];
        for(int i=0; i<samplesToUse; i++) {
            samples[i] = audioBuffer.poll() / 32768.0;
        }
        
        // Calculate power at both ends of SSTV frequency range
        double powerLow = calculatePower(samples, 1500);
        double powerHigh = calculatePower(samples, 2300);
        double ratio = powerHigh / (powerLow + powerHigh + 1e-12); // Avoid division by zero
        
        return (int)(ratio * 255);
    }

    // Goertzel algorithm implementation
    private double calculatePower(double[] samples, double targetFreq) {
        double omega = 2 * Math.PI * targetFreq / SAMPLE_RATE;
        double coeff = 2 * Math.cos(omega);
        double q0 = 0, q1 = 0, q2 = 0;
        
        for(double sample : samples) {
            q0 = coeff * q1 - q2 + sample;
            q2 = q1;
            q1 = q0;
        }
        
        return q1*q1 + q2*q2 - coeff*q1*q2;
    }

    // Remove processed samples from buffer
    private void removeSamples(int count) {
        for(int i=0; i<count && !audioBuffer.isEmpty(); i++) {
            audioBuffer.poll();
        }
    }
}