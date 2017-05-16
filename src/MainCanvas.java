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
import java.awt.image.ImageObserver;
import java.io.*;
import java.util.ArrayList;

/**
 * Created by kevin on 5/11/17.
 */
public class MainCanvas extends Canvas implements KeyListener, Runnable {

    final int PLAYER_SPEED = 3;

    ArrayList<Integer> pressedKeys = new ArrayList<Integer>();
    ArrayList<Sprite> sprites;
    Thread runThread;
    //final String[] LEVEL_FILE_NAMES = new String[]{"menu", "testlevel"};
    int currentLevel = 0;
    boolean levelXMLLoaded = false;
    boolean levelInitialized = false;
    Document levesDOM;
    Element root;
    Point playerPos = new Point(0, 0);
    BufferedImage player = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage background = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    BufferedImage loadingScreen = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    File backgroundImageFile = new File("");

    void changeLevel(int newLevel)
    {
        currentLevel = newLevel;
        playerPos.x = this.getWidth() / 2;
        playerPos.y = this.getHeight() / 2;
        levelInitialized = false;
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
        //draw background image if it exists
        if (levelXMLLoaded && levelInitialized)
        {
            g.drawImage(background, 0, 0, this);
            //draw player if not in the menu
            if (currentLevel != 0)
            {
                g.drawImage(player, playerPos.x, playerPos.y, this);
            }
            //draw all sprites
            for (Sprite sprite : sprites)
            {
                sprite.draw(g, this);
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
                        System.out.print(currentLine + "\n");
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
                            //find all sprites
                            sprites = new ArrayList<Sprite>();
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
            for (int i : pressedKeys)
            {
                switch (i)
                {
                    case (KeyEvent.VK_SPACE):
                        if (currentLevel == 0)
                        {
                            changeLevel(1);
                        }
                        break;
                    case (KeyEvent.VK_W):
                        if (playerPos.y - PLAYER_SPEED <= 0)
                            playerPos.y = 0;
                        else
                            playerPos.y -= PLAYER_SPEED;
                        break;
                    case (KeyEvent.VK_S):
                        if (playerPos.y + player.getHeight() + PLAYER_SPEED >= this.getHeight())
                            playerPos.y = this.getHeight() - player.getHeight();
                        else
                            playerPos.y += PLAYER_SPEED;
                        break;
                    case (KeyEvent.VK_A):
                        if (playerPos.x - PLAYER_SPEED <= 0)
                            playerPos.x = 0;
                        else
                            playerPos.x -= PLAYER_SPEED;
                        break;
                    case (KeyEvent.VK_D):
                        if (playerPos.x + player.getWidth() + PLAYER_SPEED >= this.getWidth())
                            playerPos.x = this.getWidth() - player.getWidth();
                        else
                            playerPos.x += PLAYER_SPEED;
                        break;
                }
            }
            repaint();
            try
            {
                Thread.sleep(15);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
