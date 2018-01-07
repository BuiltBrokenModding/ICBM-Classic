package icbm.classic.content.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockSpikes extends Block
{
    public BlockSpikes()
    {
        super(Material.IRON);
        //"spikes",
        this.setHardness(1.0F);
    }

    @Override
    public boolean isBlockNormalCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public boolean isOpaqueCube(IBlockState blockState)
    {
        return false;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        // If the entity is a living entity
        if (entity instanceof EntityLivingBase)
        {
            entity.attackEntityFrom(DamageSource.CACTUS, 1);

            if (getMetaFromState(world.getBlockState(pos)) == 1) //TODO replace with state
            {
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("poison"), 7 * 20, 0));
            }
            else if (getMetaFromState(world.getBlockState(pos)) == 2)
            {
                entity.setFire(7);
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        for (EnumSpikes spikes : EnumSpikes.values())
        {
            items.add(new ItemStack(this, 1, spikes.ordinal()));
        }
    }

    public static enum EnumSpikes implements IStringSerializable
    {
        NORMAL,
        POISON,
        FIRE;

        @Override
        public String toString()
        {
            return this.getName();
        }

        public String getName()
        {
            return name().toLowerCase();
        }
    }
}
