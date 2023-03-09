package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.content.blocks.radarstation.gui.ContainerRadarStation;
import icbm.classic.content.blocks.radarstation.gui.GuiRadarStation;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.radio.messages.IncomingMissileMessage;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.TilePoweredMachine;
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
import net.minecraftforge.fml.common.network.ByteBufUtils;

import java.util.ArrayList;
import java.util.List;

public class TileRadarStation extends TilePoweredMachine implements IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>
{
    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500;

    public static final int GUI_PACKET_ID = 1;
    public static final int SET_SAFETY_RANGE_PACKET_ID = 2;
    public static final int SET_ALARM_RANGE_PACKET_ID = 3;
    public static final int SET_FREQUENCY_PACKET_ID = 4;

    /** Range to detect any radar contracts */
    public int detectionRange = 100;

    /** Range to trigger if a threat will land in the area */
    public int triggerRange = 50;

    /** True if we should output redstone */
    public boolean enableRedstoneOutput = true;

    /** All entities detected by the radar */
    private final List<Entity> detectedRadarEntities = new ArrayList();
    /** All detected threats in our radar range*/
    private final List<Entity> detectedThreats = new ArrayList<Entity>();
    /** Threats that will cause harm to our protection area */
    private final List<IMissile> incomingThreats = new ArrayList(); //TODO decouple from missile so we can track other entities

    ExternalInventory inventory;

    // UI data
    public List<Pos> guiDrawPoints = new ArrayList();
    public RadarObjectType[] types;
    public boolean updateDrawList = true;
    public boolean hasIncomingMissiles = false;
    public boolean hasDetectedEntities = false;
    public float rotation = 0;

    public final RadioRadar radioCap = new RadioRadar(this);

