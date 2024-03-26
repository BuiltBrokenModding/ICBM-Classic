package icbm.classic.world.blast.caps;

import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.responses.BlastResponse;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 */
public class CapabilityBlast implements IBlast {
    @Override
    public double x() {
        return 0;
    }

    @Override
    public double y() {
        return 0;
    }

    @Override
    public double z() {
        return 0;
    }

    @Override
    public Level level() {
        return null;
    }

    @Nonnull
    @Override
    public BlastResponse runBlast() {
        return null;
    }

    @Override
    public void clearBlast() {

    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IBlast.class, new Capability.IStorage<IBlast>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IBlast> capability, IBlast instance, Direction side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IBlast> capability, IBlast instance, Direction side, NBTBase nbt) {

                }
            },
            CapabilityBlast::new);
    }
}
