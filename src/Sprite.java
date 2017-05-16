import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.util.ArrayList;

/**
 * Created by kevin on 5/14/17.
 */
public class Sprite {
    private int x;
    private int y;
    private String path;
    private ArrayList<String> script = new ArrayList<String>();
    private BufferedImage sprite;

    public Sprite(int x, int y, String path, ArrayList<String> script)
    {
        this.x = x;
        this.y = y;
        this.path = path;
        this.script = script;
        try
        {
            sprite = ImageIO.read(new File(this.path));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            sprite = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void draw(Graphics g, ImageObserver observer)
    {
        g.drawImage(sprite, x, y, observer);
    }

}
