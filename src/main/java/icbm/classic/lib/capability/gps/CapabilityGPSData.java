package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.saving.NbtSaveHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class CapabilityGPSData implements IGPSData, INBTSerializable<CompoundTag> {

    private Vec3 position;
    private ResourceKey<Level> dimension;

    @Override
    public void setPosition(@Nullable Vec3 position) {
        this.position = position;
    }

    @Override
    public void setWorld(@Nullable ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    @Nullable
    @Override
    public Vec3 getPosition() {
        return position;
    }

    @Nullable
    @Override
    public ResourceKey<Level> getWorldId() {
        return dimension;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    // PORT: "dim" was an int dimension id, now the dimension key as a resource location string
    private static final NbtSaveHandler<IGPSData> SAVE_LOGIC = new NbtSaveHandler<IGPSData>()
        .mainRoot()
        /* */.nodeVec3d("pos", IGPSData::getPosition, IGPSData::setPosition)
        /* */.nodeResourceLocation("dim",
            (gps) -> gps.getWorldId() != null ? gps.getWorldId().location() : null,
            (gps, loc) -> gps.setWorld(ResourceKey.create(Registries.DIMENSION, loc)))
        .base();

    // PORT: register() (CapabilityManager storage) removed, not needed in NeoForge
}
// PORTED
