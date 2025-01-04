package icbm.classic.content.cargo.parachute;

import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;

/**
 * Entity that acts as a slow falling seat for other entities to use
 */
public class EntityParachute extends EntityProjectile<EntityParachute> implements IEntityAdditionalSpawnData
{

    public static final float GRAVITY = 0.01f; // TODO config
    public static final float AIR_RESISTANCE = 0.95f; // TODO config


    private final LazyOptional<ItemStack> dropItemStack;
    @Getter
    private final LazyOptional<ItemStack> renderItemStack;


    public EntityParachute(EntityType<EntityParachute> type, World world, NonNullSupplier<ItemStack> dropStack, NonNullSupplier<ItemStack> renderStack)
    {
        super(type, world);
        this.dropItemStack = LazyOptional.of(dropStack);
        this.renderItemStack = LazyOptional.of(renderStack);
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
        data.writeInt(this.ticksInAir);
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        this.ticksInAir = data.readInt();
    }

    @Override
    protected boolean shouldExpire() {
        return ticksInAir >= inAirKillTime || getPassengers().isEmpty();
    }

    @Override
    public double getMountedYOffset()
    {
        return -0.25;
    }

    @Override
    protected boolean canFitPassenger(Entity passenger)
    {
        return this.getPassengers().isEmpty();
    }

    @Override
    public void updatePassenger(Entity passenger)
    {
        if (this.isPassenger(passenger))
        {
            if(passenger instanceof ItemEntity)
            {
                if(((ItemEntity) passenger).getItem().getItem() instanceof BlockItem)
                {
                    passenger.setPosition(this.posX, this.posY - 0.6, this.posZ);
                }
                else
                {
                    passenger.setPosition(this.posX, this.posY - 0.7, this.posZ);
                }
            }
            else if(passenger instanceof EntityFlyingBlock)
            {
                passenger.setPosition(this.posX, this.posY - 1.5, this.posZ);
            }
            else
            {
                passenger.setPosition(this.posX, this.posY + passenger.getHeight() -0.25, this.posZ);
            }
        }
    }

    @Override
    protected float getGravity() {
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
        return super.shouldCollideWith(entity) && entity != shootingEntity;
    }

    @Override
    protected void onImpact(RayTraceResult impactLocation) {
        releaseParachute();
    }

    @Override
    protected void destroy() {
        this.releaseParachute();
    }

    protected void releaseParachute() {
        this.removePassengers();
        this.remove(); //TODO have parachute drift away and then despawn with particles

        if(isServer() && this.dropItemStack.isPresent()) {
            final ItemEntity entityitem = new ItemEntity(this.world, this.posX, this.posY, this.posZ, this.dropItemStack.orElseThrow(IllegalStateException::new).copy());
            entityitem.setDefaultPickupDelay();
            world.addEntity(entityitem);
        }

        //TODO add event, idea would be to use it non-projectile items and entities to handle additional logic
        //      though this is not meant to act as a replacement for other solutions. Such as spawn eggs using a deployer item.
        //      example, adding a parachute backpack to entities, or adding to player inventory
    }
}