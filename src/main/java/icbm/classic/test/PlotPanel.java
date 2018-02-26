package icbm.classic.test;


import com.builtbroken.jlib.data.vector.Pos2DBean;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;

public class PlotPanel extends JPanel
{
    List<Pos2DBean> data;
    final int PAD = 20;

    public PlotPanel(List<Pos2DBean> data)
    {
        this.data = data;
    }

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

    public static void main(String[] args)
    {
        List<Pos2DBean> data = new ArrayList();

        //Starting data, TODO get from user input
        int start = 0;
        int end = 200;

        //Debug
        System.out.println("Start: " + start);
        System.out.println("End: " + end);

        final int height_scale = 3;

        //Calculate vector data
        double deltaX = end - start;
        double flat_distance = Math.abs(start - end);
        double max_height = 160 + (flat_distance * height_scale);
        float flight_time = (float) (Math.max(100, 2 * flat_distance) - 2);
        float acceleration = (float) (max_height * 2) / (flight_time * flight_time);

        //Debug
        System.out.println("----------------------------------");
        System.out.println("Distance: " + flat_distance);
        System.out.println("Height: " + max_height);
        System.out.println("Time: " + flight_time);
        System.out.println("Acceleration: " + acceleration);
        System.out.println("----------------------------------");

        //Calculate vector for motion
        float my = acceleration * (flight_time / 2);
        float mx = (float) (deltaX / flight_time);

        System.out.println("Motion X: " + mx);
        System.out.println("Motion Y: " + my);
        System.out.println("----------------------------------");

        //Position
        double x = start;
        double y = 0;


        //Loop until position is at ground
        int tick = 0;
        while (y >= 0)
        {
            tick++;
            //Add position to data
            data.add(new Pos2DBean(x, y));
            System.out.println(String.format("T[%d]: %10.3fx %10.3fy %10.3fmx %10.3fmy", tick, x, y, mx, my));
            if (tick % 5 == 0)
            {
                System.out.println();
            }

            //Move position by motion
            x += mx;
            y += my;

            //Decrease upward motion
            my -= acceleration;
        }


        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PlotPanel(data));
        frame.setSize(800, 800);
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }
}