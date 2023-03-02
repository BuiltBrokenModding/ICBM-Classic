package icbm.classic.content.missile.logic.source;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MissileSource implements IMissileSource {

    private World world;
    private Vec3d pos;
    private IMissileCause cause;

    public void setCause(IMissileCause cause) {
        this.cause = cause;
    }

    @Override
    public IMissileCause getCause() {
        return cause;
    }

    @Override
    public Vec3d getFiredPosition() {
        return pos;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }

    public static final NbtSaveNode<MissileSource, NBTTagCompound> CAUSE_SAVE = new NbtSaveNode<MissileSource, NBTTagCompound>("cause",
        (source) -> { //TODO convert to class to make cleaner and provide better testing surface
            final NBTTagCompound save = new NBTTagCompound();
            final IMissileCause cause = source.getCause();
            if (cause != null) {
                final NBTTagCompound logicSave = cause.serializeNBT();
                if (logicSave != null && !logicSave.hasNoTags()) {
                    save.setTag("data", logicSave);
                }
                save.setString("id", cause.getRegistryName().toString());
            }
            return save;
        },
        (source, data) -> {
            final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
            final IMissileCause cause = ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.build(saveId);
            if (cause != null) {
                if (data.hasKey("data")) {
                    cause.deserializeNBT(data.getCompoundTag("data"));
                }
                source.cause = cause;
            }
        }
    );

    private static final NbtSaveHandler<MissileSource> SAVE_LOGIC = new NbtSaveHandler<MissileSource>()
        .mainRoot()
        /* */.nodeWorldDim("dimension", MissileSource::getWorld, MissileSource::setWorld)
        /* */.nodeVec3d("pos", MissileSource::getFiredPosition, MissileSource::setPos)
        /* */.node(CAUSE_SAVE)
        .base();
}
