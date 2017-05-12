import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.*;

/**
 * Created by kevin on 5/11/17.
 */
public class MainCanvas extends Canvas implements KeyListener, Runnable {

    Thread runThread;
    final String[] LEVEL_FILE_NAMES = new String[]{"menu", "testlevel"};
    int currentLevel = 0;
    boolean currentLevelLoaded = false;
    Document level;
    Element root;

    void changeLevel(int newLevel)
    {
        currentLevel = newLevel;
        currentLevelLoaded = false;
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
        if (currentLevelLoaded)
        {
            BufferedImage background = new BufferedImage(d.width, d.height, BufferedImage.TYPE_INT_ARGB);
            try
            {
                //get the path to the background image from the DOM then use the built in ImageIO to read it into the background
                File backgroundImageFile = new File("res/images/" + root.getElementsByTagName("background").item(0).getAttributes().getNamedItem("res").getNodeValue());
                background = ImageIO.read(backgroundImageFile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
            g.drawImage(background, 0, 0, this);
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
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void run() {
        while (true)
        {
            if (!currentLevelLoaded)
            {
                //System.out.println(System.getProperty("user.dir") + File.separator + System.getProperty("sun.java.command") .substring(0, System.getProperty("sun.java.command").lastIndexOf(".")) .replace(".", File.separator));
                File file = new File("res/levels/" + LEVEL_FILE_NAMES[currentLevel] + ".xml");

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
                    level = documentBuilder.parse(byteArrayInputStream);
                    root = level.getDocumentElement();
                    currentLevelLoaded = true;
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
