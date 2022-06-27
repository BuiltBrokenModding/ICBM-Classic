package icbm.classic.content.entity;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

/**
 * Entity that spawns smoke from it's position
 */
public class EntitySmoke extends Entity implements IEntityAdditionalSpawnData
{
    //Render color
    public float red = 1;
    public float green = 0;
    public float blue = 0;
    public int ticksToLive = 100;

    public EntitySmoke(World world)
    {
        super(world);
        this.setSize(1F, 1F);
        this.preventEntitySpawning = true;
        this.ignoreFrustumCheck = true;
        this.height = 0.1f;
        this.width = 0.1f;
    }

    @Override
    protected void entityInit()
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
    public void writeSpawnData(ByteBuf data)
    {
        data.writeFloat(this.red);
        data.writeFloat(this.green);
        data.writeFloat(this.blue);
        data.writeInt(this.ticksToLive);
    }

    @Override
    public void readSpawnData(ByteBuf data)
    {
        this.red = data.readFloat();
        this.green = data.readFloat();
        this.blue = data.readFloat();
        this.ticksToLive = data.readInt();
    }

    @Override
    public void onUpdate()
    {
        //Safety in case the beam is never killed
        if (ticksExisted > ticksToLive)
        {
            setDead();
        }

        world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, posX, posY + 0.1f, posZ, 0, 0f, 0); //TODO add color
    }

    @Override
    public boolean canBePushed()
    {
        return false;
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return false;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return false;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag)
    {
        SAVE_LOGIC.load(this, tag);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag)
    {
        SAVE_LOGIC.save(this, tag);
    }

    private static final NbtSaveHandler<EntitySmoke> SAVE_LOGIC = new NbtSaveHandler<EntitySmoke>()
        .mainRoot()
        .nodeInteger("ticksToLive", (e) -> e.ticksToLive, (e, i) -> e.ticksToLive = i)
        .addRoot("color")
        /* */.nodeFloat("red", (e) -> e.red, (e, i) -> e.red = i)
        /* */.nodeFloat("green", (e) -> e.red, (e, i) -> e.red = i)
        /* */.nodeFloat("red", (e) -> e.red, (e, i) -> e.red = i)
        .base();
}