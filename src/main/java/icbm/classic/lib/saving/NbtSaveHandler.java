package icbm.classic.lib.saving;

import net.minecraft.nbt.NBTTagCompound;

import java.util.LinkedList;
import java.util.List;

public class NbtSaveHandler<E>
{
    private final List<NbtSaveRoot<E>> roots = new LinkedList();

    public NBTTagCompound save(E objectToSave, NBTTagCompound save)
    {
        roots.forEach(node -> {
            //Root workaround
            if(node.getSaveKey() == null) {
                node.nodes.forEach(subNode -> save.setTag(node.getSaveKey(), node.save(objectToSave)));
            }
            else {
                save.setTag(node.getSaveKey(), node.save(objectToSave));
            }
        });
        return save;
    }

    public void load(E objectToLoad, NBTTagCompound save)
    {
        if (!save.hasNoTags())
        {
            roots.forEach(node -> {
                //Root workaround
                if(node.getSaveKey() == null) {
                    node.nodes.forEach(subNode -> node.load(objectToLoad, save.getCompoundTag(node.getSaveKey())));
                }
                else {
                    node.load(objectToLoad, save.getCompoundTag(node.getSaveKey()));
                }
            });
        }
    }

    public NbtSaveRoot<E> addRoot(final String name) {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, this, null);
        roots.add(root);
        return root;
    }

    public NbtSaveRoot<E> mainRoot() {
        return addRoot(null);
    }
}
