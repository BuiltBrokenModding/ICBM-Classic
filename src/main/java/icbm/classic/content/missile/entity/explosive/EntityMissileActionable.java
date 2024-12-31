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
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nonnull;

/**
 * Missile with generic action handling
 */
public class EntityMissileActionable extends EntityMissile<EntityMissileActionable>
{
    /** Explosive data and settings */
    @Getter
    private final PotentialAction mainAction = new PotentialAction()
        .withProvider(new ActionFieldProvider()
            .field(ActionFields.IMPACTED, () -> hasImpacted)
            .field(ActionFields.HOST_ENTITY, () -> this)
            .field(ActionFields.HOST_POSITION, this::getPositionVector) //TODO may need to cache once conditionals get more common
            .field(ActionFields.TARGET_POSITION, () -> this.getMissileCapability().getTargetData() != null ? this.getMissileCapability().getTargetData().getPosition(): null)
        );


    @Getter @Setter @Accessors(chain = true)
    private ItemStack originalStack = ItemStack.EMPTY;


    public EntityMissileActionable(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
    }

    public EntityMissileActionable setActionData(IActionData actionData) {
        mainAction.setActionData(actionData);
        return this;
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
    public void writeSpawnData(PacketBuffer additionalMissileData)
    {
        final CompoundNBT saveData = SAVE_LOGIC.save(this, new CompoundNBT());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
        super.writeSpawnData(additionalMissileData);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalMissileData)
    {
        final CompoundNBT saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
        super.readSpawnData(additionalMissileData);
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
        if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        {
            return true;
        }
        return super.processInitialInteract(player, hand);
    }

    @Override
    protected void actionOnImpact(RayTraceResult impactLocation) {
        // TODO add impact cause
        final IActionStatus status = mainAction.doAction(getEntityWorld(), impactLocation.hitVec.x, impactLocation.hitVec.y, impactLocation.hitVec.z, new EntityCause(this));
        if(!status.isType(ActionStatusTypes.BLOCKING)) {
            super.actionOnImpact(impactLocation);
        }
    }

    @Override
    public ItemStack toStack() {
        return originalStack;
    }

    @Override
    public void readEntityFromNBT(CompoundNBT nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeEntityToNBT(CompoundNBT nbt)
    {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissileActionable> SAVE_LOGIC = new NbtSaveHandler<EntityMissileActionable>()
        .mainRoot()
        /* */.nodeINBTSerializable("potential_action", EntityMissileActionable::getMainAction)
        /* */.nodeItemStack("original_stack", EntityMissileActionable::getOriginalStack, EntityMissileActionable::setOriginalStack)
        .base();
}
