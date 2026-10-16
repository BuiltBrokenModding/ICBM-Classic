package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.StackNbt;
import icbm.classic.lib.saving.nodes.SaveNodeVec3d;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;


public class CapabilityGPSDataItem implements IGPSData {

    public static final String POS_KEY = "pos";
    public static final String DIM_KEY = "dim";

    private final ItemStack stack;

    public CapabilityGPSDataItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void setPosition(@Nullable Vec3 position) {

        // Ignore empty clearing empty
        if(position == null) {
            StackNbt.remove(stack, POS_KEY);
            return;
        }

        StackNbt.update(stack, tag -> tag.put(POS_KEY, SaveNodeVec3d.save(position)));
    }

    @Override
    public void setWorld(@Nullable ResourceKey<Level> dimension) {
        // Ignore empty clearing empty
        if(dimension == null) {
            StackNbt.remove(stack, DIM_KEY);
            return;
        }

        // PORT: int dimension id replaced by the dimension key string
        StackNbt.update(stack, tag -> tag.putString(DIM_KEY, dimension.location().toString()));
    }

    @Nullable
    @Override
    public Vec3 getPosition() {
        if(StackNbt.has(stack, POS_KEY)) {
            return SaveNodeVec3d.load(StackNbt.getCompound(stack, POS_KEY));
        }
        return null;
    }

    @Nullable
    @Override
    public ResourceKey<Level> getWorldId() {
        final CompoundTag tag = StackNbt.get(stack);
        if(tag.contains(DIM_KEY, CompoundTag.TAG_STRING)) {
            // PORT: 1.12 read the int from POS_KEY by mistake, so it never returned a real dimension
            final ResourceLocation location = ResourceLocation.tryParse(tag.getString(DIM_KEY));
            if(location != null) {
                return ResourceKey.create(Registries.DIMENSION, location);
            }
        }
        return null;
    }
}
// PORTED
