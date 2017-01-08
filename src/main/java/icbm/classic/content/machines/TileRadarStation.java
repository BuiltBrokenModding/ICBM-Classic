package icbm.classic.content.machines;

import com.builtbroken.mc.api.items.hz.IItemFrequency;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.WrenchUtility;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Point;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.radar.RadarRegistry;
import com.builtbroken.mc.lib.world.radio.RadioRegistry;
import com.builtbroken.mc.prefab.tile.Tile;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.prefab.TileFrequency;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class TileRadarStation extends TileFrequency implements IPacketReceiver, IRotatable, IRadioWaveSender
{
    public final static int MAX_DETECTION_RANGE = 500;

    public static final float WATTS = 1.5f;
    public float rotation = 0;
    public int alarmRange = 100;
    public int safetyRange = 50;
    public List<Entity> detectedEntities = new ArrayList<Entity>();
    public boolean emitAll = true;
    /** List of all incoming missiles, in order of distance. */
    private List<EntityMissile> incomingMissiles = new ArrayList<EntityMissile>();
    private byte fangXiang = 3;

    private Ticket ticket;

    public TileRadarStation()
    {
        //super("radarStation", Material.iron);
        // RadarRegistry.register(this);
        //setEnergyHandler(new EnergyStorageHandler(500, 400));
    }

    @Override
    public Tile newTile()
    {
        return null;
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlock(this.xCoord, this.yCoord, this.zCoord));
        //this.chunkLoaderInit(ForgeChunkManager.requestTicket(ICBMExplosion.instance, this.worldObj, Type.NORMAL));
    }

    @Override
    public void update()
    {
        super.update();

        if (!this.worldObj.isRemote)
        {
            //Update client every 2 seconds
            if (this.ticks % 40 == 0)
            {
                sendDescPacket();
            }//Send packets to users with the gui open
            else if (this.ticks % 3 == 0)
            {
                sendPacketToGuiUsers(this.getDescriptionPacket2());
            }
        }

        //If we have energy
        if (hasEnergy())
        {
            this.rotation += 0.08f;

            if (this.rotation > 360)
            {
                this.rotation = 0;
            }

            if (!this.worldObj.isRemote)
            {
                //this.getEnergyHandler().extractEnergy();
            }

            int prevDetectedEntities = this.detectedEntities.size();

            // Do a radar scan
            this.doScan();

            if (prevDetectedEntities != this.detectedEntities.size())
            {
                this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            }
            //Check for incoming and launch anti-missiles if
            if (this.ticks % 20 == 0 && this.incomingMissiles.size() > 0)
            {
                RadioRegistry.popMessage(world(), this, getFrequency(), "fireAntiMissile", this.incomingMissiles.get(0));
            }
        }
        else
        {
            if (detectedEntities.size() > 0)
            {
                worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
            }

            incomingMissiles.clear();
            detectedEntities.clear();
        }

        if (ticks % 40 == 0)
        {
            worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType());
        }
    }

    private boolean hasEnergy()
    {
        return true;
    }

    private void doScan()
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();

        List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world(), xi() + 1.5, yi() + 0.5, zi() + 0.5, MAX_DETECTION_RANGE, null);

        for (Entity entity : entities)
        {
            if (entity instanceof EntityMissile)
            {
                if (((EntityMissile) entity).feiXingTick > -1)
                {
                    if (!this.detectedEntities.contains(entity))
                    {
                        this.detectedEntities.add(entity);
                    }

                    if (this.isMissileGoingToHit((EntityMissile) entity))
                    {
                        if (this.incomingMissiles.size() > 0)
                        {
                            /** Sort in order of distance */
                            double dist = new Pos(this).distance(new Pos(entity));

                            for (int i = 0; i < this.incomingMissiles.size(); i++)
                            {
                                EntityMissile daoDan = this.incomingMissiles.get(i);

                                if (dist < new Pos(this).distance(new Pos(daoDan)))
                                {
                                    this.incomingMissiles.add(i, (EntityMissile) entity);
                                    break;
                                }
                                else if (i == this.incomingMissiles.size() - 1)
                                {
                                    this.incomingMissiles.add((EntityMissile) entity);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            this.incomingMissiles.add((EntityMissile) entity);
                        }
                    }
                }
            }
            else
            {
                this.detectedEntities.add(entity);
            }
        }

        List<EntityPlayer> players = this.worldObj.getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox(this.xCoord - MAX_DETECTION_RANGE, this.yCoord - MAX_DETECTION_RANGE, this.zCoord - MAX_DETECTION_RANGE, this.xCoord + MAX_DETECTION_RANGE, this.yCoord + MAX_DETECTION_RANGE, this.zCoord + MAX_DETECTION_RANGE));

        for (EntityPlayer player : players)
        {
            if (player != null)
            {
                boolean youHuoLuan = false;

                for (int i = 0; i < player.inventory.getSizeInventory(); i++)
                {
                    ItemStack itemStack = player.inventory.getStackInSlot(i);

                    if (itemStack != null)
                    {
                        if (itemStack.getItem() instanceof IItemFrequency)
                        {
                            youHuoLuan = true;
                            break;
                        }
                    }
                }

                if (!youHuoLuan)
                {
                    this.detectedEntities.add(player);
                }
            }
        }
    }

    /**
     * Checks to see if the missile will hit within the range of the radar station
     *
     * @param missile - missile being checked
     * @return true if it will
     */
    public boolean isMissileGoingToHit(EntityMissile missile)
    {
        if (missile == null || missile.targetVector == null)
        {
            return false;
        }
        return (new Pos(missile).toVector2().distance(new Point(this.xCoord, this.zCoord)) < this.alarmRange && missile.targetVector.toVector2().distance(new Point(this.xCoord, this.zCoord)) < this.safetyRange);
    }

    private PacketTile getDescriptionPacket2()
    {
        return new PacketTile(this, 1, this.alarmRange, this.safetyRange, this.getFrequency());
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 4, this.fangXiang, /* Energy */0);
    }

    @Override
    public void read(ByteBuf data, EntityPlayer player, PacketType packet)
    {
        try
        {
            final int ID = data.readInt();

            if (this.worldObj.isRemote)
            {
                if (ID == 1)
                {
                    this.alarmRange = data.readInt();
                    this.safetyRange = data.readInt();
                    this.setFrequency(data.readInt());
                }
                else if (ID == 4)
                {
                    this.fangXiang = data.readByte();
                    //this.getEnergyHandler().setEnergy(data.readLong());
                }
            }
            else if (!this.worldObj.isRemote)
            {
                if (ID == 2)
                {
                    this.safetyRange = data.readInt();
                }
                else if (ID == 3)
                {
                    this.alarmRange = data.readInt();
                }
                else if (ID == 4)
                {
                    this.setFrequency(data.readInt());
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    public boolean isPoweringTo(ForgeDirection side)
    {
        if (incomingMissiles.size() > 0)
        {
            if (this.emitAll)
            {
                return true;
            }

            for (EntityMissile incomingMissile : this.incomingMissiles)
            {
                Point position = new Pos(incomingMissile).toVector2();
                ForgeDirection missileTravelDirection = ForgeDirection.UNKNOWN;
                double closest = -1;

                for (int i = 2; i < 6; i++)
                {
                    double dist = position.distance(new Point(this.xCoord + ForgeDirection.getOrientation(i).offsetX, this.zCoord + ForgeDirection.getOrientation(i).offsetZ));

                    if (dist < closest || closest < 0)
                    {
                        missileTravelDirection = ForgeDirection.getOrientation(i);
                        closest = dist;
                    }
                }

                if (missileTravelDirection.getOpposite() == side)
                {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean isIndirectlyPoweringTo(ForgeDirection side)
    {
        return this.isPoweringTo(side);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.safetyRange = nbt.getInteger("safetyBanJing");
        this.alarmRange = nbt.getInteger("alarmBanJing");
        this.emitAll = nbt.getBoolean("emitAll");
        this.fangXiang = nbt.getByte("fangXiang");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("safetyBanJing", this.safetyRange);
        nbt.setInteger("alarmBanJing", this.alarmRange);
        nbt.setBoolean("emitAll", this.emitAll);
        nbt.setByte("fangXiang", this.fangXiang);
    }


    public boolean onActivated(EntityPlayer entityPlayer)
    {
        if (entityPlayer.inventory.getCurrentItem() != null)
        {
            if (WrenchUtility.isUsableWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), this.xCoord, this.yCoord, this.zCoord))
            {
                if (!this.worldObj.isRemote)
                {
                    this.emitAll = !this.emitAll;
                    entityPlayer.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.radar.redstone") + " " + this.emitAll));
                }

                return true;
            }
        }

        if(isServer())
        {
            entityPlayer.openGui(ICBMClassic.INSTANCE, 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        }
        return true;
    }

    @Override
    public void invalidate()
    {
        ForgeChunkManager.releaseTicket(this.ticket);
        //RadarRegistry.unregister(this);
        super.invalidate();
    }

    @Override
    public ForgeDirection getDirection()
    {
        return ForgeDirection.getOrientation(this.fangXiang);
    }

    @Override
    public void setDirection(ForgeDirection facingDirection)
    {
        this.fangXiang = (byte) facingDirection.ordinal();
    }

    @Override
    public Cube getRadioSenderRange()
    {
        return null;
    }
}
