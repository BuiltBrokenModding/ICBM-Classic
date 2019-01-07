package icbm.classic;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.events.ExplosiveRegistryEvent;
import icbm.classic.api.reg.events.ExplosiveRegistryInitEvent;
import icbm.classic.client.ICBMCreativeTab;
import icbm.classic.command.CommandICBM;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blocks.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.mobs.*;
import icbm.classic.content.blast.ExplosiveInit;
import icbm.classic.lib.explosive.reg.*;
import icbm.classic.lib.thread.WorkerThreadManager;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.blocks.explosive.TileEntityExplosive;
import icbm.classic.content.items.*;
import icbm.classic.content.blocks.battery.BlockBattery;
import icbm.classic.content.blocks.battery.TileEntityBattery;
import icbm.classic.content.blocks.emptower.BlockEmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.base.BlockLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.BlockCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.BlockRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.blocks.multiblock.BlockMultiblock;
import icbm.classic.content.blocks.multiblock.TileMulti;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.potion.PoisonContagion;
import icbm.classic.content.potion.PoisonFrostBite;
import icbm.classic.content.potion.PoisonToxin;
import icbm.classic.lib.emp.CapabilityEMP;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.energy.system.EnergySystemFE;
import icbm.classic.lib.network.netty.PacketManager;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.item.ItemBlockRotatedMultiTile;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import icbm.classic.prefab.item.ItemICBMBase;
import icbm.classic.prefab.item.LootEntryItemStack;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.EntityEntryBuilder;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
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
@Mod(modid = ICBMClassic.DOMAIN, name = "ICBM-Classic", version = ICBMClassic.VERSION, dependencies = ICBMClassic.DEPENDENCIES)
@Mod.EventBusSubscriber
public final class ICBMClassic
{

    public static final boolean runningAsDev = System.getProperty("development") != null && System.getProperty("development").equalsIgnoreCase("true");

    @Mod.Instance(ICBMClassic.DOMAIN)
    public static ICBMClassic INSTANCE;

