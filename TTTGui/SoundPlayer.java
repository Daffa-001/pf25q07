import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

/*
Java class for the sound effect player.
Contains a static method play(String filename) that:
1. Loads a .wav file from disk.
2. Plays it using Java’s built-in audio libraries.
These .wav files include
- start.wav => Plays when the TicTacToe game starts
- x_win.wav => Plays when player 1 / cross wins
- o_win.wav => Plays when player 2 / naught wins
- draw.wav => Plays when neither side wins
 */

public class SoundPlayer {

    public static void play(String filename) {
        try {
            File soundFile = new File("TTTGUI/" + filename);
            if (!soundFile.exists()) {
                System.err.println("Sound file not found: " + filename);
                return;
            }

            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundFile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            clip.start();

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            System.err.println("Error playing sound: " + e.getMessage());
        }
    }
}
