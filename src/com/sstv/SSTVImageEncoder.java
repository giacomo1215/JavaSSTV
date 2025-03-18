package src.com.sstv;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import javax.imageio.ImageIO;

public class SSTVImageEncoder {
    private static final int SAMPLE_RATE = 44100;
    private static double phase = 0.0; // Track phase across all tones

    // Add 5ms taper window for smooth transitions
    private static final double TAPER_MS = 5.0;

    // Add Scottie DX timing constants
    private static final double SYNC_MS = 9.0;
    private static final double PORCH_MS = 1.5;
    private static final double SCAN_MS = 345.6;
    private static final double LINE_MS = 508.3;
    
    
    public static void encodeImage(String filename) throws Exception {
        BufferedImage img = ImageIO.read(new File(filename));
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        
        // VIS Header with phase continuity
        renderFSK(buffer, new int[]{0,0,1,1,1,1,0,0}, 30);
        
        // Vertical sync
        renderTone(buffer, 1200, 9, true);
        renderTone(buffer, 1500, 1.5, true);

        for(int y = 0; y < img.getHeight(); y++) {
            renderLine(buffer, img, y);
        }
        
        Sound.playBuffer(buffer.toByteArray());
    }

    private static void renderLine(ByteArrayOutputStream buffer, BufferedImage img, int y) {
        // Horizontal sync
        renderTone(buffer, 1200, SYNC_MS, true);
        renderTone(buffer, 1500, PORCH_MS, true);
        
        renderColor(buffer, img, y, 2); // Red
        renderTone(buffer, 1500, PORCH_MS, true);

        renderColor(buffer, img, y, 1); // Green
        renderTone(buffer, 1500, PORCH_MS, true);
        
        renderColor(buffer, img, y, 0); // Blue
        renderTone(buffer, 1500, PORCH_MS, true);
        
        // Calculate remaining silence
        double usedMs = SYNC_MS + PORCH_MS + (SCAN_MS * 3) + (PORCH_MS * 3);
        renderSilence(buffer, LINE_MS - usedMs);
    }

    private static void renderColor(ByteArrayOutputStream buffer, BufferedImage img, int y, int colorOffset) {
        double[] freqs = new double[img.getWidth()];
        for(int x = 0; x < img.getWidth(); x++) {
            int rgb = img.getRGB(x, y);
            // int val = (rgb >> (8 * (2 - colorOffset))) & 0xFF;
            int val = (rgb >> (8 * colorOffset)) & 0xFF;
            freqs[x] = 1500 + (val / 255.0) * 800;
        }
        renderSweep(buffer, freqs, SCAN_MS);
    }

    private static void renderTone(ByteArrayOutputStream buffer, double freq, double durationMs, boolean taper) {
        int samples = (int)(durationMs * SAMPLE_RATE / 1000);
        int taperSamples = (int)(TAPER_MS * SAMPLE_RATE / 1000);
        
        for(int i = 0; i < samples; i++) {
            double amplitude = 1.0;
            if(taper) {
                // Apply cosine taper to first/last 5ms
                amplitude = i < taperSamples ? 
                    (0.5 - 0.5 * Math.cos(Math.PI * i / taperSamples)) :
                    (i > samples - taperSamples ? 
                     (0.5 - 0.5 * Math.cos(Math.PI * (samples - i) / taperSamples)) :
                     1.0);
            }
            
            short sample = (short)(Math.sin(phase) * Short.MAX_VALUE * amplitude);
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            
            buffer.write((byte)(sample & 0xFF));
            buffer.write((byte)(sample >> 8));
        }
    }

    private static void renderSweep(ByteArrayOutputStream buffer, double[] freqs, double durationMs) {
        int totalSamples = (int)(durationMs * SAMPLE_RATE / 1000);
        double samplesPerPixel = (double)totalSamples / freqs.length;
        int taperSamples = (int)(TAPER_MS * SAMPLE_RATE / 1000);

        for(int i = 0; i < totalSamples; i++) {
            int px = (int)(i / samplesPerPixel);
            double freq = freqs[Math.min(px, freqs.length - 1)];
            
            // Smooth frequency transitions
            double alpha = (i % samplesPerPixel) / samplesPerPixel;
            if(px < freqs.length - 1) {
                freq = freqs[px] * (1 - alpha) + freqs[px + 1] * alpha;
            }

            // Apply window to entire sweep
            double amplitude = 1.0;
            if(i < taperSamples) {
                amplitude = 0.5 - 0.5 * Math.cos(Math.PI * i / taperSamples);
            } else if(i > totalSamples - taperSamples) {
                amplitude = 0.5 - 0.5 * Math.cos(Math.PI * (totalSamples - i) / taperSamples);
            }

            short sample = (short)(Math.sin(phase) * Short.MAX_VALUE * amplitude);
            phase += 2 * Math.PI * freq / SAMPLE_RATE;
            
            buffer.write((byte)(sample & 0xFF));
            buffer.write((byte)(sample >> 8));
        }
    }

    private static void renderFSK(ByteArrayOutputStream buffer, int[] bits, int bitDurationMs) {
        int samplesPerBit = (int)(bitDurationMs * SAMPLE_RATE / 1000);
        int taperSamples = (int)(TAPER_MS * SAMPLE_RATE / 1000);

        for(int bit : bits) {
            double freq = bit == 0 ? 1100 : 1300;
            for(int i = 0; i < samplesPerBit; i++) {
                double amplitude = 1.0;
                if(i < taperSamples || i > samplesPerBit - taperSamples) {
                    amplitude = 0.5 - 0.5 * Math.cos(Math.PI * 
                        Math.min(i, samplesPerBit - i) / taperSamples);
                }
                
                short sample = (short)(Math.sin(phase) * Short.MAX_VALUE * amplitude);
                phase += 2 * Math.PI * freq / SAMPLE_RATE;
                
                buffer.write((byte)(sample & 0xFF));
                buffer.write((byte)(sample >> 8));
            }
        }
    }

    private static void renderSilence(ByteArrayOutputStream buffer, double durationMs) {
        int samples = (int)(durationMs * SAMPLE_RATE / 1000);
        for(int i = 0; i < samples; i++) {
            buffer.write(0);
            buffer.write(0);
            phase += 0; // Maintain phase even during silence
        }
    }
}