package src.com.sstv;

public class Color {
    private final int r, g, b;

    public Color(int r, int g, int b) {
        this.r = Math.max(0, Math.min(255, r));
        this.g = Math.max(0, Math.min(255, g));
        this.b = Math.max(0, Math.min(255, b));
    }

    public int getR() { return r; }
    public int getG() { return g; }
    public int getB() { return b; }

    String getHex() { return String.format("#%02X%02X%02X", r, g, b); }
    int getDecimal() { return (r << 16) | (g << 8) | b; }

    public double[] toYUV() {
        double y = 0.299 * r + 0.587 * g + 0.114 * b;
        double u = 0.493 * (b - y);
        double v = 0.877 * (r - y);
        return new double[]{y, u, v};
    }
}
