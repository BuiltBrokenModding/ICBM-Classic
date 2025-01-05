package icbm.classic.content.blocks.emptower;

import icbm.classic.content.reg.TileReg;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/23/2018.
 */
public class BlockEmpTowerBase extends Block implements ITileEntityProvider
{
    public static final PropertyTowerStates TOWER_MODELS = new PropertyTowerStates();
    public static BlockState COIL;
    public static BlockState ELECTRIC;

    public BlockEmpTowerBase(Properties properties)
    {
        super(properties);

        COIL = getDefaultState().with(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.COIL);
        ELECTRIC = getDefaultState().with(TOWER_MODELS, PropertyTowerStates.EnumTowerTypes.ELECTRIC);
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit)
    {
        if (!worldIn.isRemote)
        {
            //player.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ()); TODO
        }
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state)
    {
        return false;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos)
    {
        return 0; //TODO output charge amount
    }

    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
    {
        if(state == ELECTRIC) {
            return BlockRenderLayer.TRANSLUCENT == layer;
        }
        return BlockRenderLayer.SOLID == layer;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return TileReg.EMP_TOWER_BASE.get().create();
    }
}
