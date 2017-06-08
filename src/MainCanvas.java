import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by kevin on 5/11/17.
 */
public class MainCanvas extends Canvas implements KeyListener, Runnable {

    final int PLAYER_SPEED = 4;
    final int MAX_DISTANCE = 20;
    final int FRAME_DELAY = 15;
    final int MIN_FRAMES_BETWEEN_TEXT_CHANGE = 60;

    ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
    ArrayList<Sprite> sprites;
    ArrayList<Sprite> inventory = new ArrayList<Sprite>();
    Thread runThread;
    //final String[] LEVEL_FILE_NAMES = new String[]{"menu", "testlevel"};
    int currentLevel = 0;
    int numberOfLevels = 0;
    int currentLineOfText = 0;
    int currentInteractionStage = 0;
    boolean levelXMLLoaded = false;
    boolean levelInitialized = false;
    boolean stopPlayerMovement = false;
    boolean queueNextLevel = false;
    boolean isFirstMenu = true;
    int indexOfTextToDraw = -1;
    Document levesDOM;
    Element root;
    Point playerPos = new Point(0, 0);
    BufferedImage player = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage background = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage loadingScreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage textScreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    Sprite textSprite;
    Sprite lastTextInteraction;
    int totalMillis = 0;
    Font pressStart2P;

    void buildTextInteraction(Sprite spriteToGetTextFrom)
    {
        lastTextInteraction = spriteToGetTextFrom;
        textSprite = new Sprite(this.getWidth() / 2 - textScreen.getWidth() / 2, this.getHeight() - textScreen.getHeight(), textScreen);
        sprites.add(textSprite);
        System.out.println(sprites.indexOf(spriteToGetTextFrom));
        indexOfTextToDraw = sprites.indexOf(spriteToGetTextFrom);
        currentLineOfText = 0;
    }

    void changeLevel(int newLevel)
    {
        System.out.println("changeTo: " + newLevel);
        currentLevel = newLevel;
        playerPos.x = this.getWidth() / 2;
        playerPos.y = this.getHeight() / 2;
        currentInteractionStage = 0;
        levelInitialized = false;
        queueNextLevel = false;
    }

    Sprite checkInteraction()
    {
        int x = playerPos.x;
        int y = playerPos.y;
        int width = player.getWidth();
        int height = player.getHeight();
        //O(n^2) type interactibility detection because this is a very simple engine for an English project, sorry to anyone who might read this code :(
        for (Sprite sprite : sprites)
            if (spriteCanBeDrawn(sprite) && (((x + width - 1 >= sprite.getX() - MAX_DISTANCE && x + width <= sprite.getX() + sprite.getWidth() - 1 - MAX_DISTANCE) || (x >= sprite.getX() + MAX_DISTANCE && x  <= sprite.getX() + sprite.getWidth() - 1 + MAX_DISTANCE) || (x <= sprite.getX() + MAX_DISTANCE && x + width >= sprite.getX() + sprite.getWidth() - 1 - MAX_DISTANCE)) && ((y + height - 1 >= sprite.getY() - MAX_DISTANCE && y + height <= sprite.getY() + sprite.getHeight() - 1 - MAX_DISTANCE) || (y >= sprite.getY() + MAX_DISTANCE && y <= sprite.getY() + sprite.getHeight() - 1 + MAX_DISTANCE) || (y <= sprite.getY() + MAX_DISTANCE && y + height >= sprite.getY() + sprite.getHeight() - 1 - MAX_DISTANCE))))
                return sprite;
        return new Sprite("zoneEmpty");
    }

