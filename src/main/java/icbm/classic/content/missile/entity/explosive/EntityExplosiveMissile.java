package icbm.classic.content.missile.entity.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.missile.entity.CapabilityEmpMissile;
import icbm.classic.content.missile.entity.CapabilityMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.logic.TargetRangeDet;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.ex.CapabilityExplosiveEntity;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;

/**
 * Entity version of the missile
 *
 * @Author - Calclavia, Darkguardsman
 */
public class EntityExplosiveMissile extends EntityMissile<EntityExplosiveMissile> implements IEntityAdditionalSpawnData
{
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);
    public boolean isExploding = false;

    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();


    public final CapabilityMissile missileCapability = new CapabilityMissile(this);
    public final IEMPReceiver empCapability = new CapabilityEmpMissile(missileCapability);


    public EntityExplosiveMissile(World w)
    {
        super(w);
        this.setSize(.5F, .5F);
        this.inAirKillTime = 144000 /* 2 hours */;
        this.isImmuneToFire = true;
        this.ignoreFrustumCheck = true;
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
        else if(capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY) {
            return (T) explosive;
        }
        //TODO add explosive capability
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP
            || capability == ICBMClassicAPI.MISSILE_CAPABILITY
            || capability == ICBMClassicAPI.EXPLOSIVE_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = explosive.getExplosiveData();
        if (data != null)
        {
            return I18n.translateToLocal("missile." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("missile.icbmclassic:generic.name");
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = SAVE_LOGIC.save(this, new NBTTagCompound());
        ICBMClassic.logger().info("Missile: write spawn data " + saveData.toString());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        final NBTTagCompound saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
        ICBMClassic.logger().info("Missile: read spawn data " + saveData.toString());
    }

    @Override
    public void onUpdate()
    {
        targetRangeDet.update();
        super.onUpdate();
    }

    public EntityExplosiveMissile ignore(Entity entity)
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
    protected void rotateTowardsMotion() {
        //Clearing default logic to flight controllers can handle motion
    }

    @Override
    protected void decreaseMotion()
    {
        if (missileCapability.getFlightLogic() == null || missileCapability.getFlightLogic().shouldDecreaseMotion(this))
        {
            super.decreaseMotion();
        }
    }

    @Override
    protected void onImpactTile(RayTraceResult hit)
    {
        doExplosion();
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
            doExplosion();
        }
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand)
    {
        //Allow missile to override interaction
        if (ICBMClassicAPI.EX_MISSILE_REGISTRY.onInteraction(this, player, hand))
        {
            return true;
        }

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

    protected void logImpact()
    {
        // TODO make optional via config
        // TODO log to ICBM file separated from main config
        // TODO offer hook for database logging
        final String formatString = "Missile[%s] E(%s) impacted at (%sx,%sy,%sz,%sd)";
        final String formattedMessage = String.format(formatString,
            Optional.ofNullable(this.explosive.getExplosiveData()).map(IExplosiveData::getRegistryName).map(ResourceLocation::toString).orElseGet(() -> "null"),
            this.getEntityId(),
            xi(),
            yi(),
            zi(),
            world().provider.getDimension()
        );
        ICBMClassic.logger().info(formattedMessage);
    }

    public BlastResponse doExplosion()
    {
        //Eject from riding
        dismountRidingEntity();
        //Eject passengers
        removePassengers();

        try
        {
            // Make sure the missile is not already exploding
            if (!this.isExploding)
            {
                //Log that the missile impacted
                logImpact();

                //Make sure to note we are currently exploding
                this.isExploding = true;

                //Kill the misisle entity
                setDead();

                if (!this.world.isRemote)
                {
                    return ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, explosive);
                }
                return BlastState.TRIGGERED_CLIENT.genericResponse;
            }
            return BlastState.ALREADY_TRIGGERED.genericResponse;
        } catch (Exception e)
        {
            return new BlastResponse(BlastState.ERROR, e.getMessage(), e);
        }
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

    private static final NbtSaveHandler<EntityExplosiveMissile> SAVE_LOGIC = new NbtSaveHandler<EntityExplosiveMissile>()
        .mainRoot()
        /* */.node(new NbtSaveNode<>("explosive",
            (missile) -> missile.explosive.serializeNBT(),
            (missile, data) -> missile.explosive.deserializeNBT(data))
        )
        /* */.node(new NbtSaveNode<EntityExplosiveMissile, NBTTagCompound>("missile",
            (missile) -> missile.missileCapability.serializeNBT(),
            (missile, data) -> missile.missileCapability.deserializeNBT(data)
        ))
        .base();


}
