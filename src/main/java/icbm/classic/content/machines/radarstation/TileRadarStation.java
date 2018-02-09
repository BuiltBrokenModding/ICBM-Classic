package icbm.classic.content.machines.radarstation;

import icbm.classic.lib.network.IPacket;
import icbm.classic.api.map.radio.IRadioWaveSender;
import icbm.classic.api.tile.access.IGuiTile;
import icbm.classic.api.tile.provider.IInventoryProvider;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Point;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.prefab.tile.TileFrequency;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TileRadarStation extends TileFrequency implements IPacketIDReceiver, IRadioWaveSender, IGuiTile, IInventoryProvider<ExternalInventory>
{
    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500;

    public final static int GUI_PACKET_ID = 1;

    public float rotation = 0;
    public int alarmRange = 100;
    public int safetyRange = 50;

    public boolean emitAll = true;

    public List<Entity> detectedEntities = new ArrayList<Entity>();
    /** List of all incoming missiles, in order of distance. */
    private List<EntityMissile> incomingMissiles = new ArrayList<EntityMissile>();

    ExternalInventory inventory;

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
            //Update client every 1 seconds
            if (this.ticks % 20 == 0)
            {
                sendDescPacket();
            }

            //If we have energy
            if (checkExtract())
            {
                //Remove energy
                //this.extractEnergy(); TODO fix so only removes upkeep cost

                // Do a radar scan
                if (ticks % 3 == 0) //TODO make config to control scan rate to reduce lag
                {
                    this.doScan(); //TODO consider rewriting to not cache targets
                }

                //Check for incoming and launch anti-missiles if
                if (this.ticks % 20 == 0 && this.incomingMissiles.size() > 0) //TODO track if a anti-missile is already in air to hit target
                {
                    RadioRegistry.popMessage(world, this, getFrequency(), "fireAntiMissile", this.incomingMissiles.get(0));
                }
            }
            else
            {
                if (detectedEntities.size() > 0)
                {
                    world.setBlockState(getPos(), getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, true));
                }

                incomingMissiles.clear();
                detectedEntities.clear();
            }

            //Update redstone state
            final boolean shouldBeOn = checkExtract() && detectedEntities.size() > 0;
            if (world.getBlockState(getPos()).getValue(BlockRadarStation.REDSTONE_PROPERTY) != shouldBeOn)
            {
                world.setBlockState(getPos(), getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, shouldBeOn));
                for (EnumFacing facing : EnumFacing.HORIZONTALS)
                {
                    BlockPos pos = getPos().add(facing.getFrontOffsetX(), facing.getFrontOffsetY(), facing.getFrontOffsetZ());
                    for (EnumFacing enumfacing : EnumFacing.values())
                    {
                        world.notifyNeighborsOfStateChange(pos.offset(enumfacing), getBlockType(), false);
                    }
                }
            }
        }
        else
        {
            if (checkExtract()) //TODO use a boolean on client for on/off state
            {
                //Animation
                this.rotation += 0.08f;
                if (this.rotation > 360)
                {
                    this.rotation = 0;
                }
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return false; //Don't kill tile
    }

    private void doScan() //TODO document and thread
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();

        List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 1.5, yi() + 0.5, zi() + 0.5, MAX_DETECTION_RANGE);

        for (Entity entity : entities)
        {
            if (entity instanceof EntityMissile)
            {
                if (((EntityMissile) entity).getTicksInAir() > -1)
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
                            double dist = new Pos((TileEntity) this).distance(new Pos(entity));

                            for (int i = 0; i < this.incomingMissiles.size(); i++)
                            {
                                EntityMissile daoDan = this.incomingMissiles.get(i);

                                if (dist < new Pos((TileEntity) this).distance(daoDan.toPos()))
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
        return (missile.toPos().toVector2().distance(this)) < this.alarmRange && missile.targetVector.toVector2().distance(this) < this.safetyRange;
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packet = new PacketTile("gui", GUI_PACKET_ID, this);
        packet.write(alarmRange);
        packet.write(safetyRange);
        packet.write(getFrequency());
        packet.write(detectedEntities.size());
        if (detectedEntities.size() > 0)
        {
            for (Entity entity : detectedEntities)
            {
                if (entity != null && entity.isEntityAlive())
                {
                    packet.write(entity.getEntityId());
                }
                else
                {
                    packet.write(-1);
                }
            }
        }
        return packet;
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        setEnergy(buf.readInt());
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getEnergy());
    }

    @Override
    public boolean read(ByteBuf data, int ID, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, ID, player, type))
        {
            if (this.world.isRemote)
            {
                if (ID == GUI_PACKET_ID)
                {
                    this.alarmRange = data.readInt();
                    this.safetyRange = data.readInt();
                    this.setFrequency(data.readInt());

                    detectedEntities.clear();
                    int entityListSize = data.readInt();
                    for (int i = 0; i < entityListSize; i++)
                    {
                        int id = data.readInt();
                        if (id != -1)
                        {
                            Entity entity = world.getEntityByID(id);
                            if (entity != null)
                            {
                                detectedEntities.add(entity);
                            }
                        }
                    }
                    return true;
                }
            }
            else if (!this.world.isRemote)
            {
                if (ID == 2)
                {
                    this.safetyRange = data.readInt();
                    return true;
                }
                else if (ID == 3)
                {
                    this.alarmRange = data.readInt();
                    return true;
                }
                else if (ID == 4)
                {
                    this.setFrequency(data.readInt());
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public int getStrongRedstonePower(EnumFacing side)
    {
        if (incomingMissiles.size() > 0)
        {
            if (this.emitAll)
            {
                return Math.min(15, 5 + incomingMissiles.size());
            }

            for (EntityMissile incomingMissile : this.incomingMissiles)
            {
                Point position = incomingMissile.toPos().toVector2();
                EnumFacing missileTravelDirection = EnumFacing.DOWN;
                double closest = -1;

                for (EnumFacing rotation : EnumFacing.HORIZONTALS)
                {
                    double dist = position.distance(new Point(this.getPos().getX() + rotation.getFrontOffsetX(), this.getPos().getZ() + rotation.getFrontOffsetZ()));

                    if (dist < closest || closest < 0)
                    {
                        missileTravelDirection = rotation;
                        closest = dist;
                    }
                }

                if (missileTravelDirection.getOpposite().ordinal() == side.ordinal())
                {
                    return Math.min(15, 5 + incomingMissiles.size());
                }
            }
        }

        return 0;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.safetyRange = nbt.getInteger("safetyBanJing");
        this.alarmRange = nbt.getInteger("alarmBanJing");
        this.emitAll = nbt.getBoolean("emitAll");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger("safetyBanJing", this.safetyRange);
        nbt.setInteger("alarmBanJing", this.alarmRange);
        nbt.setBoolean("emitAll", this.emitAll);
        return super.writeToNBT(nbt);
    }

    @Override
    public void sendRadioMessage(float hz, String header, Object... data)
    {
        RadioRegistry.popMessage(world, this, hz, header, data);
    }

    @Override
    public Cube getRadioSenderRange()
    {
        return null;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerRadarStation(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiRadarStation(player, this);
    }
}
