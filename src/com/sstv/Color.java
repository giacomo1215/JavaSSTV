package src.com.sstv;

public class Color {
    private final int r, g, b;

    /**
     * Constructor for a color, considering the three channels Red, Green and Blue
     * @param r red
     * @param g green
     * @param b blue
     */
    public Color(int r, int g, int b) {
        this.r = Math.max(0, Math.min(255, r));
        this.g = Math.max(0, Math.min(255, g));
        this.b = Math.max(0, Math.min(255, b));
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    /**
     * Gets the HEX value of the color
     * @return hex value
     */
    public String getHex() { 
        return String.format("#%02X%02X%02X", r, g, b); 
    }

    /**
     * Gets the decimal value for the color
     * @return decimal value
     */
    public int getDecimal() { 
        return (r << 16) | (g << 8) | b; 
    }

    /**
     * Converts this color from RGB to YCbCr (used in SSTV).
     * @return Array [Y, Cb, Cr]
     */
    public double[] toYCbCr() {
        double y  =  0.299 * r + 0.587 * g + 0.114 * b;
        double cb = -0.168736 * r - 0.331264 * g + 0.5 * b + 128;
        double cr =  0.5 * r - 0.418688 * g - 0.081312 * b + 128;
        return new double[]{y, cb, cr};
    }
}