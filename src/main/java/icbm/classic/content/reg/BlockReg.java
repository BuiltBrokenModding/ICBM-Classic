package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.actions.entity.ActionSpawnEntity;
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
import icbm.classic.lib.actions.ActionDataGeneric;
import icbm.classic.lib.actions.ActionSystem;
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

    public static final RegistryObject<Block> EMP_TOWER_BASE = BLOCKS.register("emp_tower_base", () -> new BlockEmpTowerBase(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));
    public static final RegistryObject<Block> EMP_TOWER_COIL = BLOCKS.register("emp_tower_coil", () -> new BlockEmpTowerCoil(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));

    public static final RegistryObject<Block> RADAR_SCREEN = BLOCKS.register("radar_screen", () -> new BlockRadarStation(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));

    public static final RegistryObject<Block> LAUNCHER_FRAME = BLOCKS.register("launcher_frame", () -> new BlockLaunchFrame(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));
    public static final RegistryObject<Block> LAUNCHER_CONNECTOR = BLOCKS.register("launcher_connector", () -> new BlockLaunchConnector(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));
    public static final RegistryObject<Block> LAUNCHER_BASE = BLOCKS.register("launcher_base", () -> new BlockLauncherBase(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));
    public static final RegistryObject<Block> LAUNCHER_SCREEN = BLOCKS.register("launcher_screen", () -> new BlockLaunchScreen(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));
    public static final RegistryObject<Block> LAUNCHER_CRUISE = BLOCKS.register("launcher_cruise", () -> new BlockCruiseLauncher(Block.Properties.create(Material.IRON).hardnessAndResistance(10)));

    public static final RegistryObject<Block> RADIOACTIVE_DIRT = BLOCKS.register("radioactive_dirt", () -> new BlockRadioactive(Block.Properties.from(Blocks.DIRT)));
    public static final RegistryObject<Block> RADIOACTIVE_STONE = BLOCKS.register("radioactive_stone", () -> new BlockRadioactive(Block.Properties.from(Blocks.STONE)));

    // Entity explosive blocks
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CONDENSED = BLOCKS.register("explosive_condensed", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_CONDENSED, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SHRAPNEL = BLOCKS.register("explosive_shrapnel", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_SHRAPNEL, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_INCENDIARY = BLOCKS.register("explosive_incendiary", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_INCENDIARY, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_DEBILITATION = BLOCKS.register("explosive_debilitation", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_DEBILITATION, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CHEMICAL = BLOCKS.register("explosive_chemical", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_CHEMICAL, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ANVIL = BLOCKS.register("explosive_anvil", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_ANVIL, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_REPULSIVE = BLOCKS.register("explosive_repulsive", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_REPULSIVE, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ATTRACTIVE = BLOCKS.register("explosive_attractive", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_ATTRACTIVE, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_COLOR = BLOCKS.register("explosive_color", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_COLOR, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SMOKE = BLOCKS.register("explosive_smoke", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_SMOKE, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_FRAGMENTATION = BLOCKS.register("explosive_fragmentation", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_FRAGMENTATION, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_CONTAGIOUS = BLOCKS.register("explosive_contagious", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_CONTAGIOUS, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_SONIC = BLOCKS.register("explosive_sonic", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_SONIC, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_THERMOBARIC = BLOCKS.register("explosive_thermobaric", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_THERMOBARIC, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_NUCLEAR = BLOCKS.register("explosive_nuclear", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_NUCLEAR, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_EMP = BLOCKS.register("explosive_emp", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_EMP, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_EXOTHERMIC = BLOCKS.register("explosive_exothermic", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_EXOTHERMIC, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ENDOTHERMIC = BLOCKS.register("explosive_endothermic", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_ENDOTHERMIC, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_GRAVITY = BLOCKS.register("explosive_gravity", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_GRAVITY, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ENDER = BLOCKS.register("explosive_ender", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_ENDER, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_ANTIMATTER = BLOCKS.register("explosive_antimatter", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_ANTIMATTER, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_REDMATTER = BLOCKS.register("explosive_redmatter", () -> new BlockExplosive(ActionSystem.SPAWN_EXPLOSIVE_REDMATTER, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));

    // Non-Entity explosive blocks
    public static final RegistryObject<BlockExplosive> EXPLOSIVE_BREACHING = BLOCKS.register("explosive_breaching", () -> new BlockExplosive(ICBMExplosives.BREACHING, Block.Properties.create(Material.TNT).hardnessAndResistance(2)));
}
