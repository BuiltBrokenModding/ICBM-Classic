package icbm.classic.content.missile.entity.explosive;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionData;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.TargetRangeDet;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import lombok.Getter;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

/**
 * Entity version of the missile
 * @deprecated replacing with {@link EntityMissileActionable}
 */
public class EntityExplosiveMissile extends EntityMissile<EntityExplosiveMissile>
{
    /** Targeting range handler and settings for triggering explosive before impact */
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    //TODO public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    @Getter
    private final PotentialAction potentialAction = new PotentialAction();
    private final NonNullSupplier<Float> maxHealth;
    private final LazyOptional<ItemStack> itemstack;

    public EntityExplosiveMissile(EntityType<?> type, World w, IActionData data, NonNullSupplier<Float> maxHealth, NonNullSupplier<ItemStack> itemstack)
    {
        super(type, w);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.ignoreFrustumCheck = true;
        this.maxHealth = maxHealth;
        this.potentialAction.setActionData(data);
        this.itemstack = LazyOptional.of(itemstack::get);
    }

    @Override
    public float getMaxHealth()
    {
        return maxHealth.get();
    }

    @Override
    protected void onDestroyedBy(DamageSource source, float damage)
    {
       super.onDestroyedBy(source, damage);
       // TODO add config
       // TODO add random chance modifier
       if(source.isExplosion() || source.isFireDamage()) {
           potentialAction.doAction(world, posX, posY, posZ, new EntityCause(this)); //TODO track cause chain
       }
    }

    @Override
    public void tick()
    {
        targetRangeDet.update();
        super.tick();
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
        super.actionOnImpact(impactLocation);
        potentialAction.doAction(world, impactLocation.getHitVec().x, impactLocation.getHitVec().y, impactLocation.getHitVec().z, new EntityCause(this)); //TODO track cause chain
    }

    @Override
    public ItemStack toStack() {
        return itemstack.orElse(ItemStack.EMPTY);
    }
}
