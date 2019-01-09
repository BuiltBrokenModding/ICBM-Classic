package icbm.classic.lib.capability.ex;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveStack extends CapabilityExplosive
{

    public static final String NBT_STACK = "explosive_stack";
    public ItemStack stack;

    public CapabilityExplosiveStack(ItemStack stack)
    {
        super(stack.getItemDamage());
        this.stack = stack;
        this.stack.setCount(1);
    }

    public CapabilityExplosiveStack(NBTTagCompound tagCompound)
    {
        stack = new ItemStack(tagCompound);
        initData();
    }

    public void initData()
    {
        explosiveID = stack.getItemDamage();

        if (stack.getTagCompound() != null && stack.getTagCompound().hasKey(NBT_EXPLOIVE))
        {
            deserializeNBT(stack.getTagCompound().getCompoundTag(NBT_EXPLOIVE));
        }
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        if (stack != null)
        {
            ItemStack copy = stack.copy();
            if (copy.getTagCompound() == null)
            {
                copy.setTagCompound(new NBTTagCompound());
            }
            copy.getTagCompound().setTag(NBT_EXPLOIVE, serializeNBT());

            return copy;
        }
        return null;
    }
}
