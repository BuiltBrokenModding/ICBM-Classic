package icbm.classic;

import com.builtbroken.mc.core.registry.ModManager;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.mod.AbstractMod;
import com.builtbroken.mc.lib.mod.AbstractProxy;
import com.builtbroken.mc.lib.mod.loadable.LoadableHandler;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.item.ItemBlockMetadata;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.Metadata;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.registry.EntityRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.content.blocks.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.missiles.Missile;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.explosive.tile.ItemBlockExplosive;
import icbm.classic.content.items.*;
import icbm.classic.content.potion.ContagiousPoison;
import icbm.classic.content.potion.PoisonContagion;
import icbm.classic.content.potion.PoisonFrostBite;
import icbm.classic.content.potion.PoisonToxin;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockRailBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBehaviorDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;

/**
 * Main class for ICBM core to run on. The core will need to be initialized by each ICBM module.
 *
 * @author Calclavia
 */
@Mod(modid = Reference.NAME, name = Reference.NAME, version = Reference.VERSION, dependencies = "after:ResonantInduction|Atomic;required-after:ResonantEngine")
public final class ICBMClassic extends AbstractMod
{
    @Instance(Reference.NAME)
    public static ICBMClassic INSTANCE;

    @Metadata(Reference.NAME)
    public static ModMetadata metadata;

    @SidedProxy(clientSide = "icbm.core.ClientProxy", serverSide = "icbm.core.CommonProxy")
    public static CommonProxy proxy;

    public static final int ENTITY_ID_PREFIX = 50;

    // Blocks
    public static Block blockGlassPlate;
    public static Block blockGlassButton;
    public static Block blockProximityDetector;
    public static Block blockSpikes;
    public static Block blockCamo;
    public static Block blockConcrete;
    public static Block blockReinforcedGlass;
    public static Block blockExplosive;
    public static Block blockMachine;
    public static Block blockSulfurOre;
    public static Block blockRadioactive;
    public static Block blockCombatRail;

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
    public static Item itemPoisonPowder;

    public static final ContagiousPoison poisonous_potion = new ContagiousPoison("Chemical", 1, false);
    public static final ContagiousPoison contagios_potion = new ContagiousPoison("Contagious", 1, true);

    public ICBMClassic()
    {
        super(Reference.DOMAIN);
    }

    @Override
    public void loadHandlers(LoadableHandler loader)
    {

    }

    @Override
    protected void loadBlocks(ModManager manager)
    {
        blockGlassPlate = manager.newBlock(BlockGlassPressurePlate.class);
        blockGlassButton = manager.newBlock(BlockGlassButton.class);
        blockSpikes = manager.newBlock(BlockSpikes.class, ItemBlockMetadata.class);
        blockCamo = manager.newBlock(TileCamouflage.class);
        blockConcrete = manager.newBlock(BlockConcrete.class, ItemBlockMetadata.class);
        blockReinforcedGlass = manager.newBlock(BlockReinforcedGlass.class, ItemBlockMetadata.class);
        blockCombatRail = manager.newBlock(BlockReinforcedRail.class);
        blockExplosive = manager.newBlock("icbmCExplosive", BlockExplosive.class, ItemBlockExplosive.class);
        //blockMachine = manager.newBlock(BlockICBMMachine.class, ItemBlockMachine.class);
    }

    @Override
    public void loadItems(ModManager manager)
    {
        itemPoisonPowder = manager.newItem("poisonPowder", new ItemICBMBase("poisonPowder"));
        itemSulfurDust = manager.newItem(ItemSulfurDust.class);
        itemAntidote = manager.newItem(ItemAntidote.class);
        itemSignalDisrupter = manager.newItem(ItemSignalDisrupter.class);
        itemTracker = manager.newItem(ItemTracker.class);
        itemMissile = manager.newItem(ItemMissile.class);
        itemDefuser = manager.newItem(ItemDefuser.class);
        itemRadarGun = manager.newItem(ItemRadarGun.class);
        itemRemoteDetonator = manager.newItem(ItemRemoteDetonator.class);
        itemLaserDesignator = manager.newItem(ItemLaserDetonator.class);
        itemRocketLauncher = manager.newItem(ItemRocketLauncher.class);
        itemGrenade = manager.newItem(ItemGrenade.class);
        itemBombCart = manager.newItem(ItemBombCart.class);
    }

