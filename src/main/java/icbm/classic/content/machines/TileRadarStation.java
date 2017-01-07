package icbm.classic.content.machines;

import com.builtbroken.mc.api.tile.IRotatable;
import com.builtbroken.mc.core.network.IPacketReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.google.common.io.ByteArrayDataInput;
import icbm.classic.prefab.TileFrequency;
import icbm.explosion.ICBMExplosion;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.content.machines.launcher.TileLauncherScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.ForgeChunkManager.Ticket;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.*;

public class TileRadarStation extends TileFrequency implements IPacketReceiver, IRotatable
{
    public final static int MAX_DETECTION_RANGE = 500;

    public static final float WATTS = 1.5f;
    private final Set<EntityPlayer> playersUsing = new HashSet<EntityPlayer>();
    public float rotation = 0;
    public int alarmRange = 100;
    public int safetyRange = 50;
    public List<Entity> detectedEntities = new ArrayList<Entity>();

    public List<TileEntity> detectedTiles = new ArrayList<TileEntity>();
    public boolean emitAll = true;
    /** List of all incoming missiles, in order of distance. */
    private List<EntityMissile> incomingMissiles = new ArrayList<EntityMissile>();
    private byte fangXiang = 3;

    private Ticket ticket;

    public TileRadarStation()
    {
        super();
       // RadarRegistry.register(this);
        setEnergyHandler(new EnergyStorageHandler(500, 400));
    }

    @Override
    public void firstTick()
    {
        super.FirstTick();
        this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.worldObj.getBlockId(this.xCoord, this.yCoord, this.zCoord));
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
        if (this.getEnergyHandler().checkExtract())
        {
            this.rotation += 0.08f;

            if (this.rotation > 360)
            {
                this.rotation = 0;
            }

            if (!this.worldObj.isRemote)
            {
                this.getEnergyHandler().extractEnergy();
            }

            int prevDetectedEntities = this.detectedEntities.size();

            // Do a radar scan
            this.doScan();

            if (prevDetectedEntities != this.detectedEntities.size())
            {
                this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID);
            }
            //Check for incoming and launch anti-missiles if
            if (this.ticks % 20 == 0 && this.incomingMissiles.size() > 0)
            {
                for (IBlockFrequency blockFrequency : FrequencyGrid.instance().get())
                {
                    if (blockFrequency instanceof TileLauncherPrefab)
                    {
                        TileLauncherPrefab launcher = (TileLauncherPrefab) blockFrequency;

                        if (new Pos(this).distance(new Pos(launcher)) < this.alarmRange && launcher.getFrequency() == this.getFrequency())
                        {
                            if (launcher instanceof TileLauncherScreen)
                            {
                                double height = launcher.getTarget() != null ? launcher.getTarget().y : 0;
                                launcher.setTarget(new Pos(this.incomingMissiles.get(0).posX, height, this.incomingMissiles.get(0).posZ));
                            }
                            else
                            {
                                launcher.setTarget(new Pos(this.incomingMissiles.get(0)));
                            }
                        }
                    }
                }
            }
        }
        else
        {
            if (detectedEntities.size() > 0)
            {
                worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID);
            }

            incomingMissiles.clear();
            detectedEntities.clear();
            detectedTiles.clear();
        }

        if (ticks % 40 == 0)
        {
            worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, this.getBlockType().blockID);
        }
    }

    private void doScan()
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();
        this.detectedTiles.clear();

        List<Entity> entities = RadarRegistry.getEntitiesWithinRadius(new Pos(this).toVector2(), MAX_DETECTION_RANGE);

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

        for (TileEntity jiQi : RadarRegistry.getTileEntitiesInArea(new Vector2(this.xCoord - TileRadarStation.MAX_DETECTION_RANGE, this.zCoord - TileRadarStation.MAX_DETECTION_RANGE), new Vector2(this.xCoord + TileRadarStation.MAX_DETECTION_RANGE, this.zCoord + TileRadarStation.MAX_DETECTION_RANGE)))
        {
            if (jiQi instanceof TileRadarStation)
            {
                if (((TileRadarStation) jiQi).getEnergyHandler().getEnergy() > 0)
                {
                    this.detectedTiles.add(jiQi);
                }
            }
            else
            {
                if (this.detectedTiles instanceof IRadarDetectable)
                {
                    if (((IRadarDetectable) this.detectedTiles).canDetect(this))
                    {
                        this.detectedTiles.add(jiQi);
                    }
                }
                else
                {
                    this.detectedTiles.add(jiQi);
                }
            }
        }
    }

    /** Checks to see if the missile will hit within the range of the radar station
     *
     * @param missile - missile being checked
     * @return true if it will */
    public boolean isMissileGoingToHit(EntityMissile missile)
    {
        if (missile == null || missile.targetVector == null)
        {
            return false;
        }
        return (Vector2.distance(new Pos(missile).toVector2(), new Vector2(this.xCoord, this.zCoord)) < this.alarmRange && Vector2.distance(missile.targetVector.toVector2(), new Vector2(this.xCoord, this.zCoord)) < this.safetyRange);
    }

    private PacketTile getDescriptionPacket2()
    {
        return new PacketTile(this, 1, this.alarmRange, this.safetyRange, this.getFrequency());
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 4, this.fangXiang, this.getEnergyHandler().getEnergy());
    }

    @Override
    public void onReceivePacket(ByteArrayDataInput data, EntityPlayer player, Object... extra)
    {
        try
        {
            final int ID = data.readInt();

            if (ID == -1)
            {
                if (data.readBoolean())
                {
                    PacketHandler.sendPacketToClients(this.getDescriptionPacket2(), this.worldObj, new Pos(this), 15);
                    this.playersUsing.add(player);
                }
                else
                {
                    this.playersUsing.remove(player);
                }
            }
            else if (this.worldObj.isRemote)
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
                    this.getEnergyHandler().setEnergy(data.readLong());
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

    @Override
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
                Vector2 position = new Pos(incomingMissile).toVector2();
                ForgeDirection missileTravelDirection = ForgeDirection.UNKNOWN;
                double closest = -1;

                for (int i = 2; i < 6; i++)
                {
                    double dist = Vector2.distance(position, new Vector2(this.xCoord + ForgeDirection.getOrientation(i).offsetX, this.zCoord + ForgeDirection.getOrientation(i).offsetZ));

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

    @Override
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

    @Override
    public boolean onActivated(EntityPlayer entityPlayer)
    {
        if (entityPlayer.inventory.getCurrentItem() != null)
        {
            if (WrenchUtility.isUsableWrench(entityPlayer, entityPlayer.inventory.getCurrentItem(), this.xCoord, this.yCoord, this.zCoord))
            {
                if (!this.worldObj.isRemote)
                {
                    this.emitAll = !this.emitAll;
                    entityPlayer.addChatMessage(LanguageUtility.getLocal("message.radar.redstone") + " " + this.emitAll);
                }

                return true;
            }
        }

        entityPlayer.openGui(ICBMExplosion.instance, 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
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

}
