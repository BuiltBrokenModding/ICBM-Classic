package icbm.classic.lib.saving;

import icbm.classic.lib.saving.nodes.*;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.*;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NbtSaveRoot<E> implements INbtSaveNode<E, NBTTagCompound>
{
    private final String name;
    private final NbtSaveHandler<E> handler;
    private final NbtSaveRoot<E> parent;
    public final List<INbtSaveNode> nodes = new LinkedList();

    public NbtSaveRoot(String name, NbtSaveHandler<E> handler, NbtSaveRoot<E> parent)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("save key can't be null");
        }
        this.name = name;
        this.handler = handler;
        this.parent = parent;
    }

    @Override
    public String getSaveKey()
    {
        return name;
    }

    @Override
    public NBTTagCompound save(E objectToSave)
    {
        return save(objectToSave, new NBTTagCompound());
    }

    /**
     * Entry point for {@link NbtSaveHandler#save(Object, NBTTagCompound)} to save directly to main root. Shouldn't
     * be used by anything else.
     *
     * @param objectToSave used to save
     * @param tagCompound to save against
     * @return save
     */
    protected NBTTagCompound save(E objectToSave, NBTTagCompound tagCompound) {
        nodes.forEach(node -> {
            final NBTBase tag = node.save(objectToSave);
            if(tag != null && !tag.hasNoTags())
            {
                tagCompound.setTag(node.getSaveKey(), tag);
            }
        });
        return tagCompound;
    }

    @Override
    public void load(E objectToLoad, NBTTagCompound save)
    {
        if (save != null && !save.hasNoTags())
        {
            nodes.forEach(node -> {
                if (save.hasKey(node.getSaveKey()))
                {
                    node.load(objectToLoad, save.getTag(node.getSaveKey()));
                }
            });
        }
    }

    public NbtSaveRoot<E> addRoot(final String name)
    {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, handler, this);
        nodes.add(root);
        return root;
    }

    public <O extends NBTBase> NbtSaveRoot<E> node(NbtSaveNode<E, O> node)
    {
        nodes.add(node);
        return this;
    }

    public NbtSaveRoot<E> nodeInteger(final String name, Function<E, Integer> save, BiConsumer<E, Integer> load)
    {
        return node(new SaveNodeInteger<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeDouble(final String name, Function<E, Double> save, BiConsumer<E, Double> load)
    {
        return node(new SaveNodeDouble<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeFloat(final String name, Function<E, Float> save, BiConsumer<E, Float> load)
    {
        return node(new SaveNodeFloat<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBoolean(final String name, Function<E, Boolean> save, BiConsumer<E, Boolean> load)
    {
        return node(new SaveNodeBoolean<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBlockPos(final String name, Function<E, BlockPos> save, BiConsumer<E, BlockPos> load)
    {
        return node(new SaveNodeBlockPos<E>(name, save, load));
    }

    @Deprecated
    public NbtSaveRoot<E> nodePos(final String name, Function<E, Pos> save, BiConsumer<E, Pos> load)
    {
        return node(new SaveNodePos<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeFacing(final String name, Function<E, EnumFacing> save, BiConsumer<E, EnumFacing> load)
    {
        return node(new SaveNodeFacing<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBlockState(final String name, Function<E, IBlockState> save, BiConsumer<E, IBlockState> load)
    {
        return node(new SaveNodeBlockState<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeUUID(final String name, Function<E, UUID> save, BiConsumer<E, UUID> load)
    {
        return node(new SaveNodeUUID<E>(name, save, load));
    }

    /**
     * Goes up one nested level
     *
     * @return parent, can be null if this root is at base
     */
    public NbtSaveRoot<E> parent()
    {
        return parent;
    }

    /**
     * Goes to top most level in save tree
     *
     * @return handler
     */
    public NbtSaveHandler<E> base()
    {
        return handler;
    }
}
