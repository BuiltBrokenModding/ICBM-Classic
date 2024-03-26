package icbm.classic.lib.capability.emp;

import icbm.classic.IcbmConstants;
import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.EntityCreeper;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityInject;
import net.neoforged.common.capabilities.CapabilityManager;
import net.neoforged.event.AttachCapabilitiesEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID)
public class CapabilityEMP {
    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP = null;

    public static final ResourceLocation ENTITY_ITEM_CAP = new ResourceLocation(IcbmConstants.MOD_ID, "emp.wrapper.entity.item");
    public static final ResourceLocation CREEPER_CAP = new ResourceLocation(IcbmConstants.MOD_ID, "emp.wrapper.entity.creeper");

    public static void register() {
        CapabilityManager.INSTANCE.register(IEMPReceiver.class, new Capability.IStorage<IEMPReceiver>() {
                @Override
                public NBTBase writeNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, Direction side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, Direction side, NBTBase nbt) {

                }
            },
            () -> new CapabilityEmpChecker());
    }

    @SubscribeEvent
    public static void attachCapabilityItem(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof ItemEntity) {
            event.addCapability(ENTITY_ITEM_CAP, new CapabilityEmpItemEntity((ItemEntity) event.getObject()));
        } else if (event.getObject() instanceof EntityCreeper) {
            event.addCapability(CREEPER_CAP, new CapabilityEmpCreeper((EntityCreeper) event.getObject()));
        }
    }
}