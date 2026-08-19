package icbm.classic;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.parts.IMissileFlightLogic;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.reg.events.MissileFlightLogicRegistryEvent;
import icbm.classic.api.reg.events.MissileTargetRegistryEvent;
import icbm.classic.api.reg.events.ProjectileDataRegistryEvent;
import icbm.classic.client.ClientProxy;
import icbm.classic.client.ClientReg;
import icbm.classic.client.ICBMCreativeTab;
import icbm.classic.config.ConfigThread;
import icbm.classic.content.blast.caps.CapabilityBlast;
import icbm.classic.content.blast.caps.CapabilityBlastVelocity;
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
import icbm.classic.content.missile.entity.CapabilityMissile;
import icbm.classic.content.missile.entity.anti.SAMTargetData;
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
import icbm.classic.lib.capability.chicken.CapSpaceChicken;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.gps.CapabilityGPSData;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.capability.launcher.CapabilityMissileLauncher;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.EnergySystemFE;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import icbm.classic.lib.network.netty.PacketManager;
import icbm.classic.lib.projectile.CapabilityProjectileStack;
import icbm.classic.lib.projectile.ProjectileDataRegistry;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.CapabilityRadio;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.lib.tracker.EventTracker;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.datagen.BlockModelGenerator;
import icbm.datagen.BlockStateGenerator;
import icbm.datagen.ItemModelGenerator;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Mod class for ICBM Classic, contains all loading code and references to objects crated by the mod.
 *
 * @author Dark(DarkGuardsman, Robin).
 * <p>
 * Orginal author and creator of the mod: Calclavia
 */
@Mod(ICBMConstants.DOMAIN)
@Mod.EventBusSubscriber
public class ICBMClassic
{
    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    public static ICBMClassic INSTANCE;

    @OnlyIn(Dist.CLIENT)
    public static ClientProxy proxy = new ClientProxy();

    public static final int MAP_HEIGHT = 255;

    @Deprecated
    private static final Logger logger = LogManager.getLogger(ICBMConstants.DOMAIN);

    public static final ContagiousPoison chemicalPotion = new ContagiousPoison("Chemical", 0, false);
    public static final ContagiousPoison contagiousPotion = new ContagiousPoison("Contagious", 1, true);

    public static final ICBMCreativeTab CREATIVE_TAB = new ICBMCreativeTab(ICBMConstants.DOMAIN);

    public static final EventTracker MAIN_TRACKER = new EventTracker();

