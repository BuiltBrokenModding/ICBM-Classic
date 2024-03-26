package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.saving.nodes.SaveNodeVec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
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
        if (position == null) {
            if (stack.getTagCompound() == null) {
                return;
            }
            stack.getTagCompound().remove(POS_KEY);
            return;
        }

        // Setup data
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new CompoundTag());
        }

        stack.getTagCompound().put(POS_KEY, SaveNodeVec3.save(position));
    }

    @Override
    public void setLevel(@Nullable ResourceKey<Level> dimension) {
        // Ignore empty clearing empty
        if (dimension == null) {
            if (stack.getTagCompound() == null) {
                return;
            }
            stack.getTagCompound().remove(DIM_KEY);
            return;
        }

        // Setup data
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new CompoundTag());
        }

        stack.getTagCompound().setInteger(DIM_KEY, dimension);
    }

    @Nullable
    @Override
    public Vec3 getPosition() {
        if (stack.getTagCompound() != null && stack.getTagCompound().contains(POS_KEY)) {
            return SaveNodeVec3.load(stack.getTagCompound().getCompound(POS_KEY));
        }
        return null;
    }

    @Nullable
    @Override
    public ResourceKey<Level> getLevelId() {
        if (stack.getTagCompound() != null && stack.getTagCompound().contains(DIM_KEY)) {
            return stack.getTagCompound().getInteger(POS_KEY);
        }
        return null;
    }
}
