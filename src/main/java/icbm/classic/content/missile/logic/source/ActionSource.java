package icbm.classic.content.missile.logic.source;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class ActionSource implements IActionSource, INBTSerializable<CompoundTag> {

    private Level world;
    private Vec3 position;
    private IActionCause cause;

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag save) {
        SAVE_LOGIC.load(this, save);
    }

    private static final NbtSaveHandler<ActionSource> SAVE_LOGIC = new NbtSaveHandler<ActionSource>()
        .mainRoot()
        /* */.nodeWorldDim("dimension", ActionSource::getWorld, ActionSource::setWorld)
        /* */.nodeVec3d("pos", ActionSource::getPosition, ActionSource::setPosition)
        /* */.nodeBuildableObject("cause", () -> ICBMClassicAPI.ACTION_CAUSE_REGISTRY, ActionSource::getCause, ActionSource::setCause)
        .base();
}
// PORTED
