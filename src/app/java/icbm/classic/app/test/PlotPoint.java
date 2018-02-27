package icbm.classic.app.test;

import com.builtbroken.jlib.data.vector.IPos2D;

import java.awt.*;

/**
 * 2D data point with color
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/27/2018.
 */
public class PlotPoint implements IPos2D
{
    double x;
    double y;
    Color color;


    public PlotPoint(double x, double y, Color color)
    {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    @Override
    public double x()
    {
        return x;
    }

    @Override
    public double y()
    {
        return y;
    }
}
