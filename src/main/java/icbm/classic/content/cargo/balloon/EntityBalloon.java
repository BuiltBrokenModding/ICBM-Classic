package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;

/**
 * Entity that acts as a slow falling seat for other entities to use. Mimics a balloon in concept but
 * not far different from {@link icbm.classic.content.cargo.parachute.EntityParachute}. With key
 * differences being flow of movement.
 */
public class EntityBalloon extends EntityProjectile<EntityBalloon> implements IEntityAdditionalSpawnData
{
    public static final float GRAVITY = 0.005f; // TODO config
    public static final float AIR_RESISTANCE = 0.90f; // TODO config

    public static final float FLOATING_GRAVITY = -0.02f; // TODO config
    public static final int FLOATING_DURATION = ICBMConstants.TICKS_MIN_HALF; // TODO config

    public static final float BREAK_CHANCE = 0.05f; // TODO config

    /** Number of motions ticks to accelerate positive Y */
    @Getter @Setter @Accessors(chain = true)
    private int liftTicks = FLOATING_DURATION;

    /** Stack to render */
    @Getter @Accessors(chain = true)
    private final LazyOptional<ItemStack> renderStack;

    public EntityBalloon(EntityType<EntityBalloon> type, World world, NonNullSupplier<ItemStack> renderStack)
    {
        super(type, world);
        this.renderStack = LazyOptional.of(renderStack);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return 0; //TODO consider passenger weight? As heavy object on small parachute would be falling fast and do damage
    }

    @Override
    public void writeSpawnData(PacketBuffer data)
    {
        data.writeInt(this.liftTicks);
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        this.liftTicks = data.readInt();
    }

    @Override
    protected boolean shouldExpire() {
        return super.shouldExpire() || getPassengers().isEmpty();
    }

    @Override
    public double getMountedYOffset()
    {
        return -0.25;
    }

    @Override
    public void updatePassenger(@Nonnull Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            if(passenger instanceof ItemEntity)
            {
                passenger.setPosition(this.posX, this.posY - 0.65, this.posZ);
            }
            else
            {
                passenger.setPosition(this.posX, this.posY + passenger.getHeight() - 0.55, this.posZ);
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Balloon pop chance
        if(!world.isRemote && liftTicks <= 0 && world.rand.nextFloat() <= BREAK_CHANCE) {
            releaseCargoAndDespawn();
        }
    }

    @Override
    protected void decreaseMotion() {
        super.decreaseMotion();
        if(this.liftTicks > 0) {
            this.liftTicks--;
        }
    }

    @Override
    protected float getGravity() {
        if(this.liftTicks > 0) {
            return FLOATING_GRAVITY; // TODO scale with duration to act as a decrease in lift power
        }
        return GRAVITY; // TODO make dynamic based on passenger(s) and type
    }

    @Override
    protected float getAirResistance() {
        return AIR_RESISTANCE; // TODO make dynamic based on passenger(s) and type
    }

    @Override
    public boolean shouldRiderSit()
    {
        return false;
    }

    @Override
    protected boolean ignoreImpact(RayTraceResult hit) {
        // Ignore entity impacts, as we only care about the ground
        return hit.getType() != RayTraceResult.Type.ENTITY;
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && entity != getOwner();
    }

    @Override
    protected void onImpact(RayTraceResult hit) {
        releaseCargoAndDespawn();
    }

    @Override
    protected void destroy() {
        this.releaseCargoAndDespawn();
    }

    protected void releaseCargoAndDespawn() {
        if(!this.getPassengers().isEmpty()) {
            this.removePassengers();
        }
        this.remove();
        // TODO release balloon fragment particles as a "pop" affect
    }

    @Override
    public void readAdditional(CompoundNBT tag)
    {
        super.readAdditional(tag);
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    public void writeAdditional(CompoundNBT tag)
    {
        super.writeAdditional(tag);
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<EntityBalloon> SAVE_LOGIC = new NbtSaveHandler<EntityBalloon>()
        .mainRoot()
        .nodeInteger("lift_ticks", EntityBalloon::getLiftTicks, EntityBalloon::setLiftTicks)
        .base();
}