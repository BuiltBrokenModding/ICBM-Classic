package icbm.classic.prefab.item;

import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.conditions.LootCondition;

import java.util.Collection;
import java.util.Random;

/**
 * ItemStack version of {@link net.minecraft.world.storage.loot.LootEntryItem}
 */
public class LootEntryItemStack extends LootEntry
{
    public static Random random = new Random();
    protected final ItemStack itemStack;

    public LootEntryItemStack(String entryName, ItemStack itemIn, int weightIn, int qualityIn, LootCondition... conditionsIn)
    {
        super(weightIn, qualityIn, conditionsIn, entryName);
        this.itemStack = itemIn;
    }

    @Override
    public void addLoot(Collection<ItemStack> stacks, Random rand, LootContext context)
    {
        if (!itemStack.isEmpty())
        {
            ItemStack loot_stack = itemStack.copy();
            loot_stack.setCount(random.nextInt(loot_stack.getCount()));
            if (loot_stack.getCount() < loot_stack.getMaxStackSize())
            {
                stacks.add(loot_stack);
            }
            else
            {
                int i = loot_stack.getCount();
                while (i > 0)
                {
                    ItemStack itemstack1 = loot_stack.copy();
                    itemstack1.setCount(Math.min(loot_stack.getMaxStackSize(), i));
                    i -= itemstack1.getCount();
                    stacks.add(itemstack1);
                }
            }
        }
    }

    @Override
    protected void serialize(JsonObject json, JsonSerializationContext context)
    {

    }
}