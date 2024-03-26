package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTBase;

public interface INbtSaveNode<In, Out extends NBTBase> {
    String getSaveKey();

    Out save(In objectToSave);

    void load(In objectToLoad, Out save);
}
