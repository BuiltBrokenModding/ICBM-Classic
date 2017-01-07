package icbm.classic.prefab;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.Reference;
import icbm.TabICBM;
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
        this.setBlockName(Reference.PREFIX + name);
        this.setBlockTextureName(Reference.PREFIX + name);
        this.setCreativeTab(TabICBM.INSTANCE);
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

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        super.registerBlockIcons(iconRegister);

        if (this.requireSidedTextures)
        {
            this.iconTop = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "_top");
            this.iconSide = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "_side");
            this.iconBottom = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "_bottom");
        }
    }
}
