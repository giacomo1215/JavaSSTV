package src.com.sstv;

import javax.sound.sampled.*;

public class SSTVEncoder {

    /**
     * Encodes a color pixel into SSTV-compatible audio frequencies.
     * @param color The Color object representing the pixel.
     */
    public static void encodePixel(Color color) throws LineUnavailableException {
        double[] yCbCr = color.toYCbCr();
        double y = yCbCr[0], cb = yCbCr[1], cr = yCbCr[2];

        // Convert Y, Cb, Cr to frequencies for Scottie DX
        double Fy  = 1500 + (y / 255.0) * 800;
        double Fcb = 1900 + ((cb - 128) / 255.0) * 400;
        double Fcr = 1500 + ((cr - 128) / 255.0) * 400;

        playTone(Fy, 100);
        playTone(Fcb, 100);
        playTone(Fcr, 100);
    }

    /**
     * Plays a tone using the Sound class.
     * @param frequency The frequency of the tone in Hz.
     * @param duration The duration of the tone in milliseconds.
     */
    private static void playTone(double frequency, int duration) throws LineUnavailableException {
        Sound sound = new Sound(frequency, duration);
        sound.playTone();
    }
}