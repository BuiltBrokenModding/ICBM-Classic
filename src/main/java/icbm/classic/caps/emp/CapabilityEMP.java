package icbm.classic.caps.emp;

import icbm.classic.api.caps.IEMPReceiver;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public class CapabilityEMP
{
    @CapabilityInject(IEMPReceiver.class)
    public static Capability<IEMPReceiver> EMP = null;

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
        () -> new CapabilityEmpUniversal());
    }
}