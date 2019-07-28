package icbm.classic.lib.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IIngredientFactory;
import net.minecraftforge.common.crafting.JsonContext;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robert) on 7/28/2019.
 */
public class ExIngredientFactory implements IIngredientFactory
{
    @Nonnull
    @Override
    public Ingredient parse(JsonContext context, JsonObject json)
    {
        return parse(json);
    }

    @Nonnull
    public static ItemStack getStack(JsonObject json)
    {
        final String device = JsonUtils.getString(json, "device", ICBMClassicAPI.EX_BLOCK.toString());
        final String explosive = JsonUtils.getString(json, "explosive");

        final IExplosiveContentRegistry reg = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getContentRegistry(new ResourceLocation(device));
        if (reg != null)
        {
            ItemStack exStack = reg.getDeviceStack(new ResourceLocation(explosive));
            if (exStack != null)
            {
                return exStack;
            }
            else
            {
                throw new JsonSyntaxException("ExIngredientFactory: Failed to locate explosive type of [" + explosive + "]");
            }
        }
        else
        {
            throw new JsonSyntaxException("ExIngredientFactory: Failed to locate device type of [" + device + "]");
        }
    }

    @Nonnull
    public static Ingredient parse(JsonObject json)
    {
        return Ingredient.fromStacks(getStack(json));
    }
}
