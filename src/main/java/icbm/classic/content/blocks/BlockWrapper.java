package icbm.classic.content.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 2/19/2017.
 */
public class BlockWrapper extends Block
{
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
        return realBlock.colorMultiplier(world, x, y, z);
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
        return canRenderSide(side) && realBlock.shouldSideBeRendered(world, x, y, z, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getMixedBrightnessForBlock(IBlockAccess world, int x, int y, int z)
    {
        return realBlock.getMixedBrightnessForBlock(world, x, y, z);
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
        return realBlock.getIcon(world, x, y, z, side);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int p_149691_1_, int p_149691_2_)
    {
        return realBlock.getIcon(p_149691_1_, p_149691_2_);
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
