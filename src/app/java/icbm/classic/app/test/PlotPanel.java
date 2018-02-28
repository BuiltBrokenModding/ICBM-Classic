package icbm.classic.app.test;


import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple panel used to draw 2D plot points
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/26/2018.
 */
public class PlotPanel extends JPanel
{
    /** Data to display in the panel */
    List<PlotPoint> data = null;
    /** Spacing from each side */
    int PAD = 20;

    int plotSizeX = -1;
    int plotSizeY = -1;

    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

        //Get component size
        int w = getWidth();
        int h = getHeight();

        // Draw ordinate.
        g2.draw(new Line2D.Double(PAD, PAD, PAD, h - PAD));

        // Draw abcissa.
        g2.draw(new Line2D.Double(PAD, h - PAD, w - PAD, h - PAD));


        if (data != null && !data.isEmpty())
        {
            //Calculate scale to fit display
            double scaleX = getScaleX();
            double scaleY = getScaleY();

            //Render data points
            for (PlotPoint pos : data)
            {

                //Get pixel position
                double x = PAD + scaleX * pos.x();
                double y = h - PAD - scaleY * pos.y();

                if(x >= 0 && x <= w && y <= h)
                {
                    //Set color
                    g2.setPaint(pos.color != null ? pos.color : Color.red);

                    //Draw
                    g2.fill(new Ellipse2D.Double(x - (pos.size / 2), y - (pos.size / 2), pos.size, pos.size));
                }
            }
        }
    }

    /**
     * Scale to draw the data on the screen.
     * <p>
     * Modifies the position to correspond to the pixel location
     *
     * @return scale of view ((width - padding) / size)
     */
    protected double getScaleX()
    {
        return (double) (getWidth() - 2 * PAD) / (plotSizeX > 0 ? plotSizeX : getMaxY());
    }

    /**
     * Scale to draw the data on the screen.
     * <p>
     * Modifies the position to correspond to the pixel location
     *
     * @return scale of view ((width - padding) / size)
     */
    protected double getScaleY()
    {
        return (double) (getHeight() - 2 * PAD) / (plotSizeY > 0 ? plotSizeY : getMaxY());
    }

    /**
     * Max y value in the data set
     *
     * @return
     */
    private double getMaxY()
    {
        double max = -Integer.MAX_VALUE;
        for (PlotPoint pos : data)
        {
            if (pos.y() > max)
            {
                max = pos.y();
            }
        }
        return max;
    }

    /**
     * Max x value in the data set
     *
     * @return
     */
    private double getMaxX()
    {
        double max = -Integer.MAX_VALUE;
        for (PlotPoint pos : data)
        {
            if (pos.x() > max)
            {
                max = pos.x();
            }
        }
        return max;
    }

    /**
     * Sets the plot size of the display.
     * <p>
     * By default the display will auto scale to match the data.
     * This can be used to ensure the data scales to a defined value.
     *
     * @param x
     * @param y
     */
    public void setPlotSize(int x, int y)
    {
        this.plotSizeY = y;
        this.plotSizeX = x;
    }

    /**
     * Sets the data to draw
     *
     * @param data
     */
    public void setData(List<PlotPoint> data)
    {
        this.data = data;
    }

    /**
     * Adds data to the display
     *
     * @param data
     */
    public void addData(List<PlotPoint> data)
    {
        if (this.data == null)
        {
            this.data = new ArrayList();
        }
        this.data.addAll(data);
    }
}