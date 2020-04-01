package icbm.classic.lib.transform.rotation;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.jlib.data.vector.ITransform;
import icbm.classic.lib.transform.vector.Pos;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 2/9/2018.
 */
public class Quaternion implements ITransform
{
    double x = 1D;
    double y = 0D;
    double z = 0D;
    double w = 0D;

    public Quaternion()
    {
    }

    public Quaternion(Quaternion Quaternion)
    {
        x = Quaternion.x;
        y = Quaternion.y;
        z = Quaternion.z;
        w = Quaternion.w;
    }

    /**
     * Creates a Quaternion based on an angle and an axis.
     *
     * @param angle - Radians
     * @param axis  - Axis to rotate around
     */
    public Quaternion(double angle, IPos3D axis)
    {
        setAroundAxis(axis.x(), axis.y(), axis.z(), angle);
    }

    public Quaternion(double d, double d1, double d2, double d3)
    {
        x = d1;
        y = d2;
        z = d3;
        w = d;
    }

    public Quaternion set(Quaternion q)
    {
        x = q.x;
        y = q.y;
        z = q.z;
        w = q.w;
        return this;
    }

    public Quaternion set(double d, double d1, double d2, double d3)
    {
        x = d1;
        y = d2;
        z = d3;
        w = d;
        return this;
    }

    public Quaternion setAroundAxis(double ax, double ay, double az, double originalAngle)
    {
        double angle = originalAngle;
        angle *= 0.5;
        double d4 = Math.sin(angle);
        return set(Math.cos(angle), ax * d4, ay * d4, az * d4);
    }

    public Quaternion setAroundAxis(IPos3D axis, double angle)
    {
        return setAroundAxis(axis.x(), axis.y(), axis.z(), angle);
    }

    public Quaternion multiply(Quaternion quaternion)
    {
        double d = w * quaternion.w - x * quaternion.x - y * quaternion.y - z * quaternion.z;
        double d1 = w * quaternion.x + x * quaternion.w - y * quaternion.z + z * quaternion.y;
        double d2 = w * quaternion.y + x * quaternion.z + y * quaternion.w - z * quaternion.x;
        double d3 = w * quaternion.z - x * quaternion.y + y * quaternion.x + z * quaternion.w;
        w = d;
        x = d1;
        y = d2;
        z = d3;
        return this;
    }

    public Quaternion rightMultiply(Quaternion quaternion)
    {
        double d = w * quaternion.w - x * quaternion.x - y * quaternion.y - z * quaternion.z;
        double d1 = w * quaternion.x + x * quaternion.w + y * quaternion.z - z * quaternion.y;
        double d2 = w * quaternion.y - x * quaternion.z + y * quaternion.w + z * quaternion.x;
        double d3 = w * quaternion.z + x * quaternion.y - y * quaternion.x + z * quaternion.w;
        w = d;
        x = d1;
        y = d2;
        z = d3;
        return this;
    }

    public double magnitude()
    {
        return Math.sqrt(x * x + y * y + z * z + w * w);
    }

    public Quaternion normalize()
    {
        double d = magnitude();
        if (d != 0)
        {
            d = 1 / d;
            x *= d;
            y *= d;
            z *= d;
            w *= d;
        }
        return this;
    }

    @Override
    public Quaternion clone()
    {
        return new Quaternion(this);
    }

    @Override
    public Pos transform(IPos3D vector)
    {
        double d = -x * vector.x() - y * vector.y() - z * vector.z();
        double d1 = w * vector.x() + y * vector.z() - z * vector.y();
        double d2 = w * vector.y() - x * vector.z() + z * vector.x();
        double d3 = w * vector.z() + x * vector.y() - y * vector.x();
        return new Pos(d1 * w - d * x - d2 * z + d3 * y, d2 * w - d * y + d1 * z - d3 * x, d3 * w - d * z - d1 * y + d2 * x);
    }

    @Override
    public String toString()
    {
        int precision = 4; // TODO see if this level of precision is required
        return "Quaternion[" + BigDecimal.valueOf(w).setScale(precision, RoundingMode.HALF_UP) + ", " + BigDecimal.valueOf(x).setScale(precision, RoundingMode.HALF_UP) + ", " + BigDecimal.valueOf(y).setScale(precision, RoundingMode.HALF_UP) + ", " + BigDecimal.valueOf(z).setScale(precision, RoundingMode.HALF_UP) + "]";
    }
}
