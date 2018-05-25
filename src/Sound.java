import javax.sound.sampled.*;
import java.io.BufferedInputStream;

public class Sound
{
    private Clip clip;

    public Sound(String resource)
    {
        Mixer mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try
        {
            clip = (Clip)mixer.getLine(dataInfo);
            BufferedInputStream buffer = new BufferedInputStream(getClass().getResourceAsStream(resource));
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(buffer);
            clip.open(audioInputStream);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void start()
    {
        clip.start();
    }

    public void stop()
    {
        clip.stop();
    }
}