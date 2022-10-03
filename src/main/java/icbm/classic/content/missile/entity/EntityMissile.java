package icbm.classic.content.missile.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.prefab.entity.EntityProjectile;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;

/**
 * Created by Robin Seifert on 12/12/2021.
 */
public class EntityMissile<E extends EntityMissile<E>> extends EntityProjectile<E> implements IEntityAdditionalSpawnData
{
    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();

    public final CapabilityMissile missileCapability = new CapabilityMissile(this);
    public final IEMPReceiver empCapability = new CapabilityEmpMissile(missileCapability);

    /** Toggle to note the missile has impacted something and already triggered impact logic */
    protected boolean hasImpacted = false;

    public EntityMissile(World world)
    {
        super(world);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return (T) empCapability;
        } else if (capability == ICBMClassicAPI.MISSILE_CAPABILITY)
        {
            return (T) missileCapability;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP
            || capability == ICBMClassicAPI.MISSILE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    public EntityMissile ignore(Entity entity)
    {
        collisionIgnoreList.add(entity);
        return this;
    }

    @Override
    protected void updateMotion()
    {
        if (missileCapability.canRunFlightLogic())
        {
            Optional.ofNullable(missileCapability.getFlightLogic()).ifPresent(logic -> {
                logic.onEntityTick(this, ticksInAir);

                if(logic.shouldRunEngineEffects(this)) {
                    ICBMClassic.proxy.spawnMissileSmoke(this, logic, ticksInAir);
                    ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F), (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);
                }
            });

            //Trigger events
            ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(missileCapability);
        }

        super.updateMotion();
    }

    @Override
    protected void decreaseMotion()
    {
        if (missileCapability.getFlightLogic() == null || missileCapability.getFlightLogic().shouldDecreaseMotion(this))
        {
            super.decreaseMotion();
        }
    }

    /**
     * Checks to see if an entity is touching the missile. If so, blow up!
     */
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity)
    {
        if (collisionIgnoreList.contains(entity))
        {
            return null;
        }
        return getEntityBoundingBox();
    }

    @Override
    public void setDead()
    {
        if (!world.isRemote)
        {
            RadarRegistry.remove(this);
        }

        super.setDead();
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return this.getEntityBoundingBox().expand(5, 5, 5);
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
    protected void rotateTowardsMotion() {
        //Clearing default logic to flight controllers can handle motion
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        //Handle player riding missile
        if (!this.world.isRemote && (this.getRidingEntity() == null || this.getRidingEntity() == player) && !MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Start(missileCapability, player)))
        {
            player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.ticksInAir <= 0 && missileCapability.getFlightLogic() instanceof BallisticFlightLogic) //TODO abstract or find better way to handle seat position
        {
            return height;
        } else if (missileCapability.getFlightLogic() instanceof DeadFlightLogic)
        {
            return height / 10;
        }

        return height / 2 + motionY;
    }

    @Override
    protected boolean ignoreImpact(RayTraceResult hit)
    {
        return MinecraftForge.EVENT_BUS.post(new MissileEvent.PreImpact(missileCapability, this, hit));
    }

    @Override
    protected void postImpact(RayTraceResult hit)
    {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostImpact(missileCapability, this, hit));
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity)
    {
        if (!world.isRemote && entityHit.getRidingEntity() != this && entityHit != shootingEntity)
        {
            super.onImpactEntity(entityHit, velocity);
        }
    }

    @Override
    protected void onImpact() {
        if(!hasImpacted) {
            this.hasImpacted = true;
            logImpact();
            dismountRidingEntity();
            removePassengers();
            setDead();
        }
    }

    protected void logImpact()
    {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] impacted at (%sx,%sy,%sz,%sd)";
        final String formattedMessage = String.format(formatString,
            this.getEntityId(),
            xi(),
            yi(),
            zi(),
            world().provider.getDimension()
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = SAVE_LOGIC.save(this, new NBTTagCompound());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
    }

    /**
     * (abstract) Protected helper method to read subclass entity additionalMissileData from NBT.
     */
    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    /**
     * (abstract) Protected helper method to write subclass entity additionalMissileData to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityMissile> SAVE_LOGIC = new NbtSaveHandler<EntityMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<EntityMissile, NBTTagCompound>("missile",
            (missile) -> missile.missileCapability.serializeNBT(),
            (missile, data) -> missile.missileCapability.deserializeNBT(data)
        ))
        .base();
}