    Sprite checkCollisions(int xMotion, int yMotion)
    {
        int x = playerPos.x;
        int y = playerPos.y;
        int width = player.getWidth();
        int height = player.getHeight();
        //O(n^2) type collision detection because this is a very simple engine for an English project, sorry to anyone who might read this code :(
        for (Sprite sprite : sprites)
            if (spriteCanBeDrawn(sprite) && (((x + width + xMotion - 1 >= sprite.getX() && x + xMotion + width <= sprite.getX() + sprite.getWidth() - 1) || (x + xMotion >= sprite.getX() && x + xMotion <= sprite.getX() + sprite.getWidth() - 1) || (x + xMotion <= sprite.getX() && x + width + xMotion >= sprite.getX() + sprite.getWidth() - 1)) && ((y + height + yMotion - 1 >= sprite.getY() && y + height + yMotion <= sprite.getY() + sprite.getHeight() - 1) || (y + yMotion >= sprite.getY() && y + yMotion <= sprite.getY() + sprite.getHeight() - 1) || (y + yMotion <= sprite.getY() && y + height + yMotion >= sprite.getY() + sprite.getHeight() - 1))))
                return sprite;
        return new Sprite("zoneEmpty");
    }

    boolean spriteCanBeDrawn(Sprite sprite)
    {
        return (sprite.getOrder() < 0 || (currentInteractionStage == sprite.getOrder() && sprite.getHideUntilCanInteract() == true) || (currentInteractionStage > sprite.getOrder() && sprite.getHideAfterInteraction() == false) || (currentInteractionStage <= sprite.getOrder() && sprite.getHideUntilCanInteract() == false));
    }

    public void update(Graphics g)
    {
        //set up double buffering
        Graphics doubleBufferGraphics;
        BufferedImage doubleBuffer;
        Dimension d = this.getSize();
        doubleBuffer = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
        doubleBufferGraphics = doubleBuffer.getGraphics();
        doubleBufferGraphics.setColor(this.getBackground());
        doubleBufferGraphics.fillRect(0, 0, d.width, d.height);
        doubleBufferGraphics.setColor(this.getForeground());
        paint(doubleBufferGraphics);

        //flip
        g.drawImage(doubleBuffer, 0, 0, this);

    }

    public void paint(Graphics g)
    {
        Dimension d = this.getSize();
        if (runThread == null)
        {
            //start the main thread if it hasn't already and add a KeyListener
            this.addKeyListener(this);
            runThread = new Thread(this);
            runThread.start();
            currentLevel = 0;
        }
        //drawRegular background image if it exists
        if (levelXMLLoaded && levelInitialized)
        {
            g.drawImage(background, 0, 0, this);
            //drawRegular all sprites
            for (Sprite sprite : sprites)
            {
                if (spriteCanBeDrawn(sprite))
                    sprite.drawRegular(g, this);
            }
            //drawRegular inventory items
            for (Sprite sprite : inventory)
            {
                sprite.drawInInventory(g, this, sprites.get(0), inventory.indexOf(sprite));
            }
            //drawRegular player if not in the menu
            if (currentLevel != 0)
            {
                g.drawImage(player, playerPos.x, playerPos.y, this);
            }
            //draw text on screen if it needs to be
            if (indexOfTextToDraw != -1)
            {
                stopPlayerMovement = true;
                textSprite.drawRegular(g, this);
                g.setFont(pressStart2P);
                String text = sprites.get(indexOfTextToDraw).getScriptAtLine(currentLineOfText);
                Color background = g.getColor();
                g.setColor(new Color(255, 255, 255));
                g.drawString(text, d.width / 2 - (this.getFontMetrics(pressStart2P).stringWidth(text) / 2), d.height - (textScreen.getHeight() / 2) + ((int)this.getFontMetrics(pressStart2P).getStringBounds(text, g).getHeight() / 2));
                g.setColor(background);
            }
        }
        else if (levelXMLLoaded && levelInitialized == false)
        {
            g.drawImage(loadingScreen, 0, 0, this);
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!pressedKeys.contains(e.getKeyCode()))
            pressedKeys.add(e.getKeyCode());
    }

    @Override
    public void keyReleased(KeyEvent e) {
        pressedKeys.remove(pressedKeys.indexOf(e.getKeyCode()));
    }

