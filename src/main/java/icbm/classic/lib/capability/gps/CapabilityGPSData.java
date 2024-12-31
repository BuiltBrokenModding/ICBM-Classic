package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

@Data
public class CapabilityGPSData implements IGPSData, INBTSerializable<CompoundNBT> {

    private Vec3d position;
    private ResourceLocation dimensionKey;

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<IGPSData> SAVE_LOGIC = new NbtSaveHandler<IGPSData>()
        .mainRoot()
        /* */.nodeVec3d("pos", IGPSData::getPosition, IGPSData::setPosition)
        /* */.nodeResourceLocation("dim", IGPSData::getDimensionKey, IGPSData::setDimensionKey)
        .base();

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IGPSData.class, new Capability.IStorage<IGPSData>()
            {
                @Nullable
                @Override
                public INBT writeNBT(Capability<IGPSData> capability, IGPSData instance, Direction side) {
                    return SAVE_LOGIC.save(instance);
                }

                @Override
                public void readNBT(Capability<IGPSData> capability, IGPSData instance, Direction side, INBT nbt) {
                    if(nbt instanceof CompoundNBT) {
                        SAVE_LOGIC.load(instance, (CompoundNBT) nbt);
                    }
                }
            },
            CapabilityGPSData::new);
    }
}
