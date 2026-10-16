package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.EntityType;
import icbm.classic.content.reg.EntityReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

import javax.annotation.Nonnull;

public class EntityLightBeam extends Entity implements IEntityWithComplexSpawn
{
    //Render color
    public float red = 1;
    public float green = 0;
    public float blue = 0;

    //Render Size
    public float beamSize = 0.5f;
    public float beamGlowSize = 1f;

    //Client state
    public float clientBeamProgress = 0;

    //State
    public boolean deathCycle = false;
    public float targetBeamProgress = 1;
    public float beamGrowthRate = 0.05f;

    //Data
    private static final EntityDataAccessor<Float> BEAM_PROGRESS = SynchedEntityData.defineId(EntityLightBeam.class, EntityDataSerializers.FLOAT);

    public EntityLightBeam(EntityType<? extends EntityLightBeam> type, Level world)
    {
        super(type, world);
        this.blocksBuilding = true;
        this.noCulling = true;
    }

    public EntityLightBeam(Level world)
    {
        this(EntityReg.BEAM.get(), world);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(BEAM_PROGRESS, -1f);
    }

    public EntityLightBeam setPosition(IPos3D position)
    {
        this.setPos(position.x(), position.y(), position.z());
        return this;
    }

    public EntityLightBeam setColor(float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    public void startDeathCycle()
    {
        deathCycle = true;
    }

    public void setTargetBeamProgress(float value)
    {
        targetBeamProgress = value;
    }

    public void setActualBeamProgress(float value)
    {
        this.getEntityData().set(BEAM_PROGRESS, Math.min(1, Math.max(0, value)));
    }

    public float getBeamProgress()
    {
        return this.getEntityData().get(BEAM_PROGRESS);
    }

    @Override
    @Nonnull
    public AABB getBoundingBoxForCulling()
    {
        return new AABB(getX() - 5, -10, getZ() - 5, getX() + 5, Double.POSITIVE_INFINITY, getZ() + 5);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeFloat(this.red);
        data.writeFloat(this.green);
        data.writeFloat(this.blue);
        data.writeFloat(this.beamSize);
        data.writeFloat(this.beamGlowSize);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
        this.beamSize = data.readFloat();
        this.beamGlowSize = data.readFloat();
    }

    @Override
    public void tick()
    {
        // PORT: target progress and death cycle only exist on the server, the client follows the synced
        // BEAM_PROGRESS (smoothed by RenderLightBeam) and is removed by the server
        if (this.level().isClientSide())
        {
            return;
        }

        //Grow beam slowly
        if (getBeamProgress() < targetBeamProgress)
        {
            setActualBeamProgress(Math.min(targetBeamProgress, getBeamProgress() + beamGrowthRate));
        }
        //Decrease size slowly
        else if(getBeamProgress() > targetBeamProgress)
        {
            setActualBeamProgress(Math.max(targetBeamProgress, getBeamProgress() - beamGrowthRate));
        }

        //Kill off beam when animation finishes
        if (deathCycle && Math.abs(getBeamProgress() - targetBeamProgress) <= 0.01)
        {
            discard();
        }
        //Safety in case the beam is never killed
        else if (tickCount > 20 * 60 * 5) //ticks per second * seconds * mins = 5 mins
        {
            discard();
        }
    }

    @Override
    public boolean isPushable()
    {
        return false;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission()
    {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean isPickable()
    {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag var1)
    {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag var1)
    {

    }
}
// PORTED
