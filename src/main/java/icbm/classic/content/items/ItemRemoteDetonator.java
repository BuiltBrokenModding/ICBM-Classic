package icbm.classic.content.items;

import com.builtbroken.mc.api.items.hz.IItemFrequency;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.recipe.OreNames;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.world.map.radio.RadioRegistry;
import com.builtbroken.mc.prefab.hz.FakeRadioSender;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

import java.util.List;

/**
 * Remotely triggers missile launches on a set frequency, call back ID, and pass key. Will not funciton if any of those
 * data points is missing.
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/26/2016.
 */
public class ItemRemoteDetonator extends ItemICBMElectrical implements IRecipeContainer, IItemFrequency
{
    public static final int ENERGY = 1000;

    public ItemRemoteDetonator()
    {
        super("remoteDetonator");
        this.setHasSubtypes(true);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
    {
        if (!world.isRemote)
        {
            RadioRegistry.popMessage(world, new FakeRadioSender(player, stack, 2000), getBroadCastHz(stack), "activateLauncher");
        }
        return stack;
    }

    @Override
    public boolean doesSneakBypassUse(World world, int x, int y, int z, EntityPlayer player)
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean b)
    {
        list.add("Fires missiles remotely");
        list.add("Right click launcher screen to encode");
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        recipes.add(newShapedRecipe(this, "RNP", "RCW", "CTT", 'R', OreNames.ROD_IRON, 'N', OreNames.NUGGET_IRON, 'C', UniversalRecipe.CIRCUIT_T1.get(), 'T', Items.redstone, 'P', OreNames.PLATE_IRON, 'W', OreNames.WIRE_COPPER));
    }
}
