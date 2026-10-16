package icbm.classic.content.reg;

import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blocks.emptower.ItemBlockEmpTower;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.cargo.ItemThrowableProjectile;
import icbm.classic.content.cluster.bomblet.ItemBombDroplet;
import icbm.classic.content.cluster.missile.ItemClusterMissile;
import icbm.classic.content.items.ItemAntidote;
import icbm.classic.content.items.ItemBattery;
import icbm.classic.content.items.ItemBombCart;
import icbm.classic.content.items.ItemCrafting;
import icbm.classic.content.items.ItemDefuser;
import icbm.classic.content.items.ItemGrenade;
import icbm.classic.content.items.ItemLaserDetonator;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.items.ItemRadarGun;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.content.items.ItemRocketLauncher;
import icbm.classic.content.missile.entity.anti.item.ItemSurfaceToAirMissile;
import icbm.classic.content.missile.entity.itemstack.item.ItemHeldItemMissile;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Item registry.
 * <p>
 * Registry names are the 1.12 names lower cased. Meta variants of crafting items were flattened:
 * ingot:steel -> steel_ingot, clump:steel -> steel_clump, plate:steel/iron -> steel_plate/iron_plate,
 * circuit:basic/advanced/elite -> basic_circuit/advanced_circuit/elite_circuit, wire:copper/gold ->
 * copper_wire/gold_wire. ingot:copper was dropped in favour of vanilla minecraft:copper_ingot.
 * <p>
 * Explosive variants (missiles, grenades, carts, bomblets, explosive blocks) are NOT flattened, the
 * explosive is stored in {@link ComponentReg#EXPLOSIVE}.
 * <p>
 * Fields for config disabled items are null, same as 1.12 object holders.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ItemReg
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ICBMConstants.DOMAIN);

    public static final DeferredItem<ItemGrenade> itemGrenade = ITEMS.register("grenade", () -> (ItemGrenade) new ItemGrenade().setName("grenade"));
    public static final DeferredItem<ItemBombCart> itemBombCart = ITEMS.register("bombcart", () -> (ItemBombCart) new ItemBombCart().setName("bombcart"));
    public static final DeferredItem<Item> itemPoisonPowder = ITEMS.register("poisonpowder", () -> new ItemBase().setName("poisonPowder"));
    public static final DeferredItem<Item> itemSulfurDust = ITEMS.register("sulfurdust", () -> new ItemBase().setName("sulfurDust"));
    public static final DeferredItem<Item> itemSaltpeterDust = ITEMS.register("saltpeter", () -> new ItemBase().setName("saltpeter"));
    /** Crafting item used to make saltpeter dust */
    public static final DeferredItem<Item> itemSaltpeterBall = ITEMS.register("saltpeter_ball", () -> new ItemBase().setName("saltpeter_ball"));
    public static final DeferredItem<ItemAntidote> itemAntidote = ITEMS.register("antidote", () -> (ItemAntidote) new ItemAntidote().setName("antidote"));
    public static final DeferredItem<ItemDefuser> itemDefuser = ITEMS.register("defuser", ItemDefuser::new);
    public static final DeferredItem<ItemRadarGun> itemRadarGun = ITEMS.register("radargun", ItemRadarGun::new);
    public static final DeferredItem<ItemRemoteDetonator> itemRemoteDetonator = ITEMS.register("remotedetonator", ItemRemoteDetonator::new);
    public static final DeferredItem<ItemLaserDetonator> itemLaserDetonator = ITEMS.register("laserdetonator", ItemLaserDetonator::new);
    public static final DeferredItem<ItemRocketLauncher> itemRocketLauncher = ITEMS.register("rocketlauncher", () -> new ItemRocketLauncher(false));
    public static final DeferredItem<ItemRocketLauncher> itemBallisticLauncher = ITEMS.register("ballisticlauncher", () -> new ItemRocketLauncher(true));
    public static final DeferredItem<ItemMissile> itemExplosiveMissile = ITEMS.register("explosive_missile", () -> (ItemMissile) new ItemMissile().setName("explosive_missile"));
    public static final DeferredItem<ItemSurfaceToAirMissile> itemSAM = ITEMS.register("surface_to_air_missile", ItemSurfaceToAirMissile::new);
    public static final DeferredItem<ItemClusterMissile> itemClusterMissile = ITEMS.register("cluster_missile", () -> (ItemClusterMissile) new ItemClusterMissile().setName("cluster_missile"));
    public static final DeferredItem<ItemHeldItemMissile> heldItemMissile = ITEMS.register("held_item_missile", ItemHeldItemMissile::new);

    public static final DeferredItem<Item> itemBombletEmpty = ITEMS.register("empty_bomblet", () -> new ItemBase().setName("empty_bomblet"));
    public static final DeferredItem<ItemBombDroplet> itemBombletExplosive = ITEMS.register("explosive_bomblet", () -> (ItemBombDroplet) new ItemBombDroplet().setName("explosive_bomblet"));
    public static final DeferredItem<ItemThrowableProjectile> itemParachute = ITEMS.register("parachute", () -> (ItemThrowableProjectile) new ItemThrowableProjectile().setName("parachute"));
    public static final DeferredItem<ItemThrowableProjectile> itemBalloon = ITEMS.register("balloon", () -> (ItemThrowableProjectile) new ItemThrowableProjectile().setName("balloon"));

    //Block items
    public static final DeferredItem<BlockItem> itemGlassPlate = blockItem(BlockReg.blockGlassPlate);
    public static final DeferredItem<BlockItem> itemGlassButton = blockItem(BlockReg.blockGlassButton);
    public static final DeferredItem<BlockItem> itemSpikes = blockItem(BlockReg.blockSpikes);
    public static final DeferredItem<BlockItem> itemSpikesPoison = blockItem(BlockReg.blockSpikesPoison);
    public static final DeferredItem<BlockItem> itemSpikesFire = blockItem(BlockReg.blockSpikesFire);
    public static final DeferredItem<BlockItem> itemConcrete = blockItem(BlockReg.blockConcrete);
    public static final DeferredItem<BlockItem> itemConcreteCompact = blockItem(BlockReg.blockConcreteCompact);
    public static final DeferredItem<BlockItem> itemConcreteReinforced = blockItem(BlockReg.blockConcreteReinforced);
    public static final DeferredItem<BlockItem> itemReinforcedGlass = blockItem(BlockReg.blockReinforcedGlass);
    public static final DeferredItem<ItemBlockExplosive> itemBlockExplosive = ITEMS.register("explosives", () -> new ItemBlockExplosive(BlockReg.blockExplosive.get()));
    public static final DeferredItem<ItemBlockEmpTower> itemEmpTower = ITEMS.register("emptower", () -> new ItemBlockEmpTower(BlockReg.blockEmpTower.get(), false));
    public static final DeferredItem<ItemBlockEmpTower> itemEmpTowerExtender = ITEMS.register("emptower_extender", () -> new ItemBlockEmpTower(BlockReg.blockEmpTower.get(), true));
    public static final DeferredItem<BlockItem> itemRadarStation = blockItem(BlockReg.blockRadarStation);
    public static final DeferredItem<BlockItem> itemLaunchSupport = blockItem(BlockReg.blockLaunchSupport);
    public static final DeferredItem<BlockItem> itemLaunchBase = blockItem(BlockReg.blockLaunchBase);
    public static final DeferredItem<BlockItem> itemLaunchConnector = blockItem(BlockReg.blockLaunchConnector);
    public static final DeferredItem<BlockItem> itemLaunchScreen = blockItem(BlockReg.blockLaunchScreen);
    public static final DeferredItem<BlockItem> itemCruiseLauncher = blockItem(BlockReg.blockCruiseLauncher);
    public static final DeferredItem<BlockItem> itemRadioactiveDirt = blockItem(BlockReg.blockRadioactiveDirt);
    public static final DeferredItem<BlockItem> itemRadioactiveStone = blockItem(BlockReg.blockRadioactiveStone);

    //Crafting resources, null when disabled by config
    public static final DeferredItem<ItemCrafting> itemIngotSteel = crafting(ConfigItems.ENABLE_INGOTS_ITEMS, "steel_ingot", "ingot", "steel");
    public static final DeferredItem<ItemCrafting> itemIngotClumpSteel = crafting(ConfigItems.ENABLE_INGOTS_ITEMS, "steel_clump", "clump", "steel");
    public static final DeferredItem<ItemCrafting> itemPlateSteel = crafting(ConfigItems.ENABLE_PLATES_ITEMS, "steel_plate", "plate", "steel");
    public static final DeferredItem<ItemCrafting> itemPlateIron = crafting(ConfigItems.ENABLE_PLATES_ITEMS, "iron_plate", "plate", "iron");
    public static final DeferredItem<ItemCrafting> itemCircuitBasic = crafting(ConfigItems.ENABLE_CIRCUIT_ITEMS, "basic_circuit", "circuit", "basic");
    public static final DeferredItem<ItemCrafting> itemCircuitAdvanced = crafting(ConfigItems.ENABLE_CIRCUIT_ITEMS, "advanced_circuit", "circuit", "advanced");
    public static final DeferredItem<ItemCrafting> itemCircuitElite = crafting(ConfigItems.ENABLE_CIRCUIT_ITEMS, "elite_circuit", "circuit", "elite");
    public static final DeferredItem<ItemCrafting> itemWireCopper = crafting(ConfigItems.ENABLE_WIRES_ITEMS, "copper_wire", "wire", "copper");
    public static final DeferredItem<ItemCrafting> itemWireGold = crafting(ConfigItems.ENABLE_WIRES_ITEMS, "gold_wire", "wire", "gold");

    //Optional items
    public static final DeferredItem<ItemBattery> itemBattery = ConfigItems.ENABLE_BATTERY ? ITEMS.register("battery", ItemBattery::new) : null;

    private static DeferredItem<BlockItem> blockItem(DeferredBlock<? extends Block> block)
    {
        return ITEMS.register(block.getId().getPath(), () -> new ItemBlockAbstract(block.get()));
    }

    private static DeferredItem<ItemCrafting> crafting(boolean enabled, String regName, String group, String type)
    {
        if (!ConfigItems.ENABLE_CRAFTING_ITEMS || !enabled)
        {
            return null;
        }
        return ITEMS.register(regName, () -> new ItemCrafting(group, type));
    }

    public static void register(IEventBus modBus)
    {
        ITEMS.register(modBus);
    }
}
