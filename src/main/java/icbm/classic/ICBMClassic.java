package icbm.classic;

import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.events.ExplosiveContentRegistryEvent;
import icbm.classic.api.reg.events.ExplosiveRegistryEvent;
import icbm.classic.api.reg.events.MissileFlightLogicRegistryEvent;
import icbm.classic.api.reg.events.MissileTargetRegistryEvent;
import icbm.classic.client.ICBMCreativeTab;
import icbm.classic.command.ICBMCommands;
import icbm.classic.command.system.CommandEntryPoint;
import icbm.classic.config.ConfigItems;
import icbm.classic.config.ConfigThread;
import icbm.classic.content.blast.caps.CapabilityBlast;
import icbm.classic.content.blast.caps.CapabilityBlastVelocity;
import icbm.classic.content.entity.missile.explosive.CapabilityMissile;
import icbm.classic.content.entity.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.entity.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.entity.missile.logic.flight.DirectFlightLogic;
import icbm.classic.content.entity.missile.logic.reg.MissileFlightLogicRegistry;
import icbm.classic.content.entity.missile.targeting.BallisticTargetingData;
import icbm.classic.content.entity.missile.targeting.BasicTargetData;
import icbm.classic.content.entity.missile.targeting.reg.MissileTargetRegistry;
import icbm.classic.content.items.behavior.BombCartDispenseBehavior;
import icbm.classic.content.items.behavior.GrenadeDispenseBehavior;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.potion.PoisonContagion;
import icbm.classic.content.potion.PoisonFrostBite;
import icbm.classic.content.potion.PoisonToxin;
import icbm.classic.content.reg.ExplosiveInit;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.datafix.EntityExplosiveDataFixer;
import icbm.classic.datafix.EntityGrenadeDataFixer;
import icbm.classic.datafix.TileExplosivesDataFixer;
import icbm.classic.datafix.TileRadarStationDataFixer;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.ex.CapabilityExplosive;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.capability.missile.CapabilityMissileStack;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.EnergySystemFE;
import icbm.classic.lib.explosive.reg.ExBlockContentReg;
import icbm.classic.lib.explosive.reg.ExGrenadeContentReg;
import icbm.classic.lib.explosive.reg.ExMinecartContentReg;
import icbm.classic.lib.explosive.reg.ExMissileContentReg;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import icbm.classic.lib.network.netty.PacketManager;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.prefab.item.LootEntryItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
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
@Mod(modid = ICBMConstants.DOMAIN, name = "ICBM-Classic", version = "@VERSION@")
@Mod.EventBusSubscriber
public final class ICBMClassic
{

    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    @Mod.Instance(ICBMConstants.DOMAIN)
    public static ICBMClassic INSTANCE;

