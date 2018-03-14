package icbm.classic.content.blocks;

import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/19/2017.
 */
public class BlockWrapper extends Block
{
    public static int suppressErrorLimit = 5;
    public static HashMap<Block, List<Exception>> textureErrors = new HashMap();
    public static HashMap<Block, List<Exception>> otherErrors = new HashMap();

    public final Block realBlock;

    /** Bitmask **/
    private byte renderSides = 0;

    public BlockWrapper(Block block)
    {
        super(block.getMaterial());
        realBlock = block;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int colorMultiplier(IBlockAccess world, int x, int y, int z)
    {
        if (otherErrors.get(realBlock) == null || otherErrors.get(realBlock).size() < suppressErrorLimit)
        {
            try
            {
                return realBlock.colorMultiplier(world, x, y, z);
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("BlockWrapper#colorMultiplier" + world + ", " + x + "," + y + "," + z
                        + ") - Unexpected error while getting color for block '" + InventoryUtility.getRegistryName(realBlock) + "'", e);
                addOtherError(e);
            }
        }
        return super.colorMultiplier(world, x, y, z);
    }

    @Override
    public int getLightValue()
    {
        return realBlock.getLightValue();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockAccess world, int x, int y, int z, int side)
    {
        if (canRenderSide(side))
        {
            if (otherErrors.get(realBlock) == null || otherErrors.get(realBlock).size() < suppressErrorLimit)
            {
                try
                {
                    return realBlock.shouldSideBeRendered(world, x, y, z, side);

                }
                catch (Exception e)
                {
                    ICBMClassic.INSTANCE.logger().error("BlockWrapper#shouldSideBeRendered" + world + ", " + x + "," + y + "," + z + ", " + side
                            + ") - Unexpected error while checking if side could render for block '" + InventoryUtility.getRegistryName(realBlock) + "'", e);
                    addOtherError(e);
                }
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z)
    {
        if (otherErrors.get(realBlock) == null || otherErrors.get(realBlock).size() < suppressErrorLimit)
        {
            try
            {
                return realBlock.getMixedBrightnessForBlock(world, x, y, z);
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("BlockWrapper#getMixedBrightnessForBlock(" + world + ", " + x + "," + y + "," + z
                        + ") - Unexpected error while getting brightness for block '" + InventoryUtility.getRegistryName(realBlock) + "'", e);
                addOtherError(e);
            }
        }
        return super.getMixedBrightnessForBlock(world, x, y, z);
    }

    protected void addOtherError(Exception e)
    {
        if (otherErrors.get(realBlock) == null)
        {
            otherErrors.put(realBlock, new ArrayList());
        }
        otherErrors.get(realBlock).add(e);
        if (otherErrors.get(realBlock).size() >= suppressErrorLimit)
        {
            ICBMClassic.INSTANCE.logger().error("BlockWrapper - suppressing additional errors from this block");
        }
    }

    protected void addTextureError(Exception e)
    {
        if (textureErrors.get(realBlock) == null)
        {
            textureErrors.put(realBlock, new ArrayList());
        }
        textureErrors.get(realBlock).add(e);
        if (textureErrors.get(realBlock).size() >= suppressErrorLimit)
        {
            ICBMClassic.INSTANCE.logger().error("BlockWrapper - suppressing additional errors from this block");
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public float getAmbientOcclusionLightValue()
    {
        return realBlock.getAmbientOcclusionLightValue();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean getCanBlockGrass()
    {
        return realBlock.getCanBlockGrass();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side)
    {
        if (textureErrors.get(realBlock) == null || textureErrors.get(realBlock).size() < suppressErrorLimit)
        {
            try
            {
                IIcon icon = realBlock.getIcon(world, x, y, z, side);
                if (icon == null)
                {
                    //visual error noting null icon
                    return Blocks.wool.getIcon(0, side);
                }
                return icon;
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("BlockWrapper#getIcon(" + world + ", " + x + "," + y + "," + z + "," + side
                        + ") - Unexpected error while getting icon for side on block '" + InventoryUtility.getRegistryName(realBlock) + "'", e);
                addTextureError(e);
            }
        }
        //Visual error noting failed to get icon for block
        return Blocks.stained_hardened_clay.getIcon(0, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta)
    {
        if (textureErrors.get(realBlock) == null || textureErrors.get(realBlock).size() < suppressErrorLimit)
        {
            try
            {
                IIcon icon = realBlock.getIcon(side, meta);
                if (icon == null)
                {
                    //visual error noting null icon
                    return Blocks.wool.getIcon(0, side);
                }
                return icon;
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("BlockWrapper#getIcon(" + side + "," + meta
                        + ") - Unexpected error while getting icon for side on block '" + InventoryUtility.getRegistryName(realBlock) + "'", e);
                addTextureError(e);
            }
        }
        //Visual error noting failed to get icon for block
        return Blocks.stained_hardened_clay.getIcon(0, side);
    }

    public boolean canRenderSide(int side)
    {
        return (renderSides & (1 << side)) != 0;
    }

    public void setRenderSide(ForgeDirection direction, boolean can)
    {
        if (can)
        {
            renderSides = (byte) (renderSides | (1 << direction.ordinal()));
        }
        else
        {
            renderSides = (byte) (renderSides & ~(1 << direction.ordinal()));
        }
    }
}
