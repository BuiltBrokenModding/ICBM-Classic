package icbm.classic.content.explosive.ex;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.AdvancedModelLoader;
import net.minecraftforge.client.model.IModelCustom;

public abstract class Explosion extends Explosive
{
    @SideOnly(Side.CLIENT)
    private ResourceLocation resourceLocation;

    @SideOnly(Side.CLIENT)
    private IModelCustom model;

    protected String modelName;

    public Explosion(String name, int tier)
    {
        super(name, tier);
    }

    /** Called when launched. */
    public void launch(EntityMissile missileObj)
    {
    }

    /** Called every tick while flying. */
    public void update(EntityMissile missileObj)
    {
    }

    public boolean onInteract(EntityMissile missileObj, EntityPlayer entityPlayer)
    {
        return false;
    }

    /**
     * Is this missile compatible with the cruise launcher?
     *
     * @return
     */
    public boolean isCruise()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getMissileResource()
    {
        if (this.resourceLocation == null)
        {
            this.resourceLocation = new ResourceLocation(ICBMClassic.DOMAIN, "textures/models/" + "missile_" + this.getUnlocalizedName() + ".png");
        }

        return this.resourceLocation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public IModelCustom getMissileModel()
    {
        try
        {
            if (this.model == null)
            {
                model = AdvancedModelLoader.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/" + this.modelName));
            }
        }
        catch (Exception e)
        {
            ICBMClassic.INSTANCE.logger().error("Unexpected error while loading missile Model[ " + this.modelName + "]", e);
        }

        return model;
    }
}