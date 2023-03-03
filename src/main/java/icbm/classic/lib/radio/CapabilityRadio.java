package icbm.classic.lib.radio;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadio;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityRadio implements IRadio {

    @Override
    public BlockPos getBlockPos() {
        return null;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return null;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IRadio.class, new Capability.IStorage<IRadio>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IRadio> capability, IRadio instance, EnumFacing side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IRadio> capability, IRadio instance, EnumFacing side, NBTBase nbt) {

                }
            },
            CapabilityRadio::new);
    }
}
