package icbm.classic.lib.recipe;

import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.crafting.IRecipeFactory;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapedOreRecipe;

/**
 * Created by Dark(DarkGuardsman, Robin) on 7/28/2019.
 */
public class ExRecipeFactory implements IRecipeFactory
{
    public static final String RESULT_KEY = "result";
    public static final String ITEM_KEY = "item";
    public static final String METADATA_KEY = "data";
    public static final String STACKSIZE_KEY = "count";

    @Override
    public IRecipe parse(JsonContext context, JsonObject json)
    {
        //Pull result from json
        final JsonObject resultObject = JsonUtils.getJsonObject(json, RESULT_KEY);
        final ItemStack resultStack = ExIngredientFactory.getStack(resultObject);

        //Convert stack back to json
        final JsonObject newResult = new JsonObject();
        newResult.addProperty(ITEM_KEY, resultStack.getItem().getRegistryName().toString());
        if (resultStack.getHasSubtypes())
        {
            newResult.addProperty(METADATA_KEY, resultStack.getItemDamage());
        }
        newResult.addProperty(STACKSIZE_KEY, JsonUtils.getInt(json, STACKSIZE_KEY, 1));

        //TODO add support NBT

        //Update json
        json.add(RESULT_KEY, newResult);

        return ShapedOreRecipe.factory(context, json);
    }
}
