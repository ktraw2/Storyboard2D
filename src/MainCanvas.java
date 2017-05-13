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

/**
 * Created by kevin on 5/11/17.
 */
public class MainCanvas extends Canvas implements KeyListener, Runnable {

    Thread runThread;
    //final String[] LEVEL_FILE_NAMES = new String[]{"menu", "testlevel"};
    int currentLevel = 0;
    boolean levelXMLLoaded = false;
    Document levesDOM;
    Element root;
    Point playerPos = new Point(this.getWidth() / 2, this.getHeight() / 2);

    void changeLevel(int newLevel)
    {
        currentLevel = newLevel;
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
        if (levelXMLLoaded)
        {
            BufferedImage background = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            try
            {
                //find the node for the current levesDOM in the DOM
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
                    if (backgroundNode != null)
                    {
                        //get the path to the background image from the DOM then use the built in ImageIO to read it into the background
                        File backgroundImageFile = new File("res/images/" + backgroundNode.getAttributes().getNamedItem("res").getNodeValue());
                        background = ImageIO.read(backgroundImageFile);
                        g.drawImage(background, 0, 0, this);
                    }
                }
            }
            catch (Exception e)
            {
                //e.printStackTrace();
            }
            //draw player if not in the menu
            if (currentLevel != 0)
            {
                try
                {
                    BufferedImage player = new BufferedImage(103, 119, BufferedImage.TYPE_INT_ARGB);
                    player = ImageIO.read(new File("res/images/sprite.jpg"));
                    g.drawImage(player, playerPos.x, playerPos.y, this);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode())
        {
            case (KeyEvent.VK_SPACE):
                if (currentLevel == 0)
                {
                    changeLevel(1);
                }
                break;
            case (KeyEvent.VK_W):
                playerPos.y -= 2;
                break;
            case (KeyEvent.VK_S):
                playerPos.y += 2;
                break;
            case (KeyEvent.VK_A):
                playerPos.x -= 2;
                break;
            case (KeyEvent.VK_D):
                playerPos.x += 2;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        while (true)
        {
            if (!levelXMLLoaded)
            {
                //System.out.println(System.getProperty("user.dir") + File.separator + System.getProperty("sun.java.command") .substring(0, System.getProperty("sun.java.command").lastIndexOf(".")) .replace(".", File.separator));
                File file = new File("res/levels.xml");

                try
                {
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

            repaint();
            try
            {
                Thread.sleep(60);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
