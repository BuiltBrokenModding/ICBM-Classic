package icbm.classic;

import com.mojang.brigadier.CommandDispatcher;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.events.*;
import icbm.classic.client.ClientProxy;
import icbm.classic.client.particle.AntimatterParticle;
import icbm.classic.client.particle.IcbmSmokeParticle;
import icbm.classic.client.particle.LauncherSmokeParticle;
import icbm.classic.client.particle.StaleSmokeParticle;
import icbm.classic.command.BlastCommand;
import icbm.classic.command.IcbmCommand;
import icbm.classic.config.ConfigItems;
import icbm.classic.config.ConfigThread;
import icbm.classic.core.registries.IcbmBuiltinRegistries;
import icbm.classic.datafix.*;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import icbm.classic.lib.capability.gps.CapabilityGPSData;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.capability.launcher.CapabilityMissileLauncher;
import icbm.classic.lib.capability.launcher.data.LauncherStatus;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.EnergySystemFE;
import icbm.classic.lib.explosive.reg.*;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.CapabilityRadio;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.tracker.EventTracker;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.prefab.item.LootEntryItemStack;
import icbm.classic.world.*;
import icbm.classic.world.blast.caps.CapabilityBlast;
import icbm.classic.world.blast.caps.CapabilityBlastVelocity;
import icbm.classic.world.block.launcher.screen.BlockScreenCause;
import icbm.classic.world.effect.ContagiousPoison;
import icbm.classic.world.effect.PoisonContagion;
import icbm.classic.world.effect.PoisonFrostBite;
import icbm.classic.world.effect.PoisonToxin;
import icbm.classic.world.entity.flyingblock.FlyingBlock;
import icbm.classic.world.item.behavior.BombCartDispenseBehavior;
import icbm.classic.world.item.behavior.GrenadeDispenseBehavior;
import icbm.classic.world.missile.MissilePartRegistry;
import icbm.classic.world.missile.entity.CapabilityMissile;
import icbm.classic.world.missile.entity.anti.SAMTargetData;
import icbm.classic.world.missile.logic.flight.*;
import icbm.classic.world.missile.logic.flight.move.MoveByFacingLogic;
import icbm.classic.world.missile.logic.flight.move.MoveForTicksLogic;
import icbm.classic.world.missile.logic.source.cause.BlockCause;
import icbm.classic.world.missile.logic.source.cause.EntityCause;
import icbm.classic.world.missile.logic.source.cause.RedstoneCause;
import icbm.classic.world.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.world.missile.logic.targeting.BasicTargetData;
import icbm.classic.world.reg.ExplosiveInit;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.LootPool;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLConstructModEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Mod class for ICBM Classic, contains all loading code and references to objects crated by the mod.
 *
 * @author Dark(DarkGuardsman, Robert).
 * <p>
 * Orginal author and creator of the mod: Calclavia
 */
@Mod(IcbmConstants.MOD_ID)
@Mod.EventBusSubscriber
public class ICBMClassic {
    public static final int DATA_FIXER_VERSION = 5;

    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static CommonProxy proxy = FMLEnvironment.dist.isClient() ? new ClientProxy() : new CommonProxy();

    public static final int MAP_HEIGHT = 255;

    @Deprecated
    private static final Logger logger = LogManager.getLogger(IcbmConstants.MOD_ID);

    //Mod support
    public static Block blockRadioactive = Blocks.MYCELIUM; //TODO implement

    public static final ContagiousPoison chemicalPotion = new ContagiousPoison("Chemical", 0, false);
    public static final ContagiousPoison contagiousPotion = new ContagiousPoison("Contagious", 1, true);

    public static final EventTracker MAIN_TRACKER = new EventTracker();

    public ICBMClassic(IEventBus bus) {
        IcbmBlockEntityTypes.REGISTER.register(bus);
        IcbmBlocks.REGISTER.register(bus);
        IcbmCreativeModeTabs.REGISTER.register(bus);
        IcbmDamageTypes.REGISTER.register(bus);
        IcbmEntityTypes.REGISTER.register(bus);
        IcbmItems.REGISTER.register(bus);
        IcbmMobEffects.REGISTER.register(bus);
        IcbmParticleTypes.REGISTER.register(bus);
        IcbmPotions.REGISTER.register(bus);
    }

