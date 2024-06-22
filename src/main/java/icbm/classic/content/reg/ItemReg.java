package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blocks.emptower.ItemBlockEmpTower;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.cargo.ItemThrowableProjectile;
import icbm.classic.content.cluster.bomblet.ItemBombDroplet;
import icbm.classic.content.cluster.missile.ItemClusterMissile;
import icbm.classic.content.items.*;
import icbm.classic.content.missile.entity.anti.item.ItemSurfaceToAirMissile;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemBlockSubTypes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry.ObjectHolder;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class ItemReg
{
    @ObjectHolder(ICBMConstants.PREFIX + "antidote")
    public static Item itemAntidote;
    @ObjectHolder(ICBMConstants.PREFIX + "explosive_missile")
    public static Item itemExplosiveMissile;
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
    @ObjectHolder(ICBMConstants.PREFIX + "ballisticLauncher")
    public static Item itemBallisticLauncher;
    @ObjectHolder(ICBMConstants.PREFIX + "grenade")
    public static Item itemGrenade;
    @ObjectHolder(ICBMConstants.PREFIX + "bombcart")
    public static Item itemBombCart;

    @ObjectHolder(ICBMConstants.PREFIX + "explosive_bomblet")
    public static Item itemBombletExplosive;
    @ObjectHolder(ICBMConstants.PREFIX + "empty_bomblet")
    public static Item itemBombletEmpty;

    @ObjectHolder(ICBMConstants.PREFIX + "sulfurDust")
    public static Item itemSulfurDust;
    @ObjectHolder(ICBMConstants.PREFIX + "saltpeter")
    public static Item itemSaltpeterDust;
    @ObjectHolder(ICBMConstants.PREFIX + "saltpeter_ball")
    public static Item itemSaltpeterBall;

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

    @ObjectHolder(ICBMConstants.PREFIX + "surface_to_air_missile")
    public static ItemSurfaceToAirMissile itemSAM;

    @ObjectHolder(ICBMConstants.PREFIX + "cluster_missile")
    public static Item itemClusterMissile;

    @ObjectHolder(ICBMConstants.PREFIX + "parachute")
    public static ItemThrowableProjectile itemParachute;

    @ObjectHolder(ICBMConstants.PREFIX + "balloon")
    public static ItemThrowableProjectile itemBalloon;

    @SubscribeEvent
    public static void missingMapping(RegistryEvent.MissingMappings<Item> event) {

        // Name was changed in v4.2.0
        final ResourceLocation oldMissileName = new ResourceLocation(ICBMConstants.DOMAIN, "missile");
        for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings()) {
            if (oldMissileName.equals(mapping.key)) {
                mapping.remap(itemExplosiveMissile);
            }
        }
    }

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

        // Crafting item used to make saltpeter dust
        event.getRegistry().register(new ItemBase().setName("saltpeter_ball").setCreativeTab(ICBMClassic.CREATIVE_TAB));

        event.getRegistry().register(new ItemAntidote().setName("antidote").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemDefuser());
        event.getRegistry().register(new ItemRadarGun());
        event.getRegistry().register(new ItemRemoteDetonator());
        event.getRegistry().register(new ItemLaserDetonator());
        event.getRegistry().register(new ItemRocketLauncher(false));
        event.getRegistry().register(new ItemRocketLauncher(true));
        event.getRegistry().register(new ItemMissile().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemSurfaceToAirMissile());
        event.getRegistry().register(new ItemClusterMissile().setName("cluster_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));

        event.getRegistry().register(new ItemBase().setName("empty_bomblet").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemBombDroplet().setName("explosive_bomblet").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemThrowableProjectile().setName("parachute").setCreativeTab(ICBMClassic.CREATIVE_TAB));
        event.getRegistry().register(new ItemThrowableProjectile().setName("balloon").setCreativeTab(ICBMClassic.CREATIVE_TAB));

        //Block items
        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassPlate).setRegistryName(BlockReg.blockGlassPlate.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassButton).setRegistryName(BlockReg.blockGlassButton.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockSpikes));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockConcrete));
        event.getRegistry().register(new ItemBlock(BlockReg.blockReinforcedGlass).setRegistryName(BlockReg.blockReinforcedGlass.getRegistryName()));
        event.getRegistry().register(new ItemBlockExplosive(BlockReg.blockExplosive).setRegistryName(BlockReg.blockExplosive.getRegistryName()));
        event.getRegistry().register(new ItemBlockEmpTower(BlockReg.blockEmpTower));
        event.getRegistry().register(new ItemBlock(BlockReg.blockRadarStation).setRegistryName(BlockReg.blockRadarStation.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchSupport).setRegistryName(BlockReg.blockLaunchSupport.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchBase).setRegistryName(BlockReg.blockLaunchBase.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchConnector).setRegistryName(BlockReg.blockLaunchConnector.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchScreen).setRegistryName(BlockReg.blockLaunchScreen.getRegistryName()));
        event.getRegistry().register(new ItemBlock(BlockReg.blockCruiseLauncher).setRegistryName(BlockReg.blockCruiseLauncher.getRegistryName()));
        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockRadioactive));

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

        OreDictionary.registerOre("dustSulfur", new ItemStack(ItemReg.itemSulfurDust));
        OreDictionary.registerOre("dustSaltpeter", new ItemStack(ItemReg.itemSaltpeterDust));
    }
}
