package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

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
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    public static final NbtSaveNode<MissileCause, NBTTagCompound> CAUSE_SAVE = new NbtSaveNode<MissileCause, NBTTagCompound>("parent",
        (cause) -> { //TODO convert to class to make cleaner and provide better testing surface
            final NBTTagCompound save = new NBTTagCompound();
            final IMissileCause parent = cause.getPreviousCause();
            if (parent != null) {
                final NBTTagCompound logicSave = parent.serializeNBT();
                if (logicSave != null && !logicSave.hasNoTags()) {
                    save.setTag("data", logicSave);
                }
                save.setString("id", parent.getRegistryName().toString());
            }
            return save;
        },
        (cause, data) -> {
            final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
            final IMissileCause parent = ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.build(saveId);
            if (parent != null) {
                if (data.hasKey("data")) {
                    parent.deserializeNBT(data.getCompoundTag("data"));
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
