package icbm.classic.lib.saving;

import icbm.classic.api.reg.obj.IBuildableObject;
import icbm.classic.api.reg.obj.IBuilderRegistry;
import icbm.classic.lib.saving.nodes.*;
import icbm.classic.lib.transform.rotation.EulerAngle;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NbtSaveRoot<SaveObject> implements INbtSaveNode<SaveObject, CompoundNBT>
{
    private final String name;
    private final NbtSaveHandler<SaveObject> handler;
    private final NbtSaveRoot<SaveObject> parent;
    public final List<INbtSaveNode> nodes = new LinkedList();

    private boolean shouldSave = true;

    public NbtSaveRoot(String name, NbtSaveHandler<SaveObject> handler, NbtSaveRoot<SaveObject> parent)
    {
        if (name == null)
        {
            throw new IllegalArgumentException("save key can't be null");
        }
        this.name = name;
        this.handler = handler;
        this.parent = parent;
    }

    public NbtSaveRoot<SaveObject> disableSave() {
        shouldSave = false;
        return this;
    }

    @Override
    public String getSaveKey()
    {
        return name;
    }

    @Override
    public CompoundNBT save(SaveObject objectToSave)
    {
        if(!shouldSave) {
            return null;
        }
        return save(objectToSave, new CompoundNBT());
    }

    /**
     * Entry point for {@link NbtSaveHandler#save(Object, CompoundNBT)} to save directly to main root. Shouldn't
     * be used by anything else.
     *
     * @param objectToSave used to save
     * @param tagCompound  to save against
     * @return save
     */
    protected CompoundNBT save(SaveObject objectToSave, CompoundNBT tagCompound)
    {
        nodes.forEach(node -> {
            final INBT tag = node.save(objectToSave);
            if (tag != null && (!(tag instanceof CompoundNBT) || !((CompoundNBT) tag).isEmpty()))
            {
                tagCompound.put(node.getSaveKey(), tag);
            }
        });
        return tagCompound;
    }

    @Override
    public void load(SaveObject objectToLoad, CompoundNBT save)
    {
        if (save != null && save.isEmpty())
        {
            nodes.forEach(node -> {
                if (save.contains(node.getSaveKey()))
                {
                    node.load(objectToLoad, save.get(node.getSaveKey()));
                }
            });
        }
    }

    public NbtSaveRoot<SaveObject> addRoot(final String name)
    {
        final NbtSaveRoot<SaveObject> root = new NbtSaveRoot<>(name, handler, this);
        nodes.add(root);
        return root;
    }

    public <O extends INBT> NbtSaveRoot<SaveObject> node(NbtSaveNode<SaveObject, O> node)
    {
        nodes.add(node);
        return this;
    }

    public NbtSaveRoot<SaveObject> nodeString(final String name, Function<SaveObject, String> save, BiConsumer<SaveObject, String> load)
    {
        return node(new SaveNodeString<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeResourceLocation(final String name, Function<SaveObject, ResourceLocation> save, BiConsumer<SaveObject, ResourceLocation> load)
    {
        return node(new SaveNodeResourceLocation<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeCompoundTag(final String name, Function<SaveObject, CompoundNBT> save, BiConsumer<SaveObject, CompoundNBT> load)
    {
        return node(new SaveNodeCompoundTag<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeByte(final String name, Function<SaveObject, Byte> save, BiConsumer<SaveObject, Byte> load)
    {
        return node(new SaveNodeByte<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeInteger(final String name, Function<SaveObject, Integer> save, BiConsumer<SaveObject, Integer> load)
    {
        return node(new SaveNodeInteger<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeDouble(final String name, Function<SaveObject, Double> save, BiConsumer<SaveObject, Double> load)
    {
        return node(new SaveNodeDouble<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeFloat(final String name, Function<SaveObject, Float> save, BiConsumer<SaveObject, Float> load)
    {
        return node(new SaveNodeFloat<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeBoolean(final String name, Function<SaveObject, Boolean> save, BiConsumer<SaveObject, Boolean> load)
    {
        return node(new SaveNodeBoolean<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeBlockPos(final String name, Function<SaveObject, BlockPos> save, BiConsumer<SaveObject, BlockPos> load)
    {
        return node(new SaveNodeBlockPos<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeVec3d(final String name, Function<SaveObject, Vec3d> save, BiConsumer<SaveObject, Vec3d> load)
    {
        return node(new SaveNodeVec3d<SaveObject>(name, save, load));
    }

    @Deprecated
    public NbtSaveRoot<SaveObject> nodeEulerAngle(final String name, Function<SaveObject, EulerAngle> save, BiConsumer<SaveObject, EulerAngle> load)
    {
        return node(new NbtSaveNode<SaveObject, CompoundNBT>(name,
            (saveObject) -> {
                final EulerAngle angle = save.apply(saveObject);
                if (angle != null)
                {
                    return angle.toNBT();
                }
                return null;
            },
            (saveObject, data) -> {
                load.accept(saveObject, new EulerAngle(data));
            }
        ));
    }

    public <EnumVal extends Enum<EnumVal>> NbtSaveRoot<SaveObject> nodeEnumString(final String name, Function<SaveObject, EnumVal> save, BiConsumer<SaveObject, EnumVal> load, Function<String, EnumVal> accessor) {
        return node(new SaveNodeEnum<SaveObject, EnumVal>(name, save, load, accessor));
    }

    @Deprecated //switch to nodeEnumString for better human readability of save data
    public NbtSaveRoot<SaveObject> nodeFacing(final String name, Function<SaveObject, Direction> save, BiConsumer<SaveObject, Direction> load)
    {
        return node(new SaveNodeFacing<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeBlockState(final String name, Function<SaveObject, BlockState> save, BiConsumer<SaveObject, BlockState> load)
    {
        return node(new SaveNodeBlockState<SaveObject>(name, save, load));
    }

    public NbtSaveRoot<SaveObject> nodeUUID(final String name, Function<SaveObject, UUID> save, BiConsumer<SaveObject, UUID> load)
    {
        return node(new SaveNodeUUID<SaveObject>(name, save, load));
    }

    public <BuildableObject extends IBuildableObject> NbtSaveRoot<SaveObject> nodeBuildableObject(final String name,
                                                                                                  final Supplier<IBuilderRegistry<BuildableObject>> reg, Function<SaveObject, BuildableObject> getter, BiConsumer<SaveObject, BuildableObject> setter)
    {
        return node(new SaveBuildableObject<SaveObject, BuildableObject>(name, reg, getter, setter));
    }

    public <BuildableObject extends IBuildableObject> NbtSaveRoot<SaveObject> nodeBuildableObjectList(final String name,
                                                                                                  final Supplier<IBuilderRegistry<BuildableObject>> reg, Function<SaveObject, Collection<BuildableObject>> getter)
    {
        return node(new SaveBuildableObjectList<SaveObject, BuildableObject>(name, reg, getter));
    }



    public <SerializableObject extends INBTSerializable<CompoundNBT>> NbtSaveRoot<SaveObject> nodeINBTSerializable(final String name,
                                                                                                                   Function<SaveObject, SerializableObject> accessor) { //TODO recode to allow any INBT
        return node(new NbtSaveNode<SaveObject, CompoundNBT>(name,
            (source) -> Optional.ofNullable(accessor.apply(source)).map(INBTSerializable::serializeNBT).orElse(null),
            (source, data) -> {
                final SerializableObject object = accessor.apply(source);
                if(object != null) {
                    object.deserializeNBT(data);
                }
            }
        ));
    }

    public <C extends INBTSerializable<CompoundNBT>> NbtSaveRoot<SaveObject> nodeINBTSerializable(final String name, Function<SaveObject, C> save, BiConsumer<SaveObject, C> load, Supplier<C> builder) {
        return node(new NbtSaveNode<SaveObject, CompoundNBT>(name,
            (source) -> Optional.ofNullable(save.apply(source)).map(INBTSerializable::serializeNBT).orElse(null),
            (source, data) -> {
                final C object = builder.get();
                if(object != null) {
                    object.deserializeNBT(data);
                }
                load.accept(source, object);
            }
        ));
    }

    public NbtSaveRoot<SaveObject> nodeItemStack(String name, Function<SaveObject, ItemStack> save, BiConsumer<SaveObject, ItemStack> load) {
        return node(new SaveNodeItemStack<>(name, save, load));
    }

    /**
     * Goes up one nested level
     *
     * @return parent, can be null if this root is at base
     */
    public NbtSaveRoot<SaveObject> parent()
    {
        return parent;
    }

    /**
     * Goes to top most level in save tree
     *
     * @return handler
     */
    public NbtSaveHandler<SaveObject> base()
    {
        return handler;
    }
}
