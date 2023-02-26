package icbm.classic.content.blocks.multiblock;

import icbm.classic.ICBMConstants;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

/**
 * Created by Dark on 7/4/2015.
 *
 * @Deprecated for removal, leaving as placeholder to set new blocks in position
 */
@Deprecated
public class BlockMultiblock extends BlockContainer
{
    public BlockMultiblock()
    {
        super(Material.ROCK);
        this.setRegistryName(ICBMConstants.DOMAIN, "multiblock");
        this.setUnlocalizedName(ICBMConstants.PREFIX + "multiblock");
        this.setHardness(2f);
        needsRandomTick = true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta)
    {
        return new TileMulti();
    }
}
