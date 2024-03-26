package icbm.classic.world.missile.entity.explosive;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.world.missile.entity.EntityMissile;
import icbm.classic.world.missile.logic.TargetRangeDet;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Entity version of the missile
 *
 * @Author - Calclavia, Darkguardsman
 */
public class ExplosiveMissileEntity extends EntityMissile<ExplosiveMissileEntity> {
    /**
     * Targeting range handler and settings for triggering explosive before impact
     */
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    /**
     * Explosive data and settings
     */
    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);
    public boolean isExploding = false; //TODO see if this should be in cap

    public ExplosiveMissileEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;

        // Init health as explosive field is not set at time of health registration
        setHealth(getMaxHealth());
    }

    @Override
    public float getMaxHealth() {
        if (explosive.getExplosiveData() != null) {
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
    protected void onDestroyedBy(DamageSource source, float damage) {
        super.onDestroyedBy(source, damage);
        // TODO add config
        // TODO add random chance modifier
        if (source.isExplosion() || source.isFireDamage()) {
            doExplosion(this.getPositionVector());
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return ICBMClassicAPI.EXPLOSIVE_CAPABILITY.cast(explosive);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    public String getName() {
        final ExplosiveType data = explosive.getExplosiveData();
        if (data != null) {
            return I18n.translateToLocal("missile." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("missile.icbmclassic:generic.name");
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData) {
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
        super.writeSpawnData(additionalMissileData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData) {
        final CompoundTag saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
        super.readSpawnData(additionalMissileData);
    }

    @Override
    public void onUpdate() {
        targetRangeDet.update();
        super.onUpdate();
    }

    @Override
    public boolean processInitialInteract(@Nonnull Player player, @Nonnull InteractionHand hand) {
        //Allow missile to override interaction
        if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand)) {
            return true;
        }
        return super.processInitialInteract(player, hand);
    }

    @Override
    protected void onImpact(Vec3 impactLocation) {
        super.onImpact(impactLocation);
        doExplosion(impactLocation);
    }

    public BlastResponse doExplosion(Vec3 impactLocation) //TODO move to capability
    {
        try {
            // Make sure the missile is not already exploding
            if (!this.isExploding) {
                //Make sure to note we are currently exploding
                this.isExploding = true;

                if (!this.world.isClientSide()) {
                    return ExplosiveHandler.createExplosion(this, this.world, impactLocation.x, impactLocation.y, impactLocation.z, explosive);
                }
                return BlastState.TRIGGERED_CLIENT.genericResponse;
            }
            return BlastState.ALREADY_TRIGGERED.genericResponse;
        } catch (Exception e) {
            return new BlastResponse(BlastState.ERROR, e.getMessage(), e);
        }
    }

    @Override
    public ItemStack toStack() {
        return explosive.toStack();
    }

    @Override
    public void readEntityFromNBT(CompoundTag nbt) {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeEntityToNBT(CompoundTag nbt) {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<ExplosiveMissileEntity> SAVE_LOGIC = new NbtSaveHandler<ExplosiveMissileEntity>()
        .mainRoot()
        /* */.node(new NbtSaveNode<ExplosiveMissileEntity, CompoundTag>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        .base();
}
