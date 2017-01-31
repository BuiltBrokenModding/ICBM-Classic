package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastShrapnel;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExShrapnel extends Explosion
{
    public ExShrapnel(String name, int tier)
    {
        super(name, tier);
        if (name.equalsIgnoreCase("shrapnel"))
        {
            this.missileModelPath = "missiles/tier1/missile_head_shrapnel.obj";
        }
        else if (name.equalsIgnoreCase("anvil"))
        {
            this.missileModelPath = "missiles/tier1/missile_head_anvil.obj";
        }
        else if (name.equalsIgnoreCase("fragmentation"))
        {
            this.missileModelPath = "missiles/tier2/missile_head_frag.obj";
        }
    }

    @Override
    public void init()
    {
        if (this == Explosives.SHRAPNEL.handler)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.SHRAPNEL.getItemStack(),
                    "???", "?@?", "???",
                    '@', Explosives.REPLUSIVE.getItemStack(),
                    '?', Items.arrow), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
        else if (this == Explosives.ANVIL.handler)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.ANVIL.getItemStack(10),
                    "SSS", "SAS", "SSS",
                    'A', Blocks.anvil,
                    'S', Explosives.SHRAPNEL.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
        else if (this == Explosives.FRAGMENTATION.handler)
        {
            RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.FRAGMENTATION.getItemStack(),
                    " @ ", "@?@", " @ ",
                    '?', Explosives.INCENDIARY.getItemStack(),
                    '@', Explosives.SHRAPNEL.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
        }
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (this.getTier() == 2)
        {
            new BlastShrapnel(world, entity, x, y, z, 15, true, true, false).explode();
        }
        else if (this == Explosives.ANVIL.handler)
        {
            new BlastShrapnel(world, entity, x, y, z, 25, false, false, true).explode();
        }
        else
        {
            new BlastShrapnel(world, entity, x, y, z, 30, true, false, false).explode();
        }
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
