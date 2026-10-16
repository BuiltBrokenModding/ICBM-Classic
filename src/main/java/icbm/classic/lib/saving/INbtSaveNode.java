package icbm.classic.lib.saving;

import net.minecraft.nbt.Tag;

public interface INbtSaveNode<In, Out extends Tag>
{
    String getSaveKey();

    Out save(In objectToSave);

    void load(In objectToLoad, Out save);
}
