package icbm.classic.mods.cubicchunks;

import icbm.classic.ICBMClassic;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Loader;

import java.lang.reflect.Method;

public class CubicChunks {

    public static CubicChunks INSTANCE = new CubicChunks();
    boolean present = false;

    Method getMinHeightMethod;
    Method getMaxHeightMethod;

    @SuppressWarnings("JavaReflectionMemberAccess")
    private CubicChunks() {
        ICBMClassic.logger().info("ICBMClassic>>CubicChunks: Looking for CubicChunks");
        if (!Loader.isModLoaded("cubicchunks")) {
            ICBMClassic.logger().info("ICBMClassic>>CubicChunks: Didn't find CubicChunks");
            return;
        }

        ICBMClassic.logger().info("ICBMClassic>>CubicChunks: Found CubicChunks");
        try {
            getMinHeightMethod = World.class.getMethod("getMinHeight");
            getMaxHeightMethod = World.class.getMethod("getMaxHeight");
            present = true;
        } catch (Exception e) {
            ICBMClassic.logger().error("ICBMClassic>>CubicChunks: Failed to hook CubicChunks");
            e.printStackTrace();
        }
    }

    public boolean isPresent() {
        return present;
    }

    public int getMinHeight(World world) {
        try {
            return (int) getMinHeightMethod.invoke(world);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getMaxHeight(World world) {
        try {
            return (int) getMaxHeightMethod.invoke(world);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 256;
    }

}
