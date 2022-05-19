package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class NbtSaveNode<SaveObject, NbtData extends NBTBase> implements INbtSaveNode<SaveObject, NbtData>
{
    private final String name;
    private final Function<SaveObject, NbtData> save;
    private final BiConsumer<SaveObject, NbtData> load;

    public NbtSaveNode(final String name, Function<SaveObject, NbtData> save, BiConsumer<SaveObject, NbtData> load) {
        this.name = name;
        this.save = save;
        this.load = load;
    }
    @Override
    public String getSaveKey()
    {
        return name;
    }

    @Override
    public NbtData save(SaveObject objectToSave)
    {
        return save.apply(objectToSave);
    }

    @Override
    public void load(SaveObject objectToLoad, NbtData save)
    {
        load.accept(objectToLoad, save);
    }
}
