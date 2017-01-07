package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastSonic;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExSonic extends Explosion
{
    public ExSonic(String mingZi, int tier)
    {
        super(mingZi, tier);
        if (this.getTier() == 3)
        {
            this.modelName = "missile_sonic.tcn";
        }
        else
        {
            this.modelName = "missile_ion.tcn";
        }
    }

    @Override
    public void init()
    {
        if (this.getTier() == 3)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.HYPERSONIC.getItemStack(),
                    " S ", "S S", " S ", 'S', Explosives.SONIC.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
        else
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.SONIC.getItemStack(),
                    "@?@", "?R?", "@?@",
                    'R', Explosives.REPLUSIVE.getItemStack(),
                    '?', Blocks.noteblock,
                    '@', UniversalRecipe.SECONDARY_METAL.get()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (this.getTier() == 3)
        {
            new BlastSonic(world, entity, x, y, z, 20, 35).setShockWave().explode();
        }
        else
        {
            new BlastSonic(world, entity, x, y, z, 15, 30).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
