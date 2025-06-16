import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

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
