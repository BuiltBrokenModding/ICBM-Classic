package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.mods.ic2.IC2Proxy;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Logic side of the EMP tower block */
public class TileEMPTower extends TilePoweredMachine implements IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150; //TODO move to config with a min & max

    public static final int CHANGE_RADIUS_PACKET_ID = 1; //TODO migrate to its own handler
    public static final int CHANGE_MODE_PACKET_ID = 2; //TODO migrate to its own handler

    /** Tick synced rotation */
    public float rotation = 0;

    /** Client side use in render */
    public float prevRotation = 0;

    public EMPMode empMode = EMPMode.ALL; //TODO remove modes

    /** Delay before EMP can be fired again */
    protected int firingCoolDown = 0; //TODO convert into a timer object

    /** Radius of the EMP tower */
    public int empRadius = 60; //TODO convert into a min-max limiter object

    private boolean _destroyingStructure = false; //TODO Convert into a state handler

    private ExternalInventory inventory;

    private List<TileEmpTowerFake> subBlocks = new ArrayList();

    @Override
    public ExternalInventory getInventory()  //TODO remove
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2);
        }
        return inventory;
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        subBlocks.forEach(tile -> tile.setHost(null));
        subBlocks.clear();
    }

    @Override
    public void update()
    {
        super.update();
        if (ticks % 3 == 0) {
            //Find tower blocks TODO find a better solution
            subBlocks.clear();
            BlockPos above = getPos().up();
            while(world.getBlockState(above).getBlock() == getBlockType()) {
                final TileEntity tile = world.getTileEntity(above);
                if(tile instanceof TileEmpTowerFake) {
                    ((TileEmpTowerFake) tile).setHost(this);
                    subBlocks.add((TileEmpTowerFake) tile);
                }
                above = above.up();
            }
        }
        if (isServer())
        {
            if (!isReady()) //TODO convert to timer object
            {
                firingCoolDown--;
            }
            else
            {
                if (ticks % 20 == 0 && getEnergy() > 0) //TODO convert to a mix of a timer and/or event handler
                {
                    ICBMSounds.MACHINE_HUM.play(world, xi(), yi(), zi(), 0.5F, 0.85F * getChargePercentage(), true);
                    sendDescPacket();
                }

                if (world.getStrongPower(getPos()) > 0) //TODO convert to a state handler
                {
                    fire();
                }
            }
        }
        else
        {
            prevRotation = rotation;

            float rotationDelta = (float) (Math.pow(getChargePercentage(), 2) *  10); //TODO convert to a animation object
            rotation += rotationDelta;

            while(this.rotation > 180.0F) {
                this.rotation -= 360F;
            }

            while (this.rotation - this.prevRotation< -180.0F)
            {
                this.prevRotation -= 360.0F;
            }

            while (this.rotation - this.prevRotation >= 180.0F)
            {
                this.prevRotation += 360.0F;
            }
        }
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float) getEnergyConsumption());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type) //TODO migrate to a packet handler
    {
        if (!super.read(data, id, player, type))
        {
            switch (id)
            {
                case CHANGE_RADIUS_PACKET_ID:
                {
                    empRadius = data.readInt();
                    return true;
                }
                case CHANGE_MODE_PACKET_ID:
                {
                    empMode = EMPMode.values()[data.readByte()];
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(empRadius);
        buf.writeByte((byte) empMode.ordinal());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        empRadius = buf.readInt();
        empMode = EMPMode.values()[buf.readByte()];
    }

    @Override
    public int getEnergyBufferSize()
    {
        return Math.max(3000000 * (this.empRadius / MAX_RADIUS), 1000000); //TODO add configs for min-max energy state
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound readFromNBT)
    {
        super.readFromNBT(readFromNBT);
        this.empRadius = readFromNBT.getInteger(NBTConstants.EMP_RADIUS);
        this.empMode = EMPMode.values()[readFromNBT.getByte(NBTConstants.EMP_MODE)];
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound saveToNBT)
    {
        saveToNBT.setInteger(NBTConstants.EMP_RADIUS, this.empRadius);
        saveToNBT.setByte(NBTConstants.EMP_MODE, (byte) this.empMode.ordinal());
        return super.writeToNBT(saveToNBT);
    }

    protected IBlast buildBlast()
    {
        BlastEMP blast = ((BlastEMP)ICBMExplosives.EMP.create()
                .setBlastWorld(world)
                .setBlastPosition(this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5)
                .setBlastSize(empRadius))
                .clearSetEffectBlocksAndEntities();

        switch (this.empMode)
        {
            case ALL:
                return blast.setEffectBlocks().setEffectEntities().buildBlast();
            case MISSILES_ONLY:
                return blast.setEffectEntities().buildBlast();
            case ELECTRICITY_ONLY:
                return blast.setEffectBlocks().buildBlast();

            default:
                ICBMClassic.logger().error("Unknown empMode passed in TileEMPTower! Returning default blast.");
                return blast.buildBlast();
        }
    }

    //@Callback(limit = 1) TODO add CC support
    public boolean fire()
    {
        if (this.checkExtract() && this.isReady())
        {
            //Finish and trigger
            if (buildBlast().runBlast().state == BlastState.TRIGGERED)
            {
                //Consume energy
                this.extractEnergy();

                //Reset timer
                this.firingCoolDown = getMaxCooldown();

                return true;
            }
            else
            {
                ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.provider.getDimension() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
                //TODO display some info to player to explain why blast failed and more detailed debug
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback TODO add CC support
    public boolean isReady()
    {
        return getCooldown() <= 0;
    }

    //@Callback TODO add CC support
    public int getCooldown()
    {
        return firingCoolDown;
    }

    //@Callback TODO add CC support
    public int getMaxCooldown()
    {
        return 120; //TODO add to config
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerEMPTower(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiEMPTower(player, this);
    }

}
