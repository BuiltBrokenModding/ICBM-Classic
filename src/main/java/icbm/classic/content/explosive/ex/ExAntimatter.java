package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.ICBMClassic;
import icbm.classic.Settings;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastAntimatter;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExAntimatter extends Explosion
{
    public ExAntimatter()
    {
        super("antimatter", 4);
        this.setFuseTime(300);
        this.missileModelPath = "missiles/tier4/missile_head_antimatter.obj";
    }

    /** Called when the explosive is on fuse and going to explode. Called only when the explosive is
     * in it's TNT form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse */
    @Override
    public void onFuseTick(World worldObj, Pos position, int fuseTicks)
    {
        super.onFuseTick(worldObj, position, fuseTicks);

        if (fuseTicks % 25 == 0)
        {
            worldObj.playSoundEffect(position.x(), position.y(), position.z(), ICBMClassic.PREFIX + "alarm", 4F, 1F);
        }
    }

    @Override
    public void init()
    {
        if(OreDictionary.doesOreNameExist("antimatterGram"))
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.ANTIMATTER.getItemStack(),
                    "AAA", "AEA", "AAA",
                    'E', Explosives.NUCLEAR.getItemStack(),
                    'A', "antimatterGram"), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
        else
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.ANTIMATTER.getItemStack(),
                    "AAA", "AEA", "AAA",
                    'E', Explosives.NUCLEAR.getItemStack(),
                    'A', Items.nether_star), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastAntimatter(world, entity, x, y, z, Settings.ANTIMATTER_SIZE, Settings.ANTIMATTER_DESTROY_UNBREAKABLE_BLOCKS).explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
