package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.items.*;
import icbm.classic.prefab.item.ItemBlockRotatedMultiTile;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import icbm.classic.prefab.item.ItemICBMBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ItemReg
{
    //TODO object holders
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
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockBattery));
        event.getRegistry().register(new ItemBlock(BlockReg.blockCruiseLauncher).setRegistryName(BlockReg.blockCruiseLauncher.getRegistryName()));

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
        ICBMClassic.CREATIVE_TAB.itemStack = new ItemStack(itemMissile);
    }
}
