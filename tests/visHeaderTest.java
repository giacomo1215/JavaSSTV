package tests;

import javax.sound.sampled.LineUnavailableException;
import src.com.sstv.Sound;

public class visHeaderTest {
    public static void main(String[] args) {
        int[] visHeader = {0, 1, 0, 1, 1, 0, 1, 0};  // Example VIS bits
        Sound sound = new Sound(1100, 1300, 100);     // FSK between 1100 Hz and 1300 Hz
        
        try {
            sound.playFSK(visHeader, 30);                 // Play FSK with 30ms per bit
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }
            
    }
}