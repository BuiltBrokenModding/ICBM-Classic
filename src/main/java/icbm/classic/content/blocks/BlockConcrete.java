package icbm.classic.content.blocks;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class BlockConcrete extends Block
{
    public static final PropertyType TYPE_PROP = new PropertyType();

    public BlockConcrete()
    {
        super(Material.ROCK);
        this.setRegistryName(ICBMConstants.PREFIX + "concrete");
        this.setUnlocalizedName(ICBMConstants.PREFIX + "concrete");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHardness(10);
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, TYPE_PROP);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(TYPE_PROP).ordinal();
    }

    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion)
    {
        IBlockState blockState = world.getBlockState(pos);

        switch (blockState.getValue(TYPE_PROP))
        {
            case COMPACT:
                return 280;
            case REINFORCED:
                return 2800; //obsidian is 2000
            default:
            case NORMAL:
                return 28;
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == this.getCreativeTabToDisplayOn())
        {
            for (int i = 0; i < 3; i++)
            {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    public static class PropertyType extends PropertyEnum<EnumType>
    {
        public PropertyType()
        {
            super("type", EnumType.class, Lists.newArrayList(EnumType.values()));
        }
    }

    public static enum EnumType implements IStringSerializable
    {
        NORMAL,
        COMPACT,
        REINFORCED;

        @Override
        public String getName()
        {
            return name().toLowerCase();
        }

        public static EnumType get(int meta)
        {
            return meta >= 0 && meta < values().length ? values()[meta] : NORMAL;
        }
    }
}