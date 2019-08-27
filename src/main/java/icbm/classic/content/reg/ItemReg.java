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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ItemReg
{
    @ObjectHolder(ICBMClassic.PREFIX + "antidote")
    public static Item itemAntidote;
    @ObjectHolder(ICBMClassic.PREFIX + "signalDisrupter")
    public static Item itemSignalDisrupter;
    @ObjectHolder(ICBMClassic.PREFIX + "tracker")
    public static Item itemTracker;
    @ObjectHolder(ICBMClassic.PREFIX + "missile")
    public static Item itemMissile;
    @ObjectHolder(ICBMClassic.PREFIX + "defuser")
    public static Item itemDefuser;
    @ObjectHolder(ICBMClassic.PREFIX + "radarGun")
    public static Item itemRadarGun;
    @ObjectHolder(ICBMClassic.PREFIX + "remoteDetonator")
    public static Item itemRemoteDetonator;
    @ObjectHolder(ICBMClassic.PREFIX + "laserDetonator")
    public static Item itemLaserDetonator;
    @ObjectHolder(ICBMClassic.PREFIX + "rocketLauncher")
    public static Item itemRocketLauncher;
    @ObjectHolder(ICBMClassic.PREFIX + "grenade")
    public static Item itemGrenade;
    @ObjectHolder(ICBMClassic.PREFIX + "bombcart")
    public static Item itemBombCart;
    @ObjectHolder(ICBMClassic.PREFIX + "sulfurDust")
    public static Item itemSulfurDust;
    @ObjectHolder(ICBMClassic.PREFIX + "saltpeter")
    public static Item itemSaltpeterDust;
    @ObjectHolder(ICBMClassic.PREFIX + "poisonPowder")
    public static Item itemPoisonPowder;
    @ObjectHolder(ICBMClassic.PREFIX + "battery")
    public static Item itemBattery;
    @ObjectHolder(ICBMClassic.PREFIX + "ingot")
    public static ItemCrafting itemIngot;
    @ObjectHolder(ICBMClassic.PREFIX + "clump")
    public static ItemCrafting itemIngotClump;
    @ObjectHolder(ICBMClassic.PREFIX + "plate")
    public static ItemCrafting itemPlate;
    @ObjectHolder(ICBMClassic.PREFIX + "circuit")
    public static ItemCrafting itemCircuit;
    @ObjectHolder(ICBMClassic.PREFIX + "wire")
    public static ItemCrafting itemWire;

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event)
    {
        //Items
        event.getRegistry().register(new ItemGrenade());
        event.getRegistry().register(new ItemBombCart());
        event.getRegistry().register(new ItemICBMBase("poisonPowder"));
        event.getRegistry().register(new ItemICBMBase("sulfurDust"));
        event.getRegistry().register(new ItemICBMBase("saltpeter"));
        event.getRegistry().register(new ItemAntidote());
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
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockBattery));
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
