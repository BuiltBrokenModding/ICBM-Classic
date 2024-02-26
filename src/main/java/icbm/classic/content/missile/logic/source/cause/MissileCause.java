package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

public abstract class MissileCause implements IMissileCause, INBTSerializable<NBTTagCompound> {

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

    private static final NbtSaveHandler<MissileCause> SAVE_LOGIC = new NbtSaveHandler<MissileCause>()
        .mainRoot()
        /* */.nodeBuildableObject("parent", () -> ICBMClassicAPI.MISSILE_CAUSE_REGISTRY, MissileCause::getPreviousCause, MissileCause::setPreviousCause)
        .base();
}
