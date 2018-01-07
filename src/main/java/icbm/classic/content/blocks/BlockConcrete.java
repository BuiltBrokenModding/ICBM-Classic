package icbm.classic.content.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockConcrete extends Block
{

    public BlockConcrete()
    {
        super(Material.ROCK);
        //"concrete",
        this.setHardness(10);
        this.setResistance(50);
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
    {
        IBlockState blockState = world.getBlockState(pos);
    /*
        switch (metadata)
        {
            case 1:
                return 38;
            case 2:
                return 48;
        }
*/
        return this.getExplosionResistance(exploder);
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