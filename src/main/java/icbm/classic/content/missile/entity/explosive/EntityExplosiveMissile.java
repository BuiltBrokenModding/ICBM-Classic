package icbm.classic.content.missile.entity.explosive;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.config.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.TargetRangeDet;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;

/**
 * Entity version of the missile
 *
 * @Author - Calclavia, Darkguardsman
 */
public class EntityExplosiveMissile extends EntityMissile<EntityExplosiveMissile>
{
    /** Targeting range handler and settings for triggering explosive before impact */
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    /** Explosive data and settings */
    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);
    public boolean isExploding = false; //TODO see if this should be in cap

    public EntityExplosiveMissile(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public float getMaxHealth()
    {
        if(explosive.getExplosiveData() != null ) {
            switch (explosive.getExplosiveData().getTier()) {
                case TWO:
                    return ConfigMissile.TIER_2_HEALTH;
                case THREE:
                    return ConfigMissile.TIER_3_HEALTH;
                case FOUR:
                    return ConfigMissile.TIER_4_HEALTH;
            }
        }
        return ConfigMissile.TIER_1_HEALTH;
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
       super.onDestroyedBy(source, damage);
       // TODO add config
       // TODO add random chance modifier
       if(source.isExplosion() || source.isFireDamage()) {
           doExplosion();
       }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(explosive);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = explosive.getExplosiveData();
        if (data != null)
        {
            return I18n.translateToLocal("missile." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("missile.icbmclassic:generic.name");
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = SAVE_LOGIC.save(this, new NBTTagCompound());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
        super.writeSpawnData(additionalMissileData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
        super.readSpawnData(additionalMissileData);
    }

    @Override
    public void onUpdate()
    {
        targetRangeDet.update();
        super.onUpdate();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        //Allow missile to override interaction
        if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        {
            return true;
        }
        return super.processInitialInteract(player, hand);
    }

    @Override
    protected void onImpact() {
        super.onImpact();
        doExplosion();
    }

    public BlastResponse doExplosion() //TODO move to capability
    {
        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExploding)
            {
                //Make sure to note we are currently exploding
                this.isExploding = true;

                if (!this.world.isRemote)
                {
                    return ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, explosive);
                }
                return BlastState.TRIGGERED_CLIENT.genericResponse;
            }
            return BlastState.ALREADY_TRIGGERED.genericResponse;
        } catch (Exception e)
        {
            return new BlastResponse(BlastState.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public ItemStack toStack() {
        return explosive.toStack();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityExplosiveMissile> SAVE_LOGIC = new NbtSaveHandler<EntityExplosiveMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        .base();
}
