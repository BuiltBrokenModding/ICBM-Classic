package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTBase;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class NbtSaveNode<In, Out extends NBTBase> implements INbtSaveNode<In, Out>
{
    private final String name;
    private final Function<In, Out> save;
    private final BiConsumer<In, Out> load;

    public NbtSaveNode(final String name, Function<In, Out> save, BiConsumer<In, Out> load) {
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
    public Out save(In objectToSave)
    {
        return save.apply(objectToSave);
    }

    @Override
    public void load(In objectToLoad, Out save)
    {
        load.accept(objectToLoad, save);
    }
}
