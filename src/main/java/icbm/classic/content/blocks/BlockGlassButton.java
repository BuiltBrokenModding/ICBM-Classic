package icbm.classic.content.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockButton;
import net.minecraft.client.renderer.texture.IIconRegister;

import java.util.Random;

public class BlockGlassButton extends BlockButton
{
    public BlockGlassButton()
    {
        super(true);
        this.setTickRandomly(true);
        this.setBlockName(ICBMClassic.PREFIX + "glassButton");
        this.setStepSound(soundTypeGlass);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister par1IconRegister)
    {
        this.blockIcon = par1IconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", ""));
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }
}
