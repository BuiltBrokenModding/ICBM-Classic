package icbm.classic.app.test;


import com.builtbroken.jlib.data.vector.Pos2DBean;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.List;

public class PlotPanel extends JPanel
{
    List<Pos2DBean> data = null;
    int PAD = 20;

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


        if(data != null && !data.isEmpty())
        {
            //Calculate scale to fit display
            double scaleX = (double) (w - 2 * PAD) / getMaxX();
            double scaleY = (double) (h - 2 * PAD) / getMaxY();

            //Set color
            g2.setPaint(Color.red);

            //Render data points
            for (Pos2DBean pos : data)
            {
                double x = w - PAD - scaleX * pos.x();
                double y = h - PAD - scaleY * pos.y();
                g2.fill(new Ellipse2D.Double(x - 2, y - 2, 4, 4));
            }
        }
    }

    private double getMaxY()
    {
        double max = -Integer.MAX_VALUE;
        for (Pos2DBean pos : data)
        {
            if (pos.y() > max)
            {
                max = pos.y();
            }
        }
        return max;
    }

    private double getMaxX()
    {
        double max = -Integer.MAX_VALUE;
        for (Pos2DBean pos : data)
        {
            if (pos.x() > max)
            {
                max = pos.x();
            }
        }
        return max;
    }


    public void setData(List<Pos2DBean> data)
    {
        this.data = data;
    }
}