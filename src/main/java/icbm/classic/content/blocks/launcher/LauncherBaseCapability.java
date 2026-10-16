package icbm.classic.content.blocks.launcher;

import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AccessLevel;
import lombok.Setter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.UUID;

public abstract class LauncherBaseCapability implements IMissileLauncher, INBTSerializable<CompoundTag> {

    @Setter(value = AccessLevel.PRIVATE)
    private UUID uniqueId;

    @Override
    public UUID getUniqueId() {
        if(uniqueId == null) {
            uniqueId = UUID.randomUUID();
        }
        return uniqueId;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
       SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<LauncherBaseCapability> SAVE_LOGIC = new NbtSaveHandler<LauncherBaseCapability>()
        .mainRoot()
        /* */.nodeUUID("uniqueId", LauncherBaseCapability::getUniqueId, LauncherBaseCapability::setUniqueId)
        .base();
}
// PORTED
