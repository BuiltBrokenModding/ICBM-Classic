package icbm.classic.world;

import icbm.classic.IcbmConstants;
import icbm.classic.world.block.BlockSpikes;
import icbm.classic.world.block.IcbmTransparentBlock;
import icbm.classic.world.block.emptower.EmpTowerBlock;
import icbm.classic.world.block.explosive.ExplosiveBlock;
import icbm.classic.world.block.launcher.base.LauncherBaseBlock;
import icbm.classic.world.block.launcher.connector.LaunchConnectorBlock;
import icbm.classic.world.block.launcher.cruise.CruiseLauncherBlock;
import icbm.classic.world.block.launcher.frame.LaunchFrameBlock;
import icbm.classic.world.block.launcher.screen.LaunchScreenBlock;
import icbm.classic.world.block.radarstation.RadarScreenBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IcbmBlocks {

    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.Blocks.createBlocks(IcbmConstants.MOD_ID);

    public static final BlockSetType GLASS = BlockSetType.register(new BlockSetType(
        "glass",
        true,
        true,
        true,
        BlockSetType.PressurePlateSensitivity.EVERYTHING,
        SoundType.GLASS,
        SoundEvents.GLASS_STEP,
        SoundEvents.GLASS_STEP,
        SoundEvents.GLASS_STEP,
        SoundEvents.GLASS_STEP,
        SoundEvents.METAL_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.METAL_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.STONE_BUTTON_CLICK_OFF,
        SoundEvents.STONE_BUTTON_CLICK_ON
    ));

    public static final DeferredBlock<PressurePlateBlock> GLASS_PRESSURE_PLATE = REGISTER.registerBlock("glass_pressure_plate",
        properties -> new PressurePlateBlock(GLASS, properties),
        BlockBehaviour.Properties.of()
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .strength(0.3F)
            .sound(SoundType.GLASS));

    public static final DeferredBlock<ButtonBlock> GLASS_BUTTON = REGISTER.registerBlock("glass_button",
        properties -> new ButtonBlock(GLASS, 10, properties),
        BlockBehaviour.Properties.of()
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .strength(0.3F)
            .sound(SoundType.GLASS));

    public static final DeferredBlock<BlockSpikes> SPIKES = REGISTER.registerBlock("spikes",
        properties -> new BlockSpikes(properties, BlockSpikes.SpikeType.NORMAL),
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .noCollission()
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL));
    public static final DeferredBlock<BlockSpikes> POISON_SPIKES = REGISTER.registerBlock("posion_spikes",
        properties -> new BlockSpikes(properties, BlockSpikes.SpikeType.POISON),
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .noCollission()
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL));
    public static final DeferredBlock<BlockSpikes> FIRE_SPIKES = REGISTER.registerBlock("fire_spikes",
        properties -> new BlockSpikes(properties, BlockSpikes.SpikeType.FIRE),
        BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .instrument(NoteBlockInstrument.IRON_XYLOPHONE)
            .requiresCorrectToolForDrops()
            .noOcclusion()
            .noCollission()
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL));

    private static final float COMPACT_CONCRETE_DESTROY_TIME = 6F;
    private static final float COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE = 168.0F;

    public static final DeferredBlock<Block> WHITE_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "white_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.WHITE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> ORANGE_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "orange_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> MAGENTA_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "magenta_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.MAGENTA)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));

    public static final DeferredBlock<Block> LIGHT_BLUE_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "light_blue_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIGHT_BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> YELLOW_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "yellow_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.YELLOW)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> LIME_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "lime_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIME)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> PINK_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "pink_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.PINK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> GRAY_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "gray_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> LIGHT_GRAY_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "light_gray_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIGHT_GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> CYAN_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "cyan_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.CYAN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> PURPLE_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "purple_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.PURPLE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BLUE_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "blue_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BROWN_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "brown_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BROWN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> GREEN_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "green_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.GREEN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> RED_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "red_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.RED)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BLACK_COMPACT_CONCRETE = REGISTER.registerSimpleBlock(
        "black_compact_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(COMPACT_CONCRETE_DESTROY_TIME, COMPACT_CONCRETE_EXPLOSIVE_RESISTANCE));

    /////////////

    private static final float REINFORCED_CONCRETE_DESTROY_TIME = 6F;
    private static final float REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE = 1680.0F;

    public static final DeferredBlock<Block> WHITE_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "white_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.WHITE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> ORANGE_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "orange_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.ORANGE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> MAGENTA_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "magenta_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.MAGENTA)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));

    public static final DeferredBlock<Block> LIGHT_BLUE_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "light_blue_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIGHT_BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> YELLOW_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "yellow_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.YELLOW)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> LIME_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "lime_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIME)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> PINK_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "pink_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.PINK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> GRAY_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "gray_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> LIGHT_GRAY_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "light_gray_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.LIGHT_GRAY)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> CYAN_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "cyan_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.CYAN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> PURPLE_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "purple_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.PURPLE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BLUE_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "blue_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BROWN_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "brown_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BROWN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> GREEN_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "green_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.GREEN)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> RED_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "red_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.RED)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));
    public static final DeferredBlock<Block> BLACK_REINFORCED_CONCRETE = REGISTER.registerSimpleBlock(
        "black_reinforced_concrete",
        BlockBehaviour.Properties.of()
            .mapColor(DyeColor.BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(REINFORCED_CONCRETE_DESTROY_TIME, REINFORCED_CONCRETE_EXPLOSIVE_RESISTANCE));

    public static final DeferredBlock<Block> REINFORCED_GLASS = REGISTER.registerBlock(
        "reinforced_glass",
        IcbmTransparentBlock::new,
        BlockBehaviour.Properties.of()
            .instrument(NoteBlockInstrument.HAT)
            .strength(6F, 28F)
            .sound(SoundType.GLASS)
            .noOcclusion()
            .isValidSpawn(IcbmBlocks::never)
            .isRedstoneConductor(IcbmBlocks::never)
            .isSuffocating(IcbmBlocks::never)
            .isViewBlocking(IcbmBlocks::never)
    );

    public static final DeferredBlock<ExplosiveBlock> EXPLOSIVES = REGISTER.registerBlock(
        "explosives",
        ExplosiveBlock::new,
        BlockBehaviour.Properties.of()
            .strength(1F)
            .sound(SoundType.WOOL)
            .noOcclusion()
            .isValidSpawn(IcbmBlocks::never)
            .isRedstoneConductor(IcbmBlocks::never)
            .isSuffocating(IcbmBlocks::never)
            .isViewBlocking(IcbmBlocks::never)
    );

    public static final DeferredBlock<EmpTowerBlock> EMP_TOWER = REGISTER.registerBlock(
        "emp_tower",
        EmpTowerBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<RadarScreenBlock> RADAR_SCREEN = REGISTER.registerBlock(
        "radar_screen",
        RadarScreenBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<LaunchFrameBlock> LAUNCH_FRAME = REGISTER.registerBlock(
        "launcher_frame",
        LaunchFrameBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<LaunchConnectorBlock> LAUNCH_CONNECTOR = REGISTER.registerBlock(
        "launcher_connector",
        LaunchConnectorBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<LauncherBaseBlock> LAUNCHER_BASE = REGISTER.registerBlock(
        "launcher_base",
        LauncherBaseBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<LaunchScreenBlock> LAUNCH_SCREEN = REGISTER.registerBlock(
        "launcher_screen",
        LaunchScreenBlock::new,
        BlockBehaviour.Properties.of()
    );

    public static final DeferredBlock<CruiseLauncherBlock> CRUISE_LAUNCHER = REGISTER.registerBlock(
        "cruise_launcher",
        CruiseLauncherBlock::new,
        BlockBehaviour.Properties.of()
    );

    private static Boolean never(BlockState state, BlockGetter getter, BlockPos position, EntityType<?> entityType) {
        return false;
    }

    private static boolean never(BlockState state, BlockGetter getter, BlockPos position) {
        return false;
    }
}
