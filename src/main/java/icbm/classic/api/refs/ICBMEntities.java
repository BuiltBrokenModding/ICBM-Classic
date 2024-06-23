package icbm.classic.api.refs;

import icbm.classic.api.ICBMClassicAPI;
import net.minecraft.util.ResourceLocation;

/**
 * Holds references related to ICBM entities
 * Created by Robin Seifert on 1/7/19.
 */
public final class ICBMEntities
{

    //TODO refactor all entity names to match vanilla `_` style
    public static final ResourceLocation BLOCK_GRAVITY = new ResourceLocation(ICBMClassicAPI.ID, "block.gravity");
    public static final ResourceLocation BLOCK_FRAGMENT = new ResourceLocation(ICBMClassicAPI.ID, "block.fragment");
    public static final ResourceLocation BLOCK_EXPLOSIVE = new ResourceLocation(ICBMClassicAPI.ID, "block.explosive");

    public static final ResourceLocation MISSILE_EXPLOSIVE = new ResourceLocation(ICBMClassicAPI.ID, "explosive_missile");
    public static final ResourceLocation MISSILE_GENERIC = new ResourceLocation(ICBMClassicAPI.ID, "generic_missile");
    public static final ResourceLocation MISSILE_SAM = new ResourceLocation(ICBMClassicAPI.ID, "surface_to_air_missile");
    public static final ResourceLocation MISSILE_HELD_ITEM = new ResourceLocation(ICBMClassicAPI.ID, "held_item_missile");

    public static final ResourceLocation EXPLOSION = new ResourceLocation(ICBMClassicAPI.ID, "holder.explosion");
    public static final ResourceLocation BEAM = new ResourceLocation(ICBMClassicAPI.ID, "beam.light");
    public static final ResourceLocation GRENADE = new ResourceLocation(ICBMClassicAPI.ID, "item.grenade");
    public static final ResourceLocation BOMB_CART = new ResourceLocation(ICBMClassicAPI.ID, "cart.bomb");
    public static final ResourceLocation MISSILE_SEAT = new ResourceLocation(ICBMClassicAPI.ID, "holder.seat");
    public static final ResourceLocation BOMB_DROPLET = new ResourceLocation(ICBMClassicAPI.ID, "bomb_droplet");
    public static final ResourceLocation PARACHUTE = new ResourceLocation(ICBMClassicAPI.ID, "parachute");
    public static final ResourceLocation BALLOON = new ResourceLocation(ICBMClassicAPI.ID, "balloon");

    public static final ResourceLocation REDMATTER = new ResourceLocation(ICBMClassicAPI.ID, "redmatter");

    public static final ResourceLocation SMOKE = new ResourceLocation(ICBMClassicAPI.ID, "smoke");
}
