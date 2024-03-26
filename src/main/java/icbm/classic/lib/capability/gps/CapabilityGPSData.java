package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.saving.NbtSaveHandler;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class CapabilityGPSData implements IGPSData, INBTSerializable<CompoundTag> {

    private Vec3 position;
    private ResourceKey<Level> level;

    @Override
    public void setPosition(@Nullable Vec3 position) {
        this.position = position;
    }

    @Override
    public void setLevel(@Nullable ResourceKey<Level> dimension) {
        this.level = dimension;
    }

    @Nullable
    @Override
    public Vec3 getPosition() {
        return position;
    }

    @Nullable
    @Override
    public ResourceKey<Level> getLevelId() {
        level.location();
        return level;
    }

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<IGPSData> SAVE_LOGIC = new NbtSaveHandler<IGPSData>()
        .mainRoot()
        /* */.nodeVec3("pos", IGPSData::getPosition, IGPSData::setPosition)
        /* */.nodeString("dim", igpsData -> igpsData.getLevelId().location().toString(), IGPSData::setLevel)
        .base();

    public static void register() {
        CapabilityManager.INSTANCE.register(IGPSData.class, new Capability.IStorage<IGPSData>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IGPSData> capability, IGPSData instance, Direction side) {
                    return SAVE_LOGIC.save(instance);
                }

                @Override
                public void readNBT(Capability<IGPSData> capability, IGPSData instance, Direction side, NBTBase nbt) {
                    if (nbt instanceof CompoundTag) {
                        SAVE_LOGIC.load(instance, (CompoundTag) nbt);
                    }
                }
            },
            CapabilityGPSData::new);
    }
}
