package icbm.classic.content.parachute;

import icbm.classic.ICBMClassic;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.projectile.EntityProjectile;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Entity that acts as a slow falling seat for other entities to use
 */
public class EntityParachute extends EntityProjectile<EntityParachute> implements IEntityAdditionalSpawnData
{

    public static final float GRAVITY = 0.01f; // TODO config
    public static final float AIR_RESISTANCE = 0.95f; // TODO config

    public int ticksToLive = 100;
    /** Stack to render */
    @Setter @Getter @Accessors(chain = true)
    private ItemStack renderItemStack = new ItemStack(ItemReg.itemParachute);

    /** Stack to drop on impact with ground */
    @Setter @Getter @Accessors(chain = true)
    private ItemStack dropItemStack = ItemStack.EMPTY;

    public EntityParachute(World world)
    {
        super(world);
        this.setSize(0.5f, 0.5f);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public void writeSpawnData(ByteBuf data)
    {
        data.writeInt(this.ticksToLive);
        ByteBufUtils.writeItemStack(data, renderItemStack);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.ticksToLive = data.readInt();
        renderItemStack = ByteBufUtils.readItemStack(data);
    }

    @Override
    public void onUpdate()
    {
        super.onUpdate();

        // Timer to auto kill the chute
        if (!world.isRemote && (ticksExisted > ticksToLive || getPassengers().isEmpty()))
        {
            removePassengers();
            setDead();
        }
    }

    @Override
    public double getMountedYOffset()
    {
        return -0.25;
    }

    @Override
    public void updatePassenger(Entity passenger)
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


    /**
     * Gets the itemStack meant to represent the render
     *
     * @return stack to render
     */
    public ItemStack renderItemStack() {
        return renderItemStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean isInRangeToRenderDist(double distance)
    {
        double d0 = this.getEntityBoundingBox().getAverageEdgeLength() * 10.0D;

        if (Double.isNaN(d0))
        {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
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
        this.setDead();
        ICBMClassic.logger().info(this + "Impact death");
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

    private static final NbtSaveHandler<EntityParachute> SAVE_LOGIC = new NbtSaveHandler<EntityParachute>()
        .mainRoot()
        .nodeInteger("ticksToLive", (e) -> e.ticksToLive, (e, i) -> e.ticksToLive = i)
        .nodeItemStack("renderItem", (e) -> e.renderItemStack, (e, i) -> e.renderItemStack = i)
        .nodeItemStack("dropItem", (e) -> e.dropItemStack, (e, i) -> e.dropItemStack = i)
        .base();
}