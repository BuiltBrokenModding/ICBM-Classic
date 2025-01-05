package icbm.classic.content.blocks.emptower;

import icbm.classic.content.reg.TileReg;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class BlockEmpTowerCoil extends ContainerBlock
{
    public BlockEmpTowerCoil()
    {
        super(Properties.create(Material.IRON).hardnessAndResistance(10, 10));
    }


    @Override
    public BlockRenderType getRenderType(BlockState state)
    {
        return BlockRenderType.MODEL;
    }

    @Override
    public boolean canRenderInLayer(BlockState state, BlockRenderLayer layer)
    {
        return false;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn)
    {
        return TileReg.EMP_TOWER_COIL.get().create();
    }
}