    @Override
    public void run() {
        while (true)
        {
            if (!levelXMLLoaded)
            {
                try
                {
                    loadingScreen = ImageIO.read(getClass().getResourceAsStream("res/images/Loading_Screen.png"));
                    textScreen = ImageIO.read(getClass().getResourceAsStream("res/images/Text_Box.png"));
                    pressStart2P = Font.createFont(Font.TRUETYPE_FONT, getClass().getResourceAsStream("res/PressStart2P.ttf"));
                    pressStart2P = pressStart2P.deriveFont(20F);
                    //set up document builders for XML parsing
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    StringBuilder stringBuilder = new StringBuilder();
                    //read the XML file into the stringBuilder
                    InputStream inputStream = getClass().getResourceAsStream("res/levels.xml");
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    String currentLine = bufferedReader.readLine();
                    while (currentLine != null)
                    {
                        stringBuilder.append(currentLine + "\n");
                        currentLine = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    inputStream.close();
                    //parse
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
                    levesDOM = documentBuilder.parse(byteArrayInputStream);
                    root = levesDOM.getDocumentElement();
                    numberOfLevels = levesDOM.getElementsByTagName("level").getLength();
                    levelXMLLoaded = true;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
            else
            {
                if (!levelInitialized)
                {
                    try
                    {
                        //find the node for the current level in the DOM
                        NodeList levels = levesDOM.getElementsByTagName("level");
                        Node levelNode = null;
                        for (int i = 0; i < levels.getLength(); i++)
                            if (Integer.parseInt(levels.item(i).getAttributes().getNamedItem("stage").getNodeValue()) == currentLevel)
                            {
                                levelNode = levels.item(i);
                                break;
                            }
                        if (levelNode != null)
                        {
                            //find the node for the background and player sprite within the node for the current levesDOM
                            NodeList levelChildren = levelNode.getChildNodes();
                            Node currentNode = levelNode.getAttributes().getNamedItem("background");
                            if (currentNode != null)
                            {
                                //get the path to the background image from the DOM then use the built in ImageIO to read it into the background
                                background = ImageIO.read(getClass().getResourceAsStream("res/images/" + currentNode.getNodeValue()));
                            }
                            else
                                background = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

                            String playerLocation = null;
                            currentNode = levelNode.getAttributes().getNamedItem("player");
                            if (currentNode != null)
                                playerLocation = currentNode.getNodeValue();
                            if (!(playerLocation == null || playerLocation.equals("null")))
                                player = ImageIO.read(getClass().getResourceAsStream("res/images/" + playerLocation));
                            else
                                player = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

                            currentNode = levelNode.getAttributes().getNamedItem("playerStartX");
                            if (currentNode != null)
                                playerPos.x = Integer.parseInt(currentNode.getNodeValue());
                            else
                                playerPos.x = (this.getWidth() / 2) - (player.getWidth() / 2);

                            currentNode = levelNode.getAttributes().getNamedItem("playerStartY");
                            if (currentNode != null)
                                playerPos.y = Integer.parseInt(currentNode.getNodeValue());
                            else
                                playerPos.y = (this.getHeight() / 2) - (player.getHeight() / 2);

                            //find all sprites and add the sprite for the inventory screen (if currentLevel is not 0)
                            sprites = new ArrayList<Sprite>();
                            if (currentLevel != 0)
                            {
                                BufferedImage inventoryImage = ImageIO.read(getClass().getResourceAsStream("res/images/inventory.png"));
                                sprites.add(new Sprite(this.getWidth() - inventoryImage.getWidth(), this.getHeight() - inventoryImage.getHeight(), inventoryImage));
                            }
                            for (int i = 0; i < levelChildren.getLength(); i++)
                                if (levelChildren.item(i).getNodeName().equals("sprite"))
                                    sprites.add(new Sprite(levelChildren.item(i)));

                            currentNode = levelNode.getAttributes().getNamedItem("introText");
                            if (currentNode != null)
                            {
                                Sprite introTextSprite = new Sprite(currentNode.getNodeValue(), "introText");
                                sprites.add(introTextSprite);
                                buildTextInteraction(introTextSprite);
                            }

                            currentNode = levelNode.getAttributes().getNamedItem("startingItem");
                            if (currentNode != null)
                                inventory.add(new Sprite(ImageIO.read(getClass().getResourceAsStream("res/images/" + currentNode.getNodeValue())), currentNode.getNodeValue()));
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                    levelInitialized = true;
                }
            }
            //interpret key events
            Integer[] keysToProcess = pressedKeys.toArray(new Integer[pressedKeys.size()]);
            Sprite collider = null;
            for (int i : keysToProcess)
            {
                switch (i)
                {
                    case (KeyEvent.VK_SPACE):
                        if (currentLevel == 0 && (totalMillis >= FRAME_DELAY * MIN_FRAMES_BETWEEN_TEXT_CHANGE || isFirstMenu))
                        {
                            isFirstMenu = false;
                            changeLevel(1);
                        }
                        else
                        {
                            if (indexOfTextToDraw != -1 && totalMillis >= FRAME_DELAY * MIN_FRAMES_BETWEEN_TEXT_CHANGE)
                            {
                                totalMillis = 0;
                                if (currentLineOfText + 1 < sprites.get(indexOfTextToDraw).getLinesOfText())
                                    currentLineOfText++;
                                else
                                    {
                                    if (sprites.get(indexOfTextToDraw).getInteraction().equals("introText"))
                                        sprites.remove(indexOfTextToDraw);
                                    sprites.remove(textSprite);
                                    indexOfTextToDraw = -1;
                                    currentLineOfText = 0;
                                    stopPlayerMovement = false;
                                    lastTextInteraction = null;
                                    System.out.println("SPACE: " + queueNextLevel + " " + currentLevel + " " + numberOfLevels);
                                    if (queueNextLevel == true) {
                                        queueNextLevel = false;
                                        if (currentLevel != numberOfLevels - 1)
                                            changeLevel(currentLevel + 1);
                                        else
                                            changeLevel(0);
                                    }
                                }
                            }
                        }
                        break;
                    case (KeyEvent.VK_W):
                        if (!stopPlayerMovement)
                        {
                            collider = checkCollisions(0, -PLAYER_SPEED);
                            if (playerPos.y - PLAYER_SPEED <= 0)
                                playerPos.y = 0;
                            else if (collider.getInteraction().equals("zoneEmpty") || (collider.getInteraction().equals("zone") && inventory.size() == 0))
                                playerPos.y -= PLAYER_SPEED;
                            else
                                playerPos.y = collider.getY() + collider.getHeight();
                        }
                        break;
                    case (KeyEvent.VK_S):
                        if (!stopPlayerMovement)
                        {
                            collider = checkCollisions(0, PLAYER_SPEED);
                            if (playerPos.y + player.getHeight() + PLAYER_SPEED >= this.getHeight())
                                playerPos.y = this.getHeight() - player.getHeight();
                            else if (collider.getInteraction().equals("zoneEmpty") || (collider.getInteraction().equals("zone") && inventory.size() == 0))
                                playerPos.y += PLAYER_SPEED;
                            else
                                playerPos.y = collider.getY() - player.getHeight();
                        }
                        break;
                    case (KeyEvent.VK_A):
                        if (!stopPlayerMovement)
                        {
                            collider = checkCollisions(-PLAYER_SPEED, 0);
                            if (playerPos.x - PLAYER_SPEED <= 0)
                                playerPos.x = 0;
                            else if (collider.getInteraction().equals("zoneEmpty") || (collider.getInteraction().equals("zone") && inventory.size() == 0))
                                playerPos.x -= PLAYER_SPEED;
                            else
                                playerPos.x = collider.getX() + collider.getWidth();
                        }
                        break;
                    case (KeyEvent.VK_D):
                        if (!stopPlayerMovement)
                        {
                            collider = checkCollisions(PLAYER_SPEED, 0);
                            if (playerPos.x + player.getWidth() + PLAYER_SPEED >= this.getWidth())
                                playerPos.x = this.getWidth() - player.getWidth();
                            else if (collider.getInteraction().equals("zoneEmpty") || (collider.getInteraction().equals("zone") && inventory.size() == 0))
                                playerPos.x += PLAYER_SPEED;
                            else
                                playerPos.x = collider.getX() - player.getWidth();
                        }
                        break;
                    case (KeyEvent.VK_E):
                        //see if the player is within 5 pixels of a sprite
                        collider = checkInteraction();
                        if (collider.getInteraction().equals("zoneEmpty") == false && (collider.getOrder() < 0 || collider.getOrder() == currentInteractionStage))
                        {
                            boolean interactionFailed = false;
                            if (collider.getInteraction().equals("text") && collider != lastTextInteraction)
                            {
                                System.out.println("text");
                                if (collider.getLinesOfText() != -1)
                                    buildTextInteraction(collider);
                            }
                            else if (collider.getInteraction().equals("item"))
                            {
                                System.out.println("item");
                                inventory.add(collider);
                                sprites.remove(collider);
                            }
                            else if (collider.getInteraction().equals("zone"))
                            {
                                System.out.println("zone");
                                String itemFilter = collider.getItemFilter();
                                if (itemFilter != null) {
                                    int foundIndex = -1;
                                    for (Sprite sprite : inventory)
                                    {
                                        System.out.println(sprite.getResource());
                                        if (sprite.getResource().equals(itemFilter))
                                        {
                                            foundIndex = inventory.indexOf(sprite);
                                            break;
                                        }
                                    }
                                    if (foundIndex != -1)
                                    {
                                        Sprite itemToPlace = inventory.get(foundIndex);
                                        inventory.remove(itemToPlace);
                                        itemToPlace.setInteraction("collider");
                                        System.out.println(collider.getCustomInteractionRender() == null);
                                        if (collider.getCustomInteractionRender() == null)
                                        {
                                            if (itemToPlace.getWidth() > collider.getWidth())
                                                itemToPlace.setCustomWidth(collider.getWidth());
                                            if (itemToPlace.getHeight() > collider.getHeight())
                                                itemToPlace.setCustomHeight(collider.getHeight());
                                            itemToPlace.setCoordinates(new Point(collider.getX() + (collider.getWidth() / 2) - (itemToPlace.getWidth() / 2), collider.getY() + (collider.getHeight() / 2) - (itemToPlace.getHeight() / 2)));
                                            sprites.add(itemToPlace);
                                        }
                                        else
                                        {
                                            collider.changeImageToCustomRender();
                                        }
                                        if (collider.canGiveItems())
                                        {
                                            try
                                            {
                                                for (int j = 0; j < collider.getNumberOfItemsToGive(); j++)
                                                {
                                                    System.out.println(collider.getItemToGive(j));
                                                    inventory.add(new Sprite(ImageIO.read(getClass().getResourceAsStream("res/images/" + collider.getItemToGive(j))), collider.getItemToGive(j)));
                                                }
                                            }
                                            catch (Exception e)
                                            {
                                                e.printStackTrace();
                                            }
                                        }
                                        collider.setInteraction("collider");
                                        if (collider.getLinesOfText() != -1)
                                            buildTextInteraction(collider);
                                    }
                                    else
                                    {
                                        interactionFailed = true;
                                    }
                                }
                            }
                            else if (collider.getInteraction().equals("collider"))
                                System.out.println("collider");
                            if (collider.getOrder() == currentInteractionStage && !interactionFailed)
                                currentInteractionStage++;
                            //check to see if the level needs to be changed
                            if (collider.getDoesEndLevel() == true && indexOfTextToDraw == -1)
                            {
                                if (currentLevel != numberOfLevels)
                                    changeLevel(currentLevel + 1);
                                else
                                    changeLevel(0);
                            }
                            else if (collider.getDoesEndLevel() == true && indexOfTextToDraw != -1)
                                queueNextLevel = true;
                        }
                        break;
                }
            }
            repaint();
            try
            {
                Thread.sleep(FRAME_DELAY);
                if (totalMillis < FRAME_DELAY * MIN_FRAMES_BETWEEN_TEXT_CHANGE)
                    totalMillis += FRAME_DELAY;
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
