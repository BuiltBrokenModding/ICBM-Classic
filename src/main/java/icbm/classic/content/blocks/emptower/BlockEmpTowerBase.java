package icbm.classic.content.blocks.emptower;

import icbm.classic.content.reg.TileReg;
import icbm.classic.lib.BlockUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 1/23/2018.
 */
public class BlockEmpTowerBase extends Block implements ITileEntityProvider {
    final VoxelShape shape = VoxelShapes.create(0.2, 0, 0.2, 0.8, 1, 0.8);

    public BlockEmpTowerBase(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return shape;
    }

    @Override
    public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        BlockUtils.openGUI(worldIn, pos, player);
        return true;
    }

    @Override
    public boolean hasComparatorInputOverride(BlockState state) {
        return false;
    }

    @Override
    public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
        return 0; //TODO output charge amount
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return TileReg.EMP_TOWER_BASE.get().create();
    }
    
    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        //TODO figure out why this deprecated method works but the main forge one doesn't
        return TileReg.EMP_TOWER_BASE.get().create();
    }
}
