package icbm.classic.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Created by Dark(DarkGuardsman, Robert) on 7/28/2019.
 */
public class ExRecipeFactory implements IRecipeFactory
{
    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        //Pull result from json
        final JsonObject resultObject = JsonUtils.getJsonObject(json, "result");
        final ItemStack resultStack = ExIngredientFactory.getStack(resultObject);

        //Convert stack back to json
        JsonObject newResult = new JsonObject();
        newResult.addProperty("item", resultStack.getItem().getRegistryName().toString());
        if (resultStack.getHasSubtypes())
        {
            newResult.addProperty("data", resultStack.getItemDamage());
        }
        newResult.addProperty("count", JsonUtils.getInt(json, "count", 1));

        //TODO add support NBT

        //Update json
        json.add("result", newResult);

        return ShapedOreRecipe.factory(context, json);
    }
}
