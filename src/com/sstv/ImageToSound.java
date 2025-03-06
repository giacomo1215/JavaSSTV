package src.com.sstv;

import javax.sound.sampled.*;
import java.awt.image.BufferedImage;

public class ImageToSound {
    public static final float SAMPLE_RATE = 11025;        // Standard SSTV sample rate
    private static final double SYNC_PULSE_FREQ = 1200.0; // Hz
    private static final double BLACK_FREQ = 1500.0;      // Hz
    private static final double WHITE_FREQ = 2300.0;      // Hz

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

    private static SourceDataLine audioLine;
}