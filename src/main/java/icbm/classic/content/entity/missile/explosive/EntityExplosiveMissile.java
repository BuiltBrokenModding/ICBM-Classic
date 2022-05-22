package icbm.classic.content.entity.missile.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.events.MissileRideEvent;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.entity.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.entity.missile.logic.flight.DirectFlightLogic;
import icbm.classic.api.missiles.IMissileFlightLogic;
import icbm.classic.content.entity.missile.logic.TargetRangeDet;
import icbm.classic.lib.CalculationHelpers;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.emp.CapabilityEMP;
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
    private IMissileFlightLogic flightLogic;
    public final TargetRangeDet targetRangeDet = new TargetRangeDet(this);

    //Explosive cap vars
    public int explosiveID = -1;
    public NBTTagCompound blastData = new NBTTagCompound();
    public boolean isExploding = false;

    // Generic shared missile data
    private final HashSet<Entity> collisionIgnoreList = new HashSet<Entity>();

    public final IEMPReceiver empCapability = new CapabilityEmpMissile(this);
    public final CapabilityMissile missileCapability = new CapabilityMissile(this);


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
        //TODO add explosive capability
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityEMP.EMP || capability == ICBMClassicAPI.MISSILE_CAPABILITY || super.hasCapability(capability, facing);
    }

    public IMissileFlightLogic getFlightLogic()
    {
        return flightLogic;
    }

    public void setFlightLogic(IMissileFlightLogic flightLogic)
    {
        this.flightLogic = flightLogic;
    }

    @Override
    public String getName()
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(this.explosiveID);
        if (data != null)
        {
            return I18n.translateToLocal("missile." + data.getRegistryName().toString() + ".name");
        }
        return I18n.translateToLocal("missile.icbmclassic:generic.name");
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        ByteBufUtils.writeTag(additionalMissileData, SAVE_LOGIC.save(this, new NBTTagCompound()));
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        SAVE_LOGIC.load(this, ByteBufUtils.readTag(additionalMissileData));
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
        if (missileCapability.doFlight)
        {
            Optional.ofNullable(getFlightLogic()).ifPresent(logic -> logic.onEntityTick(this, ticksInAir));

            //Handle effects
            ICBMClassic.proxy.spawnMissileSmoke(this, getFlightLogic(), ticksInAir);
            ICBMSounds.MISSILE_ENGINE.play(world, posX, posY, posZ, Math.min(1, ticksInAir / 40F), (1.0F + CalculationHelpers.randFloatRange(this.world.rand, 0.2F)) * 0.7F, true);

            //Trigger events
            ICBMClassicAPI.EX_MISSILE_REGISTRY.triggerFlightUpdate(missileCapability);
        }

        super.updateMotion();
    }

    @Override
    protected void decreaseMotion()
    {
        if (getFlightLogic() == null || getFlightLogic().shouldDecreaseMotion(this))
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
        if (!world.isRemote && entityHit.getRidingEntity() != this)
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
        if (!this.world.isRemote && (this.getRidingEntity() == null || this.getRidingEntity() == player) && !MinecraftForge.EVENT_BUS.post(new MissileRideEvent.Start(this, player)))
        {
            player.startRiding(this);
            return true;
        }

        return false;
    }

    @Override
    public double getMountedYOffset()
    {
        if (this.ticksInAir <= 0 && this.getFlightLogic() instanceof BallisticFlightLogic) //TODO abstract or find better way to handle seat position
        {
            return height;
        } else if (this.getFlightLogic() instanceof DeadFlightLogic)
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
        final String formatString = "Missile[%s] E_ID(%s) impacted at (%sx,%sy,%sz,%sd)";
        final String formattedMessage = String.format(formatString,
            this.explosiveID,
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
                    return ExplosiveHandler.createExplosion(this, this.world, this.posX, this.posY, this.posZ, explosiveID, 1, blastData);
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
        this.explosiveID = nbt.getInteger(NBTConstants.EXPLOSIVE_ID);
        this.blastData = nbt.getCompoundTag(NBTConstants.ADDITIONAL_MISSILE_DATA);

        SAVE_LOGIC.load(this, nbt);
    }

    /**
     * (abstract) Protected helper method to write subclass entity additionalMissileData to NBT.
     */
    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);

        nbt.setInteger(NBTConstants.EXPLOSIVE_ID, this.explosiveID);
        nbt.setTag(NBTConstants.ADDITIONAL_MISSILE_DATA, this.blastData);

        SAVE_LOGIC.save(this, nbt);
    }

    private static final NbtSaveHandler<EntityExplosiveMissile> SAVE_LOGIC = new NbtSaveHandler<EntityExplosiveMissile>()
        .addRoot("components")
        /* */.node(new NbtSaveNode<EntityExplosiveMissile, NBTTagCompound>("flight",
            (missile) -> { //TODO convert to class to make cleaner and provide better testing surface
                final NBTTagCompound save = new NBTTagCompound();
                final IMissileFlightLogic logic = missile.getFlightLogic();
                if(logic != null)
                {
                    final NBTTagCompound logicSave = logic.save();
                    if (logicSave != null && !logicSave.hasNoTags())
                    {
                        save.setTag("data", logicSave);
                    }
                    save.setString("id", logic.getRegistryName().toString());
                }
                return save;
            },
            (missile, data) -> {
                final ResourceLocation saveId = new ResourceLocation(data.getString("id"));
                final IMissileFlightLogic logic = ICBMClassicAPI.MISSILE_FLIGHT_LOGIC_REGISTRY.build(saveId);
                if (logic != null)
                {
                    if (data.hasKey("data"))
                    {
                        logic.load(data.getCompoundTag("data"));
                    }
                    missile.setFlightLogic(logic);
                }
            }
        ))
        /* */.node(new NbtSaveNode<EntityExplosiveMissile, NBTTagCompound>("missile",
            (missile) -> missile.missileCapability.serializeNBT(),
            (missile, data) -> missile.missileCapability.deserializeNBT(data)
        ))
        //TODO save explosive component when added
        .base();


}
