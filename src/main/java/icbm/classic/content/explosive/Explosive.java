package icbm.classic.content.explosive;

import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.LanguageUtility;
import icbm.classic.prefab.EnumTier;
import icbm.classic.prefab.ModelICBM;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import resonant.api.explosion.IExplosive;

/** The explosive registry class. Used to register explosions. */
public abstract class Explosive implements IExplosive
{
    /** The unique identification name for this explosive. */
    private String nameID;
    /** The tier of this explosive */
    private EnumTier tier;
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

    public ResourceLocation missileModel;
    public ResourceLocation missileTexture;
    public boolean renderBodyForMissilTier = true;
    public float missileRenderScale = 1f; //0.00625f

    protected Explosive(String name, EnumTier tier)
    {
        this.nameID = name;
        this.tier = tier;
        this.fuseTime = 100;

        this.hasBlock = true;
        this.hasMissile = true;
        this.hasGrenade = this.tier.ordinal() <= EnumTier.ONE.ordinal();
        this.hasMinecart = this.tier.ordinal() <= EnumTier.TWO.ordinal();

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


    public EnumTier getTier()
    {
        return this.tier;
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
    public int getYinXin()
    {
        return fuseTime;
    }

    /**
     * Called at the before the explosive detonated as a block.
     *
     * @param world
     * @param entity
     */
    public void yinZhaQian(World world, Entity entity)
    {
        world.playSound(entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F, true);
    }

    /**
     * Called while the explosive is being detonated (fuse ticks) in block form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse
     */
    public void onYinZha(World world, Pos position, int fuseTicks)
    {
        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, position.x(), position.y() + 0.5D, position.z(), 0.0D, 0.0D, 0.0D);
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

    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityPlayer, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        return false;
    }

    public void createExplosion(World world, BlockPos pos, Entity entity) //TODO switch back to x y z, as this causes all blasts to be centered on the block
    {
        if (!this.isDisabled)
        {
            this.doCreateExplosion(world, pos, entity);
        }
    }

    public abstract void doCreateExplosion(World world, BlockPos pos, Entity entity);
}
