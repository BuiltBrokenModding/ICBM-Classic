package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
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
import icbm.classic.content.items.ItemSignalDisrupter;
import icbm.classic.content.items.ItemTracker;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemBlockRotatedMultiTile;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class ItemReg
{
    @ObjectHolder(ICBMConstants.PREFIX + "antidote")
    public static Item itemAntidote;
    @ObjectHolder(ICBMConstants.PREFIX + "signalDisrupter")
    public static Item itemSignalDisrupter;
    @ObjectHolder(ICBMConstants.PREFIX + "tracker")
    public static Item itemTracker;
    @ObjectHolder(ICBMConstants.PREFIX + "missile")
    public static Item itemMissile;
    @ObjectHolder(ICBMConstants.PREFIX + "defuser")
    public static Item itemDefuser;
    @ObjectHolder(ICBMConstants.PREFIX + "radarGun")
    public static Item itemRadarGun;
    @ObjectHolder(ICBMConstants.PREFIX + "remoteDetonator")
    public static Item itemRemoteDetonator;
    @ObjectHolder(ICBMConstants.PREFIX + "laserDetonator")
    public static Item itemLaserDetonator;
    @ObjectHolder(ICBMConstants.PREFIX + "rocketLauncher")
    public static Item itemRocketLauncher;
    @ObjectHolder(ICBMConstants.PREFIX + "grenade")
    public static Item itemGrenade;
    @ObjectHolder(ICBMConstants.PREFIX + "bombcart")
    public static Item itemBombCart;
    @ObjectHolder(ICBMConstants.PREFIX + "sulfurDust")
    public static Item itemSulfurDust;
    @ObjectHolder(ICBMConstants.PREFIX + "saltpeter")
    public static Item itemSaltpeterDust;
    @ObjectHolder(ICBMConstants.PREFIX + "poisonPowder")
    public static Item itemPoisonPowder;
    @ObjectHolder(ICBMConstants.PREFIX + "battery")
    public static Item itemBattery;
    @ObjectHolder(ICBMConstants.PREFIX + "ingot")
    public static ItemCrafting itemIngot;
    @ObjectHolder(ICBMConstants.PREFIX + "clump")
    public static ItemCrafting itemIngotClump;
    @ObjectHolder(ICBMConstants.PREFIX + "plate")
    public static ItemCrafting itemPlate;
    @ObjectHolder(ICBMConstants.PREFIX + "circuit")
    public static ItemCrafting itemCircuit;
    @ObjectHolder(ICBMConstants.PREFIX + "wire")
    public static ItemCrafting itemWire;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        //Items
        event.getRegistry().register(new ItemGrenade().setName("grenade").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemBombCart().setName("bombcart").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemBase().setName("poisonPowder").setCreativeTab(ICBMClassic.CREATIVE_TAB)); //TODO fix name _
        Item sulfurItem = new ItemBase().setName("sulfurDust").setCreativeTab(ICBMClassic.CREATIVE_TAB);
        event.getRegistry().register(sulfurItem); //TODO fix name _
        OreDictionary.registerOre("dustSulfur", sulfurItem);

        Item saltpeterItem = new ItemBase().setName("saltpeter").setCreativeTab(ICBMClassic.CREATIVE_TAB);
        event.getRegistry().register(saltpeterItem);
        OreDictionary.registerOre("dustSaltpeter", saltpeterItem);


        event.getRegistry().register(new ItemAntidote().setName("antidote"));
        event.getRegistry().register(new ItemSignalDisrupter());
        event.getRegistry().register(new ItemTracker());
        event.getRegistry().register(new ItemDefuser());
        event.getRegistry().register(new ItemRadarGun());
        event.getRegistry().register(new ItemRemoteDetonator());
        event.getRegistry().register(new ItemLaserDetonator());
        event.getRegistry().register(new ItemRocketLauncher());
        event.getRegistry().register(new ItemMissile());

        //Block items
        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassPlate).setRegistryName(BlockReg.blockGlassPlate.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassButton).setRegistryName(BlockReg.blockGlassButton.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockSpikes));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockConcrete));
        event.getRegistry().register(new ItemBlock(BlockReg.blockReinforcedGlass).setRegistryName(BlockReg.blockReinforcedGlass.getRegistryName()));
        event.getRegistry().register(new ItemBlockExplosive(BlockReg.blockExplosive).setRegistryName(BlockReg.blockExplosive.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockEmpTower).setRegistryName(BlockReg.blockEmpTower.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockRadarStation).setRegistryName(BlockReg.blockRadarStation.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockLaunchSupport));
        event.getRegistry().register(new ItemBlockRotatedMultiTile(BlockReg.blockLaunchBase, e -> TileLauncherBase.getLayoutOfMultiBlock(e)));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockLaunchScreen));
        event.getRegistry().register(new ItemBlock(BlockReg.blockCruiseLauncher).setRegistryName(BlockReg.blockCruiseLauncher.getRegistryName()));

        //Crafting resources
        if (ConfigItems.ENABLE_CRAFTING_ITEMS)
        {
            if (ConfigItems.ENABLE_INGOTS_ITEMS)
            {
                event.getRegistry().register(new ItemCrafting("ingot", "steel", "copper"));
                event.getRegistry().register(new ItemCrafting("clump", "steel"));
            }
            if (ConfigItems.ENABLE_PLATES_ITEMS)
            {
                event.getRegistry().register(new ItemCrafting("plate", "steel", "iron"));
            }
            if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
            {
                event.getRegistry().register(new ItemCrafting("circuit", "basic", "advanced", "elite"));
            }
            if (ConfigItems.ENABLE_WIRES_ITEMS)
            {
                event.getRegistry().register(new ItemCrafting("wire", "copper", "gold"));
            }
        }

        //Optional items
        if (ConfigItems.ENABLE_BATTERY)
        {
            event.getRegistry().register(new ItemBattery());
        }

    }
}
