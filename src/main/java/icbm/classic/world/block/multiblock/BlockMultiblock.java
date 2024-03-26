package icbm.classic.world.block.multiblock;

import icbm.classic.IcbmConstants;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Created by Dark on 7/4/2015.
 *
 * @Deprecated for removal, leaving as placeholder to set new blocks in position
 */
@Deprecated
public class BlockMultiblock extends BlockContainer {
    public BlockMultiblock() {
        super(Material.ROCK);
        this.setRegistryName(IcbmConstants.MOD_ID, "multiblock");
        this.setUnlocalizedName(IcbmConstants.PREFIX + "multiblock");
        this.setHardness(2f);
        needsRandomTick = true;
    }

    @Override
    public BlockEntity createNewBlockEntity(Level level, int meta) {
        return new TileMulti();
    }
}
