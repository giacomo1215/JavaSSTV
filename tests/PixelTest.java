package tests;

import src.com.sstv.Color;
import src.com.sstv.SSTVEncoder;

public class PixelTest {
    public static void main(String[] args) {
            Color[] pixels = new Color[2];
            for (int i = 0; i < pixels.length; i++) {
                int r = (int)(Math.random() * 256);
                int g = (int)(Math.random() * 256);
                int b = (int)(Math.random() * 256);
                pixels[i] = new Color(r, g, b);
            }
            try {
                for (Color pixel : pixels) {
                    SSTVEncoder.encodePixel(pixel);
                }
            } catch (Exception e) {}
    }
}
