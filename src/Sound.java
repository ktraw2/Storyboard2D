import javax.sound.sampled.*;
import java.net.URL;

public class Sound
{
    private Mixer mixer;
    private Clip clip;
    private URL soundURL;

    public Sound(String resource)
    {
        mixer = AudioSystem.getMixer(AudioSystem.getMixerInfo()[0]);
        DataLine.Info dataInfo = new DataLine.Info(Clip.class, null);
        try
        {
            clip = (Clip)mixer.getLine(dataInfo);
            //soundURL = Sound.class.getResource(resource);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream(resource));
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

    public boolean isPlaying()
    {
        return clip.isActive();
    }
}