    private EnumRadarState prevRenderState = EnumRadarState.OFF;

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
                if (this.ticks % 20 == 0 && !radioCap.getChannel().equals(RadioRegistry.EMPTY_HZ) && this.incomingThreats.size() > 0) //TODO track if a anti-missile is already in air to hit target
                {
                    RadioRegistry.popMessage(world, radioCap, new IncomingMissileMessage(radioCap.getChannel(), this.incomingThreats.get(0))); //TODO use static var for event name
                }
            }
            // No power, reset state
            else
            {
                incomingThreats.clear();
                detectedThreats.clear();
            }

            //Update redstone state
            final boolean shouldBeOn = checkExtract() && hasIncomingMissiles();
            if (world.getBlockState(getPos()).getValue(BlockRadarStation.REDSTONE_PROPERTY) != shouldBeOn)
            {
                final BlockPos selfPos = getPos();

                ICBMClassic.logger().info("Updating redstone state " + shouldBeOn);
                world.setBlockState(selfPos, getBlockState().withProperty(BlockRadarStation.REDSTONE_PROPERTY, shouldBeOn), 3);
                for (EnumFacing facing : EnumFacing.values())
                {
                    final BlockPos targetPos = selfPos.offset(facing);
                    world.neighborChanged(targetPos, getBlockType(), getPos());
                    world.notifyNeighborsOfStateExcept(targetPos, getBlockType(), facing.getOpposite());
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
                    for (int i = 0; i < detectedThreats.size(); i++)
                    {
                        Entity entity = detectedThreats.get(i);
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

        // Force block re-render if our state has changed
        final EnumRadarState state = getRadarState();
        if(prevRenderState != state) {
            this.markDirty();
            this.world.markAndNotifyBlock(pos, null, getBlockState().withProperty(BlockRadarStation.RADAR_STATE, prevRenderState), getBlockState().withProperty(BlockRadarStation.RADAR_STATE, state), 3);
            prevRenderState = state;
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return !(oldState.getBlock() == BlockReg.blockRadarStation && newState.getBlock() == BlockReg.blockRadarStation); //Don't kill tile if the radar station is still there
    }

    private void doScan() //TODO document and thread
    {
        this.detectedRadarEntities.clear();
        this.incomingThreats.clear();
        this.detectedThreats.clear();
        this.updateClient = true;

        final List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 1.5, yi() + 0.5, zi() + 0.5, Math.min(detectionRange, MAX_DETECTION_RANGE));

        // Store all radar contracts in range for nice visuals
        this.detectedRadarEntities.addAll(entities);

        // Loop list of contacts to ID threats
        for (Entity entity : detectedRadarEntities)
        {
            if (isThreat(entity))
            {
                final IMissile newMissile = ICBMClassicHelpers.getMissile(entity);
                if (newMissile != null && newMissile.getTicksInAir() > 1)
                {
                    this.detectedThreats.add(entity);

                    if (this.isMissileGoingToHit(newMissile))
                    {
                        if (this.incomingThreats.size() > 0)
                        {
                            // Sort in order of distance
                            double dist = new Pos((TileEntity) this).distance(newMissile);

                            for (int i = 0; i < this.incomingThreats.size(); i++) //TODO switch to priority list
                            {
                                IMissile missile = this.incomingThreats.get(i);

                                if (dist < new Pos((TileEntity) this).distance(missile))
                                {
                                    this.incomingThreats.add(i, missile);
                                    break;
                                }
                                else if (i == this.incomingThreats.size() - 1)
                                {
                                    this.incomingThreats.add(missile);
                                    break;
                                }
                            }
                        }
                        else
                        {
                            this.incomingThreats.add(newMissile);
                        }
                    }
                }
            }
        }
    }

    public static boolean isThreat(Entity entity)
    {
        // TODO let users customize threat list
        return entity != null
            // Ignore SAM missiles
            && !(entity instanceof EntitySurfaceToAirMissile)
            // Track explosive missiles (using caps to allow other mods to interact more easily)
            && entity.hasCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null)
            && entity.hasCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null); //TODO recode to use a radar classification system
    }

    /**
     * Checks to see if the missile will hit within the range of the radar station
     *
     * @param missile - missile being checked
     * @return true if it will
     */
    public boolean isMissileGoingToHit(IMissile missile)
    {
        if (missile == null || missile.getMissileEntity() == null || !missile.getMissileEntity().isEntityAlive())
        {
            return false;
        }
        //TODO rewrite this as a 2D without objects for less memory waste

        Vec3d mpos = new Vec3d(missile.xf(),missile.yf(), missile.zf());    // missile position
        Vec3d rpos = new Vec3d(this.pos.getX(),this.pos.getY(), this.pos.getZ());   // radar position

        double nextDistance = mpos.addVector(missile.getMissileEntity().motionX, missile.getMissileEntity().motionY, missile.getMissileEntity().motionZ).distanceTo(rpos);   // next distance from missile to radar
        double currentDistance = mpos.distanceTo(rpos); // current distance from missile to radar

        return nextDistance < currentDistance;   // we assume that the missile hits if the distance decreases (the missile is coming closer)
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packet = new PacketTile("gui", GUI_PACKET_ID, this);
        packet.write(detectionRange);
        packet.write(triggerRange);
        packet.write(radioCap.getChannel());
        packet.write(detectedRadarEntities.size());
        if (detectedRadarEntities.size() > 0)
        {
            for (Entity entity : detectedRadarEntities)
            {
                if (entity != null && entity.isEntityAlive()) //TODO run filter before sending so we don't rewrite empty data
                {
                    packet.write(entity.getEntityId()); //TODO send 2D coords instead of entity info

                    int type = RadarObjectType.OTHER.ordinal();
                    if (isThreat(entity))
                    {
                        final IMissile missile = entity.getCapability(ICBMClassicAPI.MISSILE_CAPABILITY, null);
                        type = isMissileGoingToHit(missile) ? RadarObjectType.THREAT_IMPACT.ordinal() : RadarObjectType.THREAT.ordinal();
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
        this.hasDetectedEntities = buf.readBoolean(); //TODO sync counts if we display in UI
        this.hasIncomingMissiles = buf.readBoolean();
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeBoolean(this.detectedThreats.size() > 0);
        buf.writeBoolean(this.incomingThreats.size() > 0);
    }

    public EnumRadarState getRadarState() {
        if(!hasPower()) {
            return EnumRadarState.OFF;
        }
        else if(hasIncomingMissiles) {
            return EnumRadarState.DANGER;
        }
        else  if(hasDetectedEntities) {
            return EnumRadarState.WARNING;
        }
        return EnumRadarState.ON;
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
                    this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));

                    // Reset state
                    this.updateDrawList = true;
                    types = null;
                    detectedThreats.clear(); //TODO recode so we are not getting entities client side

                    int entityListSize = data.readInt();
                    types = new RadarObjectType[entityListSize];

                    // Read incoming detection list data
                    for (int i = 0; i < entityListSize; i++)
                    {
                        int id = data.readInt();
                        if (id != -1)
                        {
                            Entity entity = world.getEntityByID(id);
                            if (entity != null)
                            {
                                detectedThreats.add(entity);
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
                    this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    public int getStrongRedstonePower(EnumFacing side)
    {
        if (this.enableRedstoneOutput && incomingThreats.size() > 0) //TODO add UI customization to pick side of redstone output and minimal number of missiles to trigger
        {
            return Math.min(15, incomingThreats.size());
        }
        return 0;
    }

    public boolean hasIncomingMissiles() {
        return incomingThreats.size() > 0;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.triggerRange = nbt.getInteger(NBTConstants.SAFETY_RADIUS);
        this.detectionRange = nbt.getInteger(NBTConstants.ALARM_RADIUS);
        this.enableRedstoneOutput = nbt.getBoolean(NBTConstants.EMIT_ALL);
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setInteger(NBTConstants.SAFETY_RADIUS, this.triggerRange);
        nbt.setInteger(NBTConstants.ALARM_RADIUS, this.detectionRange);
        nbt.setBoolean(NBTConstants.EMIT_ALL, this.enableRedstoneOutput);
        return super.writeToNBT(nbt);
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
