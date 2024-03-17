package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.data.LazyBuilder;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Entity that acts as a slow falling seat for other entities to use. Mimics a balloon in concept but
 * not far different from {@link icbm.classic.content.cargo.parachute.EntityParachute}. With key
 * differences being flow of movement.
 */
public class EntityBalloon extends EntityProjectile<EntityBalloon> implements IEntityAdditionalSpawnData
{
    private final static Supplier<ItemStack> DEFAULT_RENDER = new LazyBuilder<>(() -> new ItemStack(ItemReg.itemBalloon, 1, 1));

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

    public EntityBalloon(World world)
    {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return 0; //TODO consider passenger weight? As heavy object on small parachute would be falling fast and do damage
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.liftTicks);
        ByteBufUtils.writeItemStack(data, renderItemStack);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.liftTicks = data.readInt();
        renderItemStack = ByteBufUtils.readItemStack(data);
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
            if(passenger instanceof EntityItem)
            {
                passenger.setPosition(this.posX, this.posY -0.35, this.posZ);
            }
            else
            {
                passenger.setPosition(this.posX, this.posY + passenger.height -0.25, this.posZ);
            }
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        // Balloon pop chance
        if(isServer() && liftTicks <= 0 && world.rand.nextFloat() <= BREAK_CHANCE) {
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
        return hit.entityHit != null;
    }

    @Override
    protected boolean shouldCollideWith(Entity entity) {
        return super.shouldCollideWith(entity) && entity != shootingEntity;
    }

    @Override
    protected void onImpact(Vec3d impactLocation) {
        releaseCargoAndDespawn();
    }

    @Override
    protected void onExpired() {
        this.releaseCargoAndDespawn();
    }

    protected void releaseCargoAndDespawn() {
        ICBMClassic.logger().info("despan ballon " + this);
        if(!this.getPassengers().isEmpty()) {
            this.removePassengers();
        }
        this.setDead();
        // TODO release balloon fragment particles as a "pop" affect
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag)
    {
        super.readEntityFromNBT(tag);
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag)
    {
        super.writeEntityToNBT(tag);
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<EntityBalloon> SAVE_LOGIC = new NbtSaveHandler<EntityBalloon>()
        .mainRoot()
        .nodeInteger("lift_ticks", EntityBalloon::getLiftTicks, EntityBalloon::setLiftTicks)
        .nodeItemStack("render_stack", EntityBalloon::getRenderItemStack, EntityBalloon::setRenderItemStack)
        .base();
}