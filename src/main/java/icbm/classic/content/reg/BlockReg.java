package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.BlockConcrete;
import icbm.classic.content.blocks.BlockGlassButton;
import icbm.classic.content.blocks.BlockGlassPressurePlate;
import icbm.classic.content.blocks.BlockReinforcedGlass;
import icbm.classic.content.blocks.BlockSpikes;
import icbm.classic.content.blocks.emptower.BlockEmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.TileEmpTowerFake;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.content.blocks.launcher.base.BlockLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.connector.BlockLaunchConnector;
import icbm.classic.content.blocks.launcher.connector.TileLauncherConnector;
import icbm.classic.content.blocks.launcher.cruise.BlockCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.radioactive.BlockRadioactive;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Block and block entity registry.
 * <p>
 * Registry names are the 1.12 names lower cased (1.12 lower cased them on its own). Meta variants were
 * flattened into their own blocks:
 * <ul>
 *     <li>spikes:0/1/2 -> spikes, spikes_poison, spikes_fire</li>
 *     <li>concrete:0/1/2 -> concrete, concrete_compact, concrete_reinforced</li>
 *     <li>radioactive:0/1 -> radioactive_dirt, radioactive_stone</li>
 *     <li>emptower:1 (extender) stays a state of emptower, placed by the emptower_extender item</li>
 * </ul>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class BlockReg
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ICBMConstants.DOMAIN);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ICBMConstants.DOMAIN);

    public static final DeferredBlock<BlockGlassPressurePlate> blockGlassPlate = BLOCKS.register("glasspressureplate", BlockGlassPressurePlate::new);
    public static final DeferredBlock<BlockGlassButton> blockGlassButton = BLOCKS.register("glassbutton", BlockGlassButton::new);

    public static final DeferredBlock<BlockSpikes> blockSpikes = BLOCKS.register("spikes", () -> new BlockSpikes(BlockSpikes.EnumSpikes.NORMAL));
    public static final DeferredBlock<BlockSpikes> blockSpikesPoison = BLOCKS.register("spikes_poison", () -> new BlockSpikes(BlockSpikes.EnumSpikes.POISON));
    public static final DeferredBlock<BlockSpikes> blockSpikesFire = BLOCKS.register("spikes_fire", () -> new BlockSpikes(BlockSpikes.EnumSpikes.FIRE));

    public static final DeferredBlock<BlockConcrete> blockConcrete = BLOCKS.register("concrete", () -> new BlockConcrete(BlockConcrete.EnumType.NORMAL));
    public static final DeferredBlock<BlockConcrete> blockConcreteCompact = BLOCKS.register("concrete_compact", () -> new BlockConcrete(BlockConcrete.EnumType.COMPACT));
    public static final DeferredBlock<BlockConcrete> blockConcreteReinforced = BLOCKS.register("concrete_reinforced", () -> new BlockConcrete(BlockConcrete.EnumType.REINFORCED));

    public static final DeferredBlock<BlockReinforcedGlass> blockReinforcedGlass = BLOCKS.register("reinforcedglass", BlockReinforcedGlass::new);
    public static final DeferredBlock<BlockExplosive> blockExplosive = BLOCKS.register("explosives", BlockExplosive::new);

    public static final DeferredBlock<BlockLauncherBase> blockLaunchBase = BLOCKS.register("launcherbase", BlockLauncherBase::new);
    public static final DeferredBlock<BlockLaunchScreen> blockLaunchScreen = BLOCKS.register("launcherscreen", BlockLaunchScreen::new);
    public static final DeferredBlock<BlockLaunchFrame> blockLaunchSupport = BLOCKS.register("launcherframe", BlockLaunchFrame::new);
    public static final DeferredBlock<BlockLaunchConnector> blockLaunchConnector = BLOCKS.register("launcher_connector", BlockLaunchConnector::new);
    public static final DeferredBlock<BlockRadarStation> blockRadarStation = BLOCKS.register("radarstation", BlockRadarStation::new);
    public static final DeferredBlock<BlockEmpTower> blockEmpTower = BLOCKS.register("emptower", BlockEmpTower::new);
    public static final DeferredBlock<BlockCruiseLauncher> blockCruiseLauncher = BLOCKS.register("cruiselauncher", BlockCruiseLauncher::new);

    public static final DeferredBlock<BlockRadioactive> blockRadioactiveDirt = BLOCKS.register("radioactive_dirt", () -> new BlockRadioactive(BlockRadioactive.EnumType.DIRT));
    public static final DeferredBlock<BlockRadioactive> blockRadioactiveStone = BLOCKS.register("radioactive_stone", () -> new BlockRadioactive(BlockRadioactive.EnumType.STONE));

    // Block entities, constructors are (BlockPos, BlockState)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEntityExplosive>> TILE_EXPLOSIVE = tile("explosive", TileEntityExplosive::new, blockExplosive);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEMPTower>> TILE_EMP_TOWER = tile("emptower", TileEMPTower::new, blockEmpTower);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileEmpTowerFake>> TILE_EMP_TOWER_FAKE = tile("emptower_fake", TileEmpTowerFake::new, blockEmpTower);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileRadarStation>> TILE_RADAR_STATION = tile("radarstation", TileRadarStation::new, blockRadarStation);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherFrame>> TILE_LAUNCHER_FRAME = tile("launcherframe", TileLauncherFrame::new, blockLaunchSupport);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherConnector>> TILE_LAUNCHER_CONNECTOR = tile("launcher_connector", TileLauncherConnector::new, blockLaunchConnector);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherBase>> TILE_LAUNCHER_BASE = tile("launcherbase", TileLauncherBase::new, blockLaunchBase);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileLauncherScreen>> TILE_LAUNCHER_SCREEN = tile("launcherscreen", TileLauncherScreen::new, blockLaunchScreen);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<TileCruiseLauncher>> TILE_CRUISE_LAUNCHER = tile("cruiselauncher", TileCruiseLauncher::new, blockCruiseLauncher);

    @SafeVarargs
    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> tile(String name, BlockEntityType.BlockEntitySupplier<T> factory, Supplier<? extends Block>... blocks)
    {
        return BLOCK_ENTITIES.register(name, () -> {
            final Block[] validBlocks = new Block[blocks.length];
            for (int i = 0; i < blocks.length; i++)
            {
                validBlocks[i] = blocks[i].get();
            }
            //noinspection DataFlowIssue
            return BlockEntityType.Builder.of(factory, validBlocks).build(null);
        });
    }

    public static void register(IEventBus modBus)
    {
        BLOCKS.register(modBus);
        BLOCK_ENTITIES.register(modBus);
    }
}