    @Mod.Metadata(ICBMConstants.DOMAIN)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "icbm.classic.client.ClientProxy", serverSide = "icbm.classic.CommonProxy")
    public static CommonProxy proxy;

    public static final int MAP_HEIGHT = 255;

    private static final Logger logger = LogManager.getLogger(ICBMConstants.DOMAIN);


    public static final PacketManager packetHandler = new PacketManager(ICBMConstants.DOMAIN);

    //Mod support
    public static Block blockRadioactive = Blocks.MYCELIUM; //TODO implement

    public static final ContagiousPoison poisonous_potion = new ContagiousPoison("Chemical", 0, false);
    public static final ContagiousPoison contagios_potion = new ContagiousPoison("Contagious", 1, true);

    public static final ICBMCreativeTab CREATIVE_TAB = new ICBMCreativeTab(ICBMConstants.DOMAIN);

    public static ModFixs modFixs;

    @SubscribeEvent
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
    }

    @SubscribeEvent
    public static void registerLoot(LootTableLoadEvent event)
    {
        ///setblock ~ ~ ~ minecraft:chest 0 replace {LootTable:"minecraft:chests/simple_dungeon"}
        final String VANILLA_LOOT_POOL_ID = "main";
        if (event.getName().equals(LootTableList.CHESTS_ABANDONED_MINESHAFT) || event.getName().equals(LootTableList.CHESTS_SIMPLE_DUNGEON))
        {
            if (ConfigItems.ENABLE_LOOT_DROPS)
            {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null && ConfigItems.ENABLE_CRAFTING_ITEMS)
                {
                    if (ConfigItems.ENABLE_INGOTS_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "ingot.copper", ItemReg.itemIngot.getStack("copper", 10), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "ingot.steel", ItemReg.itemIngot.getStack("steel", 10), 20, 3));
                    }
                    if (ConfigItems.ENABLE_PLATES_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "plate.steel", ItemReg.itemPlate.getStack("steel", 5), 30, 3));
                    }
                    if (ConfigItems.ENABLE_WIRES_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "wire.copper", ItemReg.itemWire.getStack("copper", 20), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "wire.gold", ItemReg.itemWire.getStack("gold", 15), 30, 3));
                    }
                    if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "circuit.basic", ItemReg.itemCircuit.getStack("basic", 15), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "circuit.advanced", ItemReg.itemCircuit.getStack("advanced", 11), 30, 3));
                        lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "circuit.elite", ItemReg.itemCircuit.getStack("elite", 8), 30, 3));
                    }
                }
            }
        } else if (event.getName().equals(LootTableList.ENTITIES_CREEPER))
        {
            if (ConfigItems.ENABLE_SULFUR_LOOT_DROPS)
            {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null)
                {
                    lootPool.addEntry(new LootEntryItemStack(ICBMConstants.PREFIX + "sulfur", new ItemStack(ItemReg.itemSulfurDust, 10, 0), 2, 0));
                }
            }
        }
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //Verify that our nbt tag strings are distinct. If this fails then this will crash Minecraft!
        NBTConstants.ensureThatAllTagNamesAreDistinct();

        proxy.preInit();
        EnergySystem.register(new EnergySystemFE());

        //Register caps
        CapabilityEMP.register();
        CapabilityMissile.register();
        CapabilityExplosive.register();
        CapabilityBlast.register();
        CapabilityBlastVelocity.register();
        CapabilityMissileHolder.register();
        CapabilityMissileStack.register();

        //Register data fixers
        modFixs = FMLCommonHandler.instance().getDataFixer().init(ICBMConstants.DOMAIN, 1);
        modFixs.registerFix(FixTypes.ENTITY, new EntityExplosiveDataFixer());
        modFixs.registerFix(FixTypes.ENTITY, new EntityGrenadeDataFixer());
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileExplosivesDataFixer());
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileRadarStationDataFixer());

        MinecraftForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        handleMissileTargetRegistry();
        handleMissileFlightRegistry();
        handleExRegistry(event.getModConfigurationDirectory());
    }

    private void handleMissileTargetRegistry()
    {
        final MissileTargetRegistry registry = new MissileTargetRegistry();
        ICBMClassicAPI.MISSILE_TARGET_DATA_REGISTRY = registry;

        registry.register(BasicTargetData.REG_NAME, BasicTargetData::new);
        registry.register(BallisticTargetingData.REG_NAME, BallisticTargetingData::new);

        //Fire registry event
        MinecraftForge.EVENT_BUS.post(new MissileTargetRegistryEvent(registry));

        //Lock to prevent late registry
        registry.lock();
    }

    private void handleMissileFlightRegistry()
    {
        final MissileFlightLogicRegistry registry = new MissileFlightLogicRegistry();
        ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY = registry;

        registry.register(DirectFlightLogic.REG_NAME, DirectFlightLogic::new);
        registry.register(BallisticFlightLogic.REG_NAME, BallisticFlightLogic::new);
        registry.register(DeadFlightLogic.REG_NAME, DeadFlightLogic::new);


        //Fire registry event
        MinecraftForge.EVENT_BUS.post(new MissileFlightLogicRegistryEvent(registry));

        //Lock to prevent late registry
        registry.lock();
    }

    private void handleExRegistry(File configMainFolder)
    {
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        ICBMClassicAPI.EX_BLOCK_REGISTRY = new ExBlockContentReg();
        ICBMClassicAPI.EX_GRENADE_REGISTRY = new ExGrenadeContentReg();
        ICBMClassicAPI.EX_MINECART_REGISTRY = new ExMinecartContentReg();
        ICBMClassicAPI.EX_MISSILE_REGISTRY = new ExMissileContentReg();

        //Load data
        explosiveRegistry.loadReg(new File(configMainFolder, "icbmclassic/explosive_reg.json"));

        //Register default content types
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_BLOCK_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_GRENADE_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_MISSILE_REGISTRY);
        explosiveRegistry.registerContentRegistry(ICBMClassicAPI.EX_MINECART_REGISTRY);

        //Fire registry events for content types
        MinecraftForge.EVENT_BUS.post(new ExplosiveContentRegistryEvent(explosiveRegistry));

        //Lock content types, done to prevent errors with adding content
        explosiveRegistry.lockNewContentTypes();

        //Register internal first to reserve slots for backwards compatibility
        ExplosiveInit.init();

        //Fire registry event for explosives
        MinecraftForge.EVENT_BUS.post(new ExplosiveRegistryEvent(explosiveRegistry));
        explosiveRegistry.lockNewExplosives();

        //Do default content types per explosive
        explosiveRegistry.getExplosives().stream().filter(ex -> ex.getTier() != EnumTier.NONE).forEach(ex -> ICBMClassicAPI.EX_BLOCK_REGISTRY.enableContent(ex.getRegistryName()));
        explosiveRegistry.getExplosives().stream().filter(ex -> ex.getTier() != EnumTier.NONE).forEach(ex -> ICBMClassicAPI.EX_MISSILE_REGISTRY.enableContent(ex.getRegistryName()));
        explosiveRegistry.getExplosives().stream().filter(ex -> ex.getTier() != EnumTier.NONE).forEach(ex -> ICBMClassicAPI.EX_MINECART_REGISTRY.enableContent(ex.getRegistryName()));
        explosiveRegistry.getExplosives().stream().filter(ex -> ex.getTier() == EnumTier.ONE).forEach(ex -> ICBMClassicAPI.EX_GRENADE_REGISTRY.enableContent(ex.getRegistryName()));
        //TODO configs to disable types per explosive
        //TODO mesh mapper to match model to state

        //Lock all registry, done to prevent errors in data generation for renders and content
        explosiveRegistry.completeLock();

        //Save registry, at this point everything should be registered
        explosiveRegistry.saveReg();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        packetHandler.init();
        CREATIVE_TAB.init();

        if (ConfigItems.ENABLE_CRAFTING_ITEMS)
        {
            if (ConfigItems.ENABLE_INGOTS_ITEMS)
            {
                ItemReg.itemIngot.registerOreNames();
            }

            if (ConfigItems.ENABLE_PLATES_ITEMS)
            {
                ItemReg.itemPlate.registerOreNames("iron");
            }

            if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
            {
                ItemReg.itemCircuit.registerOreNames();
            }

            if (ConfigItems.ENABLE_WIRES_ITEMS)
            {
                ItemReg.itemWire.registerOreNames();
            }
        }

        OreDictionary.registerOre("dustSulfur", new ItemStack(ItemReg.itemSulfurDust));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(ItemReg.itemSaltpeterDust));

        /** Potion Effects */ //TODO move to effect system
        PoisonToxin.INSTANCE = MobEffects.POISON;//new PoisonToxin(true, 5149489, "toxin");
        PoisonContagion.INSTANCE = MobEffects.POISON;//new PoisonContagion(false, 5149489, "virus");
        PoisonFrostBite.INSTANCE = MobEffects.POISON;//new PoisonFrostBite(false, 5149489, "frostBite");

        /** Dispenser Handler */
        if (ItemReg.itemGrenade != null)
        {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ItemReg.itemGrenade, new GrenadeDispenseBehavior());
        }

        if (ItemReg.itemBombCart != null)
        {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ItemReg.itemBombCart, new BombCartDispenseBehavior());
        }
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
    }


    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        //Get command manager
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);

        //Setup commands
        ICBMCommands.init();

        //Register main command
        serverCommandManager.registerCommand(new CommandEntryPoint("icbm", ICBMCommands.ICBM_COMMAND));

        WorkerThreadManager.INSTANCE = new WorkerThreadManager(ConfigThread.THREAD_COUNT);
        WorkerThreadManager.INSTANCE.startThreads();
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
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
