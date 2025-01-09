package icbm.classic.content.entity;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import icbm.classic.lib.capability.emp.CapabilityEmpKill;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.MoverType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class EntityExplosive extends Entity implements IEntityAdditionalSpawnData
{
    private static int FUSE = 100; //TODO config

    // How long the fuse is (in ticks)
    public int fuse = FUSE;

    //Capabilities
    public final LazyOptional<IEMPReceiver> capabilityEMP = LazyOptional.of(() -> new CapabilityEmpKill(this));
    //TODO public final CapabilityExplosiveEntity capabilityExplosive = new CapabilityExplosiveEntity(this);

    private final PotentialAction explodeAction = new PotentialAction();
    private final LazyOptional<ItemStack> itemstack;
    private final LazyOptional<BlockState> defaultState;

    private Direction renderFace = Direction.UP; //TODO entity data, combine with mimic block for render

    public EntityExplosive(EntityType<EntityExplosive> type, World par1World, IActionData actionData, NonNullSupplier<ItemStack> itemstack, NonNullSupplier<BlockState> blockstate)
    {
        super(type, par1World);
        this.preventEntitySpawning = true;
        //this.yOffset = this.height / 2.0F;
        this.explodeAction.setActionData(actionData);
        this.itemstack = LazyOptional.of(itemstack);
        this.defaultState = LazyOptional.of(blockstate);
    }

    public BlockState getBlockRender() {
        return defaultState.orElseGet(Blocks.TNT::getDefaultState);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick()
    {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.setMotion(this.getMotion().mul(0.95f, 1f, 0.95f).add(0.0D, -0.04D, 0.0D)); //TODO magic numbers

        this.move(MoverType.SELF, this.getMotion());

        if (this.onGround)
        {
            this.setMotion(this.getMotion().mul(0.699999988079071D, -0.5D, 0.699999988079071D)); //TODO magic numbers
        }

        //Tick fuse
        if (this.fuse-- < 1)
        {
            this.explode();
        }

        super.tick();
    }

    public void explode()
    {
        this.explodeAction.doAction(world, this.posX + 0.5D, this.posY + 0.5D, this.posZ + 0.5D, new EntityCause(this)); //TODO see if 0.5D is center of entity
        this.remove();
    }

    @Override
    protected void registerData()
    {
        //TODO  this.getDataManager().register(FIELD, false); FACING
    }

    @Override
    protected boolean canTriggerWalking()
    {
        return true;
    }

    @Override
    public boolean canBeCollidedWith()
    {
        return true;
    }

    @Override
    public boolean canBePushed()
    {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this); //TODO figure out what this is
    }

    @Override
    protected void readAdditional(CompoundNBT nbt)
    {
        this.fuse = nbt.getInt(NBTConstants.FUSE);
        this.renderFace = Direction.byIndex(nbt.getByte("face"));
    }

    @Override
    protected void writeAdditional(CompoundNBT nbt)
    {
        nbt.putInt(NBTConstants.FUSE, (byte) this.fuse);
    }

    @Override
    public void writeSpawnData(PacketBuffer data)
    {
        data.writeInt(this.fuse);
        data.writeByte(renderFace.ordinal());
    }

    @Override
    public void readSpawnData(PacketBuffer data)
    {
        this.fuse = data.readInt();
        this.renderFace = Direction.byIndex(data.readByte());
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing)
    {
        if (capability == CapabilityEMP.EMP)
        {
            return capabilityEMP.cast();
        }
        return super.getCapability(capability, facing);
    }
}
