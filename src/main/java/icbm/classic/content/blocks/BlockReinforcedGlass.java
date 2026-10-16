package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

/**
 * PORT: 1.12 cutout render layer comes from the block model ("render_type": "cutout").
 * Silk touch drops come from the loot table. 1.12 resistance field 144 maps to 28.8.
 */
public class BlockReinforcedGlass extends TransparentBlock
{
    public BlockReinforcedGlass()
    {
        super(Properties.of()
            .mapColor(MapColor.NONE)
            .strength(10, 28.8f)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn((state, level, pos, type) -> false)
            .isRedstoneConductor((state, level, pos) -> false)
            .isSuffocating((state, level, pos) -> false)
            .isViewBlocking((state, level, pos) -> false));
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "reinforcedGlass";
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side)
    {
        // Same as 1.12 shouldSideBeRendered: hide faces between identical states
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }
}
// PORTED
