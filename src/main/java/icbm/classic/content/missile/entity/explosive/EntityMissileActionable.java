package icbm.classic.content.missile.entity.explosive;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * Missile with generic action handling
 */
public class EntityMissileActionable extends EntityMissile<EntityMissileActionable>
{
    /** Explosive data and settings */
    @Getter
    private final PotentialAction mainAction = new PotentialAction()
        .setSaveActionData(false) // Data is set on this::new
        .withProvider(new ActionFieldProvider()
            .field(ActionFields.IMPACTED, () -> hasImpacted)
            .field(ActionFields.HOST_ENTITY, () -> this)
            .field(ActionFields.HOST_POSITION, this::getPositionVector) //TODO may need to cache once conditionals get more common
            .field(ActionFields.TARGET_POSITION, () -> this.getMissileCapability().getTargetData() != null ? this.getMissileCapability().getTargetData().getPosition(): null)
        );

    private final LazyOptional<ItemStack> itemstack;

    public EntityMissileActionable(EntityType<EntityMissileActionable> entityType, World w, IActionData data, NonNullSupplier<Float> maxHealth, NonNullSupplier<ItemStack> itemstack)
    {
        super(entityType, w);
        this.initHealth(maxHealth.get());
        this.itemstack = LazyOptional.of(itemstack);
        this.mainAction.setActionData(data);
        this.inAirKillTime = 144_000 /* 2 hours */;
        this.ignoreFrustumCheck = true;
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
       // TODO add config
       // TODO add random chance modifier
       if(source.isExplosion() || source.isFireDamage()) {
           final IActionStatus status = this.mainAction.doAction(getEntityWorld(), posX, posY, posZ, new EntityCause(this)); // Add damage source cause
           if(!status.isType(ActionStatusTypes.BLOCKING)) {
               super.onDestroyedBy(source, damage);
           }
       }
    }

    @Override
    public void tick()
    {
        super.tick();
        this.mainAction.update(ticksExisted, !this.getEntityWorld().isRemote);

        // Ticking trigger
        final IActionStatus status = mainAction.doAction(getEntityWorld(), posX, posY, posZ, new EntityCause(this));
        if(!status.isType(ActionStatusTypes.BLOCKING)) {
            this.destroy();
        }
    }

    @Override
    public boolean processInitialInteract(@Nonnull PlayerEntity player, @Nonnull Hand hand)
    {
        //Allow missile to override interaction
        //TODO if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        return super.processInitialInteract(player, hand);
    }

    @Override
    protected void actionOnImpact(RayTraceResult impactLocation) {
        // TODO add impact cause
        final IActionStatus status = mainAction.doAction(getEntityWorld(), impactLocation.getHitVec().x, impactLocation.getHitVec().y, impactLocation.getHitVec().z, new EntityCause(this));
        if(!status.isType(ActionStatusTypes.BLOCKING)) {
            super.actionOnImpact(impactLocation);
        }
    }

    @Override
    public ItemStack toStack() {
        return itemstack.orElse(ItemStack.EMPTY);
    }

    @Override
    public void readAdditional(CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeAdditional(CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissileActionable> SAVE_LOGIC = new NbtSaveHandler<EntityMissileActionable>()
        .mainRoot()
        /* */.nodeINBTSerializable("potential_action", EntityMissileActionable::getMainAction)
        .base();
}
