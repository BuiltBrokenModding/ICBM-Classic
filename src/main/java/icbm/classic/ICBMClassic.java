package icbm.classic;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.IExplosiveCustomization;
import icbm.classic.api.reg.events.*;
import icbm.classic.client.ICBMCreativeTab;
import icbm.classic.client.ICBMSounds;
import icbm.classic.command.ICBMCommands;
import icbm.classic.config.*;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.config.blocks.ConfigBlocks;
import icbm.classic.config.machines.ConfigEmpTower;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.config.machines.ConfigRadar;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.actions.emp.EmpHandler;
import icbm.classic.content.blast.ender.EnderBlastCustomization;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.cargo.CargoHolderHandler;
import icbm.classic.content.cargo.balloon.BalloonProjectileData;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.cluster.bomblet.BombletProjectileData;
import icbm.classic.content.cluster.missile.ClusterMissileHandler;
import icbm.classic.content.entity.flyingblock.FlyingBlock;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import icbm.classic.content.items.behavior.BombCartDispenseBehavior;
import icbm.classic.content.items.behavior.GrenadeDispenseBehavior;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.anti.SAMTargetData;
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import icbm.classic.content.missile.entity.itemstack.item.HeldItemMissileHandler;
import icbm.classic.content.missile.logic.flight.*;
import icbm.classic.content.missile.logic.flight.move.MoveByFacingLogic;
import icbm.classic.content.missile.logic.flight.move.MoveByVec3Logic;
import icbm.classic.content.missile.logic.flight.move.MoveForTicksLogic;
import icbm.classic.content.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.radioactive.RadioactiveHandler;
import icbm.classic.content.reg.*;
import icbm.classic.lib.actions.ActionSystem;
import icbm.classic.lib.buildable.BuildableObjectRegistry;
import icbm.classic.lib.config.ConfigLoader;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.EnergySystemFE;
import icbm.classic.lib.explosive.reg.*;
import icbm.classic.lib.network.netty.PacketManager;
import icbm.classic.lib.projectile.ProjectileDataRegistry;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.tracker.EventTracker;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.LootTableLoadEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

/**
 * Mod class for ICBM Classic, contains all loading code and references to objects crated by the mod.
 *
 * @author Dark(DarkGuardsman, Robin).
 * <p>
 * Orginal author and creator of the mod: Calclavia
 */
@Mod(ICBMConstants.DOMAIN)
public class ICBMClassic
{
    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static ICBMClassic INSTANCE;

    /** Client proxy on the physical client, common proxy on dedicated servers */
    public static CommonProxy proxy;

    public static final int MAP_HEIGHT = 255;

    @Deprecated
    private static final Logger logger = LogManager.getLogger(ICBMConstants.DOMAIN);

