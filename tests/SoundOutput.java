package tests;

import javax.sound.sampled.LineUnavailableException;
import src.com.sstv.Sound;

public class SoundOutput {
    public static void main(String[] args) {
        Sound[] soundArr = new Sound[5];
        for (int i = 0; i < soundArr.length; i++) {
            Sound sound = new Sound((i + 1) * 1000, (i + 2) * 200, 100);
            soundArr[i] = sound;
        }

        try {
            for (Sound sound : soundArr) {
                sound.playFSK();
                
            }    
        } catch (LineUnavailableException e) {
            System.err.println(e.getMessage());
        }
            
    }
}
