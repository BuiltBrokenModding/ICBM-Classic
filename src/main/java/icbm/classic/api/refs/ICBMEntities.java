package icbm.classic.api.refs;

import icbm.classic.api.ICBMClassicAPI;
import net.minecraft.resources.ResourceLocation;

/**
 * Holds references related to ICBM entities
 * Created by Robin Seifert on 1/7/19.
 */
public final class ICBMEntities
{

    //TODO refactor all entity names to match vanilla `_` style
    public static final ResourceLocation BLOCK_GRAVITY = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "block.gravity");
    public static final ResourceLocation BLOCK_FRAGMENT = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "block.fragment");
    public static final ResourceLocation BLOCK_EXPLOSIVE = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "block.explosive");

    public static final ResourceLocation MISSILE_EXPLOSIVE = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "explosive_missile");
    public static final ResourceLocation MISSILE_GENERIC = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "generic_missile");
    public static final ResourceLocation MISSILE_SAM = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "surface_to_air_missile");
    public static final ResourceLocation MISSILE_HELD_ITEM = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "held_item_missile");

    public static final ResourceLocation EXPLOSION = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "holder.explosion");
    public static final ResourceLocation BEAM = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "beam.light");
    public static final ResourceLocation GRENADE = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "item.grenade");
    public static final ResourceLocation BOMB_CART = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "cart.bomb");
    public static final ResourceLocation MISSILE_SEAT = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "holder.seat");
    public static final ResourceLocation BOMB_DROPLET = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "bomb_droplet");
    public static final ResourceLocation PARACHUTE = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "parachute");
    public static final ResourceLocation BALLOON = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "balloon");

    public static final ResourceLocation REDMATTER = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "redmatter");

    public static final ResourceLocation SMOKE = ResourceLocation.fromNamespaceAndPath(ICBMClassicAPI.ID, "smoke");
}
// PORTED