    public ICBMClassic() {
        INSTANCE = this;

        final IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Life cycle
        modBus.addListener(this::commonSetup);
        modBus.addListener(this::registerDatagen);

        modBus.addListener(ClientReg::clientSetup);
        modBus.addListener(ClientReg::registerAllModels);
        modBus.addListener(ClientReg::registerBlockColor);

        // Registries
        BlockReg.BLOCKS.register(modBus);
        ItemReg.ITEMS.register(modBus);
        TileReg.TILES.register(modBus);
        EntityReg.ENTITIES.register(modBus);
        ContainerReg.CONTAINER_TYPES.register(modBus);

        final IEventBus forgeBus = MinecraftForge.EVENT_BUS;
        forgeBus.addListener(this::serverStarting);
        forgeBus.addListener(this::serverStopping);

        //TODO  modEventBus.addListener(EventPriority.LOW, this::addCustomRegistryDeferredRegisters);

        //TODO ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    // TODO split to a seperate datagen module
    // TODO find a way to run this headless as a gradle pre-build step to avoid forgetting to run on changes
    private void registerDatagen(final GatherDataEvent event) {
        final DataGenerator gen = event.getGenerator();
        gen.addProvider(new BlockStateGenerator(gen, ICBMConstants.DOMAIN, event.getExistingFileHelper()));
        gen.addProvider(new BlockModelGenerator(gen, ICBMConstants.DOMAIN, event.getExistingFileHelper()));
        gen.addProvider(new ItemModelGenerator(gen, ICBMConstants.DOMAIN, event.getExistingFileHelper()));
    }

    /*@SubscribeEvent TODO likely moved to JSON
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        if (ConfigItems.ENABLE_CRAFTING_ITEMS)
        {
            if (ConfigItems.ENABLE_INGOTS_ITEMS)
            {
                //Steel clump -> Steel ingot
                GameRegistry.addSmelting(new ItemStack(ItemReg.itemIngotClump, 1, 0), new ItemStack(ItemReg.itemIngot, 1, 0), 0.1f);
            }

            if (ConfigItems.ENABLE_PLATES_ITEMS)
            {
                //Fix for removing recipe of plate
                GameRegistry.addSmelting(ItemReg.itemPlate.getStack("iron", 1), new ItemStack(Items.IRON_INGOT), 0f);
            }
        }

        GameRegistry.addSmelting(new ItemStack(ItemReg.itemSaltpeterBall, 1, 0), new ItemStack(ItemReg.itemSaltpeterDust, 1, 0), 0.1f);

        // Dynamic item recipes
        event.getRegistry().register(new RecipeCargoData(new ItemStack(ItemReg.itemBalloon), BalloonProjectileData::new).setRegistryName(new ResourceLocation(ICBMConstants.DOMAIN, "balloon_cargo")));
        event.getRegistry().register(new RecipeCargoDataDecraft(ItemReg.itemBalloon).setRegistryName(new ResourceLocation(ICBMConstants.DOMAIN, "balloon_cargo_decraft")));

        event.getRegistry().register(new RecipeCargoData(new ItemStack(ItemReg.itemParachute), ParachuteProjectileData::new).setRegistryName(new ResourceLocation(ICBMConstants.DOMAIN, "parachute_cargo")));
        event.getRegistry().register(new RecipeCargoDataDecraft(ItemReg.itemParachute).setRegistryName(new ResourceLocation(ICBMConstants.DOMAIN, "parachute_cargo_decraft")));

        event.getRegistry().register(new RecipeCluster(new ItemStack(ItemReg.itemClusterMissile)).setRegistryName(new ResourceLocation(ICBMConstants.DOMAIN, "cluster_missile")));
    }*/

   /* @SubscribeEvent TODO this may be done by JSON now?
    public static void registerLoot(LootTableLoadEvent event)
    {
        final String VANILLA_LOOT_POOL_ID = "main";
        if (event.getName().equals(EntityType.CREEPER.getLootTable()) || event.getName().equals(EntityType.BLAZE.getLootTable()))
        {
            if (ConfigItems.ENABLE_SULFUR_LOOT_DROPS)
            {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null)
                {
                    lootPool.add(new LootEntryItemStack(ICBMConstants.PREFIX + "sulfur", new ItemStack(ItemReg.itemSulfurDust, 10, 0), 2, 0));
                }
            }
        }
    }*/


    public void commonSetup(FMLCommonSetupEvent event)
    {
        PacketManager.register();
        EnergySystem.register(new EnergySystemFE());

        //Network packets
        TileEMPTower.register();
        TileRadarStation.register();
        TileLauncherBase.register();
        TileLauncherScreen.register();
        TileCruiseLauncher.register();

        //Register caps
        registerCapabilities();

        MinecraftForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        //NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        handleMissileTargetRegistry();
        handleMissileFlightRegistry();
        ActionSystem.setup();
        handleExRegistry();
        handleProjectileDataRegistry();

        CREATIVE_TAB.init();
        ProjectileBlockInteraction.register();
        ClusterMissileHandler.setup();
        CargoHolderHandler.setup();
        RadioactiveHandler.setup();

        // Needs to lock late as we need content to register some types
        ((ProjectileDataRegistry) ICBMClassicAPI.PROJECTILE_DATA_REGISTRY).registerVanillaDefaults();
        MinecraftForge.EVENT_BUS.post(new ProjectileDataRegistryEvent(ICBMClassicAPI.PROJECTILE_DATA_REGISTRY));
        ((ProjectileDataRegistry) ICBMClassicAPI.PROJECTILE_DATA_REGISTRY).lock();


        // DispenserBlock.registerDispenseBehavior(ItemReg.GRENADE_ANVIL::get, new GrenadeDispenseBehavior()); TODO
        // DispenserBlock.registerDispenseBehavior(ItemReg.itemBombCart, new BombCartDispenseBehavior()); TODO

        // Generate defaults
        FlyingBlock.loadFromConfig();
    }

    void registerCapabilities() {
        CapabilityEMP.register();
        CapabilityMissile.register();
        CapabilityBlast.register();
        CapabilityBlastVelocity.register();
        CapabilityMissileHolder.register();
        CapabilityMissileStack.register();
        CapabilityProjectileStack.register();
        CapabilityMissileLauncher.register();
        CapabilityRadio.register();
        CapSpaceChicken.register();
        CapabilityGPSData.register();
    }

    void handleMissileTargetRegistry()
    {
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY = new BuildableObjectRegistry<IMissileTarget>("TARGET_DATA");

        // Default types
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BasicTargetData.REG_NAME, BasicTargetData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(BallisticTargetingData.REG_NAME, BallisticTargetingData::new);
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY.register(SAMTargetData.REG_NAME, () -> null); //Can't be restored from save but reserving name

        //Fire registry event
        MinecraftForge.EVENT_BUS.post(new MissileTargetRegistryEvent(ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY).lock();
    }

    void handleMissileFlightRegistry()
    {
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = new BuildableObjectRegistry<IMissileFlightLogic>("FLIGHT_LOGIC");

        // Register defaults
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DirectFlightLogic.REG_NAME, DirectFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(ArcFlightLogic.REG_NAME, ArcFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(WarmupFlightLogic.REG_NAME, WarmupFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByFacingLogic.REG_NAME, MoveByFacingLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveByVec3Logic.REG_NAME, MoveByVec3Logic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(MoveForTicksLogic.REG_NAME, MoveForTicksLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.register(FollowTargetLogic.REG_NAME, FollowTargetLogic::new);


        //Fire registry event
        MinecraftForge.EVENT_BUS.post(new MissileFlightLogicRegistryEvent(ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY));

        //Lock to prevent late registry
        ((BuildableObjectRegistry)ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY).lock();
    }

    void handleProjectileDataRegistry()
    {
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY =  new ProjectileDataRegistry();

        // Register defaults
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(BombletProjectileData.NAME, BombletProjectileData::new);
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(ParachuteProjectileData.NAME, () -> new ParachuteProjectileData(EntityReg.CARGO_PARACHUTE_SIZE_1::get));
        // TODO ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(ParachuteProjectileData.NAME, () -> new ParachuteProjectileData(EntityReg.CARGO_PARACHUTE_SIZE_2::get));
        ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.register(BalloonProjectileData.NAME, () -> new BalloonProjectileData(EntityReg.CARGO_BALLOON::get));
    }

    void handleExRegistry()
    {
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        //Fire registry events for content types

        //Lock content types, done to prevent errors with adding content
        explosiveRegistry.lockNewContentTypes();

        //Register internal first to reserve slots for backwards compatibility
        ExplosiveInit.init();

        //Fire registry event for explosives
        explosiveRegistry.lockNewExplosives();

        //Lock all registry, done to prevent errors in data generation for renders and content
        explosiveRegistry.completeLock();
    }


    public void serverStarting(FMLServerStartingEvent event)
    {
        //Get command manager
        //CommandDispatcher<CommandSource> commandManager = event.getCommandDispatcher();

        //Setup commands
        //TODO ICBMCommands.init();

        //Register main command
        //TODO commandManager.register(new CommandEntryPoint("icbm", ICBMCommands.ICBM_COMMAND));

        WorkerThreadManager.INSTANCE = new WorkerThreadManager(ConfigThread.THREAD_COUNT);
        WorkerThreadManager.INSTANCE.startThreads();
    }

    public void serverStopping(FMLServerStoppingEvent event)
    {
        WorkerThreadManager.INSTANCE.killThreads();
    }

    public static Logger logger()
    {
        return logger;
    }
}
