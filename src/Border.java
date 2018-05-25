import java.awt.*;
import java.awt.image.ImageObserver;

public class Border extends Drawable {

    public Border(int x, int y, int width, int height)
    {
        super(x, y, width, height);
    }

    public void drawRegular(Graphics g, ImageObserver observer)
    {
        g.drawRect(x, y, customWidth, customHeight);
    }

}
