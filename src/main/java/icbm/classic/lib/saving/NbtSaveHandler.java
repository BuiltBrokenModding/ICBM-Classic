package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTTagCompound;

import java.util.LinkedList;
import java.util.List;

public class NbtSaveHandler<E>
{
    private final List<NbtSaveRoot<E>> roots = new LinkedList();

    public void save(E objectToSave, NBTTagCompound save)
    {
        roots.forEach(node -> save.setTag(node.getSaveKey(), node.save(objectToSave)));
    }

    public void load(E objectToLoad, NBTTagCompound save)
    {
        if (!save.hasNoTags())
        {
            roots.forEach(node -> node.load(objectToLoad, save.getCompoundTag(node.getSaveKey())));
        }
    }

    public NbtSaveRoot<E> addRoot(final String name) {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, this, null);
        roots.add(root);
        return root;
    }
}