    @SubscribeEvent
    static void registerRegistries(NewRegistryEvent event) {
        event.register(IcbmBuiltinRegistries.EXPLOSIVES);
        event.register(IcbmBuiltinRegistries.MISSILES);
        event.register(IcbmBuiltinRegistries.MISSILE_FLIGHT_LOGIC);
        event.register(IcbmBuiltinRegistries.MISSILE_CAUSE);
        event.register(IcbmBuiltinRegistries.MISSILE_TARGETS);
        event.register(IcbmBuiltinRegistries.GRENADES);
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        if (ConfigItems.ENABLE_CRAFTING_ITEMS) {
            if (ConfigItems.ENABLE_INGOTS_ITEMS) {
                //Steel clump -> Steel ingot
                GameRegistry.addSmelting(new ItemStack(IcbmItems.itemIngotClump, 1, 0), new ItemStack(IcbmItems.itemIngot, 1, 0), 0.1f);
            }

            if (ConfigItems.ENABLE_PLATES_ITEMS) {
                //Fix for removing recipe of plate
                GameRegistry.addSmelting(IcbmItems.itemPlate.getStack("iron", 1), new ItemStack(Items.IRON_INGOT), 0f);
            }
        }

        GameRegistry.addSmelting(new ItemStack(IcbmItems.itemSaltpeterBall, 1, 0), new ItemStack(IcbmItems.itemSaltpeterDust, 1, 0), 0.1f);
    }

