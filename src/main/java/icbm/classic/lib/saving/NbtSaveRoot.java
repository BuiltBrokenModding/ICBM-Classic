package icbm.classic.lib.saving;

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
    private final List<INbtSaveNode> nodes = new LinkedList();

    public NbtSaveRoot(String name, NbtSaveHandler<E> handler, NbtSaveRoot<E> parent)
    {
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
        final NBTTagCompound tagCompound = new NBTTagCompound();
        nodes.forEach(node -> tagCompound.setTag(node.getSaveKey(), node.save(objectToSave)));
        return tagCompound;
    }

    @Override
    public void load(E objectToLoad, NBTTagCompound save)
    {
        if (!save.hasNoTags())
        {
            nodes.forEach(node -> node.load(objectToLoad, save.getTag(node.getSaveKey())));
        }
    }

    public NbtSaveRoot<E> addRoot(final String name)
    {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, handler, this);
        nodes.add(root);
        return root;
    }

    public <O extends NBTBase> NbtSaveRoot<E> node(final String name, Function<E, O> save, BiConsumer<E, O> load)
    {
        nodes.add(new NbtSaveNode(name, save, load));
        return this;
    }

    public NbtSaveRoot<E> nodeInteger(final String name, Function<E, Integer> save, BiConsumer<E, Integer> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagInt>(name,
            (obj) -> new NBTTagInt(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getInt())));
        return this;
    }

    public NbtSaveRoot<E> nodeDouble(final String name, Function<E, Double> save, BiConsumer<E, Double> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagDouble>(name,
            (obj) -> new NBTTagDouble(save.apply(obj)),
            (obj, data) -> load.accept(obj, data.getDouble())
        ));
        return this;
    }

    public NbtSaveRoot<E> nodeBoolean(final String name, Function<E, Boolean> save, BiConsumer<E, Boolean> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagByte>(name,
            (obj) -> new NBTTagByte((byte) (save.apply(obj) ? 1 : 0)),
            (obj, data) -> load.accept(obj, data.getByte() == 1)
        ));
        return this;
    }

    public NbtSaveRoot<E> nodeBlockPos(final String name, Function<E, BlockPos> save, BiConsumer<E, BlockPos> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagCompound>(name,
            (obj) -> {
                final BlockPos pos = save.apply(obj);
                if (pos != null)
                {
                    final NBTTagCompound compound = new NBTTagCompound();
                    compound.setInteger("x", pos.getX());
                    compound.setInteger("y", pos.getY());
                    compound.setInteger("z", pos.getZ());
                    return compound;
                }
                return null;
            },
            (obj, data) -> {
                final BlockPos pos = new BlockPos(
                    data.getInteger("x"),
                    data.getInteger("y"),
                    data.getInteger("z")
                );
                load.accept(obj, pos);
            }));
        return this;
    }

    public NbtSaveRoot<E> nodePos(final String name, Function<E, Pos> save, BiConsumer<E, Pos> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagCompound>(name,
            (obj) -> {
                final Pos pos = save.apply(obj);
                if (pos != null)
                {
                    final NBTTagCompound compound = new NBTTagCompound();
                    compound.setDouble("x", pos.getX());
                    compound.setDouble("y", pos.getY());
                    compound.setDouble("z", pos.getZ());
                    return compound;
                }
                return null;
            },
            (obj, data) -> {
                final Pos pos = new Pos(
                    data.getDouble("x"),
                    data.getDouble("y"),
                    data.getDouble("z")
                );
                load.accept(obj, pos);
            }));
        return this;
    }

    public NbtSaveRoot<E> nodeFacing(final String name, Function<E, EnumFacing> save, BiConsumer<E, EnumFacing> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagByte>(name,
            (obj) -> {
                final EnumFacing facing = save.apply(obj);
                if (facing != null)
                {
                    final byte b = (byte) facing.getIndex();
                    return new NBTTagByte(b);
                }
                return null;
            },
            (obj, data) -> {
                byte b = data.getByte();
                EnumFacing facing = EnumFacing.getFront(b);
                load.accept(obj, facing);
            }));
        return this;
    }

    public NbtSaveRoot<E> nodeBlockState(final String name, Function<E, IBlockState> save, BiConsumer<E, IBlockState> load)
    {
        nodes.add(new NbtSaveNode<E, NBTTagCompound>(name,
            (obj) -> {
                final IBlockState blockState = save.apply(obj);
                if (blockState != null)
                {
                    return NBTUtil.writeBlockState(new NBTTagCompound(), blockState);
                }
                return null;
            },
            (obj, data) -> {
                load.accept(obj, NBTUtil.readBlockState(data));
            }));
        return this;
    }

    public NbtSaveRoot<E> nodeUUID(final String name, Function<E, UUID> save, BiConsumer<E, UUID> load)
{
    nodes.add(new NbtSaveNode<E, NBTTagCompound>(name,
        (obj) -> {
            final UUID blockState = save.apply(obj);
            if (blockState != null)
            {
                return NBTUtil.createUUIDTag(blockState);
            }
            return null;
        },
        (obj, data) -> {
            load.accept(obj, NBTUtil.getUUIDFromTag(data));
        }));
    return this;
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
