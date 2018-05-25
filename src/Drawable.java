import java.awt.*;
import java.awt.image.ImageObserver;

abstract class Drawable {
    int x;
    int y;
    int customWidth;
    int customHeight;

    public Drawable()
    {
        return;
    }

    public Drawable(int x, int y, int customWidth, int customHeight)
    {
        this.x = x;
        this.y = y;
        this.customWidth = customWidth;
        this.customHeight = customHeight;
    }

    public Drawable(int customWidth, int customHeight)
    {
        this.customWidth = customWidth;
        this.customHeight = customHeight;
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

    abstract public void drawRegular(Graphics g, ImageObserver observer);

}
