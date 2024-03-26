package icbm.classic.world;

import icbm.classic.IcbmConstants;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.world.item.*;
import icbm.classic.world.missile.entity.anti.item.ItemSurfaceToAirMissile;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class IcbmItems {

    public static final DeferredRegister.Items REGISTER = DeferredRegister.Items.createItems(IcbmConstants.MOD_ID);

    public static final DeferredItem<AntidoteItem> ANTIDOTE = REGISTER.registerItem("antidote", AntidoteItem::new);
    public static final DeferredItem<Item> SIGNAL_DISRUPTER = REGISTER.registerSimpleItem("signal_disrupter");
    public static final DeferredItem<Item> TRACKER = REGISTER.registerSimpleItem("tracker");
    public static final DeferredItem<MissileItem> EXPLOSIVE_MISSILE = REGISTER.registerItem("explosive_missile", MissileItem::new);
    public static final DeferredItem<DefuserItem> DEFUSER = REGISTER.registerItem("defuser", DefuserItem::new);
    public static final DeferredItem<RadarGunItem> RADAR_GUN = REGISTER.registerItem("radar_gun", RadarGunItem::new);
    public static final DeferredItem<RemoteDetonatorItem> REMOTE_DETONATOR = REGISTER.registerItem("remote_detonator", RemoteDetonatorItem::new);
    public static final DeferredItem<LaserDetonatorItem> LASER_DETONATOR = REGISTER.registerItem("laser_detonator", LaserDetonatorItem::new);
    public static final DeferredItem<RocketLauncherItem> ROCKET_LAUNCHER = REGISTER.registerItem("rocket_launcher", RocketLauncherItem::new);
    public static final DeferredItem<GrenadeItem> GRENADE = REGISTER.registerItem("grenade", GrenadeItem::new);
    public static final DeferredItem<BombCartItem> BOMB_CART = REGISTER.registerItem("bomb_cart", BombCartItem::new);

    public static final DeferredItem<Item> SULFUR_DUST = REGISTER.registerSimpleItem("sulfurDust");
    public static final DeferredItem<Item> SALTPETER_DUST = REGISTER.registerSimpleItem("saltpeter_dust");
    public static final DeferredItem<Item> SALTPETER_BALL = REGISTER.registerSimpleItem("saltpeter_ball");

    public static final DeferredItem<ItemBase> POISON_POWDER = REGISTER.registerItem("poison_powder", ItemBase::new);
    public static final DeferredItem<BatteryItem> BATTERY = REGISTER.registerItem("battery", BatteryItem::new);
    public static final DeferredItem<CraftingItem> INGOT = REGISTER.registerItem("ingot",
        properties -> new CraftingItem(properties, "ingot", "steel", "copper"));
    public static final DeferredItem<CraftingItem> CLUMP = REGISTER.registerItem("clump",
        properties -> new CraftingItem(properties, "clump", "steel"));
    public static final DeferredItem<CraftingItem> PLATE = REGISTER.registerItem("plate",
        properties -> new CraftingItem(properties, "plate", "steel", "iron"));
    public static final DeferredItem<CraftingItem> CIRCUIT = REGISTER.registerItem("circuit",
        properties -> new CraftingItem(properties, "circuit", "basic", "advanced", "elite"));
    public static final DeferredItem<CraftingItem> WIRE = REGISTER.registerItem("wire",
        properties -> new CraftingItem(properties, "wire", "copper", "gold"));

    public static final DeferredItem<ItemSurfaceToAirMissile> SURFACE_TO_AIR_MISSILE = REGISTER.registerItem("surface_to_air_missile", ItemSurfaceToAirMissile::new);

//    @SubscribeEvent
//    public static void missingMapping(RegistryEvent.MissingMappings<Item> event) {
//
//        // Name was changed in v4.2.0
//        final ResourceLocation oldMissileName = new ResourceLocation(ICBMConstants.MOD_ID, "missile");
//        for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getMappings()) {
//            if (oldMissileName.equals(mapping.key)) {
//                mapping.remap(itemExplosiveMissile);
//            }
//        }
//    }
//
//    @SubscribeEvent
//    public static void registerItems(NewRegistryEvent.Register<Item> event) {
//        //Items
//        event.getRegistry().register(new GrenadeItem().setName("grenade").setCreativeTab(ICBMClassic.CREATIVE_TAB));
//        event.getRegistry().register(new BombCartItem().setName("bombcart").setCreativeTab(ICBMClassic.CREATIVE_TAB));
//        event.getRegistry().register(new ItemBase().setName("poisonPowder").setCreativeTab(ICBMClassic.CREATIVE_TAB)); //TODO fix name _
//        Item sulfurItem = new ItemBase().setName("sulfurDust").setCreativeTab(ICBMClassic.CREATIVE_TAB);
//        event.getRegistry().register(sulfurItem); //TODO fix name _
//        OreDictionary.registerOre("dustSulfur", sulfurItem);
//
//        Item saltpeterItem = new ItemBase().setName("saltpeter").setCreativeTab(ICBMClassic.CREATIVE_TAB);
//        event.getRegistry().register(saltpeterItem);
//        OreDictionary.registerOre("dustSaltpeter", saltpeterItem);
//
//        // Crafting item used to make saltpeter dust
//        event.getRegistry().register(new ItemBase().setName("saltpeter_ball").setCreativeTab(ICBMClassic.CREATIVE_TAB));
//
//        event.getRegistry().register(new AntidoteItem().setName("antidote").setCreativeTab(ICBMClassic.CREATIVE_TAB));
//        event.getRegistry().register(new DefuserItem());
//        event.getRegistry().register(new RadarGunItem());
//        event.getRegistry().register(new RemoteDetonatorItem());
//        event.getRegistry().register(new LaserDetonatorItem());
//        event.getRegistry().register(new RocketLauncherItem());
//        event.getRegistry().register(new MissileItem().setName("explosive_missile").setCreativeTab(ICBMClassic.CREATIVE_TAB));
//        event.getRegistry().register(new ItemSurfaceToAirMissile());
//
//        //Block items
//        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassPlate).setRegistryName(BlockReg.blockGlassPlate.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockGlassButton).setRegistryName(BlockReg.blockGlassButton.getRegistryName()));
//        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockSpikes));
//        event.getRegistry().register(new ItemBlockSubTypes(BlockReg.blockConcrete));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockReinforcedGlass).setRegistryName(BlockReg.blockReinforcedGlass.getRegistryName()));
//        event.getRegistry().register(new ItemBlockExplosive(BlockReg.blockExplosive).setRegistryName(BlockReg.blockExplosive.getRegistryName()));
//        event.getRegistry().register(new ItemBlockEmpTower(BlockReg.blockEmpTower));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockRadarStation).setRegistryName(BlockReg.blockRadarStation.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchSupport).setRegistryName(BlockReg.blockLaunchSupport.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchBase).setRegistryName(BlockReg.blockLaunchBase.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchConnector).setRegistryName(BlockReg.blockLaunchConnector.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockLaunchScreen).setRegistryName(BlockReg.blockLaunchScreen.getRegistryName()));
//        event.getRegistry().register(new ItemBlock(BlockReg.blockCruiseLauncher).setRegistryName(BlockReg.blockCruiseLauncher.getRegistryName()));
//
//        //Crafting resources
//        if (ConfigItems.ENABLE_CRAFTING_ITEMS)
//        {
//            if (ConfigItems.ENABLE_INGOTS_ITEMS)
//            {
//                event.getRegistry().register(new CraftingItem("ingot", "steel", "copper"));
//                event.getRegistry().register(new CraftingItem("clump", "steel"));
//            }
//            if (ConfigItems.ENABLE_PLATES_ITEMS)
//            {
//                event.getRegistry().register(new CraftingItem("plate", "steel", "iron"));
//            }
//            if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
//            {
//                event.getRegistry().register(new CraftingItem("circuit", "basic", "advanced", "elite"));
//            }
//            if (ConfigItems.ENABLE_WIRES_ITEMS)
//            {
//                event.getRegistry().register(new CraftingItem("wire", "copper", "gold"));
//            }
//        }
//
//        //Optional items
//        if (ConfigItems.ENABLE_BATTERY)
//        {
//            event.getRegistry().register(new BatteryItem());
//        }
//
//        OreDictionary.registerOre("dustSulfur", new ItemStack(IcbmItems.itemSulfurDust));
//        OreDictionary.registerOre("dustSaltpeter", new ItemStack(IcbmItems.itemSaltpeterDust));
//    }
}
