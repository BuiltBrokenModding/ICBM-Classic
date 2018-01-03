package icbm.classic.prefab;

import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class BlockICBM extends Block
{
    @SideOnly(Side.CLIENT)
    protected IIcon iconTop, iconSide, iconBottom;
    protected boolean requireSidedTextures = false;

    public BlockICBM(String name, Material mat)
    {
        super(mat);
        this.setBlockName(ICBMClassic.PREFIX + name);
        this.setBlockTextureName(ICBMClassic.PREFIX + name);
    }

    public BlockICBM(String name)
    {
        this(name, Material.iron);
    }

    @Override
    public int damageDropped(int metadata)
    {
        return metadata;
    }
}
