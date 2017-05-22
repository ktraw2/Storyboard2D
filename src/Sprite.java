import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.File;
import java.rmi.server.ExportException;
import java.util.ArrayList;

/**
 * Created by kevin on 5/14/17.
 */
public class Sprite {
    private int x;
    private int y;
    private String interaction;
    private BufferedImage spriteImage;
    private Node spriteNode;
    ArrayList<String> interactionAction;

    public Sprite(String interaction)
    {
        this.interaction = interaction;
    }

    public Sprite(int x, int y, BufferedImage spriteImage)
    {
        this.x = x;
        this.y = y;
        this.spriteImage = spriteImage;
        this.interaction = "collider";
    }

    public Sprite(Node spriteNode)
    {
        this.spriteNode = spriteNode;
        NamedNodeMap attributes = spriteNode.getAttributes();
        try
        {
            this.x = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
            this.y = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
            this.interaction = attributes.getNamedItem("interaction").getNodeValue();
            String path = attributes.getNamedItem("res").getNodeValue();
            spriteImage = ImageIO.read(new File("res/images/" + path));
            String rawScript = "";
            if (spriteNode.hasChildNodes())
            {
                rawScript = spriteNode.getFirstChild().getTextContent();
                interactionAction = new ArrayList<String>();
                //parse the interaction script, looking for ` characters
                int startIndex = 0;
                for (int i = 0; i < rawScript.length(); i++) {
                    if (rawScript.charAt(i) == '`')
                    {
                        interactionAction.add(rawScript.substring(startIndex, i));
                        startIndex = i + 1;
                    }
                }
                for (String s : interactionAction)
                    System.out.println(s);
            }
            System.out.println(rawScript);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            spriteImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void drawRegular(Graphics g, ImageObserver observer)
    {
        g.drawImage(spriteImage, x, y, observer);
    }

    public void drawInInventory (Graphics g, ImageObserver observer, Sprite inventorySprite, int indexInInventory)
    {
        g.drawImage(spriteImage, inventorySprite.getX() + 10 + (75 * indexInInventory), inventorySprite.getY() + 50, 64, 64, observer);
    }

    public int getX()
    {
        return x;
    }

    public int getY()
    {
        return y;
    }

    public int getWidth()
    {
        return spriteImage.getWidth();
    }

    public int getHeight()
    {
        return spriteImage.getHeight();
    }

    public String getInteraction()
    {
        return interaction;
    }

    public BufferedImage getSpriteImage()
    {
        return spriteImage;
    }
}
