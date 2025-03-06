package tests;

import javax.sound.sampled.LineUnavailableException;
import src.com.sstv.Sound;

public class SoundOutput {
    public static void main(String[] args) {
        Sound[] fskArr = new Sound[5];
        for (int i = 0; i < fskArr.length; i++) {
            Sound sound = new Sound((i + 1) * 1000, (i + 2) * 200, 100);
            fskArr[i] = sound;
        }

        Sound[] sinArr = new Sound[5];
        for (int i = 0; i < sinArr.length; i++) {
            Sound sound = new Sound((i + 1) * 1000, 100);
            sinArr[i] = sound;
        }

        try {
            for (Sound sound : fskArr) {
                sound.playFSK();
            }    
            for (Sound sound : sinArr) {
                sound.playTone();
            }    
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }
            
    }
}
