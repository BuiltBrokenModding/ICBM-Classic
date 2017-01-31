package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastChemical;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExDebilitation extends Explosion
{
    public ExDebilitation(String mingZi, int tier)
    {
        super(mingZi, tier);
        this.missileModelPath = "missiles/tier1/missile_head_dibilitation.obj";
    }

    @Override
    public void init()
    {
        RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.DEBLITATION.getItemStack(3),
                "SSS", "WRW", "SSS",
                'R', Explosives.REPLUSIVE.getItemStack(),
                'W', Items.water_bucket,
                'S', "dustSulfur"), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastChemical(world, entity, x, y, z, 20, 20 * 30, false).setConfuse().explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
