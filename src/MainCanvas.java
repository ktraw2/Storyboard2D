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
    int currentLineOfText = 0;
    boolean levelXMLLoaded = false;
    boolean levelInitialized = false;
    boolean stopPlayerMovement = false;
    int indexOfTextToDraw = -1;
    Document levesDOM;
    Element root;
    Point playerPos = new Point(0, 0);
    BufferedImage player = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage background = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage loadingScreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage textScreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    File backgroundImageFile = new File("");
    Sprite textSprite;
    Sprite lastTextInteraction;
    int totalMillis = 0;

    void buildTextInteraction(Sprite spriteToGetTextFrom, int startingIndex)
    {
        lastTextInteraction = spriteToGetTextFrom;
        textSprite = new Sprite(this.getWidth() / 2 - textScreen.getWidth() / 2, this.getHeight() - textScreen.getHeight(), textScreen);
        sprites.add(textSprite);
        indexOfTextToDraw = sprites.indexOf(spriteToGetTextFrom);
        currentLineOfText = startingIndex;
    }

    void changeLevel(int newLevel)
    {
        currentLevel = newLevel;
        playerPos.x = this.getWidth() / 2;
        playerPos.y = this.getHeight() / 2;
        levelInitialized = false;
    }

    Sprite checkInteraction()
    {
        int x = playerPos.x;
        int y = playerPos.y;
        int width = player.getWidth();
        int height = player.getHeight();
        //O(n^2) type interactibility detection because this is a very simple engine for an English project, sorry to anyone who might read this code :(
        for (Sprite sprite : sprites)
            if (((x + width - 1 >= sprite.getX() - MAX_DISTANCE && x + width <= sprite.getX() + sprite.getWidth() - 1 - MAX_DISTANCE) || (x >= sprite.getX() + MAX_DISTANCE && x  <= sprite.getX() + sprite.getWidth() - 1 + MAX_DISTANCE) || (x <= sprite.getX() + MAX_DISTANCE && x + width >= sprite.getX() + sprite.getWidth() - 1 - MAX_DISTANCE)) && ((y + height - 1 >= sprite.getY() - MAX_DISTANCE && y + height <= sprite.getY() + sprite.getHeight() - 1 - MAX_DISTANCE) || (y >= sprite.getY() + MAX_DISTANCE && y <= sprite.getY() + sprite.getHeight() - 1 + MAX_DISTANCE) || (y <= sprite.getY() + MAX_DISTANCE && y + height >= sprite.getY() + sprite.getHeight() - 1 - MAX_DISTANCE)))
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
            if (((x + width + xMotion - 1 >= sprite.getX() && x + xMotion + width <= sprite.getX() + sprite.getWidth() - 1) || (x + xMotion >= sprite.getX() && x + xMotion <= sprite.getX() + sprite.getWidth() - 1) || (x + xMotion <= sprite.getX() && x + width + xMotion >= sprite.getX() + sprite.getWidth() - 1)) && ((y + height + yMotion - 1 >= sprite.getY() && y + height + yMotion <= sprite.getY() + sprite.getHeight() - 1) || (y + yMotion >= sprite.getY() && y + yMotion <= sprite.getY() + sprite.getHeight() - 1) || (y + yMotion <= sprite.getY() && y + height + yMotion >= sprite.getY() + sprite.getHeight() - 1)))
                return sprite;
        return new Sprite("zoneEmpty");
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
            //drawRegular player if not in the menu
            if (currentLevel != 0)
            {
                g.drawImage(player, playerPos.x, playerPos.y, this);
            }
            //drawRegular all sprites
            for (Sprite sprite : sprites)
            {
                sprite.drawRegular(g, this);
            }
            //drawRegular inventory items
            for (Sprite sprite : inventory)
            {
                sprite.drawInInventory(g, this, sprites.get(0), inventory.indexOf(sprite));
            }
            //draw text on screen if it needs to be
            if (indexOfTextToDraw != -1)
            {
                stopPlayerMovement = true;
                //textSprite.drawRegular(g, this);
                Font myFont = new Font(this.getFont().getName(), 1, 72);
                g.setFont(myFont);
                String text = sprites.get(indexOfTextToDraw).getScriptAtLine(currentLineOfText);
                g.drawString(text, d.width / 2 - (this.getFontMetrics(myFont).stringWidth(text) / 2), d.height - (textScreen.getHeight() / 2) - (myFont.getSize() / 2));

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
                File file = new File("res/levels.xml");
                try
                {
                    loadingScreen = ImageIO.read(new File("res/images/loading.jpg"));
                    textScreen = ImageIO.read(new File("res/images/textscreen.png"));
                    player = ImageIO.read(new File("res/images/sprite.jpg"));
                    //set up document builders for XML parsing
                    DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
                    StringBuilder stringBuilder = new StringBuilder();
                    //read the XML file into the stringBuilder
                    FileReader fileReader = new FileReader(file);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String currentLine = bufferedReader.readLine();
                    while (currentLine != null)
                    {
                        stringBuilder.append(currentLine + "\n");
                        currentLine = bufferedReader.readLine();
                    }
                    bufferedReader.close();
                    fileReader.close();
                    //parse
                    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(stringBuilder.toString().getBytes("UTF-8"));
                    levesDOM = documentBuilder.parse(byteArrayInputStream);
                    root = levesDOM.getDocumentElement();
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
                            //find the node for the background within the node for the current levesDOM
                            NodeList levelChildren = levelNode.getChildNodes();
                            Node backgroundNode = null;
                            for (int i = 0; i < levelChildren.getLength(); i++)
                                if (levelChildren.item(i).getNodeName().equals("background"))
                                {
                                    backgroundNode = levelChildren.item(i);
                                    break;
                                }
                            if (backgroundNode != null) {
                                //get the path to the background image from the DOM then use the built in ImageIO to read it into the background
                                backgroundImageFile = new File("res/images/" + backgroundNode.getAttributes().getNamedItem("res").getNodeValue());
                                background = ImageIO.read(backgroundImageFile);
                            }
                            //find all sprites and add the sprite for the inventory screen (if currentLevel is not 0)
                            sprites = new ArrayList<Sprite>();
                            if (currentLevel != 0)
                            {
                                BufferedImage inventoryImage = ImageIO.read(new File("res/images/inventory.png"));
                                sprites.add(new Sprite(this.getWidth() - inventoryImage.getWidth(), this.getHeight() - inventoryImage.getHeight(), inventoryImage));
                            }
                            for (int i = 0; i < levelChildren.getLength(); i++)
                            {
                                if (levelChildren.item(i).getNodeName().equals("sprite"))
                                {
                                    sprites.add(new Sprite(levelChildren.item(i)));
                                }
                            }
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
                        if (currentLevel == 0)
                        {
                            changeLevel(1);
                        }
                        else
                        {
                            if (indexOfTextToDraw != -1 && totalMillis >= FRAME_DELAY * MIN_FRAMES_BETWEEN_TEXT_CHANGE) {
                                totalMillis = 0;
                                if (currentLineOfText + 1 < sprites.get(indexOfTextToDraw).getLinesOfText())
                                    currentLineOfText++;
                                else {
                                    sprites.remove(textSprite);
                                    indexOfTextToDraw = -1;
                                    currentLineOfText = 0;
                                    stopPlayerMovement = false;
                                    lastTextInteraction = null;
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
                            else if (collider.getInteraction().startsWith("zone"))
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
                            else if (collider.getInteraction().startsWith("zone"))
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
                            else if (collider.getInteraction().startsWith("zone"))
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
                            else if (collider.getInteraction().startsWith("zone"))
                                playerPos.x += PLAYER_SPEED;
                            else
                                playerPos.x = collider.getX() - player.getWidth();
                        }
                        break;
                    case (KeyEvent.VK_E):
                        //see if the player is within 5 pixels of a sprite
                        collider = checkInteraction();
                        if (collider.getInteraction().equals("text") && collider != lastTextInteraction)
                        {
                            System.out.println("text");
                            buildTextInteraction(collider, 0);
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
                            String itemFilter = collider.getScriptAtLine(0);
                            if (!itemFilter.equals("any"))
                            {
                                int foundIndex = -1;
                                for (Sprite sprite : inventory)
                                {
                                    System.out.println(sprite.getPhotoID());
                                    if (sprite.getPhotoID().equals(itemFilter))
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
                                    if (itemToPlace.getWidth() > collider.getWidth())
                                        itemToPlace.setCustomWidth(collider.getWidth());
                                    if (itemToPlace.getHeight() > collider.getHeight())
                                        itemToPlace.setCustomHeight(collider.getHeight());
                                    itemToPlace.setCoordinates(new Point(collider.getX() + (collider.getWidth() / 2) - (itemToPlace.getWidth() / 2), collider.getY() + (collider.getHeight() / 2) - (itemToPlace.getHeight() / 2)));
                                    sprites.add(itemToPlace);
                                    collider.setInteraction("collider");
                                    if (collider.getLinesOfText() > 1)
                                    {
                                        buildTextInteraction(collider, 1);
                                    }
                                }
                            }
                        }
                        else if (collider.getInteraction().equals("collider"))
                            System.out.println("collider");
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
