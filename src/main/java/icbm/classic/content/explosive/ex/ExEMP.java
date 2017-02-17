package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastEMP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExEMP extends Explosion
{
    public ExEMP()
    {
        super("emp", 3);
        this.missileModelPath = "missiles/tier3/missile_head_emp.obj";
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastEMP(world, entity, x, y, z, 50).setEffectBlocks().setEffectEntities().explode();
    }

    @Override
    public void init()
    {
        boolean registered = false;
        //IC2:itemAdvBat

        Object[] items = {"battery", InventoryUtility.getItem("IC2:itemBatRE"), "capacitorBasic"};
        for (Object object : items)
        {
            if (object != null && (!(object instanceof String) || OreDictionary.doesOreNameExist((String) object)))
            {
                RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.EMP.getItemStack(),
                        "RBR", "BTB", "RBR",
                        'T', Explosives.REPLUSIVE.getItemStack(),
                        'R', Blocks.redstone_block,
                        'B', object), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
                registered = true;
            }
        }
        if (!registered)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.EMP.getItemStack(),
                    "RBR", "BTB", "RBR",
                    'T', Explosives.REPLUSIVE.getItemStack(),
                    'R', Blocks.redstone_block,
                    'B', Items.emerald), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