    @Override
    public void loadEntities(ModManager manager)
    {
        EntityRegistry.registerGlobalEntityID(EntityFlyingBlock.class, "ICBMGravityBlock", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityFragments.class, "ICBMFragment", EntityRegistry.findGlobalUniqueEntityId());

        EntityRegistry.registerModEntity(EntityFlyingBlock.class, "ICBMGravityBlock", 0, this, 50, 15, true);
        EntityRegistry.registerModEntity(EntityFragments.class, "ICBMFragment", 1, this, 40, 8, true);

        EntityRegistry.registerGlobalEntityID(EntityExplosive.class, "ICBMExplosive", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityMissile.class, "ICBMMissile", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityExplosion.class, "ICBMProceduralExplosion", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityLightBeam.class, "ICBMLightBeam", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityGrenade.class, "ICBMGrenade", EntityRegistry.findGlobalUniqueEntityId());
        EntityRegistry.registerGlobalEntityID(EntityBombCart.class, "ICBMChe", EntityRegistry.findGlobalUniqueEntityId());

        EntityRegistry.registerModEntity(EntityExplosive.class, "ICBMExplosive", ENTITY_ID_PREFIX, this, 50, 5, true);
        EntityRegistry.registerModEntity(EntityMissile.class, "ICBMMissile", ENTITY_ID_PREFIX + 1, this, 500, 1, true);
        EntityRegistry.registerModEntity(EntityExplosion.class, "ICBMProceduralExplosion", ENTITY_ID_PREFIX + 2, this, 100, 5, true);
        EntityRegistry.registerModEntity(EntityLightBeam.class, "ICBMLightBeam", ENTITY_ID_PREFIX + 4, this, 80, 5, true);
        EntityRegistry.registerModEntity(EntityGrenade.class, "ICBMGrenade", ENTITY_ID_PREFIX + 6, this, 50, 5, true);
        EntityRegistry.registerModEntity(EntityBombCart.class, "ICBMChe", ENTITY_ID_PREFIX + 8, this, 50, 4, true);
    }

