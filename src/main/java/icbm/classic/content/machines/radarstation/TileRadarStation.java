package icbm.classic.content.machines.radarstation;

import com.builtbroken.jlib.data.vector.IPos3D;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.lib.IGuiTile;
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
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TileRadarStation extends TileFrequency implements IPacketIDReceiver, IRadioWaveSender, IGuiTile, IInventoryProvider<ExternalInventory>, IPeripheral
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
                    RadioRegistry.popMessage(world, this, getFrequency(), "fireAntiMissile", this.incomingMissiles.get(0));
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
                    BlockPos pos = getPos().add(facing.getXOffset(), facing.getYOffset(), facing.getZOffset());
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
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate)
    {
        return false; //Don't kill tile
    }

    private void doScan() //TODO document and thread
    {
        this.incomingMissiles.clear();
        this.detectedEntities.clear();

        List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 1.5, yi() + 0.5, zi() + 0.5, Math.min(alarmRange, MAX_DETECTION_RANGE));

        for (Entity entity : entities)
        {
            if (entity instanceof EntityMissile && ((EntityMissile) entity).getExplosiveType() != Explosives.MISSILE_ANTI.handler)
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
                                EntityMissile missile = this.incomingMissiles.get(i);

                                if (dist < new Pos((TileEntity) this).distance((IPos3D) missile))
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
        if (missile == null || missile.targetPos == null)
        {
            return false;
        }
        double d = missile.targetPos.distance(this);
        //TODO simplify code to not use vector system
        return d < this.safetyRange;
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

                    int type = RadarObjectType.OTHER.ordinal();
                    if (entity instanceof EntityMissile)
                    {
                        type = isMissileGoingToHit((EntityMissile) entity) ? RadarObjectType.MISSILE_IMPACT.ordinal() : RadarObjectType.MISSILE.ordinal();
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
                    this.alarmRange = data.readInt();
                    this.safetyRange = data.readInt();
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
                Point position = new Point(incomingMissile.x(), incomingMissile.y());
                EnumFacing missileTravelDirection = EnumFacing.DOWN;
                double closest = -1;

                for (EnumFacing rotation : EnumFacing.HORIZONTALS)
                {
                    double dist = position.distance(new Point(this.getPos().getX() + rotation.getXOffset(), this.getPos().getZ() + rotation.getZOffset()));

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

    /**
     * @return Peripheral name
     */
    @Nonnull
    @Override
    public String getType() {
        return "ICBM_radar";
    }

    /**
     * @return Methods callable in LUA with ComputerCraft
     */
    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"getDetectedEntities", "getIncomingMissiles", "getAlarmRange", "getSafetyRange", "setAlarmRange", "setSafetyRange"};
    }

    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        if (!this.hasPower()) {
            throw new LuaException("Radar needs power to operate!");
        }

        switch (method) {
            case 0:                 // return entities detected by radar
//                doScan();            used for testing if radar thread is not active
                List<Entity> entities;
                synchronized (this) {
                    entities = detectedEntities;
                }
                if (entities.size() > 0) {
                    ArrayList<HashMap<String, String>> output = new ArrayList<>();
                    for (Entity entity : entities) {
                        // Generate HashMap with entity information
                        HashMap<String, String> entry = new HashMap<>();
                        entry.put("x", String.valueOf(entity.posX));
                        entry.put("y", String.valueOf(entity.posY));
                        entry.put("z", String.valueOf(entity.posZ));
                        if (entity instanceof EntityMissile) {
                            entry.put("type", ((EntityMissile) entity).explosiveID.handler.getMissileName());
                            entry.put("xt", String.valueOf(((EntityMissile) entity).targetPos.xi()));
                            entry.put("yt", String.valueOf(((EntityMissile) entity).targetPos.yi()));
                            entry.put("zt", String.valueOf(((EntityMissile) entity).targetPos.zi()));
                            output.add(entry);
                        } else {
                            entry.put("what", entity.toString());
                        }
                    }
                    // return all the detected entities
                    return output.toArray();
                }
                break;
            case 1:                     // return missile that are going to hit safe zone
//                doScan();
                List<EntityMissile> missiles;
                synchronized (this) {
                    missiles = incomingMissiles;
                }
                if (missiles.size() > 0) {
                    ArrayList<HashMap<String, String>> output = new ArrayList<>();
                    for (EntityMissile missile : missiles) {
                        HashMap<String, String> entry = new HashMap<>();
                        // When called, for each missile it will return missile position, target position and missile type
                        entry.put("type", missile.explosiveID.handler.getMissileName());
                        entry.put("x", String.valueOf(missile.xi()));
                        entry.put("y", String.valueOf(missile.yi()));
                        entry.put("z", String.valueOf(missile.zi()));
                        entry.put("xt", String.valueOf(missile.targetPos.xi()));
                        entry.put("yt", String.valueOf(missile.targetPos.yi()));
                        entry.put("zt", String.valueOf(missile.targetPos.zi()));
                        output.add(entry);
                    }
                    return output.toArray();
                }
                return new Object[0];
            case 2:                     // Return alarm range
                return new String[]{String.valueOf(this.alarmRange)};
            case 3:                     // Return safety range
                return new String[]{String.valueOf(this.safetyRange)};
            case 4:                     // Set alarm range
                if (arguments.length == 1) {
                    try {
                        this.alarmRange = (int) Double.parseDouble(String.valueOf(arguments[0]));
                        return new Object[]{true};
                    } catch (NumberFormatException e) {
                        throw new LuaException("Parameter alarmRange must be a valid integer");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. 1 required");
                }
            case 5:                     // set safety range
                if (arguments.length == 1) {
                    try {
                        this.safetyRange = (int) Double.parseDouble(String.valueOf(arguments[0]));
                        return new Object[]{true};
                    } catch (NumberFormatException e) {
                        throw new LuaException("Parameter safetyRange must be a valid integer");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. 1 required");
                }
            default:
                break;
        }
        return new Object[0];
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }
}
