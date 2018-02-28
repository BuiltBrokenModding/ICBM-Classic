package icbm.classic.app.test;

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
    public static final String COMMAND_CLEAR = "clear";

    PlotPanel plotPanel;
    JTextField distanceField;

    JTextField minHeightField;
    JTextField maxHeightField;

    JTextField heightInitField;
    JTextField heightScaleField;

    JTextField plotSizeXField;
    JTextField plotSizeYField;

    Label maxHeightLabel;
    Label flightTimeLabel;
    Label accelerationLabel;
    Label motionYLabel;
    Label motionXLabel;
    Label motionLabel;
    Label maxYLabel;
    Label arcDistanceLabel;

    public FramePathTest()
    {
        //Set frame properties
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setMinimumSize(new Dimension(800, 800));
        setLocation(200, 200);
        setTitle("Missile Path Visualizer");

        //Output data
        add(buildEastSection(), BorderLayout.EAST);

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
        controlPanel.setLayout(new GridLayout(0, 2));

        //Spacer
        controlPanel.add(new JLabel("Path Variables"));
        controlPanel.add(new JPanel());

        //Distance field
        controlPanel.add(new Label("Distance"));
        controlPanel.add(distanceField = new JTextField(6));
        distanceField.setText(200 + "");

        //Calculate button
        controlPanel.add(new JPanel());
        JButton calculateButton = new JButton("Calculate");
        calculateButton.setActionCommand(COMMAND_CALCULATE);
        calculateButton.addActionListener(this);
        controlPanel.add(calculateButton);

        //Spacer
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //---------------------------------------------------------------

        //Spacer
        controlPanel.add(new JLabel("Path Constants"));
        controlPanel.add(new JPanel());

        //Min Height field
        controlPanel.add(new Label("Min Height"));
        controlPanel.add(minHeightField = new JTextField(6));
        minHeightField.setText(100 + "");

        //Max Height field
        controlPanel.add(new Label("Max Height"));
        controlPanel.add(maxHeightField = new JTextField(6));
        maxHeightField.setText(1000 + "");

        //Height Init field
        controlPanel.add(new Label("Height Init"));
        controlPanel.add(heightInitField = new JTextField(6));
        heightInitField.setText(160 + "");

        //Height Scale field
        controlPanel.add(new Label("Height Scale"));
        controlPanel.add(heightScaleField = new JTextField(6));
        heightScaleField.setText(3 + "");

        //Spacer
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //---------------------------------------------------------------

        //Spacer
        controlPanel.add(new JLabel("Draw Options"));
        controlPanel.add(new JPanel());

        //Plot size fields
        controlPanel.add(new Label("Plot Size X"));
        controlPanel.add(plotSizeXField = new JTextField(6));
        plotSizeXField.setText(-1 + "");

        controlPanel.add(new Label("Plot Size Y"));
        controlPanel.add(plotSizeYField = new JTextField(6));
        plotSizeYField.setText(-1 + "");

        //Spacer
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //---------------------------------------------------------------


        //Calculate button
        controlPanel.add(new JPanel());
        JButton clearButton = new JButton("Clear Display");
        clearButton.setActionCommand(COMMAND_CLEAR);
        clearButton.addActionListener(this);
        controlPanel.add(clearButton);

        //Add and return
        westPanel.add(controlPanel);
        return westPanel;
    }

    protected JPanel buildEastSection()
    {
        JPanel westPanel = new JPanel();

        //Controls
        JPanel controlPanel = new JPanel();
        controlPanel.setLayout(new GridLayout(11, 2));

        //Header
        controlPanel.add(new Label("Field"));
        controlPanel.add(new Label("Value"));

        //Max Height
        controlPanel.add(new Label("Max_Height"));
        controlPanel.add(maxHeightLabel = new Label("--m"));

        //Flight Time
        controlPanel.add(new Label("Flight Time"));
        controlPanel.add(flightTimeLabel = new Label("-- ticks"));

        //Acceleration
        controlPanel.add(new Label("Acceleration"));
        controlPanel.add(accelerationLabel = new Label("--m/tick"));

        //Spacer
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //Motion Y
        controlPanel.add(new Label("Motion Y"));
        controlPanel.add(motionYLabel = new Label("--m/tick"));

        //Motion X
        controlPanel.add(new Label("Motion X"));
        controlPanel.add(motionXLabel = new Label("--m/tick"));

        //Motion X & Y
        controlPanel.add(new Label("Motion"));
        controlPanel.add(motionLabel = new Label("--m/tick"));

        //Spacer
        controlPanel.add(new JPanel());
        controlPanel.add(new JPanel());

        //Max Y
        controlPanel.add(new Label("Max Y"));
        controlPanel.add(maxYLabel = new Label("--m"));

        //Arc Distance
        controlPanel.add(new Label("Approx Arc Length"));
        controlPanel.add(arcDistanceLabel = new Label("--m"));

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
                //Set plot size
                plotPanel.setPlotSize((int) Double.parseDouble(plotSizeXField.getText().trim()), (int) Double.parseDouble(plotSizeYField.getText().trim()));

                //Get data
                double distance = Double.parseDouble(distanceField.getText().trim());
                double heightInit = Double.parseDouble(heightInitField.getText().trim());
                double heightScale = Double.parseDouble(heightScaleField.getText().trim());

                //Draw data
                calculateData(0, distance, heightScale, heightInit, randomColor(), true);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else if (event.getActionCommand().equalsIgnoreCase(COMMAND_CLEAR))
        {
            //Clear data
            plotPanel.data = null;
        }

        plotPanel.repaint();
    }

    //Creates a random color for use
    private Color randomColor()
    {
        return new Color((int) (255f * Math.random()), (int) (255f * Math.random()), (int) (255f * Math.random()));
    }

    /**
     * Calculates the data points for the path from 0 to distance
     * <p>
     * Pulls distance from {@link #distanceField} and outputs data into
     * console. In addition this the function sets data into the display
     *
     * @return list of 2D data points (x, y)
     */
    public void calculateData(double start, double end, double height_scale, double height_init, Color color, boolean addToExistingData)
    //TODO break method down into sub methods and store all data values for display
    {
        //Constants derived from original equations
        final int ticksPerMeterFlat = 2;
        final int minFlightTime = 100;
        final double minHeight = Double.parseDouble(minHeightField.getText().trim());
        final double maxHeight = Double.parseDouble(maxHeightField.getText().trim());


        //Debug
        outputDebug("\n======================================"); //TODO replace debug called with debugger object
        outputDebug("==========Running Calculation=========");
        outputDebug("======================================");

        //Collects points to draw in display
        List<PlotPoint> data = new ArrayList();

        //Debug
        outputDebug("\tStart: " + start);
        outputDebug("\tEnd: " + end);

        //Calculate vector data
        double deltaX = end - start;
        double flat_distance = Math.abs(start - end);

        double max_height = Math.min(maxHeight, Math.max(minHeight, height_init + (flat_distance * height_scale)));
        double flight_time = ticksPerMeterFlat * flat_distance;


        double HD = max_height / flat_distance;
        double HT = max_height / flight_time;
        double TD = flight_time / flat_distance;
        double drag = ((max_height - HD) * HD) / (flight_time / TD) / (HT * flat_distance);//(float) (height_scale * ticksPerMeterFlat * (max_height / (flight_time * flight_time)));

        //Set data in display
        maxHeightLabel.setText(max_height + " m");
        flightTimeLabel.setText(flight_time + " ticks");
        accelerationLabel.setText(drag + " m/tick");

        //Debug
        outputDebug("----------------------------------");
        outputDebug("\tDistance: " + flat_distance);
        outputDebug("\tHeight: " + max_height);
        outputDebug("\tTime: " + flight_time);
        outputDebug("\tAcceleration: " + drag);
        outputDebug("----------------------------------");

        //Calculate vector for motion
        double my = drag * (flight_time / 2); //I think this is asking "how much speed to get to center of ark"
        double mx = deltaX / flight_time;

        //Output to display
        motionXLabel.setText(String.format("%.2f m/tick", mx));
        motionYLabel.setText(String.format("%.2f m/tick", my));

        //Calculate magnitude of motion
        double motion = Math.sqrt(mx * mx + my * my);
        motionLabel.setText(String.format("%.2f m/tick", motion));

        outputDebug("\tMotion X: " + mx);
        outputDebug("\tMotion Y: " + my);
        outputDebug("----------------------------------");

        outputDebug("\tData Points:");
        //Position
        double x = start;
        double y = 0;

        data.add(new PlotPoint(start, -1, color.darker().darker(), 10));
        data.add(new PlotPoint(end, -1, color, 10));

        //Loop until position is at ground
        for (int tick = 0; tick < flight_time * 2 && y >= 0; tick++)
        {
            //Add position to data
            data.add(new PlotPoint(x, y, color));
            outputDebug(String.format("\t\tT[%d]: %10.3fx %10.3fy %10.3fmx %10.3fmy", tick, x, y, mx, my));
            if (tick % 5 == 0)
            {
                outputDebug("");
            }

            //Move position by motion
            x += mx;
            y += my;

            //Decrease upward motion
            my -= drag;
        }

        outputDebug("----------------------------------");

        //Calc distance traveled
        //http://tutorial.math.lamar.edu/Classes/CalcII/ArcLength.aspx
        double arc_distance = 0;

        for (int i = 0; i < data.size() - 1; i++)
        {
            PlotPoint p1 = data.get(i);
            PlotPoint p2 = data.get(i + 1);

            arc_distance += Math.sqrt(Math.pow(p2.x() - p1.x(), 2) + Math.pow(p2.y() - p1.y(), 2));
        }
        outputDebug("\tApprox Arc Distance: " + arc_distance);
        arcDistanceLabel.setText(String.format("%.2fm", arc_distance));

        double max_y = 0;
        for (PlotPoint pos : data)
        {
            if (pos.y() > max_y)
            {
                max_y = pos.y();
            }
        }
        outputDebug("\tMax Y: " + max_y);
        maxYLabel.setText(String.format("%.2fm", max_y));

        outputDebug("======================================\n");

        //Set data into display
        if (!addToExistingData)
        {
            plotPanel.setData(data);
        }
        else
        {
            plotPanel.addData(data);
        }
    }

    protected void outputDebug(String msg)
    {
        System.out.println(msg);
    }
}
