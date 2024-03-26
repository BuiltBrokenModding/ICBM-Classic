package icbm.classic.world.missile.logic.source;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MissileSource implements IMissileSource {

    private Level level;
    private Vec3 position;
    private IMissileCause cause;

    @Override
    public CompoundTag serializeNBT() {
        return SAVE_LOGIC.save(this, new CompoundTag());
    }

    @Override
    public void deserializeNBT(CompoundTag save) {
        SAVE_LOGIC.load(this, save);
    }

    public static final NbtSaveNode<MissileSource, CompoundTag> CAUSE_SAVE = new NbtSaveNode<MissileSource, CompoundTag>("cause",
        (source) -> { //TODO convert to class to make cleaner and provide better testing surface
            final CompoundTag save = new CompoundTag();
            final IMissileCause cause = source.getCause();
            if (cause != null) {
                final CompoundTag logicSave = cause.serializeNBT();
                if (logicSave != null && !logicSave.isEmpty()) {
                    save.put("data", logicSave);
                }
                save.putString("id", cause.getRegistryName().toString());
            }
            return save;
        },
        (source, data) -> {
            final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
            final IMissileCause cause = ICBMClassicAPI.MISSILE_CAUSE_REGISTRY.build(saveId);
            if (cause != null) {
                if (data.contains("data")) {
                    cause.deserializeNBT(data.getCompound("data"));
                }
                source.cause = cause;
            }
        }
    );

    private static final NbtSaveHandler<MissileSource> SAVE_LOGIC = new NbtSaveHandler<MissileSource>()
        .mainRoot()
        /* */.nodeWorldDim("dimension", MissileSource::getLevel, MissileSource::setWorld)
        /* */.nodeVec3("pos", MissileSource::getPosition, MissileSource::setPosition)
        /* */.node(CAUSE_SAVE)
        .base();
}
