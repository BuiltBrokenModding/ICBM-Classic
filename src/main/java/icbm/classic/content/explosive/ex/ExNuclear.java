package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastNuclear;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExNuclear extends Explosion
{
    public ExNuclear(String mingZi, int tier)
    {
        super(mingZi, tier);
        if (this.getTier() == 3)
        {
            this.missileModelPath = "missiles/tier3/missile_head_nuclear.obj";
        }
        else
        {
            this.missileModelPath = "missiles/tier3/missile_head_conflag.obj";
        }
    }

    @Override
    public void init()
    {
        if (this.getTier() == 3)
        {
            if (OreDictionary.getOres("ingotUranium").size() > 0)
            {
                RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.NUCLEAR.getItemStack(),
                        "UUU", "UEU", "UUU",
                        'E', Explosives.THERMOBARIC.getItemStack(),
                        'U', "ingotUranium"), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
            }
            else
            {
                RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.NUCLEAR.getItemStack(),
                        "EEE", "EEE", "EEE",
                        'E', Explosives.THERMOBARIC.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);

            }
        }
        else
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.THERMOBARIC.getItemStack(),
                    "CIC", "IRI", "CIC",
                    'R', Explosives.REPLUSIVE.getItemStack(),
                    'C', Explosives.CHEMICAL.getItemStack(),
                    'I', Explosives.INCENDIARY.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);

        }
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (this.getTier() == 3)
        {
            new BlastNuclear(world, entity, x, y, z, 50, 80).setNuclear().explode();
        }
        else
        {
            new BlastNuclear(world, entity, x, y, z, 30, 45).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
