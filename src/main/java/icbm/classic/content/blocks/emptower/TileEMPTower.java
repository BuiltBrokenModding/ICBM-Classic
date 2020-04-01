package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

/** Logic side of the EMP tower block */
public class TileEMPTower extends TilePoweredMachine implements IMultiTileHost, IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150; //TODO move to config with a min & max

    public static final int CHANGE_RADIUS_PACKET_ID = 1; //TODO migrate to its own handler
    public static final int CHANGE_MODE_PACKET_ID = 2; //TODO migrate to its own handler

    public static List<BlockPos> tileMapCache = new ArrayList(); //TODO convert to something else

    static
    {
        tileMapCache.add(new BlockPos(0, 1, 0)); //TODO convert to multi-block handler
    }

    public float rotation = 0;
    private float rotationDelta;

    public EMPMode empMode = EMPMode.ALL; //TODO remove modes

    /** Delay before EMP can be fired again */
    protected int firingCoolDown = 0; //TODO convert into a timer object

    /** Radius of the EMP tower */
    public int empRadius = 60; //TODO convert into a min-max limiter object

    private boolean _destroyingStructure = false; //TODO Convert into a state handler

    private ExternalInventory inventory;

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
    public void update()
    {
        super.update();
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

                if (world.getRedstonePowerFromNeighbors(getPos()) > 0) //TODO convert to a state handler
                {
                    fire();
                }
            }
        }
        else
        {
            rotationDelta = (float) (Math.pow(getChargePercentage(), 2) * 0.5); //TODO convert to a animation object
            rotation += rotationDelta;
            while (rotation > 360) rotation -= 360;
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
            if (buildBlast().runBlast() == BlastState.TRIGGERED)
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

    //==========================================
    //==== Multi-Block code
    //=========================================

    //TODO convert all multi-block code to a handler object that tracks the pattern and state of the structure to remove repetitive code

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (multiBlockContains(tileMulti))
        {
            tileMulti.setHost(this);
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && multiBlockContains(tileMulti))
        {
            MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
            return true;
        }
        return false;
    }

    public boolean multiBlockContains(IMultiTile tile)
    {
        return tile instanceof TileEntity
                && getLayoutOfMultiBlock().contains(getRelativePosition((TileEntity) tile));
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        if (isServer())
        {
            openGui(player, 0);
        }
        return true;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public List<BlockPos> getLayoutOfMultiBlock()
    {
        return tileMapCache;
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
