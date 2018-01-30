package icbm.classic;

import com.builtbroken.mc.framework.mod.AbstractMod;
import com.builtbroken.mc.framework.mod.AbstractProxy;
import com.builtbroken.mc.framework.mod.ModCreativeTab;
import com.builtbroken.mc.framework.mod.loadable.LoadableHandler;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.prefab.items.ItemBlockSubTypes;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blocks.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.content.explosive.tile.TileEntityExplosive;
import icbm.classic.content.items.*;
import icbm.classic.content.machines.emptower.BlockEmpTower;
import icbm.classic.content.machines.emptower.TileEMPTower;
import icbm.classic.content.machines.launcher.base.BlockLauncherBase;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.content.machines.launcher.frame.BlockLaunchFrame;
import icbm.classic.content.machines.launcher.frame.TileLauncherFrame;
import icbm.classic.content.machines.launcher.screen.BlockLaunchScreen;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import icbm.classic.content.machines.radarstation.BlockRadarStation;
import icbm.classic.content.machines.radarstation.TileRadarStation;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.potion.PoisonContagion;
import icbm.classic.content.potion.PoisonFrostBite;
import icbm.classic.content.potion.PoisonToxin;
import icbm.classic.prefab.item.ItemICBMBase;
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
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.Arrays;

/**
 * Mod class for ICBM Classic, contains all loading code and references to objects crated by the mod.
 *
 * @author DarkGuardsman
 * Orginal author and creator of the mod: Calclavia
 */
@Mod(modid = ICBMClassic.DOMAIN, name = "ICBM-Classic", version = ICBMClassic.VERSION, dependencies = ICBMClassic.DEPENDENCIES)
@Mod.EventBusSubscriber
public final class ICBMClassic extends AbstractMod
{
    @Mod.Instance(ICBMClassic.DOMAIN)
    public static ICBMClassic INSTANCE;

