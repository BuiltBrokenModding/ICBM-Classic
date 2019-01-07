package icbm.classic.lib.explosive.cap;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.emp.CapabilityEmpChecker;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosive implements IExplosive, ICapabilitySerializable<NBTTagCompound>
{

    public int explosiveID;
    public NBTTagCompound blastNBT;

    @Nullable
    @Override
    public IExplosiveData getExplosiveData()
    {
        return ICBMClassicHelpers.getExplosive(explosiveID, false);
    }

    @Nullable
    @Override
    public NBTTagCompound getCustomBlastData()
    {
        if (blastNBT == null)
        {
            blastNBT = new NBTTagCompound();
        }
        return blastNBT;
    }

    @Override
    public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY;
    }

    @Nullable
    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY)
        {
            return (T) this;
        }
        return null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tagCompound = new NBTTagCompound();
        tagCompound.setInteger("explosiveID", explosiveID);
        tagCompound.setTag("blastData", getCustomBlastData());
        return tagCompound;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        explosiveID = nbt.getInteger("explosiveID");
        blastNBT = nbt.getCompoundTag("blastData");
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IExplosive.class, new Capability.IStorage<IExplosive>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IExplosive> capability, IExplosive instance, EnumFacing side)
                    {
                        if (instance instanceof CapabilityExplosive)
                        {
                            return ((CapabilityExplosive) instance).serializeNBT();
                        }
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IExplosive> capability, IExplosive instance, EnumFacing side, NBTBase nbt)
                    {
                        if (instance instanceof CapabilityExplosive && nbt instanceof NBTTagCompound)
                        {
                            ((CapabilityExplosive) instance).deserializeNBT((NBTTagCompound) nbt);
                        }
                    }
                },
                () -> new CapabilityExplosive());
    }
}
