package icbm.classic.content.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.TabICBM;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import java.util.List;

public class BlockSpikes extends BlockICBM
{
    @SideOnly(Side.CLIENT)
    private IIcon iconPoison, iconFlammable;

    public BlockSpikes()
    {
        super("spikes", Material.cactus);
        this.setHardness(1.0F);
        this.setCreativeTab(TabICBM.INSTANCE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerBlockIcons(IIconRegister iconRegister)
    {
        super.registerBlockIcons(iconRegister);
        this.iconPoison = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "Poison");
        this.iconFlammable = iconRegister.registerIcon(this.getUnlocalizedName().replace("tile.", "") + "Flammable");
    }

    @Override
    public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int x, int y, int z)
    {
        return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.5F, z + 1);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int par2, int par3, int par4)
    {
        return null;
    }

    @Override
    public IIcon getIcon(int par1, int metadata)
    {
        if (metadata == 2)
        {
            return this.iconFlammable;
        }
        else if (metadata == 1)
        {
            return this.iconPoison;
        }

        return this.blockIcon;
    }

    @Override
    public boolean renderAsNormalBlock()
    {
        return false;
    }


    @Override
    public boolean isOpaqueCube()
    {
        return false;
    }

    @Override
    public int getRenderType()
    {
        return 1;
    }

    @Override
    public int getMobilityFlag()
    {
        return 0;
    }

    @Override
    public boolean canPlaceBlockAt(World par1World, int par2, int par3, int par4)
    {
        return canBlockStay(par1World, par2, par3, par4);
    }

    @Override
    public void onEntityCollidedWithBlock(World par1World, int par2, int par3, int par4, Entity par5Entity)
    {
        // If the entity is a living entity
        if (par5Entity instanceof EntityLiving)
        {
            par5Entity.attackEntityFrom(DamageSource.cactus, 1);

            if (par1World.getBlockMetadata(par2, par3, par4) == 1)
            {
                ((EntityLiving) par5Entity).addPotionEffect(new PotionEffect(Potion.poison.id, 7 * 20, 0));
            }
            else if (par1World.getBlockMetadata(par2, par3, par4) == 2)
            {
                ((EntityLiving) par5Entity).setFire(7);
            }
        }
    }

    @Override
    public void getSubBlocks(Item par1, CreativeTabs par2CreativeTabs, List par3List)
    {
        for (int i = 0; i < 3; i++)
        {
            par3List.add(new ItemStack(this, 1, i));
        }
    }
}
