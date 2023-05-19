package icbm.classic.lib.capability.gps;

import icbm.classic.api.caps.IGPSData;
import icbm.classic.lib.saving.nodes.SaveNodeVec3d;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;


public class CapabilityGPSDataItem implements IGPSData {

    public static final String POS_KEY = "pos";
    public static final String DIM_KEY = "dim";

    private final ItemStack stack;

    public CapabilityGPSDataItem(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public void setPosition(@Nullable Vec3d position) {

        // Ignore empty clearing empty
        if(position == null) {
            if(stack.getTagCompound() == null) {
                return;
            }
            stack.getTagCompound().removeTag(POS_KEY);
            return;
        }

        // Setup data
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setTag(POS_KEY, SaveNodeVec3d.save(position));
    }

    @Override
    public void setWorld(@Nullable Integer dimension) {
        // Ignore empty clearing empty
        if(dimension == null) {
            if(stack.getTagCompound() == null) {
                return;
            }
            stack.getTagCompound().removeTag(DIM_KEY);
            return;
        }

        // Setup data
        if(stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        stack.getTagCompound().setInteger(DIM_KEY, dimension);
    }

    @Nullable
    @Override
    public Vec3d getPosition() {
        if(stack.getTagCompound() != null && stack.getTagCompound().hasKey(POS_KEY)) {
            return SaveNodeVec3d.load(stack.getTagCompound().getCompoundTag(POS_KEY));
        }
        return null;
    }

    @Nullable
    @Override
    public Integer getWorldId() {
        if(stack.getTagCompound() != null && stack.getTagCompound().hasKey(DIM_KEY)) {
            return stack.getTagCompound().getInteger(POS_KEY);
        }
        return null;
    }
}
