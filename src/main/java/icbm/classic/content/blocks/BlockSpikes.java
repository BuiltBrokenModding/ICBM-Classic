package icbm.classic.content.blocks;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockSpikes extends Block
{
    public static final SpikeProperty SPIKE_PROPERTY = new SpikeProperty();

    public BlockSpikes()
    {
        super(Material.IRON);
        this.setRegistryName(ICBMClassic.PREFIX + "spikes");
        this.setUnlocalizedName(ICBMClassic.PREFIX + "spikes");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHardness(1.0F);
    }

    @Override
    public int damageDropped(IBlockState state)
    {
        return getMetaFromState(state);
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos)
    {
        return null;
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, SPIKE_PROPERTY);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return getDefaultState().withProperty(SPIKE_PROPERTY, EnumSpikes.get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state)
    {
        return state.getValue(SPIKE_PROPERTY).ordinal();
    }

    @Deprecated
    public IBlockState getStateFromMeta(int meta)
    {
        return this.getDefaultState().withProperty(SPIKE_PROPERTY, EnumSpikes.get(meta));
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
    public boolean isFullCube(IBlockState state)
    {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public void onEntityCollidedWithBlock(World world, BlockPos pos, IBlockState state, Entity entity)
    {
        // If the entity is a living entity
        if (entity instanceof EntityLivingBase)
        {
            entity.attackEntityFrom(DamageSource.CACTUS, 1);

            if (world.getBlockState(pos).getValue(SPIKE_PROPERTY) == EnumSpikes.POISON) //TODO replace with state
            {
                ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(Potion.getPotionFromResourceLocation("poison"), 7 * 20, 0));
            }
            else if (world.getBlockState(pos).getValue(SPIKE_PROPERTY) == EnumSpikes.FIRE)
            {
                entity.setFire(7);
            }
        }
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTabToDisplayOn())
        {
            for (EnumSpikes spikes : EnumSpikes.values())
            {
                items.add(new ItemStack(this, 1, spikes.ordinal()));
            }
        }
    }

    public static class SpikeProperty extends PropertyEnum<EnumSpikes>
    {
        protected SpikeProperty()
        {
            super("type", EnumSpikes.class, Lists.newArrayList(EnumSpikes.values()));
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

        @Override
        public String getName()
        {
            return name().toLowerCase();
        }

        public static EnumSpikes get(int meta)
        {
            return meta >= 0 && meta < values().length ? values()[meta] : NORMAL;
        }
    }
}
