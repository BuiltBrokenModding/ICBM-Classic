package icbm.classic.lib.explosive.cap;

import net.minecraft.item.ItemStack;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityExplosiveStack extends CapabilityExplosive
{
    public final ItemStack stack;

    public CapabilityExplosiveStack(ItemStack stack)
    {
        super(stack.getItemDamage());
        this.stack = stack;
    }

    @Nullable
    @Override
    public ItemStack toStack()
    {
        return stack;
    }
}
