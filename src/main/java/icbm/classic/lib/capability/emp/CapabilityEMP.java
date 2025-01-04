package icbm.classic.lib.capability.emp;

import icbm.classic.ICBMConstants;
import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN)
public class CapabilityEMP
{
    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP = null;

    public static final ResourceLocation ENTITY_ITEM_CAP = new ResourceLocation(ICBMConstants.DOMAIN, "emp.wrapper.entity.item");
    public static final ResourceLocation CREEPER_CAP = new ResourceLocation(ICBMConstants.DOMAIN, "emp.wrapper.entity.creeper");

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IEMPReceiver.class, new Capability.IStorage<IEMPReceiver>()
                {
                    @Override
                    public INBT writeNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, Direction side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, Direction side, INBT nbt)
                    {

                    }
                },
                () -> new CapabilityEmpChecker());
    }

    @SubscribeEvent
    public static void attachCapabilityItem(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof ItemEntity)
        {
            event.addCapability(ENTITY_ITEM_CAP, new CapabilityEmpEntityItem((ItemEntity) event.getObject()));
        }
        else if (event.getObject() instanceof CreeperEntity)
        {
            event.addCapability(CREEPER_CAP, new CapabilityEmpCreeper((CreeperEntity) event.getObject()));
        }
    }
}