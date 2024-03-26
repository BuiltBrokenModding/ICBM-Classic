package icbm.classic.world.entity;

import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.neoforged.fml.common.registry.IEntityAdditionalSpawnData;

/**
 * Entity that spawns smoke from it's position
 */
public class SmokeEntity extends Entity implements IEntityAdditionalSpawnData {
    //Render color
    public float red = 1;
    public float green = 0;
    public float blue = 0;
    public int ticksToLive = 100;

    public SmokeEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.setSize(1F, 1F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.height = 0.1f;
        this.width = 0.1f;
    }

    @Override
    protected void entityInit() {

    }

    public SmokeEntity setColor(float red, float green, float blue) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        return this;
    }

    @Override
    public void writeSpawnData(ByteBuf data) {
        data.writeFloat(this.red);
        data.writeFloat(this.green);
        data.writeFloat(this.blue);
        data.writeInt(this.ticksToLive);
    }

    @Override
    public void readSpawnData(ByteBuf data) {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
        this.ticksToLive = data.readInt();
    }

    @Override
    public void onUpdate() {
        //Safety in case the beam is never killed
        if (ticksExisted > ticksToLive) {
            setDead();
        }

        // TODO scale with age with mid life having most, early low and later trailing off
        final int spawnCount = 3 + world.rand.nextInt(4);
        for (int i = 0; i < spawnCount; i++) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.1f, posZ,
                0.05f * world.rand.nextFloat() - 0.05f * world.rand.nextFloat(),
                0.1f,
                0.05f * world.rand.nextFloat() - 0.05f * world.rand.nextFloat());
        }
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected boolean canTriggerWalking() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected void readEntityFromNBT(CompoundTag tag) {
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    protected void writeEntityToNBT(CompoundTag tag) {
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<SmokeEntity> SAVE_LOGIC = new NbtSaveHandler<SmokeEntity>()
        .mainRoot()
        .nodeInteger("ticksToLive", (e) -> e.ticksToLive, (e, i) -> e.ticksToLive = i)
        .addRoot("color")
        /* */.nodeFloat("red", (e) -> e.red, (e, i) -> e.red = i)
        /* */.nodeFloat("green", (e) -> e.red, (e, i) -> e.red = i)
        /* */.nodeFloat("red", (e) -> e.red, (e, i) -> e.red = i)
        .base();
}