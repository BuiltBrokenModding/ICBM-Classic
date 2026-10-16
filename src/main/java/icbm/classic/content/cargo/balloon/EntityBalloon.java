package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.data.LazyBuilder;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Entity that acts as a slow falling seat for other entities to use. Mimics a balloon in concept but
 * not far different from {@link icbm.classic.content.cargo.parachute.EntityParachute}. With key
 * differences being flow of movement.
 */
public class EntityBalloon extends EntityProjectile<EntityBalloon> implements IEntityWithComplexSpawn
{
    /** PORT: 1.12 used fake meta 1 for the 3D render model, now custom model data 1 (balloon.json override -> balloon_3d) */
    private final static Supplier<ItemStack> DEFAULT_RENDER = new LazyBuilder<>(() -> {
        // PORT: 3D balloon model is applied by the client renderer (RenderAsItem#setModelOverride), plain stack here
        return new ItemStack(ItemReg.itemBalloon.get(), 1);
    });

    public static final float GRAVITY = 0.005f; // TODO config
    public static final float AIR_RESISTANCE = 0.90f; // TODO config

    public static final float FLOATING_GRAVITY = -0.02f; // TODO config
    public static final int FLOATING_DURATION = ICBMConstants.TICKS_MIN_HALF; // TODO config

    public static final float BREAK_CHANCE = 0.05f; // TODO config

    /** Number of motions ticks to accelerate positive Y */
    @Getter @Setter @Accessors(chain = true)
    private int liftTicks = FLOATING_DURATION;

    /** Stack to render */
    @Nonnull
    @Setter @Getter @Accessors(chain = true)
    private ItemStack renderItemStack = DEFAULT_RENDER.get();

    public EntityBalloon(EntityType<? extends EntityBalloon> type, Level world)
    {
        super(type, world);
        this.blocksBuilding = true;
        this.noCulling = true;
    }

    public EntityBalloon(Level world)
    {
        this(EntityReg.BALLOON.get(), world);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return 0; //TODO consider passenger weight? As heavy object on small parachute would be falling fast and do damage
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeInt(this.liftTicks);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, renderItemStack);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.liftTicks = data.readInt();
        renderItemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
    }

    @Override
    protected boolean shouldExpire() {
        // PORT: passenger check is server only, the client copy is paired before the passenger packet arrives
        // and would otherwise discard itself, leaving the cargo to fall and desync on the client
        return super.shouldExpire() || (!level().isClientSide() && getPassengers().isEmpty());
    }

    // PORT: 1.12 getMountedYOffset() = -0.25 was only used by the default passenger positioning, which positionRider replaces

    @Override
    protected void positionRider(@Nonnull Entity passenger, Entity.MoveFunction moveFunction)
    {
        if (this.hasPassenger(passenger))
        {
            if(passenger instanceof ItemEntity)
            {
                moveFunction.accept(passenger, this.getX(), this.getY() - 0.65, this.getZ());
            }
            else
            {
                moveFunction.accept(passenger, this.getX(), this.getY() + passenger.getBbHeight() - 0.55, this.getZ());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Balloon pop chance
        if(isServer() && liftTicks <= 0 && level().random.nextFloat() <= BREAK_CHANCE) {
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
    protected double getDefaultGravity() {
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
    protected boolean ignoreImpact(HitResult hit) {
        // Ignore entity impacts, as we only care about the ground
        return hit instanceof EntityHitResult;
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && entity != shootingEntity;
    }

    @Override
    protected void onImpact(@Nonnull HitResult hit) {
        releaseCargoAndDespawn();
    }

    @Override
    protected void destroy() {
        this.releaseCargoAndDespawn();
    }

    protected void releaseCargoAndDespawn() {
        if(!this.getPassengers().isEmpty()) {
            this.ejectPassengers();
        }
        this.discard();
        // TODO release balloon fragment particles as a "pop" affect
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        super.readAdditionalSaveData(tag);
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        super.addAdditionalSaveData(tag);
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<EntityBalloon> SAVE_LOGIC = new NbtSaveHandler<EntityBalloon>()
        .mainRoot()
        .nodeInteger("lift_ticks", EntityBalloon::getLiftTicks, EntityBalloon::setLiftTicks)
        .nodeItemStack("render_stack", EntityBalloon::getRenderItemStack, EntityBalloon::setRenderItemStack)
        .base();
}
// PORTED
