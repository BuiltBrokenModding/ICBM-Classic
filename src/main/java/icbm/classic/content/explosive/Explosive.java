package icbm.classic.content.explosive;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.framework.explosive.handler.ExplosiveHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.prefab.ModelICBM;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IModelCustom;
import resonant.api.explosion.IExplosive;

/** The explosive registry class. Used to register explosions. */
public abstract class Explosive extends ExplosiveHandler implements IExplosive
{
    /** The unique identification name for this explosive. */
    private String nameID;
    /** The tier of this explosive */
    private int tier;
    /** The fuse of this explosive */
    private int fuseTime;
    /** Is this explosive disabled? */
    protected boolean isDisabled;
    /** Is this explosive able to be pushed by other explosions? */
    protected boolean isMobile = false;

    protected boolean hasBlock;
    protected boolean hasGrenade;
    protected boolean hasMinecart;
    protected boolean hasMissile;

    protected Explosive(String name, int tier)
    {
        super(name);
        this.nameID = name;
        this.tier = tier;
        this.fuseTime = 100;

        this.hasBlock = true;
        this.hasMissile = true;
        this.hasGrenade = this.tier <= 1;
        this.hasMinecart = this.tier <= 2;

        //this.flagName = //FlagRegistry.registerFlag("ban_" + this.nameID);
        this.isDisabled = false;//Settings.CONFIGURATION.get("Disable_Explosives", "Disable " + this.nameID, false).getBoolean(false);

    }

    //@Override
    //public final int getID()
    //{
    //    return ExplosiveRegistry.getID(this.getUnlocalizedName());
    //}


    public String getUnlocalizedName()
    {
        return this.nameID;
    }


    public String getExplosiveName()
    {
        return LanguageUtility.getLocal("icbm.explosive." + this.nameID + ".name");
    }


    public String getGrenadeName()
    {
        return LanguageUtility.getLocal("icbm.grenade." + this.nameID + ".name");
    }


    public String getMissileName()
    {
        return LanguageUtility.getLocal("icbm.missile." + this.nameID + ".name");
    }


    public String getMinecartName()
    {
        return LanguageUtility.getLocal("icbm.minecart." + this.nameID + ".name");
    }


    public int getTier()
    {
        return this.tier;
    }


    public void setTier(int tier)
    {
        this.tier = tier;
    }

    public Explosive setFuseTime(int fuse)
    {
        this.fuseTime = fuse;
        return this;
    }

    /**
     * The fuse of the explosion
     *
     * @return The Fuse
     */
    public int getFuseTime()
    {
        return fuseTime;
    }

    /**
     * Called at the before the explosive detonated as a block.
     *
     * @param world
     * @param entity
     */
    public void playFuseSound(World world, Entity entity)
    {
        world.playSoundAtEntity(entity, "random.fuse", 1.0F, 1.0F);
    }

    /**
     * Called while the explosive is being detonated (fuse ticks) in block form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse
     */
    public void onFuseTick(World world, Pos position, int fuseTicks)
    {
        world.spawnParticle("smoke", position.x(), position.y() + 0.5D, position.z(), 0.0D, 0.0D, 0.0D);
    }

    @SideOnly(Side.CLIENT)
    public ModelICBM getBlockModel()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public ResourceLocation getBlockResource()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IIcon getIcon()
    {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IModelCustom getMissileModel()
    {
        return null;
    }

    public boolean hasGrenadeForm()
    {
        return this.hasGrenade;
    }

    public boolean hasMissileForm()
    {
        return this.hasMissile;
    }

    public boolean hasMinecartForm()
    {
        return this.hasMinecart;
    }

    public boolean hasBlockForm()
    {
        return this.hasBlock;
    }

    /** Called to add the recipe for this explosive */
    public void init()
    {

    }

    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer entityPlayer, int par6, float par7, float par8, float par9)
    {
        return false;
    }

    public void createExplosion(World world, double x, double y, double z, Entity entity)
    {
        if (!this.isDisabled)
        {
            this.doCreateExplosion(world, x, y, z, entity);
        }
    }

    public abstract void doCreateExplosion(World world, double x, double y, double z, Entity entity);
}
