package icbm.classic.content.cargo.parachute;

import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;

/**
 * Entity that acts as a slow falling seat for other entities to use
 */
public class EntityParachute extends EntityProjectile<EntityParachute> implements IEntityWithComplexSpawn
{

    public static final float GRAVITY = 0.01f; // TODO config
    public static final float AIR_RESISTANCE = 0.95f; // TODO config
    public static final float ENTITY_SIZE = 0.5f;

    /** Stack to render */
    @Nonnull @Setter @Getter @Accessors(chain = true)
    private ItemStack renderItemStack = new ItemStack(ItemReg.itemParachute.get());

    /** Stack to drop on impact with ground */
    @Nonnull @Setter @Getter @Accessors(chain = true)
    private ItemStack dropItemStack = new ItemStack(ItemReg.itemParachute.get()); // TODO consider used parachute item

    @Getter
    private float renderScale = 1;
    private float prevRenderScale = 1;


    public EntityParachute(EntityType<? extends EntityParachute> type, Level world)
    {
        super(type, world);
        this.blocksBuilding = true;
        this.noCulling = true;
    }

    public EntityParachute(Level world)
    {
        this(EntityReg.PARACHUTE.get(), world);
    }

    protected void setRenderScale(float scale) {
        this.renderScale = scale;
        if(Math.abs(prevRenderScale - scale) > 0.01) {
            this.prevRenderScale = this.renderScale;
            // PORT: 1.12 setSize(ENTITY_SIZE * scale), type size is ENTITY_SIZE so scale the dimensions
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(prevRenderScale <= 0 ? 1 : prevRenderScale); // field is 0 while the super constructor runs
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return 0; //TODO consider passenger weight? As heavy object on small parachute would be falling fast and do damage
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeInt(this.ticksInAir);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(data, renderItemStack);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.ticksInAir = data.readInt();
        renderItemStack = ItemStack.OPTIONAL_STREAM_CODEC.decode(data);
        // TODO pull iBakedModel and cache
    }

    @Override
    protected boolean shouldExpire() {
        // PORT: passenger check is server only, the client copy is paired before the passenger packet arrives
        // and would otherwise discard itself, leaving the cargo to fall and desync on the client
        return ticksInAir >= inAirKillTime || (!level().isClientSide() && getPassengers().isEmpty());
    }

    // PORT: 1.12 getMountedYOffset() = -0.25 was only used by the default passenger positioning, which positionRider replaces

    @Override
    protected boolean canAddPassenger(Entity passenger)
    {
        return this.getPassengers().isEmpty();
    }

    @Override
    protected void removePassenger(Entity passenger) {
        super.removePassenger(passenger);
        this.setRenderScale(1);
    }

    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if(passenger instanceof ItemEntity)
        {
            this.setRenderScale(1);
        }
        else if(passenger instanceof EntityFlyingBlock)
        {
            this.setRenderScale(2);
        }
        else
        {
            this.setRenderScale(2);
        }
    }

    @Override
    protected void positionRider(Entity passenger, Entity.MoveFunction moveFunction)
    {
        if (this.hasPassenger(passenger))
        {
            if(passenger instanceof ItemEntity)
            {
                if(((ItemEntity) passenger).getItem().getItem() instanceof BlockItem)
                {
                    moveFunction.accept(passenger, this.getX(), this.getY() - 0.6, this.getZ());
                }
                else
                {
                    moveFunction.accept(passenger, this.getX(), this.getY() - 0.7, this.getZ());
                }
            }
            else if(passenger instanceof EntityFlyingBlock)
            {
                moveFunction.accept(passenger, this.getX(), this.getY() - 1.5, this.getZ());
            }
            else
            {
                moveFunction.accept(passenger, this.getX(), this.getY() + passenger.getBbHeight() - 0.25, this.getZ());
            }
        }
    }

    @Override
    protected double getDefaultGravity() {
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
    protected void onImpact(@Nonnull HitResult impactLocation) {
        releaseParachute();
    }

    @Override
    protected void destroy() {
        this.releaseParachute();
    }

    protected void releaseParachute() {
        this.ejectPassengers();
        this.discard(); //TODO have parachute drift away and then despawn with particles

        if(isServer() && this.dropItemStack != null && !this.dropItemStack.isEmpty()) {
            final ItemEntity entityitem = new ItemEntity(this.level(), this.getX(), this.getY(), this.getZ(), this.dropItemStack.copy());
            entityitem.setDefaultPickUpDelay();
            level().addFreshEntity(entityitem);
        }

        //TODO add event, idea would be to use it non-projectile items and entities to handle additional logic
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

    private static final NbtSaveHandler<EntityParachute> SAVE_LOGIC = new NbtSaveHandler<EntityParachute>()
        .mainRoot()
        .nodeItemStack("renderItem", (e) -> e.renderItemStack, (e, i) -> e.renderItemStack = i)
        .nodeItemStack("dropItem", (e) -> e.dropItemStack, (e, i) -> e.dropItemStack = i)
        .base();
}
// PORTED
