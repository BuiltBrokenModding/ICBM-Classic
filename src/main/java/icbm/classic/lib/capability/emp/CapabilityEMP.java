package icbm.classic.lib.capability.emp;

import icbm.classic.ICBMConstants;
import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
                    public NBTBase writeNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IEMPReceiver> capability, IEMPReceiver instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new CapabilityEmpChecker());
    }

    @SubscribeEvent
    public static void attachCapabilityItem(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof EntityItem)
        {
            event.addCapability(ENTITY_ITEM_CAP, new CapabilityEmpEntityItem((EntityItem) event.getObject()));
        }
        else if (event.getObject() instanceof EntityCreeper)
        {
            event.addCapability(CREEPER_CAP, new CapabilityEmpCreeper((EntityCreeper) event.getObject()));
        }
    }
}