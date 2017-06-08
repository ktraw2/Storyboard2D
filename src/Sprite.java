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
    private int customWidth = 0;
    private int customHeight = 0;
    private int order;
    private String interaction;
    private String resource;
    private String filterToItem;
    private String customInteractionRender;
    private BufferedImage spriteImage;
    private boolean hideUntilCanInteract;
    private boolean hideAfterInteraction;
    private boolean isHidden;
    private boolean doesEndLevel;
    ArrayList<String> interactionText;
    ArrayList<String> itemsToGive;

    private ArrayList<String> parseMultiScript(String rawScript)
    {
        ArrayList<String> parseResult = new ArrayList<String>();
        //parse the interaction script, looking for ` characters
        int startIndex = 0;
        for (int i = 0; i < rawScript.length(); i++) {
            if (rawScript.charAt(i) == '`')
            {
                parseResult.add(rawScript.substring(startIndex, i));
                startIndex = i + 1;
            }
        }
        return parseResult;
    }

    public Sprite (BufferedImage spriteImage, String resource)
    {
        this.spriteImage = spriteImage;
        customWidth = spriteImage.getWidth();
        customHeight = spriteImage.getHeight();
        this.resource = resource;
    }

    public Sprite(String interaction)
    {
        this.interaction = interaction;
    }

    public Sprite (String interactionScript, String interaction)
    {
        this.interactionText = parseMultiScript(interactionScript);
        this.interaction = interaction;
    }

    public Sprite(int x, int y, BufferedImage spriteImage)
    {
        this.x = x;
        this.y = y;
        this.spriteImage = spriteImage;
        customWidth = spriteImage.getWidth();
        customHeight = spriteImage.getHeight();
        this.interaction = "collider";
    }

    public Sprite(Node spriteNode)
    {
        NamedNodeMap attributes = spriteNode.getAttributes();
        try
        {
            Node currentAttribute = attributes.getNamedItem("x");
            if (currentAttribute != null)
                this.x = Integer.parseInt(currentAttribute.getNodeValue());
            else
                this.x =  0;

            currentAttribute = attributes.getNamedItem("y");
            if (currentAttribute != null)
                this.y = Integer.parseInt(currentAttribute.getNodeValue());
            else
                this.y =  0;

            currentAttribute = attributes.getNamedItem("order");
            if (currentAttribute != null)
                this.order = Integer.parseInt(currentAttribute.getNodeValue());
            else
                this.order =  -1;

            currentAttribute = attributes.getNamedItem("interaction");
            if (currentAttribute != null)
                this.interaction = currentAttribute.getNodeValue();
            else
                this.interaction = "collider";

            currentAttribute = attributes.getNamedItem("filterToItem");
            if (currentAttribute != null)
                this.filterToItem = currentAttribute.getNodeValue();
            else
                this.filterToItem = null;

            currentAttribute = attributes.getNamedItem("customInteractionRender");
            if (currentAttribute != null)
                this.customInteractionRender = currentAttribute.getNodeValue();
            else
                this.customInteractionRender = null;

            currentAttribute = attributes.getNamedItem("givesItem");
            if (currentAttribute != null)
                this.itemsToGive = parseMultiScript(currentAttribute.getNodeValue());

            currentAttribute = attributes.getNamedItem("res");
            if (currentAttribute != null)
            {
                this.resource = currentAttribute.getNodeValue();
                spriteImage = ImageIO.read(getClass().getResourceAsStream("res/images/" + resource));
            }
            else
            {
                this.resource = "null";
                spriteImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
            }
            customWidth = spriteImage.getWidth();
            customHeight = spriteImage.getHeight();

            currentAttribute = attributes.getNamedItem("hideUntilCanInteract");
            if (currentAttribute != null)
                if (currentAttribute.getNodeValue().toLowerCase().equals("true"))
                    this.hideUntilCanInteract = true;
                else
                    this.hideUntilCanInteract = false;
            else
                this.hideUntilCanInteract = false;

            currentAttribute = attributes.getNamedItem("hideAfterInteraction");
            if (currentAttribute != null)
                if (currentAttribute.getNodeValue().toLowerCase().equals("true"))
                    this.hideAfterInteraction = true;
                else
                    this.hideAfterInteraction = false;
            else
                this.hideAfterInteraction = false;

            currentAttribute = attributes.getNamedItem("doesEndLevel");
            if (currentAttribute != null)
                if (currentAttribute.getNodeValue().toLowerCase().equals("true"))
                    this.doesEndLevel = true;
                else
                    this.doesEndLevel = false;
            else
                this.doesEndLevel = false;

            if (spriteNode.hasChildNodes())
                this.interactionText = parseMultiScript(spriteNode.getFirstChild().getTextContent());

            this.isHidden = false;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            spriteImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        }
    }

    public void changeImageToCustomRender()
    {
        if (customInteractionRender != null)
        {
            try
            {
                System.out.println(customInteractionRender);
                spriteImage = ImageIO.read(getClass().getResourceAsStream("res/images/" + customInteractionRender));
                customWidth = spriteImage.getWidth();
                customHeight = spriteImage.getHeight();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void drawRegular(Graphics g, ImageObserver observer)
    {
        g.drawImage(spriteImage, x, y, customWidth, customHeight, observer);
    }

    public void drawInInventory (Graphics g, ImageObserver observer, Sprite inventorySprite, int indexInInventory)
    {
        g.drawImage(spriteImage, inventorySprite.getX() + 15 + (75 * indexInInventory), inventorySprite.getY() + 60, 32, 32, observer);
    }

    public void setHidden(boolean isHidden)
    {
        this.isHidden = isHidden;
    }

    public void setInteraction(String interaction)
    {
        this.interaction = interaction;
    }

    public void setCoordinates(Point coordinates)
    {
        this.x = coordinates.x;
        this.y = coordinates.y;
    }

    public void setCustomWidth(int customWidth)
    {
        this.customWidth = customWidth;
    }

    public void setCustomHeight(int customHeight)
    {
        this.customHeight = customHeight;
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
        return customWidth;
    }

    public int getHeight()
    {
        return customHeight;
    }

    public int getOrder()
    {
        return order;
    }

    public String getInteraction()
    {
        return interaction;
    }

    public String getScriptAtLine(int line)
    {
        return interactionText.get(line);
    }

    public String getItemToGive(int number)
    {
        return itemsToGive.get(number);
    }

    public String getItemFilter()
    {
        return filterToItem;
    }

    public String getCustomInteractionRender()
    {
        return customInteractionRender;
    }

    public int getLinesOfText()
    {
        if (interactionText != null)
            return interactionText.size();
        else
            return -1;
    }

    public int getNumberOfItemsToGive()
    {
        return itemsToGive.size();
    }

    public String getResource()
    {
        return resource;
    }

    public boolean getIsHidden()
    {
        return isHidden;
    }

    public boolean getDoesEndLevel() {
        return doesEndLevel;
    }

    public boolean getHideUntilCanInteract()
    {
        return hideUntilCanInteract;
    }

    public boolean getHideAfterInteraction()
    {
        return hideAfterInteraction;
    }

    public boolean canGiveItems()
    {
        if (itemsToGive == null)
            return false;
        else
            return true;
    }
}