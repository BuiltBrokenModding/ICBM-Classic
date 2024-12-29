package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.blocks.*;
import icbm.classic.content.blocks.emptower.BlockEmpTowerBase;
import icbm.classic.content.blocks.emptower.BlockEmpTowerCoil;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.launcher.base.BlockLauncherBase;
import icbm.classic.content.blocks.launcher.connector.BlockLaunchConnector;
import icbm.classic.content.blocks.launcher.cruise.BlockCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.blocks.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.radioactive.BlockRadioactive;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockReg {
    public static final DeferredRegister<Block> BLOCKS = new DeferredRegister<Block>(ForgeRegistries.BLOCKS, ICBMConstants.DOMAIN);
    public static final RegistryObject<BlockSpikes> SPIKE_NORMAL = BLOCKS.register("spikes_normal", BlockSpikes::new);
    public static final RegistryObject<BlockSpikes> SPIKE_FIRE = BLOCKS.register("spikes_fire", () -> new BlockSpikes().setFire(true));
    public static final RegistryObject<BlockSpikes> SPIKE_POISON = BLOCKS.register("spikes_poison", () -> new BlockSpikes().setPoison(true));

    public static final RegistryObject<Block> CONCRETE_NORMAL = BLOCKS.register("concrete_normal", () -> new Block(
        Block.Properties.create(Material.ROCK).hardnessAndResistance(10, 28))
    );
    public static final RegistryObject<Block> CONCRETE_COMPACT = BLOCKS.register("concrete_compact", () -> new Block(
        Block.Properties.create(Material.ROCK).hardnessAndResistance(10, 280))
    );
    public static final RegistryObject<Block> CONCRETE_REINFORCED = BLOCKS.register("concrete_reinforced", () -> new Block(
        Block.Properties.create(Material.ROCK).hardnessAndResistance(10, 2800))
    );
    public static final RegistryObject<Block> GLASS_REINFORCED = BLOCKS.register("glass_reinforced", () -> new Block(
        Block.Properties.create(Material.ROCK).hardnessAndResistance(10, 280))
    );

    public static final RegistryObject<Block> EMP_TOWER_BASE = BLOCKS.register("emp_tower_base", BlockEmpTowerBase::new);
    public static final RegistryObject<Block> EMP_TOWER_COIL = BLOCKS.register("emp_tower_coil", BlockEmpTowerCoil::new);

    public static final RegistryObject<Block> RADAR_SCREEN = BLOCKS.register("radar_screen", BlockRadarStation::new);

    public static final RegistryObject<Block> LAUNCHER_FRAME = BLOCKS.register("launcher_frame", BlockLaunchFrame::new);
    public static final RegistryObject<Block> LAUNCHER_CONNECTOR = BLOCKS.register("launcher_connector", BlockLaunchConnector::new);
    public static final RegistryObject<Block> LAUNCHER_BASE = BLOCKS.register("launcher_base", BlockLauncherBase::new);
    public static final RegistryObject<Block> LAUNCHER_SCREEN = BLOCKS.register("launcher_screen", BlockLaunchScreen::new);
    public static final RegistryObject<Block> LAUNCHER_CRUISE = BLOCKS.register("launcher_cruise", BlockCruiseLauncher::new);

    public static final RegistryObject<Block> RADIOACTIVE_DIRT = BLOCKS.register("radioactive_dirt", () -> new BlockRadioactive(Block.Properties.from(Blocks.DIRT)));
    public static final RegistryObject<Block> RADIOACTIVE_STONE = BLOCKS.register("radioactive_stone", () -> new BlockRadioactive(Block.Properties.from(Blocks.STONE)));

    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CONDENSED = BLOCKS.register("explosive_condensed", () -> new BlockExplosive(ICBMExplosives.CONDENSED));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SHRAPNEL = BLOCKS.register("explosive_shrapnel", () -> new BlockExplosive(ICBMExplosives.SHRAPNEL));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_INCENDIARY = BLOCKS.register("explosive_incendiary", () -> new BlockExplosive(ICBMExplosives.INCENDIARY));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_DEBILITATION = BLOCKS.register("explosive_debilitation", () -> new BlockExplosive(ICBMExplosives.DEBILITATION));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CHEMICAL = BLOCKS.register("explosive_chemical", () -> new BlockExplosive(ICBMExplosives.CHEMICAL));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ANVIL = BLOCKS.register("explosive_anvil", () -> new BlockExplosive(ICBMExplosives.ANVIL));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_REPULSIVE = BLOCKS.register("explosive_repulsive", () -> new BlockExplosive(ICBMExplosives.REPULSIVE));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ATTRACTIVE = BLOCKS.register("explosive_attractive", () -> new BlockExplosive(ICBMExplosives.ATTRACTIVE));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_COLOR = BLOCKS.register("explosive_color", () -> new BlockExplosive(ICBMExplosives.COLOR));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SMOKE = BLOCKS.register("explosive_smoke", () -> new BlockExplosive(ICBMExplosives.SMOKE));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_FRAGMENTATION = BLOCKS.register("explosive_fragmentation", () -> new BlockExplosive(ICBMExplosives.FRAGMENTATION));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CONTAGIOUS = BLOCKS.register("explosive_contagious", () -> new BlockExplosive(ICBMExplosives.CONTAGIOUS));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SONIC = BLOCKS.register("explosive_sonic", () -> new BlockExplosive(ICBMExplosives.SONIC));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_BREACHING = BLOCKS.register("explosive_breaching", () -> new BlockExplosive(ICBMExplosives.BREACHING));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_THERMOBARIC = BLOCKS.register("explosive_thermobaric", () -> new BlockExplosive(ICBMExplosives.THERMOBARIC));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_NUCLEAR = BLOCKS.register("explosive_nuclear", () -> new BlockExplosive(ICBMExplosives.NUCLEAR));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_EMP = BLOCKS.register("explosive_emp", () -> new BlockExplosive(ICBMExplosives.EMP));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_EXOTHERMIC = BLOCKS.register("explosive_exothermic", () -> new BlockExplosive(ICBMExplosives.EXOTHERMIC));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ENDOTHERMIC = BLOCKS.register("explosive_endothermic", () -> new BlockExplosive(ICBMExplosives.ENDOTHERMIC));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_GRAVITY = BLOCKS.register("explosive_gravity", () -> new BlockExplosive(ICBMExplosives.GRAVITY));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ENDER = BLOCKS.register("explosive_ender", () -> new BlockExplosive(ICBMExplosives.ENDER));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ANTIMATTER = BLOCKS.register("explosive_antimatter", () -> new BlockExplosive(ICBMExplosives.ANTIMATTER));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_REDMATTER = BLOCKS.register("explosive_redmatter", () -> new BlockExplosive(ICBMExplosives.REDMATTER));
}
