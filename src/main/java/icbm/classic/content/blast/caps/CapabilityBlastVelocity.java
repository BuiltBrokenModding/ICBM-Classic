package icbm.classic.content.blast.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.redmatter.IBlastVelocity;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 */
public class CapabilityBlastVelocity implements IBlastVelocity
{
    @Override
    public boolean onBlastApplyMotion(@Nullable Entity source, IBlast blast, double xDifference, double yDifference, double zDifference, double distance)
    {
        return false;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IBlastVelocity.class, new Capability.IStorage<IBlastVelocity>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IBlastVelocity> capability, IBlastVelocity instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IBlastVelocity> capability, IBlastVelocity instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                CapabilityBlastVelocity::new);
    }
}
