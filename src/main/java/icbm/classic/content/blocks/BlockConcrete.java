package icbm.classic.content.blocks;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockConcrete extends BlockICBM
{

    public BlockConcrete()
    {
        super("concrete", Material.ROCK);
        this.setHardness(10);
        this.setResistance(50);
    }

    @Override
    public float getExplosionResistance(Entity par1Entity, World world, BlockPos pos, double explosionX, double explosionY, double explosionZ)
    {
        IBlockState blockState = world.getBlockState(pos);

        switch (metadata)
        {
            case 1:
                return 38;
            case 2:
                return 48;
        }

        return this.getExplosionResistance(par1Entity);
    }

    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        for (int i = 0; i < 3; i++)
        {
            items.add(new ItemStack(this, 1, i));
        }
    }
}