    @Mod.Metadata(ICBMClassic.DOMAIN)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "icbm.classic.client.ClientProxy", serverSide = "icbm.classic.ServerProxy")
    public static CommonProxy proxy;

    public static final String DOMAIN = "icbmclassic";
    public static final String PREFIX = DOMAIN + ":";

    public static final String MAJOR_VERSION = "@MAJOR@";
    public static final String MINOR_VERSION = "@MINOR@";
    public static final String REVISION_VERSION = "@REVIS@";
    public static final String BUILD_VERSION = "@BUILD@";
    public static final String MC_VERSION = "@MC@";
    public static final String VERSION = MC_VERSION + "-" + MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION_VERSION + "." + BUILD_VERSION;
    public static final String DEPENDENCIES = "required-after:voltzengine;";

    public static final int ENTITY_ID_PREFIX = 50;
    private static int nextID = ENTITY_ID_PREFIX;

    //Mod support
    public static Block blockRadioactive;
    public static int blockRadioactiveMeta;

    // Blocks
    public static Block blockGlassPlate;
    public static Block blockGlassButton;
    public static Block blockProximityDetector;
    public static Block blockSpikes;
    public static Block blockCamo;
    public static Block blockConcrete;
    public static Block blockReinforcedGlass;
    public static Block blockExplosive;
    public static Block blockCombatRail;

    public static Block blockLaunchBase;
    public static Block blockLaunchScreen;
    public static Block blockLaunchSupport;
    public static Block blockRadarStation;
    public static Block blockEmpTower;
    public static Block blockCruiseLauncher;
    public static Block blockMissileCoordinator;


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

    public static final ContagiousPoison poisonous_potion = new ContagiousPoison("Chemical", 0, false);
    public static final ContagiousPoison contagios_potion = new ContagiousPoison("Contagious", 1, true);

    public static final ModCreativeTab CREATIVE_TAB = new ModCreativeTab(DOMAIN);

    public ICBMClassic()
    {
        super(ICBMClassic.DOMAIN, "/bbm/ICBM-Classic");
    }

    @Override
    public void loadJsonContentHandlers()
    {
        super.loadJsonContentHandlers();
        //JsonBlockListenerProcessor.addBuilder(new ListenerExplosiveBreakTrigger.Builder()); TODO re-implement redmatter ore
    }

    @Override
    public void loadHandlers(LoadableHandler loader)
    {
        //loader.applyModule(WailaLoader.class, Mods.WAILA.isLoaded()); TODO add waila support back
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        //TODO add missing mappings for the following
        //"icbmCPoisonPowder" -> "poisonPowder"
        //"icbmCSulfurDust" -> "sulfurDust"
        //"icbmCAntidote" -> "antidote"
        //"icbmCSignalDisrupter" -> "signalDisrupter"
        //"icbmCTracker" -> "tracker"
        //"icbmCMissile", ->"missile"
        //"icbmCDefuser", -> "defuser"
        //"icbmCRadarGun", -> "radarGun"
        //"icbmCRemoteDetonator", -> "remoteDetonator"
        //"icbmCLaserDetonator", -> "laserDetonator"
        //"icbmCRocketLauncher", -> "rocketLauncher"
        //"icbmCGrenade", -> "grenade"
        //"icbmCBombCart", -> "bombcart"

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


        event.getRegistry().register(new ItemBlock(blockGlassPlate).setRegistryName(blockGlassPlate.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockGlassButton).setRegistryName(blockGlassButton.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockSpikes).setRegistryName(blockSpikes.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockConcrete).setRegistryName(blockConcrete.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockReinforcedGlass).setRegistryName(blockReinforcedGlass.getRegistryName()));
        event.getRegistry().register(new ItemBlockExplosive(blockExplosive).setRegistryName(blockExplosive.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockEmpTower).setRegistryName(blockEmpTower.getRegistryName()));
        event.getRegistry().register(new ItemBlock(blockRadarStation).setRegistryName(blockRadarStation.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockLaunchSupport).setRegistryName(blockLaunchSupport.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockLaunchBase).setRegistryName(blockLaunchBase.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(blockLaunchScreen).setRegistryName(blockLaunchScreen.getRegistryName()));

        CREATIVE_TAB.itemStack = new ItemStack(itemMissile);
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event)
    {
        //TODO add conversion for
        //"icbmCGlassPlate" -> "glassPressurePlate"
        //"icbmCGlassButton" -> "glassButton"
        //"icbmCSpike" -> "spikes"
        //"icbmCConcrete" -> "concrete"
        //"icbmCGlass" -> "reinforcedGlass"
        //"icbmCRail"
        //"icbmCExplosive" -> "explosives"
        //"icbmCEmpTower" -> "emptower"
        //"icbmCRadarStation" -> "radarStation"
        //"icbmCLauncherFrame" -> "launcherFrame"
        //"icbmCLauncherBase" -> "launcherBase"
        //"icbmCLauncherScreen" -> "launcherScreen"

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
    }

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        proxy.doLoadModels();
    }

    @SubscribeEvent
    public static void registerRecipes(RegistryEvent.Register<IRecipe> event)
    {

    }

    private void registerEntity(Class<? extends Entity> entityClass, String entityName, int trackingRange, int updateFrequency)
    {
        EntityRegistry.registerModEntity(new ResourceLocation(DOMAIN, entityName), entityClass, entityName, nextID++, this, trackingRange, updateFrequency, true);
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
        ICBMSounds.registerAll();
        //Engine.requestMultiBlock(); TODO ?
        Settings.load(getConfig());

        //Register entities
        registerEntity(EntityFlyingBlock.class, "ICBMGravityBlock", 128, 15);
        registerEntity(EntityFragments.class, "ICBMFragment", 40, 8);
        registerEntity(EntityExplosive.class, "ICBMExplosive", 50, 5);
        registerEntity(EntityMissile.class, "ICBMMissile", 500, 1);
        registerEntity(EntityExplosion.class, "ICBMProceduralExplosion", 100, 5);
        registerEntity(EntityLightBeam.class, "ICBMLightBeam", 80, 5);
        registerEntity(EntityGrenade.class, "ICBMGrenade", 50, 5);
        registerEntity(EntityBombCart.class, "ICBMChe", 50, 2);
        registerEntity(EntityPlayerSeat.class, "ICBMSeat", 50, 2);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        setModMetadata(ICBMClassic.DOMAIN, "ICBM-Classic", metadata);

        OreDictionary.registerOre("dustSulfur", new ItemStack(itemSulfurDust));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemSaltpeterDust));

        /** Check for existence of radioactive block. If it does not exist, then create it. */
        if (OreDictionary.getOres("blockRadioactive").size() > 0)
        {
            NonNullList<ItemStack> stacks = OreDictionary.getOres("blockRadioactive");
            for (ItemStack stack : stacks)
            {
                if (stack != null && stack.getItem() instanceof ItemBlock)
                {
                    //TODO add code to handle this from the ItemStack or test if this block is valid
                    //      As assuming the metadata is valid may not be a good idea, and the block may not be valid as well
                    //TODO add config to force block that is used
                    //TODO add error checking
                    blockRadioactive = ((ItemBlock) stack.getItem()).getBlock();
                    blockRadioactiveMeta = stack.getItem().getMetadata(stack.getItemDamage());
                    logger().info("Detected radioative block from another mod.");
                    logger().info("Radioactive explosives will use: " + blockRadioactive);
                }
            }
        }

        if (blockRadioactive == null)
        {
            blockRadioactive = Blocks.MYCELIUM;
        }

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

                        EntityGrenade entity = new EntityGrenade(world, new Pos(blockSource.getBlockPos()), Explosives.get(itemStack.getItemDamage()));
                        entity.setThrowableHeading(enumFacing.getFrontOffsetX(), 0.10000000149011612D, enumFacing.getFrontOffsetZ(), 0.5F, 1.0F);
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
                    double x = source.getX() + (double) enumfacing.getFrontOffsetX() * 1.125D;
                    double y = Math.floor(source.getY()) + (double) enumfacing.getFrontOffsetY();
                    double z = source.getZ() + (double) enumfacing.getFrontOffsetZ() * 1.125D;
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

                    EntityBombCart cart = new EntityBombCart(world, x, y + heightDelta, z, Explosives.get(stack.getItemDamage()));

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


    public void setModMetadata(String id, String name, ModMetadata metadata)
    {
        metadata.modId = id;
        metadata.name = name;
        metadata.description = "ICBM is a Minecraft Mod that introduces intercontinental ballistic missiles to Minecraft. But the fun doesn't end there! This mod also features many different explosives, missiles and machines classified in three different tiers. If strategic warfare, carefully coordinated airstrikes, messing with matter and general destruction are up your alley, then this mod is for you!";
        metadata.url = "http://www.builtbroken.com/";
        metadata.logoFile = "/icbm_logo.png";
        metadata.version = ICBMClassic.VERSION;
        metadata.authorList = Arrays.asList(new String[]{"Calclavia", "DarkGuardsman aka Darkcow"});
        metadata.credits = "Please visit the website.";
        metadata.autogenerated = false;
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandICBM());
    }
}