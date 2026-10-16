package icbm.classic.lib.capability.emp;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * PORT: 1.12 registered the capability with CapabilityManager and attached wrappers to item entities and
 * creepers with AttachCapabilitiesEvent. The capabilities are declared in {@link ICBMClassicAPI}
 * (EMP_ENTITY/BLOCK/ITEM_CAPABILITY) and vanilla providers are registered in {@link #registerCapabilities}.
 * The wrappers held no state, so a new instance is returned for each lookup.
 */
public class CapabilityEMP
{
    /** @deprecated alias for {@link ICBMClassicAPI#EMP_ENTITY_CAPABILITY}, kept for code comparing against the 1.12 field */
    @Deprecated
    public static final EntityCapability<IEMPReceiver, Void> EMP = ICBMClassicAPI.EMP_ENTITY_CAPABILITY;

    public static final ResourceLocation ENTITY_ITEM_CAP = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "emp.wrapper.entity.item");
    public static final ResourceLocation CREEPER_CAP = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "emp.wrapper.entity.creeper");

    /**
     * Registers EMP handling for vanilla objects, called from CapabilityReg on the mod bus.
     * <p>
     * 1.12 only attached EMP wrappers to item entities and creepers. Energy storages (tiles, entities, items)
     * are drained by {@link icbm.classic.content.actions.emp.ActionEmpArea} through the energy system and
     * inventories are wrapped on demand by {@link CapabilityEmpInventory}, neither needs a registered provider.
     */
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerEntity(ICBMClassicAPI.EMP_ENTITY_CAPABILITY, EntityType.ITEM, (entity, ctx) -> new CapabilityEmpEntityItem((ItemEntity) entity));
        event.registerEntity(ICBMClassicAPI.EMP_ENTITY_CAPABILITY, EntityType.CREEPER, (entity, ctx) -> new CapabilityEmpCreeper((Creeper) entity));
    }
}
// PORTED
