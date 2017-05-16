import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

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
    private BufferedImage spriteImage;
    private Node spriteNode;


    public Sprite(Node spriteNode)
    {
        this.spriteNode = spriteNode;
        NamedNodeMap attributes = spriteNode.getAttributes();
        this.x = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
        this.y = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
        this.path = attributes.getNamedItem("res").getNodeValue();
        try
        {
            spriteImage = ImageIO.read(new File("res/images/" + this.path));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            spriteImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void draw(Graphics g, ImageObserver observer)
    {
        g.drawImage(spriteImage, x, y, observer);
    }

}
