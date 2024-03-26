package icbm.classic.lib.radio;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadio;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityRadio implements IRadio {

    @Override
    public BlockPos getBlockPos() {
        return null;
    }

    @Override
    public Level getLevel() {
        return null;
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return null;
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(IRadio.class, new Capability.IStorage<IRadio>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IRadio> capability, IRadio instance, Direction side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IRadio> capability, IRadio instance, Direction side, NBTBase nbt) {

                }
            },
            CapabilityRadio::new);
    }
}
