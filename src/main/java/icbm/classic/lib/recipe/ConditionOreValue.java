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
 * Checks if the given ore-dictionary value matches our condition
 *
 * condition(false) -> requires no ore-dictionary values to be present
 * condition(true) -> requires at least 1 valid stack to match the name
 *
 * Json:
 *  "condition" -> true/false, true you are looking for it, false you are looking for it to not exist
 *  "value" -> string name of the ore value to find, Ex: "ingotSteel"
 *
 * Created by Dark(DarkGuardsman, Robin) on 3/20/2018.
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
        final NonNullList<ItemStack> list = OreDictionary.getOres(value);
        return !check && (list == null || list.isEmpty())
            || check == (list != null && list.stream().anyMatch(stack -> stack != null && !stack.isEmpty()));
    }
}
