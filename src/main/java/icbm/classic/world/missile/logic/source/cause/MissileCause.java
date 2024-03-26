package icbm.classic.world.missile.logic.source.cause;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public abstract class MissileCause implements IMissileCause {

    private IMissileCause parent;

    public IMissileCause setPreviousCause(IMissileCause parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public IMissileCause getPreviousCause() {
        return parent;
    }

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    public static final NbtSaveNode<MissileCause, CompoundTag> CAUSE_SAVE = new NbtSaveNode<MissileCause, CompoundTag>("parent",
        (cause) -> { //TODO convert to class to make cleaner and provide better testing surface
            final CompoundTag save = new CompoundTag();
            final IMissileCause parent = cause.getPreviousCause();
            if (parent != null) {
                final CompoundTag logicSave = parent.serializeNBT();
                if (logicSave != null && !logicSave.isEmpty()) {
                    save.put("data", logicSave);
                }
                save.putString("id", parent.getRegistryName().toString());
            }
            return save;
        },
        (cause, data) -> {
            final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
            final IMissileCause parent = ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.build(saveId);
            if (parent != null) {
                if (data.contains("data")) {
                    parent.deserializeNBT(data.getCompound("data"));
                }
                cause.parent = parent;
            }
        }
    );

    private static final NbtSaveHandler<MissileCause> SAVE_LOGIC = new NbtSaveHandler<MissileCause>()
        .mainRoot()
        /* */.node(CAUSE_SAVE)
        .base();
}
