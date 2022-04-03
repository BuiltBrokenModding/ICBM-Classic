package icbm.classic.lib.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.content.reg.ItemReg;
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
    public static final String DEVICE_KEY = "device";
    public static final String EX_KEY = "explosive";

    @Nonnull
    @Override
    public Ingredient parse(JsonContext context, JsonObject json)
    {
        return parse(json);
    }

    @Nonnull
    public static ItemStack getStack(JsonObject json)
    {
        final String device = JsonUtils.getString(json, DEVICE_KEY, ICBMClassicAPI.EX_BLOCK.toString());

        //TODO fix having to work around missile module not showing in content registry as `icbmclassic:missile`
        if("icbmclassic:missile.module".equalsIgnoreCase(device)) {
            return new ItemStack(ItemReg.itemMissile, 1, 24);
        }

        final String explosive = JsonUtils.getString(json, EX_KEY);

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