    public static final PacketManager packetHandler = new PacketManager(ICBMConstants.DOMAIN);

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, ICBMConstants.DOMAIN);
    public static final DeferredHolder<MobEffect, ContagiousPoison> chemicalPotion = EFFECTS.register("chemical", () -> new ContagiousPoison("Chemical", 0, false));
    public static final DeferredHolder<MobEffect, ContagiousPoison> contagiousPotion = EFFECTS.register("contagious", () -> new ContagiousPoison("Contagious", 1, true));

    public static final EventTracker MAIN_TRACKER = new EventTracker();

    /** Set once common setup has run, config reload listeners are ignored before that */
    private static volatile boolean setupDone = false;

    public ICBMClassic(IEventBus modBus, ModContainer container)
    {
        INSTANCE = this;
        proxy = createProxy();

        // Configs first, ConfigItems (STARTUP) is read while building the item registry
        registerConfigs(container);

        // Content registries
        ComponentReg.register(modBus);
        AttachmentReg.register(modBus);
        BlockReg.register(modBus);
        ItemReg.register(modBus);
        EntityReg.register(modBus);
        MenuReg.register(modBus);
        RecipeReg.register(modBus);
        EFFECTS.register(modBus);
        ICBMSounds.register(modBus);
        ICBMCreativeTab.register(modBus);

        // Packet codexes, ids come from registration order so they must all be registered here, in a fixed order
        TileEMPTower.register();
        TileRadarStation.register();
        TileLauncherBase.register();
        TileLauncherScreen.register();
        TileCruiseLauncher.register();
        EntityMissile.register();
        CapSpaceChicken.register();

        // Mod bus listeners
        modBus.addListener(CapabilityReg::registerCapabilities);
        modBus.addListener(PacketManager::registerPayloads);
        modBus.addListener(ModConfigEvent.Loading.class, ConfigLoader::onConfigEvent);
        modBus.addListener(ModConfigEvent.Reloading.class, ConfigLoader::onConfigEvent);
        modBus.addListener(this::commonSetup);

        // Game bus listeners
        NeoForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        NeoForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        NeoForge.EVENT_BUS.addListener(ICBMClassic::registerLoot);
        NeoForge.EVENT_BUS.addListener(ICBMClassic::registerCommands);
        NeoForge.EVENT_BUS.addListener(ICBMClassic::serverStarting);
        NeoForge.EVENT_BUS.addListener(ICBMClassic::serverStopping);

        EnergySystem.register(new EnergySystemFE());
        proxy.preInit(modBus);
    }

    private static CommonProxy createProxy()
    {
        if (FMLEnvironment.dist.isClient())
        {
            try
            {
                // Loaded reflectively so the dedicated server never touches client classes
                return (CommonProxy) Class.forName("icbm.classic.client.ClientProxy").getDeclaredConstructor().newInstance();
            }
            catch (ReflectiveOperationException e)
            {
                throw new RuntimeException("Failed to create ICBM client proxy", e);
            }
        }
        return new CommonProxy();
    }

    private static void registerConfigs(ModContainer container)
    {
        ConfigLoader.register(container, ConfigItems.class);
        ConfigLoader.register(container, ConfigMain.class);
        ConfigLoader.register(container, ConfigBlast.class);
        ConfigLoader.register(container, ConfigBlocks.class);
        ConfigLoader.register(container, ConfigBattery.class);
        ConfigLoader.register(container, ConfigDebug.class);
        ConfigLoader.register(container, ConfigFlyingBlocks.class);
        ConfigLoader.register(container, ConfigJEI.class);
        ConfigLoader.register(container, ConfigThread.class);
        ConfigLoader.register(container, ConfigMissile.class);
        ConfigLoader.register(container, ConfigEmpTower.class);
        ConfigLoader.register(container, ConfigLauncher.class);
        ConfigLoader.register(container, ConfigRadar.class);
        ConfigLoader.register(container, ConfigClient.class);

        ConfigLoader.addReloadListener(() -> {
            if (!setupDone)
            {
                return;
            }
            // PORT: 1.12 reloaded on the main thread (ConfigChangedEvent). NeoForge fires ModConfigEvent.Reloading on its
            //  config file-watcher thread (toml edited on disk) or on the client thread (config screen). The handler lists
            //  rebuilt here are read by the server thread every tick, so the rebuild is handed to the server thread.
            final MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            if (server != null && !server.isSameThread())
            {
                server.execute(ConfigMain::onConfigReloaded);
            }
            else
            {
                ConfigMain.onConfigReloaded();
            }
        });
    }

    private void commonSetup(FMLCommonSetupEvent event)
    {
        // Common configs are loaded by now, explosive data reads blast config values
        handleMissileTargetRegistry();
        handleMissileFlightRegistry();
        ActionSystem.setup();
        handleExplosiveCustomizationRegistry();
        handleExRegistry(FMLPaths.CONFIGDIR.get().toFile());
        handleProjectileDataRegistry();

        ProjectileBlockInteraction.register();
        ClusterMissileHandler.setup();
        CargoHolderHandler.setup();
        HeldItemMissileHandler.setup();
        RadioactiveHandler.setup();
        ProtectiveArmorHandler.setup();
        EmpHandler.setup();

        // Needs to lock late as we need content to register some types
        ((ProjectileDataRegistry) ICBMClassicAPI.PROJECTILE_DATA_REGISTRY).registerVanillaDefaults();
        NeoForge.EVENT_BUS.post(new ProjectileDataRegistryEvent(ICBMClassicAPI.PROJECTILE_DATA_REGISTRY));
        ((ProjectileDataRegistry) ICBMClassicAPI.PROJECTILE_DATA_REGISTRY).lock();

        event.enqueueWork(() -> {
            // Dispenser handlers, the vanilla map isn't thread safe
            if (ItemReg.itemGrenade != null)
            {
                DispenserBlock.registerBehavior(ItemReg.itemGrenade.get(), new GrenadeDispenseBehavior());
            }
            if (ItemReg.itemBombCart != null)
            {
                DispenserBlock.registerBehavior(ItemReg.itemBombCart.get(), new BombCartDispenseBehavior());
            }

            // Generate defaults
            FlyingBlock.loadFromConfig();
            setupDone = true;
        });

        proxy.init();
    }

    void handleMissileTargetRegistry()
    {
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY = new BuildableObjectRegistry<IMissileTarget>("TARGET_DATA");

        // Default types
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BasicTargetData.REG_NAME, BasicTargetData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BallisticTargetingData.REG_NAME, BallisticTargetingData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(SAMTargetData.REG_NAME, () -> null); //Can't be restored from save but reserving name

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileTargetRegistryEvent(ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY).lock();
    }

    void handleMissileFlightRegistry()
    {
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = new BuildableObjectRegistry<IMissileFlightLogic>("FLIGHT_LOGIC");

        // Register defaults
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DirectFlightLogic.REG_NAME, DirectFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(BallisticFlightLogicOld.REG_NAME, BallisticFlightLogicOld::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(ArcFlightLogic.REG_NAME, ArcFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(WarmupFlightLogic.REG_NAME, WarmupFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByFacingLogic.REG_NAME, MoveByFacingLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByVec3Logic.REG_NAME, MoveByVec3Logic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveForTicksLogic.REG_NAME, MoveForTicksLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(FollowTargetLogic.REG_NAME, FollowTargetLogic::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new MissileFlightLogicRegistryEvent(ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY).lock();
    }

    void handleExplosiveCustomizationRegistry()
    {
        ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY = new BuildableObjectRegistry<IExplosiveCustomization>("EXPLOSIVE_CUSTOMIZATION");

        // Register defaults
        ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY.register(EnderBlastCustomization.NAME, EnderBlastCustomization::new);

        //Fire registry event
        NeoForge.EVENT_BUS.post(new ExplosiveCustomizationRegistryEvent(ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry) ICBMClassicAPI.EXPLOSIVE_CUSTOMIZATION_REGISTRY).lock();
    }

    void handleProjectileDataRegistry()
    {
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY = new ProjectileDataRegistry();

        // Register defaults
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(BombletProjectileData.NAME, BombletProjectileData::new);
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(ParachuteProjectileData.NAME, ParachuteProjectileData::new);
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(BalloonProjectileData.NAME, BalloonProjectileData::new);
    }

    void handleExRegistry(File configMainFolder) //TODO move away from singleton instances for better testing controls
    {
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        ICBMClassicAPI.EX_BLOCK_REGISTRY = new ExBlockContentReg();
        ICBMClassicAPI.EX_GRENADE_REGISTRY = new ExGrenadeContentReg();
        ICBMClassicAPI.EX_MINECART_REGISTRY = new ExMinecartContentReg();
        ICBMClassicAPI.EX_MISSILE_REGISTRY = new ExMissileContentReg();

        //Load data
        if (configMainFolder != null)
        {
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
        if (configMainFolder != null)
        {
            explosiveRegistry.saveReg();
        }
    }

    /**
     * 1.12 injected entries into the vanilla "main" pool. Pools are immutable in 1.21 so ICBM adds its own pool
     * with the same weights, plus an empty entry so the extra pool doesn't always drop something.
     */
    public static void registerLoot(LootTableLoadEvent event)
    {
        final ResourceLocation name = event.getName();
        if (name.equals(BuiltInLootTables.ABANDONED_MINESHAFT.location()) || name.equals(BuiltInLootTables.SIMPLE_DUNGEON.location()))
        {
            if (ConfigItems.ENABLE_LOOT_DROPS && ConfigItems.ENABLE_CRAFTING_ITEMS)
            {
                final LootPool.Builder pool = LootPool.lootPool().name(ICBMConstants.DOMAIN + ":loot").setRolls(ConstantValue.exactly(1));
                pool.add(EmptyLootItem.emptyItem().setWeight(100));
                if (ConfigItems.ENABLE_INGOTS_ITEMS)
                {
                    lootEntry(pool, () -> Items.COPPER_INGOT, 10, 15, 5);
                    lootEntry(pool, ItemReg.itemIngotSteel, 10, 20, 3);
                }
                if (ConfigItems.ENABLE_PLATES_ITEMS)
                {
                    lootEntry(pool, ItemReg.itemPlateSteel, 5, 30, 3);
                }
                if (ConfigItems.ENABLE_WIRES_ITEMS)
                {
                    lootEntry(pool, ItemReg.itemWireCopper, 20, 15, 5);
                    lootEntry(pool, ItemReg.itemWireGold, 15, 30, 3);
                }
                if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
                {
                    lootEntry(pool, ItemReg.itemCircuitBasic, 15, 15, 5);
                    lootEntry(pool, ItemReg.itemCircuitAdvanced, 11, 30, 3);
                    lootEntry(pool, ItemReg.itemCircuitElite, 8, 30, 3);
                }
                event.getTable().addPool(pool.build());
            }
        }
        else if (name.equals(EntityType.CREEPER.getDefaultLootTable().location()) || name.equals(EntityType.BLAZE.getDefaultLootTable().location()))
        {
            if (ConfigItems.ENABLE_SULFUR_LOOT_DROPS)
            {
                final LootPool.Builder pool = LootPool.lootPool().name(ICBMConstants.DOMAIN + ":sulfur").setRolls(ConstantValue.exactly(1));
                lootEntry(pool, ItemReg.itemSulfurDust, 10, 2, 0);
                event.getTable().addPool(pool.build());
            }
        }
    }

    /** Matches 1.12 LootEntryItemStack, which dropped 0 to (count - 1) items */
    private static void lootEntry(LootPool.Builder pool, Supplier<? extends ItemLike> item, int count, int weight, int quality)
    {
        if (item != null)
        {
            pool.add(LootItem.lootTableItem(item.get())
                .setWeight(weight)
                .setQuality(quality)
                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0, count - 1))));
        }
    }

    public static void registerCommands(RegisterCommandsEvent event)
    {
        ICBMCommands.register(event.getDispatcher());
    }

    public static void serverStarting(ServerStartingEvent event)
    {
        WorkerThreadManager.INSTANCE = new WorkerThreadManager(ConfigThread.THREAD_COUNT);
        WorkerThreadManager.INSTANCE.startThreads();
    }

    public static void serverStopping(ServerStoppingEvent event)
    {
        WorkerThreadManager.INSTANCE.killThreads();
    }

    public static Logger logger()
    {
        return logger;
    }

    public static boolean isJUnitTest()
    {
        //TODO do boolean flag from VoltzTestRunner to simplify solution
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        List<StackTraceElement> list = Arrays.asList(stackTrace);
        for (StackTraceElement element : list)
        {
            if (element.getClassName().startsWith("org.junit.") || element.getClassName().startsWith("com.builtbroken.mc.testing.junit.VoltzTestRunner"))
            {
                return true;
            }
        }
        return false;
    }
}
// PORTED
