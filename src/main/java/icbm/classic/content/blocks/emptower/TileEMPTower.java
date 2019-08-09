package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.api.NBTConstants;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.item.TilePoweredMachine;
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

public class TileEMPTower extends TilePoweredMachine implements IMultiTileHost, IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>
{

    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150;

    public static final int CHANGE_RADIUS_PACKET_ID = 1;
    public static final int CHANGE_MODE_PACKET_ID = 2;

    public static List<BlockPos> tileMapCache = new ArrayList();

    static
    {
        tileMapCache.add(new BlockPos(0, 1, 0));
    }

    public float rotation = 0;
    private float rotationDelta;

    public EMPMode empMode = EMPMode.ALL;

    private int cooldownTicks = 0;

    // The EMP explosion radius
    public int empRadius = 60;

    private boolean _destroyingStructure = false;

    private ExternalInventory inventory;

    @Override
    public ExternalInventory getInventory()
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
            if (!isReady())
            {
                cooldownTicks--;
            }
            else
            {
                if (ticks % 20 == 0 && getEnergy() > 0)
                {
                    ICBMSounds.MACHINE_HUM.play(world, xi(), yi(), zi(), 0.5F, 0.85F * getChargePercentage(), true);
                    sendDescPacket();
                }
                if (world.getRedstonePowerFromNeighbors(getPos()) > 0)
                {
                    fire();
                }
            }
        }
        else
        {
            rotationDelta = (float) (Math.pow(getChargePercentage(), 2) * 0.5);
            rotation += rotationDelta;
            if (rotation > 360)
            {
                rotation = 0;
            }
        }
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float) getEnergyConsumption());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
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
        buf.writeByte((byte)empMode.ordinal());
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
        return Math.max(3000000 * (this.empRadius / MAX_RADIUS), 1000000);
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.empRadius = par1NBTTagCompound.getInteger(NBTConstants.EMP_RADIUS);
        this.empMode = EMPMode.values()[par1NBTTagCompound.getByte(NBTConstants.EMP_MODE)];
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setInteger(NBTConstants.EMP_RADIUS, this.empRadius);
        par1NBTTagCompound.setByte(NBTConstants.EMP_MODE, (byte)this.empMode.ordinal());
        return super.writeToNBT(par1NBTTagCompound);
    }

    //@Callback(limit = 1)
    public boolean fire()
    {
        if (this.checkExtract())
        {
            if (isReady())
            {
                BlastEMP emp = (BlastEMP) new BlastEMP()
                        .setBlastWorld(world)
                        .setPosition(this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5)
                        .setBlastSize(empRadius);

                //Apply settings
                switch (this.empMode)
                {
                    default:
                        emp.setEffectBlocks().setEffectEntities();
                        break;
                    case MISSILES_ONLY:
                        emp.setEffectEntities();
                        break;
                    case ELECTRICITY_ONLY:
                        emp.setEffectBlocks();
                        break;
                }

                //Finish and trigger
                if (emp.buildBlast().runBlast() == BlastState.TRIGGERED)
                {
                    //Consume energy
                    this.extractEnergy();

                    //Reset timer
                    this.cooldownTicks = getMaxCooldown();
                    return true;
                }
                else
                {
                    ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.provider.getDimension() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
                    //TODO display some info to player to explain why blast failed and more detailed debug
                }
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback
    public boolean isReady()
    {
        return getCooldown() <= 0;
    }

    //@Callback
    public int getCooldown()
    {
        return cooldownTicks;
    }

    //@Callback
    public int getMaxCooldown()
    {
        return 120;
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
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

    public static enum EMPMode
    {
        ALL, MISSILES_ONLY, ELECTRICITY_ONLY;
    }
}