    @Override
    public void loadRecipes(ModManager manager)
    {
/** LOAD. */
        ArrayList dustCharcoal = OreDictionary.getOres("dustCharcoal");
        ArrayList dustCoal = OreDictionary.getOres("dustCoal");
        // Sulfur
        GameRegistry.addSmelting(blockSulfurOre, new ItemStack(itemSulfurDust, 4), 0.8f);
        GameRegistry.addSmelting(Items.reeds, new ItemStack(itemSulfurDust, 4, 1), 0f);
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", Items.coal));
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", new ItemStack(Items.coal, 1, 1)));

        if (dustCharcoal != null && dustCharcoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCharcoal"));
        }
        if (dustCoal != null && dustCoal.size() > 0)
        {
            GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(Items.gunpowder, 2), "dustSulfur", "dustSaltpeter", "dustCoal"));
        }

        GameRegistry.addRecipe(new ShapedOreRecipe(Blocks.tnt,
                "@@@", "@R@", "@@@",
                '@', Items.gunpowder,
                'R', Items.redstone));

        // Poison Powder
        GameRegistry.addRecipe(new ShapelessOreRecipe(new ItemStack(itemPoisonPowder, 3), Items.spider_eye, Items.rotten_flesh));
        /** Add all Recipes */
        // Spikes
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSpikes, 6),
                "CCC", "BBB",
                'C', Blocks.cactus,
                'B', Items.iron_ingot));
        GameRegistry.addRecipe(new ItemStack(blockSpikes, 1, 1),
                "E", "S",
                'E', itemPoisonPowder,
                'S', blockSpikes);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockSpikes, 1, 2),
                "E", "S",
                'E', itemSulfurDust,
                'S', blockSpikes));

        // Camouflage
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCamo, 12),
                "WGW", "G G", "WGW",
                'G', Blocks.vine,
                'W', Blocks.wool));

        // Tracker
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemTracker),
                " Z ", "SBS", "SCS",
                'Z', Items.compass,
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'B', UniversalRecipe.BATTERY.get(),
                'S', Items.iron_ingot));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemTracker),
                " Z ", "SBS", "SCS",
                'Z', Items.compass,
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'B', Items.ender_pearl,
                'S', Items.iron_ingot));

        // Glass Pressure Plate
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockGlassPlate, 1, 0), "##", '#', Blocks.glass));

        // Glass Button
        GameRegistry.addRecipe(new ItemStack(blockGlassButton, 2), "G", "G", 'G', Blocks.glass);

        // Proximity Detector
        GameRegistry.addRecipe(new ShapedOreRecipe(blockProximityDetector,
                "SSS", "S?S", "SSS",
                'S', Items.iron_ingot,
                '?', itemTracker));

        // Signal Disrupter
        GameRegistry.addRecipe(new ShapedOreRecipe(itemSignalDisrupter,
                "WWW", "SCS", "SSS",
                'S', Items.iron_ingot,
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'W', UniversalRecipe.WIRE.get()));

        // Antidote
        OreDictionary.registerOre("seeds", Items.wheat_seeds);
        OreDictionary.registerOre("seeds", Items.pumpkin_seeds);
        OreDictionary.registerOre("seeds", Items.melon_seeds);
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemAntidote, 6),
                "@@@", "@@@", "@@@",
                '@', "seeds"));

        // Concrete
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConcrete, 8, 0),
                "SGS", "GWG", "SGS",
                'G', Blocks.gravel,
                'S', Blocks.sand,
                'W', Items.water_bucket));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConcrete, 8, 1),
                "COC", "OCO", "COC",
                'C', new ItemStack(blockConcrete, 1, 0),
                'O', Blocks.obsidian));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockConcrete, 8, 2),
                "COC", "OCO", "COC",
                'C', new ItemStack(blockConcrete, 1, 1),
                'O', Items.iron_ingot));

        // Reinforced rails
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockCombatRail, 16, 0), new Object[]{"C C", "CIC", "C C", 'I', new ItemStack(blockConcrete, 1, 0), 'C', Items.iron_ingot}));

        // Reinforced Glass
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockReinforcedGlass, 8), new Object[]{"IGI", "GIG", "IGI", 'G', Blocks.glass, 'I', Items.iron_ingot}));

        // Rocket Launcher
        GameRegistry.addRecipe(new ShapedOreRecipe(itemRocketLauncher,
                "SCR", "SB ",
                'R', itemRadarGun,
                'C', new ItemStack(blockMachine, 1, MachineData.CruiseLauncher.ordinal() + 6),
                'B', Blocks.stone_button,
                'S', UniversalRecipe.PRIMARY_METAL.get()));
        // Radar Gun
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRadarGun),
                "@#!", " $!", "  !",
                '@', Blocks.glass,
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '#', UniversalRecipe.CIRCUIT_T1.get(),
                '$', Blocks.stone_button));
        // Remote
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemRemoteDetonator),
                "?@@", "@#$", "@@@",
                '@', UniversalRecipe.PRIMARY_METAL.get(),
                '?', Items.redstone,
                '#', UniversalRecipe.CIRCUIT_T2.get(),
                '$', Blocks.stone_button));
        // Laser Designator
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemLaserDesignator),
                "!  ", " ? ", "  @",
                '@', itemRemoteDetonator,
                '?', UniversalRecipe.CIRCUIT_T3.get(),
                '!', itemRadarGun));
        // Defuser
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(itemDefuser),
                "I  ", " W ", "  C",
                'C', UniversalRecipe.CIRCUIT_T2.get(),
                'W', UniversalRecipe.WRENCH.get(),
                'I', UniversalRecipe.WIRE.get()));
        // Missile Launcher Platform
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 0),
                "! !", "!C!", "!!!",
                '!', UniversalRecipe.SECONDARY_METAL.get(),
                'C', UniversalRecipe.CIRCUIT_T1.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 1),
                "! !", "!C!", "!@!",
                '@', new ItemStack(blockMachine, 1, 0),
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                'C', UniversalRecipe.CIRCUIT_T2.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 2),
                "! !", "!C!", "!@!",
                '@', new ItemStack(blockMachine, 1, 1),
                '!', UniversalRecipe.PRIMARY_PLATE.get(),
                'C', UniversalRecipe.CIRCUIT_T3.get()));
        // Missile Launcher Panel
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 3),
                "!!!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T1.get(),
                '!', Blocks.glass,
                '?', UniversalRecipe.WIRE.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 4),
                "!$!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T2.get(),
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '?', UniversalRecipe.WIRE.get(),
                '$', new ItemStack(blockMachine, 1, 3)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 5),
                "!$!", "!#!", "!?!",
                '#', UniversalRecipe.CIRCUIT_T3.get(),
                '!', Items.gold_ingot,
                '?', UniversalRecipe.WIRE.get(),
                '$', new ItemStack(blockMachine, 1, 4)));
        // Missile Launcher Support Frame
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 6),
                "! !", "!!!", "! !",
                '!', UniversalRecipe.SECONDARY_METAL.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 7),
                "! !", "!@!", "! !",
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                '@', new ItemStack(blockMachine, 1, 6)));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 8),
                "! !", "!@!", "! !",
                '!', UniversalRecipe.PRIMARY_PLATE.get(),
                '@', new ItemStack(blockMachine, 1, 7)));
        // Radar Station
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 9),
                "?@?", " ! ", "!#!",
                '@', new ItemStack(itemRadarGun),
                '!', UniversalRecipe.PRIMARY_PLATE.get(),
                '#', UniversalRecipe.CIRCUIT_T1.get(),
                '?', Items.gold_ingot));
        // EMP Tower
        RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 10),
                        "?W?", "@!@", "?#?",
                        '?', UniversalRecipe.PRIMARY_PLATE.get(),
                        '!', UniversalRecipe.CIRCUIT_T3.get(),
                        '@', UniversalRecipe.BATTERY_BOX.get(),
                        '#', UniversalRecipe.MOTOR.get(),
                        'W', UniversalRecipe.WIRE.get()),
                "EMP Tower", getConfig(), true);
        // Cruise Launcher
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 11),
                "?! ", "@@@",
                '@', UniversalRecipe.PRIMARY_PLATE.get(),
                '!', new ItemStack(blockMachine, 1, 2),
                '?', new ItemStack(blockMachine, 1, 8)));
        // Missile Coordinator
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(blockMachine, 1, 12),
                "R R", "SCS", "SSS",
                'C', UniversalRecipe.CIRCUIT_T2.get(),
                'S', UniversalRecipe.PRIMARY_PLATE.get(),
                'R', itemRemoteDetonator));
        // Missile module
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE.getItemStack(),
                " @ ", "@#@", "@?@",
                '@', UniversalRecipe.PRIMARY_METAL.get(),
                '?', Items.flint_and_steel,
                '#', UniversalRecipe.CIRCUIT_T1.get()));
        // Homing
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_HOMING.getItemStack(),
                " B ", " C ", "BMB",
                'M', Explosives.MISSILE.getItemStack(),
                'C', UniversalRecipe.CIRCUIT_T1.get(),
                'B', UniversalRecipe.SECONDARY_METAL.get()));
        // Anti-ballistic
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_ANTI.getItemStack(),
                "!", "?", "@",
                '@', Explosives.MISSILE.getItemStack(),
                '?', new ItemStack(blockExplosive, 1, 0),
                '!', UniversalRecipe.CIRCUIT_T1.get()));
        // Cluster
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_CLUSTER.getItemStack(),
                " ! ", " ? ", "!@!",
                '@', Explosives.MISSILE.getItemStack(),
                '?', Explosives.FRAGMENTATION.getItemStack(),
                '!', new ItemStack(itemMissile, 1, 0)));
        // Nuclear Cluster
        GameRegistry.addRecipe(new ShapedOreRecipe(Explosives.MISSILE_CLUSTER_NUKE.getItemStack(),
                " N ", "NCN",
                'C', Explosives.MISSILE_CLUSTER.getItemStack(),
                'N', Explosives.NUCLEAR.getItemStack()));

        // Add all explosive recipes.
        //if (!Loader.isModLoaded("ResonantInduction|Atomic"))
        //    OreDictionary.registerOre("strangeMatter", new ItemStack(397, 1, 1));

        for (Explosives ex : Explosives.values())
        {
            Explosive explosive = ex.handler;
            explosive.init();

            if (!(explosive instanceof Missile))
            {
                // Missile
                RecipeUtility.addRecipe(new ShapelessOreRecipe(new ItemStack(itemMissile, 1, ex.ordinal()),
                        Explosives.MISSILE.getItemStack(),
                        new ItemStack(blockExplosive, 1, ex.ordinal())), explosive.getUnlocalizedName() + " Missile", getConfig(), true);
                if (explosive.getTier() < 2)
                {
                    // Grenade
                    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemGrenade, 1, ex.ordinal()),
                            "?", "@",
                            '@', new ItemStack(blockExplosive, 1, ex.ordinal()),
                            '?', Items.string), explosive.getUnlocalizedName() + " Grenade", getConfig(), true);
                }
                if (explosive.getTier() < 3)
                {
                    // Minecart
                    RecipeUtility.addRecipe(new ShapedOreRecipe(new ItemStack(itemBombCart, 1, ex.ordinal()),
                            "?", "@",
                            '?', new ItemStack(blockExplosive, 1, ex.ordinal()),
                            '@', Items.minecart), explosive.getUnlocalizedName() + " Minecart", getConfig(), true);
                }
            }
        }
    }

    @Override
    public AbstractProxy getProxy()
    {
        return proxy;
    }


    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        super.preInit(event);
    }

    @EventHandler
    public void init(FMLInitializationEvent event)
    {
        super.init(event);
        Settings.setModMetadata(Reference.NAME, Reference.NAME, metadata);

        OreDictionary.registerOre("dustSulfur", new ItemStack(itemSulfurDust, 1, 0));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(itemSulfurDust, 1, 1));

        /** Check for existence of radioactive block. If it does not exist, then create it. */
        if (OreDictionary.getOres("blockRadioactive").size() > 0)
        {
            //blockRadioactive = Blocks.blocksList[OreDictionary.getOres("blockRadioactive").get(0).itemID];
            logger().info("Detected radioative block from another mod, utilizing it.");
        }

        if (blockRadioactive == null)
        {
            blockRadioactive = Blocks.mycelium;
        }

        /** Potion Effects */
        PoisonToxin.INSTANCE = new PoisonToxin(true, 5149489, "toxin");
        PoisonContagion.INSTANCE = new PoisonContagion(false, 5149489, "virus");
        PoisonFrostBite.INSTANCE = new PoisonFrostBite(false, 5149489, "frostBite");

        /** Dispenser Handler */
        BlockDispenser.dispenseBehaviorRegistry.putObject(itemGrenade, new IBehaviorDispenseItem()
        {
            @Override
            public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
            {
                World world = blockSource.getWorld();

                if (!world.isRemote)
                {
                    int x = blockSource.getXInt();
                    int y = blockSource.getYInt();
                    int z = blockSource.getZInt();
                    EnumFacing enumFacing = EnumFacing.getFront(blockSource.getBlockMetadata());

                    EntityGrenade entity = new EntityGrenade(world, new Pos(x, y, z), Explosives.get(itemStack.getItemDamage()));
                    entity.setThrowableHeading(enumFacing.getFrontOffsetX(), 0.10000000149011612D, enumFacing.getFrontOffsetZ(), 0.5F, 1.0F);
                    world.spawnEntityInWorld(entity);
                }

                itemStack.stackSize--;
                return itemStack;
            }
        });

        BlockDispenser.dispenseBehaviorRegistry.putObject(itemBombCart, new IBehaviorDispenseItem()
        {
            private final BehaviorDefaultDispenseItem defaultItemDispenseBehavior = new BehaviorDefaultDispenseItem();

            @Override
            public ItemStack dispense(IBlockSource blockSource, ItemStack itemStack)
            {
                World world = blockSource.getWorld();

                if (!world.isRemote)
                {
                    int x = blockSource.getXInt();
                    int y = blockSource.getYInt();
                    int z = blockSource.getZInt();

                    EnumFacing var3 = EnumFacing.getFront(blockSource.getBlockMetadata());
                    World var4 = blockSource.getWorld();
                    double var5 = blockSource.getX() + var3.getFrontOffsetX() * 1.125F;
                    double var7 = blockSource.getY();
                    double var9 = blockSource.getZ() + var3.getFrontOffsetZ() * 1.125F;
                    int var11 = blockSource.getXInt() + var3.getFrontOffsetX();
                    int var12 = blockSource.getYInt();
                    int var13 = blockSource.getZInt() + var3.getFrontOffsetZ();
                    Block var14 = var4.getBlock(var11, var12, var13);
                    double var15;

                    if (BlockRailBase.func_150051_a(var14))
                    {
                        var15 = 0.0D;
                    }
                    else
                    {
                        if (var14 == Blocks.air || !BlockRailBase.func_150051_a(var4.getBlock(var11, var12 - 1, var13)))
                        {
                            return this.defaultItemDispenseBehavior.dispense(blockSource, itemStack);
                        }

                        var15 = -1.0D;
                    }

                    EntityBombCart var22 = new EntityBombCart(world, var5, var7 + var15, var9, Explosives.get(itemStack.getItemDamage()));
                    world.spawnEntityInWorld(var22);
                    world.playAuxSFX(1000, x, y, z, 0);
                }

                itemStack.stackSize--;
                return itemStack;
            }
        });
        proxy.init();
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        super.postInit(event);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        // Setup command
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandICBM());
    }
}