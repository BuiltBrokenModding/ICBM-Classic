package icbm.classic.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.crafting.IConditionFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.OreDictionary;

import java.util.function.BooleanSupplier;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 3/20/2018.
 */
public class ConditionOreValue implements IConditionFactory
{
    @Override
    public BooleanSupplier parse(JsonContext context, JsonObject json)
    {
        final boolean condition = Boolean.parseBoolean(JsonUtils.getString(json, "condition").toLowerCase());
        final String value = JsonUtils.getString(json, "value");
        return () -> hasOreValue(value, condition);
    }

    private boolean hasOreValue(String value, boolean check)
    {
        NonNullList<ItemStack> list = OreDictionary.getOres(value);
        boolean hasValue = false;
        for (ItemStack stack : list)
        {
            if (!stack.isEmpty())
            {
                return hasValue == check;
            }
        }
        return hasValue == check;
    }
}
