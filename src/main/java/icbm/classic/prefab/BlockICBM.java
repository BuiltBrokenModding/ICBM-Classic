package icbm.classic.prefab;

import com.builtbroken.mc.data.Direction;
import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public abstract class BlockICBM extends BlockContainer
{
    public static final PropertyDirection ROTATION_PROP = PropertyDirection.create("rotation");

    public BlockICBM(String name, Material mat)
    {
        super(mat);
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
        Direction direction = VectorHelper.getOrientationFromSide(Direction.getOrientation(determineOrientation(world, pos, placer)), Direction.NORTH);
        world.setBlockState(pos, getDefaultState().withProperty(ROTATION_PROP, direction.getEnumFacing()), 2);
        return getStateForPlacement(world, pos, facing, hitX, hitY, hitZ, meta, placer);
    }

    /** gets the way this piston should face for that entity that placed it. */
    protected static byte determineOrientation(World world, BlockPos pos, EntityLivingBase entityLiving)
    {
        if (entityLiving != null)
        {
            if (MathHelper.abs((float) entityLiving.posX - pos.getX()) < 2.0F && MathHelper.abs((float) entityLiving.posZ - pos.getZ()) < 2.0F)
            {
                double var5 = entityLiving.posY + 1.82D - entityLiving.height;

                if (var5 - pos.getY() > 2.0D)
                {
                    return 1;
                }

                if (pos.getY() - var5 > 0.0D)
                {
                    return 0;
                }
            }

            int rotation = MathHelper.floor(entityLiving.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            return (byte) (rotation == 0 ? 2 : (rotation == 1 ? 5 : (rotation == 2 ? 3 : (rotation == 3 ? 4 : 0))));
        }
        return 0;
    }
}
