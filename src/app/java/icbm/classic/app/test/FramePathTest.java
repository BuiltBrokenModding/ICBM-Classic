package icbm.classic.app.test;

import com.builtbroken.jlib.data.vector.Pos2DBean;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2018.
 */
public class FramePathTest extends JFrame implements ActionListener
{
    public static final String COMMAND_CALCULATE = "calculate";

    PlotPanel plotPanel;
    JTextField distanceField;

    public FramePathTest()
    {
        //Set frame properties
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setMinimumSize(new Dimension(800, 800));
        setLocation(200, 200);
        setTitle("Missile Path Visualizer");

        //Add plot panel to left side
        add(buildMainDisplay(), BorderLayout.CENTER);

        //Setup control panel (Contains input fields and action buttons)
        add(buildWestSection(), BorderLayout.WEST);

        //Debug panel TODO add later
        JPanel debugPanel = new JPanel();
        debugPanel.setMinimumSize(new Dimension(800, 200));
        add(debugPanel, BorderLayout.SOUTH);

        pack();
    }

    protected JPanel buildMainDisplay()
    {
        plotPanel = new PlotPanel();
        plotPanel.setMinimumSize(new Dimension(600, 600));
        return plotPanel;
    }

    protected JPanel buildWestSection()
    {
        JPanel westPanel = new JPanel();

        //Controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(4, 2));

        //Distance field
        controlPanel.add(new Label("Distance"));
        controlPanel.add(distanceField = new JTextField(4));
        distanceField.setText(200 + "");

        //Spacers
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //Spacers
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //Spacer
        controlPanel.add(new JPanel());

        //Calculate button
        JButton calculateButton = new JButton("Calculate");
        calculateButton.setActionCommand(COMMAND_CALCULATE);
        calculateButton.addActionListener(this);
        controlPanel.add(calculateButton);

        //Add and return
        westPanel.add(controlPanel);
        return westPanel;
    }

    @Override
    public void actionPerformed(ActionEvent event)
    {
        if (event.getActionCommand().equalsIgnoreCase(COMMAND_CALCULATE))
        {
            try
            {
                List<Pos2DBean> data = calculateData();
                plotPanel.setData(data);
                plotPanel.repaint();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * Calculates the data points for the path from 0 to distance
     * <p>
     * Pulls distance from {@link #distanceField} and outputs data into
     * console. In addition to returning data as a list for use in the display.
     *
     * @return list of 2D data points (x, y)
     */
    public List<Pos2DBean> calculateData() //TODO break method down into sub methods and store all data values for display
    {
        outputDebug("\n======================================"); //TODO replace debug called with debugger object
        outputDebug("==========Running Calculation=========");
        outputDebug("======================================");
        List<Pos2DBean> data = new ArrayList();

        //Starting data, TODO get from user input
        int start = 0;
        int end = Integer.parseInt(distanceField.getText().trim());

        //Debug
        outputDebug("\tStart: " + start);
        outputDebug("\tEnd: " + end);

        final int height_scale = 3;

        //Calculate vector data
        double deltaX = end - start;
        double flat_distance = Math.abs(start - end);
        double max_height = 160 + (flat_distance * height_scale);
        float flight_time = (float) (Math.max(100, 2 * flat_distance) - 2);
        float acceleration = (float) (max_height * 2) / (flight_time * flight_time);

        //Debug
        outputDebug("----------------------------------");
        outputDebug("\tDistance: " + flat_distance);
        outputDebug("\tHeight: " + max_height);
        outputDebug("\tTime: " + flight_time);
        outputDebug("\tAcceleration: " + acceleration);
        outputDebug("----------------------------------");

        //Calculate vector for motion
        float my = acceleration * (flight_time / 2);
        float mx = (float) (deltaX / flight_time);

        outputDebug("\tMotion X: " + mx);
        outputDebug("\tMotion Y: " + my);
        outputDebug("----------------------------------");

        outputDebug("\tData Points:");
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
            outputDebug(String.format("\t\tT[%d]: %10.3fx %10.3fy %10.3fmx %10.3fmy", tick, x, y, mx, my));
            if (tick % 5 == 0)
            {
                outputDebug("");
            }

            //Move position by motion
            x += mx;
            y += my;

            //Decrease upward motion
            my -= acceleration;
        }
        outputDebug("======================================\n");

        return data;
    }

    protected void outputDebug(String msg)
    {
        System.out.println(msg);
    }
}
