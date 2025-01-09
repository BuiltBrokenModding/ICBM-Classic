package icbm.classic.content.reg;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.blocks.explosive.ItemBlockExplosive;
import icbm.classic.content.cargo.ItemThrowableProjectile;
import icbm.classic.content.cluster.bomblet.ItemBombDroplet;
import icbm.classic.content.cluster.missile.ItemClusterMissile;
import icbm.classic.content.items.*;
import icbm.classic.content.missile.entity.anti.item.ItemSurfaceToAirMissile;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ItemReg
{
    public static final DeferredRegister<Item> ITEMS = new DeferredRegister<Item>(ForgeRegistries.ITEMS, ICBMConstants.DOMAIN);

    // <editor-fold desc="simple-resources">
    public static final RegistryObject<Item> DUST_POISON = ITEMS.register("dust_poison", () -> new Item(new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> DUST_SULFUR = ITEMS.register("dust_sulfur", () -> new Item(new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> DUST_SALTPETER = ITEMS.register("dust_saltpeter", () -> new Item(new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> CLUMP_SALTPETER = ITEMS.register("clump_saltpeter", () -> new Item(new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));

    //TODO OreDictionary.registerOre("dustSulfur", new ItemStack(ItemReg.itemSulfurDust));
    //TODO OreDictionary.registerOre("dustSaltpeter", new ItemStack(ItemReg.itemSaltpeterDust));
    //TODO OreDictionary.registerOre("dustSulfur", sulfurItem);
    //TODO OreDictionary.registerOre("dustSaltpeter", saltpeterItem);
    // </editor-fold>

    // <editor-fold desc="entity-spawner-grenades">
    public static final RegistryObject<ItemGrenade> GRENADE_CONDENSED = ITEMS.register("grenade_condensed", () -> new ItemGrenade(EntityReg.GRENADE_CONDENSED::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_SHRAPNEL = ITEMS.register("grenade_shrapnel", () -> new ItemGrenade(EntityReg.GRENADE_SHRAPNEL::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_INCENDIARY = ITEMS.register("grenade_incendiary", () -> new ItemGrenade(EntityReg.GRENADE_INCENDIARY::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_DEBILITATION = ITEMS.register("grenade_debilitation", () -> new ItemGrenade(EntityReg.GRENADE_DEBILITATION::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_CHEMICAL = ITEMS.register("grenade_chemical", () -> new ItemGrenade(EntityReg.GRENADE_CHEMICAL::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_ANVIL = ITEMS.register("grenade_anvil", () -> new ItemGrenade(EntityReg.GRENADE_ANVIL::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_REPULSIVE = ITEMS.register("grenade_repulsive", () -> new ItemGrenade(EntityReg.GRENADE_REPULSIVE::get, new Item.Properties().maxStackSize(16)));
    public static final RegistryObject<ItemGrenade> GRENADE_ATTRACTIVE = ITEMS.register("grenade_attractive", () -> new ItemGrenade(EntityReg.GRENADE_ATTRACTIVE::get, new Item.Properties().maxStackSize(16)));
    // </editor-fold>

    // <editor-fold desc="entity-spawner-carts">
    public static final RegistryObject<ItemBombCart> CART_CONDENSED = ITEMS.register("cart_explosive_condensed", () -> new ItemBombCart(EntityReg.BOMBCART_CONDENSED::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_SHRAPNEL = ITEMS.register("cart_explosive_shrapnel", () -> new ItemBombCart(EntityReg.BOMBCART_SHRAPNEL::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_INCENDIARY = ITEMS.register("cart_explosive_incendiary", () -> new ItemBombCart(EntityReg.BOMBCART_INCENDIARY::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_DEBILITATION = ITEMS.register("cart_explosive_debilitation", () -> new ItemBombCart(EntityReg.BOMBCART_DEBILITATION::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_CHEMICAL = ITEMS.register("cart_explosive_chemical", () -> new ItemBombCart(EntityReg.BOMBCART_CHEMICAL::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_ANVIL = ITEMS.register("cart_explosive_anvil", () -> new ItemBombCart(EntityReg.BOMBCART_ANVIL::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_REPULSIVE = ITEMS.register("cart_explosive_repulsive", () -> new ItemBombCart(EntityReg.BOMBCART_REPULSIVE::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_ATTRACTIVE = ITEMS.register("cart_explosive_attractive", () -> new ItemBombCart(EntityReg.BOMBCART_ATTRACTIVE::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_COLOR = ITEMS.register("cart_explosive_color", () -> new ItemBombCart(EntityReg.BOMBCART_COLOR::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_SMOKE = ITEMS.register("cart_explosive_smoke", () -> new ItemBombCart(EntityReg.BOMBCART_SMOKE::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_FRAGMENTATION = ITEMS.register("cart_explosive_fragmentation", () -> new ItemBombCart(EntityReg.BOMBCART_FRAGMENTATION::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_CONTAGIOUS = ITEMS.register("cart_explosive_contagious", () -> new ItemBombCart(EntityReg.BOMBCART_CONTAGIOUS::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_SONIC = ITEMS.register("cart_explosive_sonic", () -> new ItemBombCart(EntityReg.BOMBCART_SONIC::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_BREACHING = ITEMS.register("cart_explosive_breaching", () -> new ItemBombCart(EntityReg.BOMBCART_BREACHING::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_THERMOBARIC = ITEMS.register("cart_explosive_thermobaric", () -> new ItemBombCart(EntityReg.BOMBCART_THERMOBARIC::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_NUCLEAR = ITEMS.register("cart_explosive_nuclear", () -> new ItemBombCart(EntityReg.BOMBCART_NUCLEAR::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_EMP = ITEMS.register("cart_explosive_emp", () -> new ItemBombCart(EntityReg.BOMBCART_EMP::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_EXOTHERMIC = ITEMS.register("cart_explosive_exothermic", () -> new ItemBombCart(EntityReg.BOMBCART_EXOTHERMIC::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_ENDOTHERMIC = ITEMS.register("cart_explosive_endothermic", () -> new ItemBombCart(EntityReg.BOMBCART_ENDOTHERMIC::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_GRAVITY = ITEMS.register("cart_explosive_gravity", () -> new ItemBombCart(EntityReg.BOMBCART_GRAVITY::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_ENDER = ITEMS.register("cart_explosive_ender", () -> new ItemBombCart(EntityReg.BOMBCART_ENDER::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_ANTIMATTER = ITEMS.register("cart_explosive_antimatter", () -> new ItemBombCart(EntityReg.BOMBCART_ANTIMATTER::get, new Item.Properties().maxStackSize(3)));
    public static final RegistryObject<ItemBombCart> CART_REDMATTER = ITEMS.register("cart_explosive_redmatter", () -> new ItemBombCart(EntityReg.BOMBCART_REDMATTER::get, new Item.Properties().maxStackSize(3)));
    // </editor-fold>

    // <editor-fold desc="entity-spawner-bomblets">
    public static final RegistryObject<Item> BOMBLET_EMPTY = ITEMS.register("bomblet_empty", () -> new Item(new Item.Properties().maxStackSize(16).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> BOMBLET_CONDENSED = ITEMS.register("bomblet_explosive_condensed", () -> new ItemBombDroplet(EntityReg.BOMBLET_CONDENSED::get, new Item.Properties().maxStackSize(16).group(ICBMClassic.CREATIVE_TAB)));
    // TODO add other explosive types
    // </editor-fold>

    // <editor-fold desc="entity-spawner-missiles">
    public static final RegistryObject<Item> MISSILE_SURFACE_TO_AIR = ITEMS.register("missile_surface_to_air", () -> new ItemSurfaceToAirMissile(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_CLUSTER = ITEMS.register("missile_cluster", () -> new ItemClusterMissile(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));

    public static final RegistryObject<Item> MISSILE_CONDENSED = ITEMS.register("missile_explosive_condensed", () -> new ItemMissile(EntityReg.MISSILE_CONDENSED::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_SHRAPNEL = ITEMS.register("missile_explosive_shrapnel", () -> new ItemMissile(EntityReg.MISSILE_SHRAPNEL::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_INCENDIARY = ITEMS.register("missile_explosive_incendiary", () -> new ItemMissile(EntityReg.MISSILE_INCENDIARY::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_DEBILITATION = ITEMS.register("missile_explosive_debilitation", () -> new ItemMissile(EntityReg.MISSILE_DEBILITATION::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_CHEMICAL = ITEMS.register("missile_explosive_chemical", () -> new ItemMissile(EntityReg.MISSILE_CHEMICAL::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_ANVIL = ITEMS.register("missile_explosive_anvil", () -> new ItemMissile(EntityReg.MISSILE_ANVIL::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_REPULSIVE = ITEMS.register("missile_explosive_repulsive", () -> new ItemMissile(EntityReg.MISSILE_REPULSIVE::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_ATTRACTIVE = ITEMS.register("missile_explosive_attractive", () -> new ItemMissile(EntityReg.MISSILE_ATTRACTIVE::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_COLOR = ITEMS.register("missile_explosive_color", () -> new ItemMissile(EntityReg.MISSILE_COLOR::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_SMOKE = ITEMS.register("missile_explosive_smoke", () -> new ItemMissile(EntityReg.MISSILE_SMOKE::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_FRAGMENTATION = ITEMS.register("missile_explosive_fragmentation", () -> new ItemMissile(EntityReg.MISSILE_FRAGMENTATION::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_CONTAGIOUS = ITEMS.register("missile_explosive_contagious", () -> new ItemMissile(EntityReg.MISSILE_CONTAGIOUS::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_SONIC = ITEMS.register("missile_explosive_sonic", () -> new ItemMissile(EntityReg.MISSILE_SONIC::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_BREACHING = ITEMS.register("missile_explosive_breaching", () -> new ItemMissile(EntityReg.MISSILE_BREACHING::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_THERMOBARIC = ITEMS.register("missile_explosive_thermobaric", () -> new ItemMissile(EntityReg.MISSILE_THERMOBARIC::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_NUCLEAR = ITEMS.register("missile_explosive_nuclear", () -> new ItemMissile(EntityReg.MISSILE_NUCLEAR::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_EMP = ITEMS.register("missile_explosive_emp", () -> new ItemMissile(EntityReg.MISSILE_EMP::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_EXOTHERMIC = ITEMS.register("missile_explosive_exothermic", () -> new ItemMissile(EntityReg.MISSILE_EXOTHERMIC::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_ENDOTHERMIC = ITEMS.register("missile_explosive_endothermic", () -> new ItemMissile(EntityReg.MISSILE_ENDOTHERMIC::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_GRAVITY = ITEMS.register("missile_explosive_gravity", () -> new ItemMissile(EntityReg.MISSILE_GRAVITY::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_ENDER = ITEMS.register("missile_explosive_ender", () -> new ItemMissile(EntityReg.MISSILE_ENDER::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_ANTIMATTER = ITEMS.register("missile_explosive_antimatter", () -> new ItemMissile(EntityReg.MISSILE_ANTIMATTER::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> MISSILE_REDMATTER = ITEMS.register("missile_explosive_redmatter", () -> new ItemMissile(EntityReg.MISSILE_REDMATTER::get, new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>

    // <editor-fold desc="entity-spawner-cargo">
    public static final RegistryObject<Item> PARACHUTE = ITEMS.register("parachute", () -> new ItemThrowableProjectile(new Item.Properties().maxStackSize(16).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> BALLON = ITEMS.register("balloon", () -> new ItemThrowableProjectile(new Item.Properties().maxStackSize(16).group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>

    // <editor-fold desc="tools">
    public static final RegistryObject<Item> ANTIDOTE_PILL = ITEMS.register("antidote_pill", () -> new ItemAntidote(new Item.Properties().maxStackSize(16).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> TOOL_DEACTIVATION_KIT = ITEMS.register("tool_deactivation_kit", () -> new ItemDeactivationTool(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> TOOL_POSITION_LASER = ITEMS.register("tool_position_laser", () -> new ItemRadarGun(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> TOOL_DETONATOR_REMOTE = ITEMS.register("tool_detonator_remote", () -> new ItemRemoteDetonator(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> TOOL_DETONATOR_LASER = ITEMS.register("tool_detonator_laser", () -> new ItemLaserDetonator(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> LAUNCHER_HELD_DIRECT = ITEMS.register("launcher_held_direct", () -> new ItemRocketLauncher(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB), false));
    public static final RegistryObject<Item> LAUNCHER_HELD_BALLISTIC = ITEMS.register("launcher_held_ballistic", () -> new ItemRocketLauncher(new Item.Properties().maxStackSize(1).group(ICBMClassic.CREATIVE_TAB), true));
    // </editor-fold>

    // <editor-fold desc="blocks-explosives">
    public static final RegistryObject<Item> EXPLOSIVE_CONDENSED = ITEMS.register("explosive_condensed", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_CONDENSED.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_SHRAPNEL = ITEMS.register("explosive_shrapnel", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_SHRAPNEL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_INCENDIARY = ITEMS.register("explosive_incendiary", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_INCENDIARY.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_DEBILITATION = ITEMS.register("explosive_debilitation", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_DEBILITATION.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_CHEMICAL = ITEMS.register("explosive_chemical", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_CHEMICAL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_ANVIL = ITEMS.register("explosive_anvil", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_ANVIL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_REPULSIVE = ITEMS.register("explosive_repulsive", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_REPULSIVE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_ATTRACTIVE = ITEMS.register("explosive_attractive", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_ATTRACTIVE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_COLOR = ITEMS.register("explosive_color", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_COLOR.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_SMOKE = ITEMS.register("explosive_smoke", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_SMOKE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_FRAGMENTATION = ITEMS.register("explosive_fragmentation", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_FRAGMENTATION.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_CONTAGIOUS = ITEMS.register("explosive_contagious", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_CONTAGIOUS.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_SONIC = ITEMS.register("explosive_sonic", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_SONIC.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_BREACHING = ITEMS.register("explosive_breaching", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_BREACHING.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_THERMOBARIC = ITEMS.register("explosive_thermobaric", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_THERMOBARIC.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_NUCLEAR = ITEMS.register("explosive_nuclear", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_NUCLEAR.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_EMP = ITEMS.register("explosive_emp", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_EMP.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_EXOTHERMIC = ITEMS.register("explosive_exothermic", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_EXOTHERMIC.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_ENDOTHERMIC = ITEMS.register("explosive_endothermic", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_ENDOTHERMIC.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_GRAVITY = ITEMS.register("explosive_gravity", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_GRAVITY.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_ENDER = ITEMS.register("explosive_ender", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_ENDER.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_ANTIMATTER = ITEMS.register("explosive_antimatter", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_ANTIMATTER.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EXPLOSIVE_REDMATTER = ITEMS.register("explosive_redmatter", () -> new ItemBlockExplosive(BlockReg.EXPLOSIVE_REDMATTER.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>

    // <editor-fold desc="blocks-base-building"> TODO split to another mod, as it doesn't match the core content
    public static final RegistryObject<Item> GLASS_REINFORCED = ITEMS.register("glass_reinforced", () -> new BlockItem(BlockReg.GLASS_REINFORCED.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    
    public static final RegistryObject<Item> SPIKE_NORMAL = ITEMS.register("spikes_normal", () -> new BlockItem(BlockReg.SPIKE_NORMAL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> SPIKE_FIRE = ITEMS.register("spikes_fire", () -> new BlockItem(BlockReg.SPIKE_FIRE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> SPIKE_POISON = ITEMS.register("spikes_poison", () -> new BlockItem(BlockReg.SPIKE_POISON.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    
    public static final RegistryObject<Item> CONCRETE_NORMAL = ITEMS.register("concrete_normal", () -> new BlockItem(BlockReg.CONCRETE_NORMAL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> CONCRETE_COMPACT = ITEMS.register("concrete_compact", () -> new BlockItem(BlockReg.CONCRETE_COMPACT.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> CONCRETE_REINFORCED = ITEMS.register("concrete_reinforced", () -> new BlockItem(BlockReg.CONCRETE_REINFORCED.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>

    // <editor-fold desc="blocks-machines">
    public static final RegistryObject<Item> EMP_TOWER_BASE = ITEMS.register("emp_tower_base", () -> new BlockItem(BlockReg.EMP_TOWER_BASE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> EMP_TOWER_COIL = ITEMS.register("emp_tower_coil", () -> new BlockItem(BlockReg.EMP_TOWER_COIL.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    
    public static final RegistryObject<Item> RADAR_SCREEN = ITEMS.register("radar_screen", () -> new BlockItem(BlockReg.RADAR_SCREEN.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    
    public static final RegistryObject<Item> LAUNCHER_FRAME = ITEMS.register("launcher_frame", () -> new BlockItem(BlockReg.LAUNCHER_FRAME.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> LAUNCHER_BASE = ITEMS.register("launcher_base", () -> new BlockItem(BlockReg.LAUNCHER_BASE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> LAUNCHER_CONNECTOR = ITEMS.register("launcher_connector", () -> new BlockItem(BlockReg.LAUNCHER_CONNECTOR.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> LAUNCHER_SCREEN = ITEMS.register("launcher_screen", () -> new BlockItem(BlockReg.LAUNCHER_SCREEN.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    
    public static final RegistryObject<Item> LAUNCHER_CRUISE = ITEMS.register("launcher_cruise", () -> new BlockItem(BlockReg.LAUNCHER_CRUISE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>

    // <editor-fold desc="blocks-world">
    public static final RegistryObject<Item> RADIOACTIVE_DIRT = ITEMS.register("radioactive_dirt", () -> new BlockItem(BlockReg.RADIOACTIVE_DIRT.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    public static final RegistryObject<Item> RADIOACTIVE_STONE = ITEMS.register("radioactive_stone", () -> new BlockItem(BlockReg.RADIOACTIVE_STONE.get(), new Item.Properties().group(ICBMClassic.CREATIVE_TAB)));
    // </editor-fold>
}
