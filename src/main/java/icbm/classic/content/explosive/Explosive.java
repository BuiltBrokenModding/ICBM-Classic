package icbm.classic.content.explosive;

import icbm.classic.api.EnumTier;
import icbm.classic.client.models.ModelICBM;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** The explosive registry class. Used to register explosions. */
public abstract class Explosive
{
    /** The unique identification name for this explosive. */
    private String nameID;
    /** The tier of this explosive */
    private EnumTier tier;
    /** The fuse of this explosive */
    private int fuseTime;
    /** Is this explosive disabled? */
    protected boolean isDisabled;

    protected boolean hasBlock;
    protected boolean hasGrenade;
    protected boolean hasMissile;

    protected Explosive(String name, EnumTier tier)
    {
        this.nameID = name;
        this.tier = tier;
        this.fuseTime = 100;

        this.hasBlock = true;
        this.hasMissile = true;
        this.hasGrenade = this.tier.ordinal() <= EnumTier.ONE.ordinal();

        //this.flagName = //FlagRegistry.registerFlag("ban_" + this.nameID);
        this.isDisabled = false;//Settings.CONFIGURATION.get("Disable_Explosives", "Disable " + this.nameID, false).getBoolean(false);

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
    public int getFuseTimer()
    {
        return fuseTime;
    }

    /**
     * Called at the before the explosive detonated as a block.
     *
     * @param world
     * @param entity
     */
    public void onEntityCreated(World world, Entity entity)
    {
        world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1.0F, 1.0F);
    }

    /**
     * Called while the explosive is being detonated (fuse ticks) in block form.
     *
     * @param fuseTicks - The amount of ticks this explosive is on fuse
     */
    public void onFuseTick(World world, Pos position, int fuseTicks)
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
        return hasBlockForm();
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

    public void createExplosion(World world, BlockPos pos, Entity entity, float scale) //TODO switch back to x y z, as this causes all blasts to be centered on the block
    {
        if (!this.isDisabled)
        {
            this.doCreateExplosion(world, pos, entity, scale);
        }
    }

    public abstract void doCreateExplosion(World world, BlockPos pos, Entity entity, float scale);
}
