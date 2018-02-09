package com.builtbroken.mc.core;

import com.builtbroken.mc.core.network.netty.PacketManager;
import net.minecraft.block.Block;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * Mod class for Voltz Engine that handles common loading
 *
 * @author Calclavia, DarkGuardsman
 */

@Deprecated
public class Engine
{
    protected static Logger logger = LogManager.getLogger("VoltzEngine");

    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    @Deprecated
    public static Block heatedStone;

    @Deprecated
    @GameRegistry.ObjectHolder("voltzengine:multiblock")
    public static Block multiBlock;

    @Deprecated
    public final static PacketManager packetHandler = new PacketManager(References.CHANNEL); //TODO move to internal packet calls

    public static Logger logger()
    {
        return logger;
    }

    public static boolean isJUnitTest()
    {
        //TODO do boolean flag from VoltzTestRunner to simplify solution
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list)
        {
            if (element.getClassName().startsWith("org.junit.") || element.getClassName().startsWith("com.builtbroken.mc.testing.junit.VoltzTestRunner"))
            {
                return true;
            }
        }
        return false;
    }
}
