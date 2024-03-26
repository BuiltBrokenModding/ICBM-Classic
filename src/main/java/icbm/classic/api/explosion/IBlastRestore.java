package icbm.classic.api.explosion;

import net.minecraft.nbt.CompoundTag;

/**
 * Version of the blast that can be restored from save
 * Created by Dark(DarkGuardsman, Robert) on 2/10/2019.
 */
public interface IBlastRestore extends IBlast {
    void load(CompoundTag nbt);

    void save(CompoundTag nbt);
}
