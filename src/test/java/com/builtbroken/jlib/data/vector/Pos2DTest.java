package com.builtbroken.jlib.data.vector;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class TestPos extends Pos2D<TestPos> implements IPos2D
{

    TestPos(double x, double y)
    {
        super(x, y);
    }

    TestPos()
    {
        super();
    }

    @Override
    public TestPos newPos(double x, double y)
    {
        return new TestPos(x, y);
    }
}

class Pos2DTest
{
    TestPos getNewPos2D()
    {
        return new TestPos();
    }

    TestPos getNewPos2D(double x, double y)
    {
        return new TestPos(x, y);
    }

    @Test
    void blankInstanceValueTest()
    {
        Pos2D instance = getNewPos2D();
        Assertions.assertEquals(0d, instance.x());
        Assertions.assertEquals(0d, instance.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void createNewInstance1(int signa, int signb)
    {
        double posX = 15.54d * signa;
        double posY = 123.14d * signb;

        Pos2D instance = getNewPos2D(posX, posY);

        Assertions.assertEquals(posX, instance.x());
        Assertions.assertEquals((float) posX, instance.xf());
        Assertions.assertEquals((int) Math.floor(posX), instance.xi());

        Assertions.assertEquals(posY, instance.y());
        Assertions.assertEquals((float) posY, instance.yf());
        Assertions.assertEquals((int) Math.floor(posY), instance.yi());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class) // okay so here is how the tests are designed
    void addPos(int signa, int signb) // signa and signb are either -1, 0 or 1
    {
        double posXa = 15.54d * signa; // these signs are multiplied with a random double value to make sure bad logic fails
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        IPos2D instance2 = getNewPos2D(posXb, posYb);

        Pos2D instance3 = instance.add(instance2);

        Assertions.assertEquals(posXa + posXb, instance3.x());
        Assertions.assertEquals(posYa + posYb, instance3.y());

    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void addDoubleDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2 = instance.add(posXb, posYb);

        Assertions.assertEquals(posXa + posXb, instance2.x());
        Assertions.assertEquals(posYa + posYb, instance2.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void addDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2x = instance.add(posXb);
        Pos2D instance2y = instance.add(posYb);

        Assertions.assertEquals(posXa + posXb, instance2x.x());
        Assertions.assertEquals(posYa + posXb, instance2x.y());

        Assertions.assertEquals(posXa + posYb, instance2y.x());
        Assertions.assertEquals(posYa + posYb, instance2y.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void subPos(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        IPos2D instance2 = getNewPos2D(posXb, posYb);

        Pos2D instance3 = instance.sub(instance2);

        Assertions.assertEquals(posXa - posXb, instance3.x());
        Assertions.assertEquals(posYa - posYb, instance3.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void subDoubleDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2 = instance.sub(posXb, posYb);

        Assertions.assertEquals(posXa - posXb, instance2.x());
        Assertions.assertEquals(posYa - posYb, instance2.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void subDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2x = instance.sub(posXb);
        Pos2D instance2y = instance.sub(posYb);

        Assertions.assertEquals(posXa - posXb, instance2x.x());
        Assertions.assertEquals(posYa - posXb, instance2x.y());

        Assertions.assertEquals(posXa - posYb, instance2y.x());
        Assertions.assertEquals(posYa - posYb, instance2y.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void multiplyPos(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        IPos2D instance2a = getNewPos2D(posXb, posYb);

        Pos2D instance2b = instance.multiply(instance2a);

        Assertions.assertEquals(posXa * posXb, instance2b.x());
        Assertions.assertEquals(posYa * posYb, instance2b.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void multiplyDoubleDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2a = instance.multiply(posXb, posYb);


        Assertions.assertEquals(posXa * posXb, instance2a.x());
        Assertions.assertEquals(posYa * posYb, instance2a.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void multiplyDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2a = instance.multiply(posXb);
        Pos2D instance2b = instance.multiply(posYb);


        Assertions.assertEquals(posXa * posXb, instance2a.x());
        Assertions.assertEquals(posYa * posXb, instance2a.y());
        Assertions.assertEquals(posXa * posYb, instance2b.x());
        Assertions.assertEquals(posYa * posYb, instance2b.y());
    }


    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void dividePos(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        IPos2D instance2a = getNewPos2D(posXb, posYb);

        Pos2D instance2b = instance.divide(instance2a);

        Assertions.assertEquals(posXa / posXb, instance2b.x());
        Assertions.assertEquals(posYa / posYb, instance2b.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void divideDoubleDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;

        Pos2D instance2a = instance.divide(posXb, posYb);


        Assertions.assertEquals(posXa / posXb, instance2a.x());
        Assertions.assertEquals(posYa / posYb, instance2a.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void divideDouble(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2a = instance.divide(posXb);
        Pos2D instance2b = instance.divide(posYb);


        Assertions.assertEquals(posXa / posXb, instance2a.x());
        Assertions.assertEquals(posYa / posXb, instance2a.y());
        Assertions.assertEquals(posXa / posYb, instance2b.x());
        Assertions.assertEquals(posYa / posYb, instance2b.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void rotate(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double angle = signb * 10.39735d;

        Pos2D res = instance.rotate(angle);

        double resX = instance.x() * Math.cos(angle) - instance.y() * Math.sin(angle);
        double resY = instance.x() * Math.sin(angle) + instance.y() * Math.cos(angle);

        Assertions.assertEquals(resX, res.x());
        Assertions.assertEquals(resY, res.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void dotProduct(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);
        double res = instance2.dotProduct((IPos2D) instance);
        double res2 = instance.dotProduct((IPos2D) instance2);

        Assertions.assertEquals(posXa * posXb + posYa * posYb, res);
        Assertions.assertEquals(posXa * posXb + posYa * posYb, res2);
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void magnitudeSquared(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        double m = instance.magnitudeSquared();

        Assertions.assertEquals(posYa * posYa + posXa * posXa, m);
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void magnitude(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        double m = instance.magnitude();

        Assertions.assertEquals(Math.sqrt(posYa * posYa + posXa * posXa), m);
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void normalize(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        Pos2D normal = instance.normalize();

        Assertions.assertTrue(normal.x() <= 1d && normal.x() >= -1d);
        Assertions.assertTrue(normal.y() <= 1d && normal.y() >= -1d);

        if (signa != 0 || signb != 0) // skip checks for when its a zero vector
        {
            double len = Math.sqrt(posXa * posXa + posYa * posYa);

            Assertions.assertEquals(posXa / len, normal.x());
            Assertions.assertEquals(posYa / len, normal.y());
        }
    }

    @Test
    void normalizeZeroTest()
    {
        Pos2D instance = getNewPos2D(0, 0);
        Pos2D normal = instance.normalize();

        Assertions.assertEquals(0, normal.x());
        Assertions.assertEquals(0, normal.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void distance(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);
        Pos2D delta = instance.sub((IPos2D) instance2);

        double dist = instance.distance((IPos2D) instance2);
        double dist2 = instance2.distance((IPos2D) instance);

        Assertions.assertEquals(dist, dist2);

        Assertions.assertEquals((posXb - posXa) * (posXb - posXa), delta.x() * delta.x());
        Assertions.assertEquals((posYb - posYa) * (posYb - posYa), delta.y() * delta.y());

    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void midpoint(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);
        Pos2D res = instance.midpoint((IPos2D) instance2);
        Pos2D res2 = instance2.midpoint((IPos2D) instance);

        Assertions.assertEquals((posXa + posXb) / 2, res.x());
        Assertions.assertEquals((posYa + posYb) / 2, res.y());

        Assertions.assertEquals((posXa + posXb) / 2, res2.x());
        Assertions.assertEquals((posYa + posYb) / 2, res2.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void isZero(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        boolean zero = instance.isZero();
        Assertions.assertEquals(posXa == 0 && posYa == 0, zero);
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void slope(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);


        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);
        double res = instance2.slope((IPos2D) instance);
        double res2 = instance.slope((IPos2D) instance2);

        Assertions.assertEquals((posYa - posYb) / (posXa - posXb), res);
        Assertions.assertEquals((posYa - posYb) / (posXa - posXb), res2);
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void round(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        Pos2D res = instance.round();

        Assertions.assertEquals(Math.round(posXa), res.x());
        Assertions.assertEquals(Math.round(posYa), res.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void ceil(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        Pos2D res = instance.ceil();

        Assertions.assertEquals(Math.ceil(posXa), res.x());
        Assertions.assertEquals(Math.ceil(posYa), res.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void floor(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        Pos2D res = instance.floor();

        Assertions.assertEquals(Math.floor(posXa), res.x());
        Assertions.assertEquals(Math.floor(posYa), res.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void max(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);

        Pos2D res = instance.max((IPos2D) instance2);
        Pos2D res2 = instance2.max((IPos2D) instance);


        Assertions.assertEquals(Math.max(posXa, posXb), res.x());
        Assertions.assertEquals(Math.max(posYa, posYb), res.y());
        Assertions.assertEquals(Math.max(posXa, posXb), res2.x());
        Assertions.assertEquals(Math.max(posYa, posYb), res2.y());
    }

    @ParameterizedTest
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void min(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signa;

        Pos2D instance = getNewPos2D(posXa, posYa);

        double posXb = 1095.156744d * signb;
        double posYb = -23.157456d * signb;
        Pos2D instance2 = getNewPos2D(posXb, posYb);

        Pos2D res = instance.min((IPos2D) instance2);
        Pos2D res2 = instance2.min((IPos2D) instance);


        Assertions.assertEquals(Math.min(posXa, posXb), res.x());
        Assertions.assertEquals(Math.min(posYa, posYb), res.y());
        Assertions.assertEquals(Math.min(posXa, posXb), res2.x());
        Assertions.assertEquals(Math.min(posYa, posYb), res2.y());
    }

    @ParameterizedTest()
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void reciprocal(int signa, int signb)
    {
        double posXa = 15.54d * signa;
        double posYa = 123.14d * signb;

        Pos2D instance = getNewPos2D(posXa, posYa);
        Pos2D reciprocal = instance.reciprocal();

        Assertions.assertEquals(1d / posXa, reciprocal.x());
        Assertions.assertEquals(1d / posYa, reciprocal.y());
    }

    @ParameterizedTest()
    @ArgumentsSource(Pos2DTestArgProvider2Signs.class)
    void duplicateVector(int signa, int signb)
    {
        double posX = 15.54d * signa;
        double posY = 123.14d * signb;

        Pos2D instance = getNewPos2D(posX, posY);
        Pos2D instanceb = instance.clone();

        Assertions.assertEquals(posX, instanceb.x());
        Assertions.assertEquals((float) posX, instanceb.xf());
        Assertions.assertEquals((int) Math.floor(posX), instanceb.xi());

        Assertions.assertEquals(posY, instanceb.y());
        Assertions.assertEquals((float) posY, instanceb.yf());
        Assertions.assertEquals((int) Math.floor(posY), instanceb.yi());
    }

    public static class Pos2DTestArgProvider2Signs implements ArgumentsProvider
    {
        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext extensionContext)
        {
            final List<Arguments> list = new ArrayList<>();
            for (int signa : new int[]{-1,0,1})
            {
                for (int signb : new int[]{-1,0,1})
                {
                    //Arguments: sign1, sign2
                    list.add(Arguments.of(signa, signb));
                }
            }

            return list.stream();
        }
    }
}