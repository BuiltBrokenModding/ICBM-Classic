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
import net.minecraftforge.common.util.INBTSerializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public final class MissileSource implements IMissileSource, INBTSerializable<NBTTagCompound> {

    private World world;
    private Vec3d position;
    private IMissileCause cause;

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void deserializeNBT(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }

    private static final NbtSaveHandler<MissileSource> SAVE_LOGIC = new NbtSaveHandler<MissileSource>()
        .mainRoot()
        /* */.nodeWorldDim("dimension", MissileSource::getWorld, MissileSource::setWorld)
        /* */.nodeVec3d("pos", MissileSource::getPosition, MissileSource::setPosition)
        /* */.nodeBuildableObject("cause", () -> ICBMClassicAPI.MISSILE_CAUSE_REGISTRY, MissileSource::getCause, MissileSource::setCause)
        .base();
}
