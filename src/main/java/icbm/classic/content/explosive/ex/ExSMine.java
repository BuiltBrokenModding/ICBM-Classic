package icbm.classic.content.explosive.ex;

import com.builtbroken.mc.api.edit.IWorldChangeAction;
import com.builtbroken.mc.api.event.TriggerCause;
import com.builtbroken.mc.lib.helper.recipe.RecipeUtility;
import com.builtbroken.mc.imp.transform.vector.Pos;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.client.models.ModelSMine;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.blast.BlastMine;
import icbm.classic.prefab.ModelICBM;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.oredict.ShapedOreRecipe;

public class ExSMine extends Explosive
{
    public ExSMine(String mingZi, int tier)
    {
        super(mingZi, tier);
        this.setFuseTime(20);
        this.hasGrenade = false;
        this.hasMinecart = false;
        this.hasMissile = false;
    }

    @Override
    public void onYinZha(World worldObj, Pos position, int fuseTicks)
    {

    }

    @Override
    public void init()
    {
        RecipeUtility.addRecipe(new ShapedOreRecipe(Explosives.SMINE.getItemStack(),
                "S", "L", "R",
                'S', Explosives.FRAGMENTATION.getItemStack(),
                'L', Explosives.ATTRACTIVE.getItemStack(),
                'R', Explosives.REPLUSIVE.getItemStack()), this.getUnlocalizedName(), ICBMClassic.INSTANCE.getConfig(), true);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelICBM getBlockModel()
    {
        return ModelSMine.INSTANCE;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ResourceLocation getBlockResource()
    {
        return ModelSMine.TEXTURE;
    }

    @Override
    public void doCreateExplosion(World world, double x, double y, double z, Entity entity)
    {
        new BlastMine(world, entity, x, y, z, 5).explode();
    }

    @Override
    public IWorldChangeAction createBlastForTrigger(World world, double x, double y, double z, TriggerCause triggerCause, double size, NBTTagCompound tag)
    {
        return null;
    }
}
