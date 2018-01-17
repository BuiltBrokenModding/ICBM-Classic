package icbm.classic.prefab;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BlockICBM extends BlockContainer
{
    public static final PropertyDirection ROTATION_PROP = PropertyDirection.create("rotation");
    public static final PropertyTier TIER_PROP = new PropertyTier();

    public BlockICBM(String name, Material mat)
    {
        super(mat);
        blockHardness = 10f;
        blockResistance = 10f;
        setRegistryName(ICBMClassic.DOMAIN, name.toLowerCase());
        setCreativeTab(ICBMClassic.CREATIVE_TAB);
    }

    public BlockICBM(String name)
    {
        this(name, Material.IRON);
    }

    @Override
    protected BlockStateContainer createBlockState()
    {
        return new BlockStateContainer(this, ROTATION_PROP);
    }

    @Override
    public IBlockState getStateFromMeta(int meta)
    {
        return getDefaultState().withProperty(ROTATION_PROP, EnumFacing.getFront(meta));
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand)
    {
        return getDefaultState().withProperty(ROTATION_PROP, placer.getHorizontalFacing());
    }

    public static final class PropertyTier extends PropertyEnum<EnumTier>
    {
        public PropertyTier()
        {
            super("tier", EnumTier.class, Lists.newArrayList(EnumTier.values()));
        }
    }

    public static enum EnumTier implements IStringSerializable
    {
        ONE,
        TWO,
        THREE,
        FOUR;

        @Override
        public String toString()
        {
            return this.getName();
        }

        public String getName()
        {
            return name().toLowerCase();
        }

        public static EnumTier get(int itemDamage)
        {
            if (itemDamage > 0 && itemDamage < values().length)
            {
                return values()[itemDamage];
            }
            return ONE;
        }
    }
}
