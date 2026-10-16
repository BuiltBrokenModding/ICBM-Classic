package icbm.classic.api.explosion;

import net.minecraft.nbt.CompoundTag;

/** Version of the blast that can be restored from save
 * Created by Dark(DarkGuardsman, Robin) on 2/10/2019.
 *
 * @deprecated will be replaced with {@link icbm.classic.api.actions.IAction} which doesn't do save/load as
 * it is instant.
 */
public interface IBlastRestore extends IBlast
{
    void load(CompoundTag nbt);

    void save(CompoundTag nbt);
}
// PORTED
