package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

/**
 * PORT: 1.12 Sensitivity.EVERYTHING now comes from the block set type. Cutout render layer comes
 * from the block model. 1.12 creative tab REDSTONE is replaced by the ICBM tab (all items listed there).
 */
public class BlockGlassPressurePlate extends PressurePlateBlock
{
    /** Glass block set: activated by everything, stone click sounds, glass sound type */
    public static final BlockSetType GLASS_SET = BlockSetType.register(new BlockSetType(
        ICBMConstants.PREFIX + "glass",
        true, // can open by hand
        true, // can open by wind charge
        false, // buttons can't be pressed by arrows, like stone buttons
        BlockSetType.PressurePlateSensitivity.EVERYTHING,
        SoundType.GLASS,
        SoundEvents.IRON_DOOR_CLOSE,
        SoundEvents.IRON_DOOR_OPEN,
        SoundEvents.IRON_TRAPDOOR_CLOSE,
        SoundEvents.IRON_TRAPDOOR_OPEN,
        SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.STONE_BUTTON_CLICK_OFF,
        SoundEvents.STONE_BUTTON_CLICK_ON)); // PORT-CHECK: BlockSetType record constructor argument order in 1.21.1

    public BlockGlassPressurePlate()
    {
        // 1.12 resistance 1 (internal 3, hardness 0.3 kept it) = 0.6
        super(GLASS_SET, Properties.of()
            .mapColor(MapColor.NONE)
            .strength(0.3F, 0.6F)
            .sound(SoundType.GLASS)
            .noCollission()
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY));
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "glassPressurePlate";
    }
}
// PORTED
