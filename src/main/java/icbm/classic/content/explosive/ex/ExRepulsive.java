package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastTNT;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExRepulsive extends Explosion
{
    public ExRepulsive(String name, int tier)
    {
        super(name, tier);
        this.setYinXin(120);
        if (name.equalsIgnoreCase("attractive"))
        {
            this.missileModelPath = "missiles/tier1/missile_head_attraction.obj";
        }
        else
        {
            this.missileModelPath = "missiles/tier1/missile_head_repulsion.obj";
        }
    }

    @Override
    public void init()
    {
        if (this == Explosives.ATTRACTIVE.handler)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.ATTRACTIVE.getItemStack(),
                    "YY",
                    'Y', Explosives.CONDENSED.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
        else
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.REPLUSIVE.getItemStack(),
                    "Y", "Y",
                    'Y', Explosives.CONDENSED.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (this == Explosives.ATTRACTIVE.handler)
        {
            new BlastTNT(world, entity, x, y, z, 2f).setDestroyItems().setPushType(1).explode();
        }
        else
        {
            new BlastTNT(world, entity, x, y, z, 2f).setDestroyItems().setPushType(2).explode();

        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
