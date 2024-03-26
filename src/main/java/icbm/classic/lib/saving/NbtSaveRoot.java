package icbm.classic.lib.saving;

import icbm.classic.api.missiles.parts.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.nodes.*;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.common.DimensionManager;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NbtSaveRoot<E> implements INbtSaveNode<E, CompoundTag> {
    private final String name;
    private final NbtSaveHandler<E> handler;
    private final NbtSaveRoot<E> parent;
    public final List<INbtSaveNode> nodes = new LinkedList();

    private boolean shouldSave = true;

    public NbtSaveRoot(String name, NbtSaveHandler<E> handler, NbtSaveRoot<E> parent) {
        if (name == null) {
            throw new IllegalArgumentException("save key can't be null");
        }
        this.name = name;
        this.handler = handler;
        this.parent = parent;
    }

    public NbtSaveRoot<E> disableSave() {
        shouldSave = false;
        return this;
    }

    @Override
    public String getSaveKey() {
        return name;
    }

    @Override
    public CompoundTag save(E objectToSave) {
        if (!shouldSave) {
            return null;
        }
        return save(objectToSave, new CompoundTag());
    }

    /**
     * Entry point for {@link NbtSaveHandler#save(Object, CompoundTag)} to save directly to main root. Shouldn't
     * be used by anything else.
     *
     * @param objectToSave used to save
     * @param tagCompound  to save against
     * @return save
     */
    protected CompoundTag save(E objectToSave, CompoundTag tagCompound) {
        nodes.forEach(node -> {
            final NBTBase tag = node.save(objectToSave);
            if (tag != null && !tag.isEmpty()) {
                tagCompound.put(node.getSaveKey(), tag);
            }
        });
        return tagCompound;
    }

    @Override
    public void load(E objectToLoad, CompoundTag save) {
        if (save != null && !save.isEmpty()) {
            nodes.forEach(node -> {
                if (save.contains(node.getSaveKey())) {
                    node.load(objectToLoad, save.getTag(node.getSaveKey()));
                }
            });
        }
    }

    public NbtSaveRoot<E> addRoot(final String name) {
        final NbtSaveRoot<E> root = new NbtSaveRoot<>(name, handler, this);
        nodes.add(root);
        return root;
    }

    public <O extends NBTBase> NbtSaveRoot<E> node(NbtSaveNode<E, O> node) {
        nodes.add(node);
        return this;
    }

    public NbtSaveRoot<E> nodeString(final String name, Function<E, String> save, BiConsumer<E, String> load) {
        return node(new SaveNodeString(name, save, load));
    }

    public NbtSaveRoot<E> nodeInteger(final String name, Function<E, Integer> save, BiConsumer<E, Integer> load) {
        return node(new SaveNodeInteger<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeDouble(final String name, Function<E, Double> save, BiConsumer<E, Double> load) {
        return node(new SaveNodeDouble<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeFloat(final String name, Function<E, Float> save, BiConsumer<E, Float> load) {
        return node(new SaveNodeFloat<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBoolean(final String name, Function<E, Boolean> save, BiConsumer<E, Boolean> load) {
        return node(new SaveNodeBoolean<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBlockPos(final String name, Function<E, BlockPos> save, BiConsumer<E, BlockPos> load) {
        return node(new SaveNodeBlockPos<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeVec3(final String name, Function<E, Vec3> save, BiConsumer<E, Vec3> load) {
        return node(new SaveNodeVec3<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeWorldDim(final String name, Function<E, World> save, BiConsumer<E, World> load) {
        return node(new NbtSaveNode<E, NBTTagInt>(name,
            (e) -> {
                final Level level = save.apply(e);
                if (world != null && world.provider != null) {
                    return new NBTTagInt(world.provider.getDimension());
                }
                return null;
            },
            (e, data) -> {
                final int dim = data.getInt();
                final Level level = DimensionManager.getLevel(dim);
                load.accept(e, world);
            }
        ));
    }

    @Deprecated
    public NbtSaveRoot<E> nodePos(final String name, Function<E, Pos> save, BiConsumer<E, Pos> load) {
        return node(new SaveNodePos<E>(name, save, load));
    }

    @Deprecated
    public NbtSaveRoot<E> nodeEulerAngle(final String name, Function<E, EulerAngle> save, BiConsumer<E, EulerAngle> load) {
        return node(new NbtSaveNode<E, CompoundTag>(name,
            (e) -> {
                final EulerAngle angle = save.apply(e);
                if (angle != null) {
                    return angle.toNBT();
                }
                return null;
            },
            (e, data) -> {
                load.accept(e, new EulerAngle(data));
            }
        ));
    }

    public NbtSaveRoot<E> nodeFacing(final String name, Function<E, Direction> save, BiConsumer<E, Direction> load) {
        return node(new SaveNodeFacing<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeBlockState(final String name, Function<E, BlockState> save, BiConsumer<E, BlockState> load) {
        return node(new SaveNodeBlockState<E>(name, save, load));
    }

    public NbtSaveRoot<E> nodeUUID(final String name, Function<E, UUID> save, BiConsumer<E, UUID> load) {
        return node(new SaveNodeUUID<E>(name, save, load));
    }

    public <C extends IBuildableObject> NbtSaveRoot<E> nodeBuildableObject(final String name, final IBuilderRegistry<C> reg, Function<E, C> getter, BiConsumer<E, C> setter) {
        return node(new SaveBuildableObject<E, C>(name, reg, getter, setter));
    }

    public <C extends INBTSerializable<CompoundTag>> NbtSaveRoot<E> nodeINBTSerializable(final String name, Function<E, C> accessor) { //TODO recode to allow any NBTBase
        return node(new NbtSaveNode<E, CompoundTag>(name,
            (source) -> Optional.ofNullable(accessor.apply(source)).map(INBTSerializable::serializeNBT).orElse(null),
            (source, data) -> {
                final C object = accessor.apply(source);
                if (object != null) {
                    object.deserializeNBT(data);
                }
            }
        ));
    }

    public <C extends INBTSerializable<CompoundTag>> NbtSaveRoot<E> nodeINBTSerializable(final String name, Function<E, C> save, BiConsumer<E, C> load, Supplier<C> builder) {
        return node(new NbtSaveNode<E, CompoundTag>(name,
            (source) -> Optional.ofNullable(save.apply(source)).map(INBTSerializable::serializeNBT).orElse(null),
            (source, data) -> {
                final C object = builder.get();
                if (object != null) {
                    object.deserializeNBT(data);
                }
                load.accept(source, object);
            }
        ));
    }

    /**
     * Goes up one nested level
     *
     * @return parent, can be null if this root is at base
     */
    public NbtSaveRoot<E> parent() {
        return parent;
    }

    /**
     * Goes to top most level in save tree
     *
     * @return handler
     */
    public NbtSaveHandler<E> base() {
        return handler;
    }
}
