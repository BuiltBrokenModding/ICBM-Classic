package icbm.classic.content.entity;

import icbm.classic.lib.saving.NbtSaveHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import icbm.classic.content.reg.EntityReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;

/**
 * Entity that spawns smoke from it's position
 */
public class EntitySmoke extends Entity implements IEntityWithComplexSpawn
{
    //Render color
    public float red = 1;
    public float green = 0;
    public float blue = 0;
    public int ticksToLive = 100;

    public EntitySmoke(EntityType<? extends EntitySmoke> type, Level world)
    {
        super(type, world);
        this.refreshDimensions();
        this.blocksBuilding = true;
        this.noCulling = true;
    }

    public EntitySmoke(Level world)
    {
        this(EntityReg.SMOKE.get(), world);
    }

    /** 1.12 set width/height to 0.1 after setSize(1, 1), the registered type is 1x1 */
    @Override
    public EntityDimensions getDimensions(Pose pose)
    {
        return EntityDimensions.fixed(0.1f, 0.1f);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {

    }

    public EntitySmoke setColor(float red, float green, float blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf data)
    {
        data.writeFloat(this.red);
        data.writeFloat(this.green);
        data.writeFloat(this.blue);
        data.writeInt(this.ticksToLive);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf data)
    {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
        this.ticksToLive = data.readInt();
    }

    @Override
    public void tick()
    {
        //Safety in case the beam is never killed
        if (tickCount > ticksToLive)
        {
            discard();
        }

        // TODO scale with age with mid life having most, early low and later trailing off
        final Level world = level();
        final int spawnCount = 3 + world.random.nextInt(4);
        for(int i = 0; i < spawnCount; i++) {
            world.addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.1f, getZ(),
                0.05f * world.random.nextFloat() - 0.05f * world.random.nextFloat(),
                0.1f,
                0.05f * world.random.nextFloat() - 0.05f * world.random.nextFloat());
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
    protected void readAdditionalSaveData(CompoundTag tag)
    {
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag)
    {
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<EntitySmoke> SAVE_LOGIC = new NbtSaveHandler<EntitySmoke>()
        .mainRoot()
        .nodeInteger("ticksToLive", (e) -> e.ticksToLive, (e, i) -> e.ticksToLive = i)
        .addRoot("color")
        /* */.nodeFloat("red", (e) -> e.red, (e, i) -> e.red = i)
        // PORT: 1.12 keyed green and blue to the red field, fixed so the color survives reloads
        /* */.nodeFloat("green", (e) -> e.green, (e, i) -> e.green = i)
        /* */.nodeFloat("blue", (e) -> e.blue, (e, i) -> e.blue = i)
        .base();
}
// PORTED
