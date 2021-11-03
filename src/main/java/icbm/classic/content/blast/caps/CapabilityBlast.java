package icbm.classic.content.blast.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.responses.BlastResponse;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 */
public class CapabilityBlast implements IBlast
{
    @Override
    public double x()
    {
        return 0;
    }

    @Override
    public double y()
    {
        return 0;
    }

    @Override
    public double z()
    {
        return 0;
    }

    @Override
    public World world()
    {
        return null;
    }

    @Nonnull
    @Override
    public BlastResponse runBlast()
    {
        return null;
    }

    @Override
    public void clearBlast()
    {

    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IBlast.class, new Capability.IStorage<IBlast>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IBlast> capability, IBlast instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IBlast> capability, IBlast instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                CapabilityBlast::new);
    }
}