    @Mod.Metadata(ICBMClassic.DOMAIN)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "icbm.classic.client.ClientProxy", serverSide = "icbm.classic.CommonProxy")
    public static CommonProxy proxy;

    public static final String DOMAIN = "icbmclassic";
    public static final String PREFIX = DOMAIN + ":";

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String MC_VERSION = "@MC@";
    public static final String VERSION = MC_VERSION + "-" + MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    public static final String DEPENDENCIES = "";

    public static final String TEXTURE_DIRECTORY = "textures/";
    public static final String GUI_DIRECTORY = TEXTURE_DIRECTORY + "gui/";

    public static final int ENTITY_ID_PREFIX = 50;
    public static final int MAP_HEIGHT = 255;

    protected static Logger logger = LogManager.getLogger(DOMAIN);
    private static int nextEntityID = ENTITY_ID_PREFIX;


    public static final PacketManager packetHandler = new PacketManager(DOMAIN);

    //Mod support
    public static Block blockRadioactive = Blocks.MYCELIUM; //TODO implement

    // Blocks
    public static Block blockGlassPlate;
    public static Block blockGlassButton;
    public static Block blockSpikes;
    public static Block blockCamo; //TODO re-implement
    public static Block blockConcrete;
    public static Block blockReinforcedGlass;
    public static Block blockExplosive;

    public static Block blockLaunchBase;
    public static Block blockLaunchScreen;
    public static Block blockLaunchSupport;
    public static Block blockRadarStation;
    public static Block blockEmpTower;
    public static Block blockCruiseLauncher; //TODO re-implement
    public static Block blockMissileCoordinator; //TODO re-implement

    public static Block blockBattery;

    public static Block multiBlock;


    // Items
    public static Item itemAntidote;
    public static Item itemSignalDisrupter;
    public static Item itemTracker;
    public static Item itemMissile;
    public static Item itemDefuser;
    public static Item itemRadarGun;
    public static Item itemRemoteDetonator;
    public static Item itemLaserDesignator;
    public static Item itemRocketLauncher;
    public static Item itemGrenade;
    public static Item itemBombCart;
    public static Item itemSulfurDust;
    public static Item itemSaltpeterDust;
    public static Item itemPoisonPowder;
    public static Item itemBattery;

    public static ItemCrafting itemIngot;
    public static ItemCrafting itemIngotClump;
    public static ItemCrafting itemPlate;
    public static ItemCrafting itemCircuit;
    public static ItemCrafting itemWire;

    public static final ContagiousPoison poisonous_potion = new ContagiousPoison("Chemical", 0, false);
    public static final ContagiousPoison contagios_potion = new ContagiousPoison("Contagious", 1, true);

    public static final ICBMCreativeTab CREATIVE_TAB = new ICBMCreativeTab(DOMAIN);

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        //Items
        event.getRegistry().register(itemGrenade = new ItemGrenade());
        event.getRegistry().register(itemBombCart = new ItemBombCart());
        event.getRegistry().register(itemPoisonPowder = new ItemICBMBase("poisonPowder"));
        event.getRegistry().register(itemSulfurDust = new ItemICBMBase("sulfurDust"));
        event.getRegistry().register(itemSaltpeterDust = new ItemICBMBase("saltpeter"));
        event.getRegistry().register(itemAntidote = new ItemAntidote());
        event.getRegistry().register(itemSignalDisrupter = new ItemSignalDisrupter());
        event.getRegistry().register(itemTracker = new ItemTracker());
        event.getRegistry().register(itemDefuser = new ItemDefuser());
        event.getRegistry().register(itemRadarGun = new ItemRadarGun());
        event.getRegistry().register(itemRemoteDetonator = new ItemRemoteDetonator());
        event.getRegistry().register(itemLaserDesignator = new ItemLaserDetonator());
        event.getRegistry().register(itemRocketLauncher = new ItemRocketLauncher());
        event.getRegistry().register(itemMissile = new ItemMissile());

        //Block items
        event.getRegistry().register(new ItemBlock(blockGlassPlate).setRegistryName(blockGlassPlate.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockGlassButton).setRegistryName(blockGlassButton.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockSpikes));
        event.getRegistry().register(new ItemBlockSubTypes(blockConcrete));
        event.getRegistry().register(new ItemBlock(blockReinforcedGlass).setRegistryName(blockReinforcedGlass.getRegistryName()));
        event.getRegistry().register(new ItemBlockExplosive(blockExplosive).setRegistryName(blockExplosive.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockEmpTower).setRegistryName(blockEmpTower.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockRadarStation).setRegistryName(blockRadarStation.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockLaunchSupport));
        event.getRegistry().register(new ItemBlockRotatedMultiTile(blockLaunchBase, e -> TileLauncherBase.getLayoutOfMultiBlock(e)));
        event.getRegistry().register(new ItemBlockSubTypes(blockLaunchScreen));
        event.getRegistry().register(new ItemBlockSubTypes(blockBattery));
        event.getRegistry().register(new ItemBlock(blockCruiseLauncher).setRegistryName(blockCruiseLauncher.getRegistryName()));

        //Crafting resources
        if (ConfigItems.ENABLE_CRAFTING_ITEMS)
        {
            if (ConfigItems.ENABLE_INGOTS_ITEMS)
            {
                event.getRegistry().register(itemIngot = new ItemCrafting("ingot", "steel", "copper"));
                event.getRegistry().register(itemIngotClump = new ItemCrafting("clump", "steel"));
                itemIngot.registerOreNames();
            }
            if (ConfigItems.ENABLE_PLATES_ITEMS)
            {
                event.getRegistry().register(itemPlate = new ItemCrafting("plate", "steel", "iron"));
                itemPlate.registerOreNames("iron");
            }
            if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
            {
                event.getRegistry().register(itemCircuit = new ItemCrafting("circuit", "basic", "advanced", "elite"));
                itemCircuit.registerOreNames();
            }
            if (ConfigItems.ENABLE_WIRES_ITEMS)
            {
                event.getRegistry().register(itemWire = new ItemCrafting("wire", "copper", "gold"));
                itemWire.registerOreNames();
            }
        }

        //Optional items
        if (ConfigItems.ENABLE_BATTERY)
        {
            event.getRegistry().register(itemBattery = new ItemBattery());
        }

        //update tab
        CREATIVE_TAB.itemStack = new ItemStack(itemMissile);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        event.getRegistry().register(blockGlassPlate = new BlockGlassPressurePlate());
        event.getRegistry().register(blockGlassButton = new BlockGlassButton());
        event.getRegistry().register(blockSpikes = new BlockSpikes());
        event.getRegistry().register(blockConcrete = new BlockConcrete());
        event.getRegistry().register(blockReinforcedGlass = new BlockReinforcedGlass());
        //event.getRegistry().register(blockCombatRail = new BlockReinforcedRail());
        event.getRegistry().register(blockExplosive = new BlockExplosive());

        event.getRegistry().register(blockEmpTower = new BlockEmpTower());
        event.getRegistry().register(blockRadarStation = new BlockRadarStation());
        event.getRegistry().register(blockLaunchSupport = new BlockLaunchFrame());
        event.getRegistry().register(blockLaunchBase = new BlockLauncherBase());
        event.getRegistry().register(blockLaunchScreen = new BlockLaunchScreen());
        event.getRegistry().register(multiBlock = new BlockMultiblock());
        event.getRegistry().register(blockBattery = new BlockBattery());

        event.getRegistry().register(blockCruiseLauncher = new BlockCruiseLauncher());

        /*
        blockCamo = manager.newBlock("icbmCCamouflage", TileCamouflage.class);
        ICBMClassic.blockCruiseLauncher = ICBMClassic.INSTANCE.getManager().newBlock("icbmCCruiseLauncher", new TileCruiseLauncher());
        ICBMClassic.blockMissileCoordinator = ICBMClassic.INSTANCE.getManager().newBlock("icbmCMissileCoordinator", new TileMissileCoordinator());
        */

        GameRegistry.registerTileEntity(TileEntityExplosive.class, PREFIX + "explosive");
        GameRegistry.registerTileEntity(TileEMPTower.class, PREFIX + "emptower");
        GameRegistry.registerTileEntity(TileRadarStation.class, PREFIX + "radarstation");
        GameRegistry.registerTileEntity(TileLauncherFrame.class, PREFIX + "launcherframe");
        GameRegistry.registerTileEntity(TileLauncherBase.class, PREFIX + "launcherbase");
        GameRegistry.registerTileEntity(TileLauncherScreen.class, PREFIX + "launcherscreen");
        GameRegistry.registerTileEntity(TileMulti.class, PREFIX + "multiblock");
        GameRegistry.registerTileEntity(TileEntityBattery.class, PREFIX + "batterybox");
        GameRegistry.registerTileEntity(TileCruiseLauncher.class, PREFIX + "cruiseLauncher");
    }

    @SubscribeEvent
    public static void registerEntity(RegistryEvent.Register<EntityEntry> event)
    {
        event.getRegistry().register(buildEntityEntry(EntityFlyingBlock.class, "block.gravity", 128, 15));
        event.getRegistry().register(buildEntityEntry(EntityFragments.class, "block.fragment", 40, 8));
        event.getRegistry().register(buildEntityEntry(EntityExplosive.class, "block.explosive", 50, 5));
        event.getRegistry().register(buildEntityEntry(EntityMissile.class, "missile", 500, 1));
        event.getRegistry().register(buildEntityEntry(EntityExplosion.class, "holder.explosion", 100, 5));
        event.getRegistry().register(buildEntityEntry(EntityLightBeam.class, "beam.light", 80, 5));
        event.getRegistry().register(buildEntityEntry(EntityGrenade.class, "item.grenade", 50, 5));
        event.getRegistry().register(buildEntityEntry(EntityBombCart.class, "cart.bomb", 50, 2));
        event.getRegistry().register(buildEntityEntry(EntityPlayerSeat.class, "holder.seat", 50, 2));

        //Green team
        event.getRegistry().register(buildMobEntry(EntityXmasSkeleton.class, "skeleton.xmas.elf", Color.GREEN, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasSkeletonBoss.class, "skeleton.xmas.boss", Color.GREEN, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasSnowman.class, "skeleton.xmas.snowman", Color.BLACK, Color.CYAN));

        //Red team
        event.getRegistry().register(buildMobEntry(EntityXmasZombie.class, "zombie.xmas.elf", Color.RED, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasZombieBoss.class, "zombie.xmas.boss", Color.RED, Color.CYAN));
        event.getRegistry().register(buildMobEntry(EntityXmasCreeper.class, "zombie.xmas.creeper", Color.RED, Color.CYAN));


        event.getRegistry().register(buildEntityEntry(EntityXmasRPG.class, "skeleton.snowman.rocket", 64, 1));
    }

    private static EntityEntry buildEntityEntry(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency)
    {
        EntityEntryBuilder builder = EntityEntryBuilder.create();
        builder.name(PREFIX + entityName);
        builder.id(new ResourceLocation(DOMAIN, entityName), nextEntityID++);
        builder.tracker(trackingRange, updateFrequency, true);
        builder.entity(entityClass);
        return builder.build();
    }

    private static EntityEntry buildMobEntry(Class<? extends Entity> entityClass, String entityName, Color p, Color s)
    {
        EntityEntryBuilder builder = EntityEntryBuilder.create();
        builder.name(PREFIX + entityName);
        builder.id(new ResourceLocation(DOMAIN, entityName), nextEntityID++);
        builder.tracker(64, 1, true);
        builder.egg(p.getRGB(), s.getRGB());
        builder.entity(entityClass);
        return builder.build();
    }

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        proxy.doLoadModels();
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {
        if (ConfigItems.ENABLE_INGOTS_ITEMS)
        {
            //Steel clump -> Steel ingot
            GameRegistry.addSmelting(new ItemStack(itemIngotClump, 1, 0), new ItemStack(itemIngot, 1, 0), 0.1f);
        }

        if (ConfigItems.ENABLE_PLATES_ITEMS)
        {
            //Fix for removing recipe of plate
            GameRegistry.addSmelting(itemPlate.getStack("iron", 1), new ItemStack(Items.IRON_INGOT), 0f);
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
                if (lootPool != null)
                {
                    if (ConfigItems.ENABLE_INGOTS_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "ingot.copper", itemIngot.getStack("copper", 10), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "ingot.steel", itemIngot.getStack("steel", 10), 20, 3));
                    }
                    if (ConfigItems.ENABLE_PLATES_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "plate.steel", itemPlate.getStack("steel", 5), 30, 3));
                    }
                    if (ConfigItems.ENABLE_WIRES_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "wire.copper", itemWire.getStack("copper", 20), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "wire.gold", itemWire.getStack("gold", 15), 30, 3));
                    }
                    if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
                    {
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "circuit.basic", itemCircuit.getStack("basic", 15), 15, 5));
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "circuit.advanced", itemCircuit.getStack("advanced", 11), 30, 3));
                        lootPool.addEntry(new LootEntryItemStack(PREFIX + "circuit.elite", itemCircuit.getStack("elite", 8), 30, 3));
                    }
                }
            }
        }
        else if (event.getName().equals(LootTableList.ENTITIES_CREEPER))
        {
            if (ConfigItems.ENABLE_SULFUR_LOOT_DROPS)
            {
                LootPool lootPool = event.getTable().getPool(VANILLA_LOOT_POOL_ID);
                if (lootPool != null)
                {
                    lootPool.addEntry(new LootEntryItemStack(PREFIX + "sulfur", new ItemStack(itemSulfurDust, 10, 0), 2, 0));
                }
            }
        }
    }


    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        proxy.preInit();
        EnergySystem.register(new EnergySystemFE());

        //Register caps
        CapabilityEMP.register();

        MinecraftForge.EVENT_BUS.register(RadarRegistry.INSTANCE);
        MinecraftForge.EVENT_BUS.register(RadioRegistry.INSTANCE);
        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

        handleExRegistry(event.getModConfigurationDirectory());
    }

    private void handleExRegistry(File configMainFolder)
    {
        //Init registry
        final ExplosiveRegistry explosiveRegistry = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = explosiveRegistry;

        ICBMClassicAPI.EX_BLOCK_REGISTRY = new ExBlockContentReg();
        ICBMClassicAPI.EX_GRENADE_REGISTRY = new ExGrenadeContentReg();
        ICBMClassicAPI.EX_MINECRT_REGISTRY = new ExMinecartContentReg();
        ICBMClassicAPI.EX_MISSILE_REGISTRY = new ExMissileContentReg();

        //Load data
        explosiveRegistry.loadReg(new File(configMainFolder, "icbmclassic/explosive_reg.json"));

        //Fire registry events for content types
        MinecraftForge.EVENT_BUS.post(new ExplosiveRegistryInitEvent(explosiveRegistry));

        //Lock content types, done to prevent errors with adding content
        explosiveRegistry.lockNewContentTypes();

        //Register internal first to reserve slots for backwards compatibility
        ExplosiveInit.init();

        //Fire registry event for explosives
        MinecraftForge.EVENT_BUS.post(new ExplosiveRegistryEvent(explosiveRegistry));

        //Lock all registry, done to prevent errors in data generation for renders and content
        explosiveRegistry.lockRegistry();

        //Save registry, at this point everything should be registered
        explosiveRegistry.saveReg();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        proxy.init();
        packetHandler.init();
        CREATIVE_TAB.init();

        setModMetadata(ICBMClassic.DOMAIN, "ICBM-Classic", metadata);

        OreDictionary.registerOre("dustSulfur", new ItemStack(itemSulfurDust));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemSaltpeterDust));

        /** Potion Effects */ //TODO move to effect system
        PoisonToxin.INSTANCE = MobEffects.POISON;//new PoisonToxin(true, 5149489, "toxin");
        PoisonContagion.INSTANCE = MobEffects.POISON;//new PoisonContagion(false, 5149489, "virus");
        PoisonFrostBite.INSTANCE = MobEffects.POISON;//new PoisonFrostBite(false, 5149489, "frostBite");

        /** Dispenser Handler */ //TODO move to its own class
        if (itemGrenade != null)
        {
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemGrenade, new IBehaviorDispenseItem()
            {
                @Override
                public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
                {
                    World world = blockSource.getWorld();

                    if (!world.isRemote)
                    {
                        EnumFacing enumFacing = blockSource.getBlockState().getValue(BlockDispenser.FACING);

                        EntityGrenade entity = new EntityGrenade(world, new Pos(blockSource.getBlockPos()), itemStack.getItemDamage());
                        entity.setThrowableHeading(enumFacing.getXOffset(), 0.10000000149011612D, enumFacing.getZOffset(), 0.5F, 1.0F);
                        world.spawnEntity(entity);
                    }

                    itemStack.shrink(1);
                    return itemStack;
                }
            });
        }

        if (itemBombCart != null)
        {
            //TODO move to its own class
            BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(itemBombCart, new BehaviorDefaultDispenseItem()
            {
                private final BehaviorDefaultDispenseItem behaviourDefaultDispenseItem = new BehaviorDefaultDispenseItem();

                @Override
                public ItemStack dispenseStack(IBlockSource source, ItemStack stack)
                {
                    EnumFacing enumfacing = source.getBlockState().getValue(BlockDispenser.FACING);
                    World world = source.getWorld();
                    double x = source.getX() + (double) enumfacing.getXOffset() * 1.125D;
                    double y = Math.floor(source.getY()) + (double) enumfacing.getYOffset();
                    double z = source.getZ() + (double) enumfacing.getZOffset() * 1.125D;
                    BlockPos blockpos = source.getBlockPos().offset(enumfacing);
                    IBlockState iblockstate = world.getBlockState(blockpos);
                    BlockRailBase.EnumRailDirection rail =
                            iblockstate.getBlock() instanceof BlockRailBase
                                    ? ((BlockRailBase) iblockstate.getBlock()).getRailDirection(world, blockpos, iblockstate, null)
                                    : BlockRailBase.EnumRailDirection.NORTH_SOUTH;

                    double heightDelta;

                    if (BlockRailBase.isRailBlock(iblockstate))
                    {
                        if (rail.isAscending())
                        {
                            heightDelta = 0.6D;
                        }
                        else
                        {
                            heightDelta = 0.1D;
                        }
                    }
                    else
                    {
                        if (iblockstate.getMaterial() != Material.AIR || !BlockRailBase.isRailBlock(world.getBlockState(blockpos.down())))
                        {
                            return this.behaviourDefaultDispenseItem.dispense(source, stack);
                        }

                        IBlockState blockB = world.getBlockState(blockpos.down());
                        BlockRailBase.EnumRailDirection railB =
                                blockB.getBlock() instanceof BlockRailBase ?
                                        ((BlockRailBase) blockB.getBlock()).getRailDirection(world, blockpos.down(), blockB, null)
                                        : BlockRailBase.EnumRailDirection.NORTH_SOUTH;

                        if (enumfacing != EnumFacing.DOWN && railB.isAscending())
                        {
                            heightDelta = -0.4D;
                        }
                        else
                        {
                            heightDelta = -0.9D;
                        }
                    }

                    EntityBombCart cart = new EntityBombCart(world, x, y + heightDelta, z, stack.getItemDamage());

                    if (stack.hasDisplayName())
                    {
                        cart.setCustomNameTag(stack.getDisplayName());
                    }

                    world.spawnEntity(cart);
                    stack.shrink(1);
                    return stack;
                }
            });
        }
        proxy.init();
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        proxy.postInit();
    }


    public void setModMetadata(String id, String name, ModMetadata metadata)
    {
        metadata.modId = id;
        metadata.name = name;
        metadata.description = "ICBM is a Minecraft Mod that introduces intercontinental ballistic missiles to Minecraft. " +
                "But the fun doesn't end there! This mod also features many different explosives, missiles and machines " +
                "classified in three different tiers. If strategic warfare, carefully coordinated airstrikes, messing " +
                "with matter and general destruction are up your alley, then this mod is for you!";
        metadata.url = "http://www.builtbroken.com/";
        metadata.logoFile = "/icbm_logo.png";
        metadata.version = ICBMClassic.VERSION;
        metadata.authorList = Arrays.asList(new String[]{"Calclavia", "DarkGuardsman aka Darkcow"});
        metadata.credits = "Please visit the website.";
        metadata.autogenerated = false;
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        //Get command manager
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);

        //Register main command
        serverCommandManager.registerCommand(new CommandICBM("icbmc"));

        //Register secondary, to help with usability
        if (!Loader.isModLoaded("icbm"))
        {
            serverCommandManager.registerCommand(new CommandICBM("icbm"));
        }

        WorkerThreadManager.INSTANCE = new WorkerThreadManager(4); //TODO add config TODO do (cpu cores - 1) as default config
        WorkerThreadManager.INSTANCE.startThreads();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStoppingEvent event)
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