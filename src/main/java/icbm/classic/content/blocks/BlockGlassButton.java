package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.PushReaction;

/**
 * PORT: cutout render layer comes from the block model. 1.12 played the stone click at volume 0.3 and
 * pitch 0.6/0.5; 1.21 ButtonBlock plays the block set sounds at default volume and pitch.
 */
public class BlockGlassButton extends ButtonBlock
{
    public BlockGlassButton()
    {
        // 1.12 BlockButton(false) = stone button timing (20 ticks), hardness 0.5
        super(BlockGlassPressurePlate.GLASS_SET, 20, Properties.of()
            .noCollission()
            .strength(0.5F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY));
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "glassButton";
    }
}
// PORTED