    @SubscribeEvent
    public static void registerLoot(LootTableLoadEvent event) {
        ///setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/simple_dungeon"}
        final String VANILLA_LOOT_POOL_ID = "main";
        if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT) || event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON)) {
            if (ConfigItems.ENABLE_LOOT_DROPS) {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null && ConfigItems.ENABLE_CRAFTING_ITEMS) {
                    if (ConfigItems.ENABLE_INGOTS_ITEMS) {
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "ingot.copper", IcbmItems.itemIngot.getStack("copper", 10), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "ingot.steel", IcbmItems.itemIngot.getStack("steel", 10), 20, 3));
                    }
                    if (ConfigItems.ENABLE_PLATES_ITEMS) {
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "plate.steel", IcbmItems.itemPlate.getStack("steel", 5), 30, 3));
                    }
                    if (ConfigItems.ENABLE_WIRES_ITEMS) {
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "wire.copper", IcbmItems.itemWire.getStack("copper", 20), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "wire.gold", IcbmItems.itemWire.getStack("gold", 15), 30, 3));
                    }
                    if (ConfigItems.ENABLE_CIRCUIT_ITEMS) {
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "circuit.basic", IcbmItems.itemCircuit.getStack("basic", 15), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "circuit.advanced", IcbmItems.itemCircuit.getStack("advanced", 11), 30, 3));
                        lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "circuit.elite", IcbmItems.itemCircuit.getStack("elite", 8), 30, 3));
                    }
                }
            }
        } else if (event.getName().equals(LootTableList.ENTITIES_CREEPER) || event.getName().equals(LootTableList.ENTITIES_BLAZE)) {
            if (ConfigItems.ENABLE_SULFUR_LOOT_DROPS) {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null) {
                    lootPool.addEntry(new LootEntryItemStack(IcbmConstants.PREFIX + "sulfur", new ItemStack(IcbmItems.itemSulfurDust, 10, 0), 2, 0));
                }
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(IcbmParticleTypes.STALE_SMOKE.get(), StaleSmokeParticle.Provider::new);
        event.registerSpriteSet(IcbmParticleTypes.ICBM_SMOKE.get(), IcbmSmokeParticle.Provider::new);
        event.registerSpriteSet(IcbmParticleTypes.LAUNCHER_SMOKE.get(), LauncherSmokeParticle.Provider::new);
        event.registerSpriteSet(IcbmParticleTypes.ANTIMATTER.get(), AntimatterParticle.Provider::new);
    }


    @SubscribeEvent
    public void preInit(FMLConstructModEvent event) {
        proxy.preInit();
        EnergySystem.register(new EnergySystemFE());

        //Register caps
        registerCapabilities();

        //Register data fixers
        modFixs = FMLCommonHandler.instance().getDataFixer().init(IcbmConstants.MOD_ID, DATA_FIXER_VERSION);
        modFixs.registerFix(FixTypes.ENTITY, new EntityExplosiveDataFixer());
        modFixs.registerFix(FixTypes.ENTITY, new EntityBombCartDataFixer());
        modFixs.registerFix(FixTypes.ENTITY, new EntityGrenadeDataFixer());
        modFixs.registerFix(FixTypes.ENTITY, EntityMissileDataFixer.INSTANCE);
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileExplosivesDataFixer());
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileRadarStationDataFixer());
        modFixs.registerFix(FixTypes.ITEM_INSTANCE, new ItemStackDataFixer());

        MinecraftForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        handleMissileTargetRegistry();
        handleMissileFlightRegistry();
        handleMissileCauseRegistry();
        handleStatusRegistry();
        handleExRegistry(event.getModConfigurationDirectory());
    }

    void registerCapabilities() {
        CapabilityEMP.register();
        CapabilityMissile.register();
        CapabilityExplosive.register();
        CapabilityBlast.register();
        CapabilityBlastVelocity.register();
        CapabilityMissileHolder.register();
        CapabilityMissileStack.register();
        CapabilityMissileLauncher.register();
        CapabilityRadio.register();
        CapSpaceChicken.register();
        CapabilityGPSData.register();
    }

    void handleMissileTargetRegistry() {
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY = new MissilePartRegistry<IMissileTarget>("TARGET_DATA");

        // Default types
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BasicTargetData.REG_NAME, BasicTargetData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BallisticTargetingData.REG_NAME, BallisticTargetingData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(SAMTargetData.REG_NAME, () -> null); //Can't be restored from save but reserving name

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileTargetRegistryEvent(ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY));

        //Lock to prevent late registry
        ((MissilePartRegistry) ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY).lock();
    }

    void handleMissileFlightRegistry() {
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = new MissilePartRegistry<IMissileFlightLogic>("FLIGHT_LOGIC");

        // Register defaults
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DirectFlightLogic.REG_NAME, DirectFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(BallisticFlightLogicOld.REG_NAME, BallisticFlightLogicOld::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(ArcFlightLogic.REG_NAME, ArcFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(WarmupFlightLogic.REG_NAME, WarmupFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByFacingLogic.REG_NAME, MoveByFacingLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveForTicksLogic.REG_NAME, MoveForTicksLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(FollowTargetLogic.REG_NAME, FollowTargetLogic::new);


        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileFlightLogicRegistryEvent(ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY));

        //Lock to prevent late registry
        ((MissilePartRegistry) ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY).lock();
    }

    void handleMissileCauseRegistry() {
        ICBMClassicAPI.MISSILE_CAUSE_REGISTRY = new MissilePartRegistry<IMissileCause>("CAUSE_DATA");
        ;

        // Register defaults
        ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.register(EntityCause.REG_NAME, EntityCause::new);
        ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.register(BlockCause.REG_NAME, BlockCause::new);
        ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.register(BlockScreenCause.REG_NAME, BlockScreenCause::new);
        ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.register(RedstoneCause.REG_NAME, RedstoneCause::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileCauseRegistryEvent(ICBMClassicAPI.MISSILE_CAUSE_REGISTRY));

        //Lock to prevent late registry
        ((MissilePartRegistry) ICBMClassicAPI.MISSILE_CAUSE_REGISTRY).lock();
    }

    void handleStatusRegistry() {
        ICBMClassicAPI.ACTION_STATUS_REGISTRY = new MissilePartRegistry<IActionStatus>("ACTION_STATUS");

        // Register defaults
        LauncherStatus.registerTypes();

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ActionStatusRegistryEvent(ICBMClassicAPI.ACTION_STATUS_REGISTRY));

        //Lock to prevent late registry
        ((MissilePartRegistry) ICBMClassicAPI.ACTION_STATUS_REGISTRY).lock();
    }

    void handleExRegistry(File configMainFolder) { // TODO: move away from singleton instances for better testing controls
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        ICBMClassicAPI.EX_BLOCK_REGISTRY = new ExBlockContentReg();
        ICBMClassicAPI.EX_GRENADE_REGISTRY = new ExGrenadeContentReg();
        ICBMClassicAPI.EX_MINECART_REGISTRY = new ExMinecartContentReg();
        ICBMClassicAPI.EX_MISSILE_REGISTRY = new ExMissileContentReg();

        //Load data
        if (configMainFolder != null) {
            explosiveRegistry.loadReg(new File(configMainFolder, "icbmclassic/explosive_reg.json"));
        }

        //Register default content types
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_BLOCK_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_GRENADE_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_MISSILE_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_MINECART_REGISTRY);

        //Fire registry events for content types
        NeoForge.EVENT_BUS.post(new ExplosiveContentRegistryEvent(explosiveRegistry));

        //Lock content types, done to prevent errors with adding content
        explosiveRegistry.lockNewContentTypes();

        //Register internal first to reserve slots for backwards compatibility
        ExplosiveInit.init();

        //Fire registry event for explosives
        NeoForge.EVENT_BUS.post(new ExplosiveRegistryEvent(explosiveRegistry));
        explosiveRegistry.lockNewExplosives();

        //Lock all registry, done to prevent errors in data generation for renders and content
        explosiveRegistry.completeLock();

        //Save registry, at this point everything should be registered
        if (configMainFolder != null) {
            explosiveRegistry.saveReg();
        }
    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent event) {
        proxy.init();
        ProjectileBlockInteraction.register();

        /** Potion Effects */ //TODO move to effect system
        PoisonToxin.INSTANCE = MobEffects.POISON;//new PoisonToxin(true, 5149489, "toxin");
        PoisonContagion.INSTANCE = MobEffects.POISON;//new PoisonContagion(false, 5149489, "virus");
        PoisonFrostBite.INSTANCE = MobEffects.POISON;//new PoisonFrostBite(false, 5149489, "frostBite");

        /** Dispenser Handler */
        IcbmItems.GRENADE.asOptional().ifPresent(grenadeItem -> {
            DispenserBlock.registerBehavior(grenadeItem, new GrenadeDispenseBehavior());
        });

        IcbmItems.BOMB_CART.asOptional().ifPresent(bombCartItem -> {
            DispenserBlock.registerBehavior(bombCartItem, new BombCartDispenseBehavior());
        });

        // Generate defaults
        FlyingBlock.loadFromConfig();
    }

    @SubscribeEvent
    public void postInit(FMLLoadCompleteEvent event) {
        proxy.postInit();
    }


    @SubscribeEvent
    public void serverStarting(ServerStartingEvent event) {
        WorkerThreadManager.INSTANCE = new WorkerThreadManager(ConfigThread.THREAD_COUNT);
        WorkerThreadManager.INSTANCE.startThreads();
    }

    @SubscribeEvent
    public void registerCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        IcbmCommand.register(dispatcher);
        BlastCommand.register(dispatcher);
    }

    @SubscribeEvent
    public void serverStopping(ServerStoppingEvent event) {
        WorkerThreadManager.INSTANCE.killThreads();
    }

    public static Logger logger() {
        return logger;
    }

    public static boolean isJUnitTest() {
        //TODO do boolean flag from VoltzTestRunner to simplify solution
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list) {
            if (element.getClassName().startsWith("org.junit.") || element.getClassName().startsWith("com.builtbroken.mc.testing.junit.VoltzTestRunner")) {
                return true;
            }
        }
        return false;
    }
}
