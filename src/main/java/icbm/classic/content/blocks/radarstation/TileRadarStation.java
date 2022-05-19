package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.content.entity.missile.explosive.EntityExplosiveMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Point;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.TileFrequency;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class TileRadarStation extends TileFrequency implements IPacketIDReceiver, IRadioWaveSender, IGuiTile, IInventoryProvider<ExternalInventory>
{
    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500;

    public final static int GUI_PACKET_ID = 1;
    public static final int SET_SAFETY_RANGE_PACKET_ID = 2;
    public static final int SET_ALARM_RANGE_PACKET_ID = 3;
    public static final int SET_FREQUENCY_PACKET_ID = 4;

    public float rotation = 0;
    public int detectionRange = 100;
    public int triggerRange = 50;

    public boolean emitAll = true;

    public List<Entity> detectedEntities = new ArrayList<Entity>();
    /** List of all incoming missiles, in order of distance. */
    private List<IMissile> incomingMissiles = new ArrayList();

    ExternalInventory inventory;

    protected List<Pos> guiDrawPoints = new ArrayList();
    protected RadarObjectType[] types;
    protected boolean updateDrawList = true;

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
                    RadioRegistry.popMessage(world, this, getFrequency(), "fireAntiMissile", this.incomingMissiles.get(0)); //TODO use static var for event name
                }
            }
            else
            {
                if (detectedEntities.size() > 0)
                {
                    world.setBlockState(getPos(), getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, false));
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
                if (updateDrawList)
                {
                    guiDrawPoints.clear();
                    for (int i = 0; i < detectedEntities.size(); i++)
                    {
                        Entity entity = detectedEntities.get(i);
                        if (entity != null)
                        {
                            guiDrawPoints.add(new Pos(entity.posX, entity.posZ, types[i].ordinal()));
                        }
                    }
                }

                //Animation
                this.rotation += 0.08f;
                if (this.rotation > 360)
                {
                    this.rotation = 0;
                }
            }
            else
            {
                guiDrawPoints.clear();
            }
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return !(oldState.getBlock() == BlockReg.blockRadarStation && newState.getBlock() == BlockReg.blockRadarStation); //Don't kill tile if the radar station is still there
    }

    private void doScan() //TODO document and thread
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();

        List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 1.5, yi() + 0.5, zi() + 0.5, Math.min(detectionRange, MAX_DETECTION_RANGE));

        ICBMClassic.logger().info("Radar" + entities.size());

        for (Entity entity : entities)
        {
            if (ICBMClassicHelpers.isMissile(entity)) //TODO && ((EntityMissile) entity).getExplosiveType() != Explosives.MISSILE_ANTI.handler)
            {
                final IMissile newMissile = ICBMClassicHelpers.getMissile(entity);
                if (newMissile != null && newMissile.getTicksInAir() > 1)
                {
                    if (!this.detectedEntities.contains(entity))
                    {
                        this.detectedEntities.add(entity);
                    }

                    if (this.isMissileGoingToHit((EntityExplosiveMissile) entity))
                    {
                        if (this.incomingMissiles.size() > 0)
                        {
                            /** Sort in order of distance */
                            double dist = new Pos((TileEntity) this).distance(newMissile);

                            for (int i = 0; i < this.incomingMissiles.size(); i++)
                            {
                                IMissile missile = this.incomingMissiles.get(i);

                                if (dist < new Pos((TileEntity) this).distance(missile))
                                {
                                    this.incomingMissiles.add(i, missile);
                                    break;
                                }
                                else if (i == this.incomingMissiles.size() - 1)
                                {
                                    this.incomingMissiles.add(missile);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            this.incomingMissiles.add(newMissile);
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
    public boolean isMissileGoingToHit(EntityExplosiveMissile missile)
    {
        if (missile == null)
        {
            return false;
        }


        if (missile.ballisticFlightLogic.targetPos == null)
        {
            Vec3d mpos = new Vec3d(missile.xf(),missile.yf(), missile.zf());    // missile position
            Vec3d rpos = new Vec3d(this.pos.getX(),this.pos.getY(), this.pos.getZ());   // radar position

            double nextDistance = mpos.add(missile.getVelocity().toVec3d()).distanceTo(rpos);   // next distance from missile to radar
            double currentDistance = mpos.distanceTo(rpos); // current distance from missile to radar

            return nextDistance < currentDistance;   // we assume that the missile hits if the distance decreases (the missile is coming closer)
        }

        double d = missile.ballisticFlightLogic.targetPos.distance(this);
        //TODO simplify code to not use vector system
        return d < this.triggerRange;
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packet = new PacketTile("gui", GUI_PACKET_ID, this);
        packet.write(detectionRange);
        packet.write(triggerRange);
        packet.write(getFrequency());
        packet.write(detectedEntities.size());
        if (detectedEntities.size() > 0)
        {
            for (Entity entity : detectedEntities)
            {
                if (entity != null && entity.isEntityAlive())
                {
                    packet.write(entity.getEntityId());

                    int type = RadarObjectType.OTHER.ordinal();
                    if (entity instanceof EntityExplosiveMissile)
                    {
                        type = isMissileGoingToHit((EntityExplosiveMissile) entity) ? RadarObjectType.MISSILE_IMPACT.ordinal() : RadarObjectType.MISSILE.ordinal();
                    }
                    packet.write(type);
                }
                else
                {
                    packet.write(-1);
                    packet.write(0);
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
                    this.detectionRange = data.readInt();
                    this.triggerRange = data.readInt();
                    this.setFrequency(data.readInt());

                    this.updateDrawList = true;

                    types = null;
                    detectedEntities.clear(); //TODO recode so we are not getting entities client side

                    int entityListSize = data.readInt();
                    types = new RadarObjectType[entityListSize];

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
                        types[i] = RadarObjectType.get(data.readInt());
                    }
                    return true;
                }
            }
            else if (!this.world.isRemote)
            {
                if (ID == SET_SAFETY_RANGE_PACKET_ID)
                {
                    this.triggerRange = data.readInt();
                    return true;
                }
                else if (ID == SET_ALARM_RANGE_PACKET_ID)
                {
                    this.detectionRange = data.readInt();
                    return true;
                }
                else if (ID == SET_FREQUENCY_PACKET_ID)
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

            for (IMissile incomingMissile : this.incomingMissiles)
            {
                Point position = new Point(incomingMissile.x(), incomingMissile.y());
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
        this.triggerRange = nbt.getInteger(NBTConstants.SAFETY_RADIUS);
        this.detectionRange = nbt.getInteger(NBTConstants.ALARM_RADIUS);
        this.emitAll = nbt.getBoolean(NBTConstants.EMIT_ALL);
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.SAFETY_RADIUS, this.triggerRange);
        nbt.setInteger(NBTConstants.ALARM_RADIUS, this.detectionRange);
        nbt.setBoolean(NBTConstants.EMIT_ALL, this.emitAll);
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
