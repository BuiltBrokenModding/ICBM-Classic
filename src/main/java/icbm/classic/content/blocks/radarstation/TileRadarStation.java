package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.config.ConfigMain;
import icbm.classic.content.blocks.radarstation.data.RadarRenderData;
import icbm.classic.content.blocks.radarstation.gui.ContainerRadarStation;
import icbm.classic.content.blocks.radarstation.gui.GuiRadarStation;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.radio.messages.IncomingMissileMessage;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.TileMachine;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class TileRadarStation extends TileMachine implements IPacketIDReceiver, IGuiTile
{
    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500; //TODO config
    public final static int ENERGY_COST = 1000;
    public final static int ENERGY_CAPACITY = 20000;

    public static final int GUI_PACKET_ID = 1;
    public static final int SET_TRIGGER_RANGE_PACKET_ID = 2;
    public static final int SET_DETECTION_RANGE_PACKET_ID = 3;
    public static final int SET_FREQUENCY_PACKET_ID = 4;
    public static final int SET_OUTPUT_REDSTONE_ID = 5;

    public static final ITextComponent TRANSLATION_GUI_NAME = new TextComponentTranslation("gui.icbmclassic:radar.name");
    public static final ITextComponent TRANSLATION_TOOLTIP_RANGE = new TextComponentTranslation("gui.icbmclassic:radar.range");
    public static final ITextComponent TRANSLATION_TOOLTIP_RANGE_SHIFT = new TextComponentTranslation("gui.icbmclassic:radar.range.shift");
    public static final ITextComponent TRANSLATION_TOOLTIP_REDSTONE_OFF = new TextComponentTranslation("gui.icbmclassic:radar.redstone.off");
    public static final ITextComponent TRANSLATION_TOOLTIP_REDSTONE_ON = new TextComponentTranslation("gui.icbmclassic:radar.redstone.on");

    public static final String NBT_DETECTION_RANGE = "detection_range"; //TODO fix name
    public static final String NBT_TRIGGER_RANGE = "safetyRadius"; //TODO fix name
    public static final String NBT_OUTPUT_REDSTONE = "emitAll"; //TODO fix name
    public static final String NBT_INVENTORY = "inventory";
    public static final String NBT_RADIO = "radio";

    /** Range to detect any radar contracts */
    @Getter @Setter
    private int detectionRange = 100;

    /** Range to trigger if a threat will land in the area */
    @Getter @Setter
    private int triggerRange = 50;

    /** True if we should output redstone */
    @Getter @Setter
    private boolean outputRedstone = true;

    /** All detected threats in our radar range*/
    @Getter
    private final List<Entity> detectedThreats = new ArrayList<Entity>();
    /** Threats that will cause harm to our protection area */
    @Getter
    private final List<IMissile> incomingThreats = new ArrayList(); //TODO decouple from missile so we can track other entities

    @Getter
    private final RadarRenderData radarRenderData = new RadarRenderData(this);

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ENERGY_CAPACITY)
        .withOnChange((p,c,s) -> {this.updateClient = true; this.markDirty();});
    @Getter
    private final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    @Getter
    private final RadioRadar radio = new RadioRadar(this);

    private EnumRadarState radarVisualState = EnumRadarState.OFF;
    private EnumRadarState preRadarVisualState = EnumRadarState.OFF;

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        super.provideInformation(consumer);
        consumer.accept("NEEDS_POWER", ConfigMain.REQUIRES_POWER);
        consumer.accept("ENERGY_COST_TICK", getEnergyCost());
    }

    @Override
    public void update()
    {
        super.update();

        // Tick inventory
        inventory.onTick();

        if (isServer())
        {
            //Update client every 1 seconds
            if (this.ticks % 20 == 0)
            {
                sendDescPacket();
            }

            final boolean hasPower = energyStorage.consumePower(getEnergyCost(), false);

            //If we have energy
            if (hasPower)
            {
                energyStorage.consumePower(getEnergyCost(), true);

                // Do a radar scan
                if (ticks % 3 == 0) //TODO make config to control scan rate to reduce lag
                {
                    this.doScan(); //TODO consider rewriting to not cache targets
                }

                //Check for incoming and launch anti-missiles if
                if (this.ticks % 20 == 0 && !radio.getChannel().equals(RadioRegistry.EMPTY_HZ) && this.incomingThreats.size() > 0) //TODO track if a anti-missile is already in air to hit target
                {
                    RadioRegistry.popMessage(world, radio, new IncomingMissileMessage(radio.getChannel(), this.incomingThreats.get(0))); //TODO use static var for event name
                }
            }
            // No power, reset state
            else
            {
                incomingThreats.clear();
                detectedThreats.clear();
            }

            //Update redstone state
            final boolean shouldBeOn = hasPower && hasIncomingMissiles();
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

        // Track our state server side
        if(isServer()) {
            this.radarVisualState = getRadarState();
        }

        // Force block re-render if our state has changed
        if(preRadarVisualState != radarVisualState) {
            this.updateClient = true;
            this.markDirty();
            this.world.markAndNotifyBlock(pos, null, getBlockState().withProperty(BlockRadarStation.RADAR_STATE, preRadarVisualState), getBlockState().withProperty(BlockRadarStation.RADAR_STATE, radarVisualState), 3);
            preRadarVisualState = radarVisualState;
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return !(oldState.getBlock() == BlockReg.blockRadarStation && newState.getBlock() == BlockReg.blockRadarStation); //Don't kill tile if the radar station is still there
    }

    private void doScan() //TODO document and thread
    {
        this.incomingThreats.clear();
        this.detectedThreats.clear();
        this.updateClient = true;
        this.radarRenderData.clear();

        final List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, xi() + 0.5, yi() + 0.5, zi() + 0.5, Math.min(detectionRange, MAX_DETECTION_RANGE));

        // Loop list of contacts to ID threats
        for (Entity entity : entities)
        {
            if (isThreat(entity))
            {
                final IMissile newMissile = ICBMClassicHelpers.getMissile(entity);
                if (newMissile != null && newMissile.getTicksInAir() > 1)
                {
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
                    else {
                        this.detectedThreats.add(entity);
                    }
                }
            }
        }

        // Only update render data if we have players viewing the UI
        if(this.getPlayersUsing().size() > 0) {
            radarRenderData.update();
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

    public EnumRadarState getRadarState() {

        if(isClient()) {
            return radarVisualState;
        }

        if(!this.energyStorage.consumePower(getEnergyCost(), false)) {
            return EnumRadarState.OFF;
        }
        else if(this.incomingThreats.size() > 0) {
            return EnumRadarState.DANGER;
        }
        else  if(this.detectedThreats.size() > 0) {
            return EnumRadarState.WARNING;
        }
        return EnumRadarState.ON;
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        this.radarVisualState = EnumRadarState.get(buf.readByte());
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeByte(radarVisualState.ordinal());
    }

    @Override
    public boolean read(ByteBuf data, int ID, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, ID, player, type))
        {
            if (!this.world.isRemote)
            {
                if (ID == SET_TRIGGER_RANGE_PACKET_ID)
                {
                    this.triggerRange = data.readInt();
                    return true;
                }
                else if (ID == SET_DETECTION_RANGE_PACKET_ID)
                {
                    this.detectionRange = data.readInt();
                    return true;
                }
                else if (ID == SET_FREQUENCY_PACKET_ID)
                {
                    this.radio.setChannel(ByteBufUtils.readUTF8String(data));
                    return true;
                }
                else if(ID == SET_OUTPUT_REDSTONE_ID) {
                    this.outputRedstone = data.readBoolean();
                    return true;
                }
            }
            else if (ID == GUI_PACKET_ID) {

                // Fields
                this.detectionRange = data.readInt();
                this.triggerRange = data.readInt();
                this.radio.setChannel(ByteBufUtils.readUTF8String(data));
                this.outputRedstone = data.readBoolean();

                // radar data
                this.radarRenderData.readBytes(data);

                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packet = new PacketTile("gui", GUI_PACKET_ID, this);
        packet.addData(detectionRange);
        packet.addData(triggerRange);
        packet.addData(radio.getChannel());
        packet.addData(this.outputRedstone);
        packet.addData(radarRenderData::writeBytes);
        return packet;
    }

    public void sendHzPacket(String channel) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", SET_FREQUENCY_PACKET_ID, this).addData(channel));
        }
    }

    public void sendTriggerRangePacket(int range) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("triggerRange_C>S", SET_TRIGGER_RANGE_PACKET_ID, this).addData(range));
        }
    }

    public void sendDetectionRangePacket(int range) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("detectionRange_C>S", SET_DETECTION_RANGE_PACKET_ID, this).addData(range));
        }
    }

    public void sendOutputRedstonePacket() {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("outputRedstone_C>S", SET_OUTPUT_REDSTONE_ID, this).addData(!this.outputRedstone));
        }
    }

    public int getStrongRedstonePower(EnumFacing side)
    {
        if (this.outputRedstone && incomingThreats.size() > 0) //TODO add UI customization to pick side of redstone output and minimal number of missiles to trigger
        {
            return Math.min(15, incomingThreats.size());
        }
        return 0;
    }

    public int getEnergyCost() {
        return ENERGY_COST; //TODO scale cost by scan area... maybe scan duration?
    }

    public boolean hasIncomingMissiles() {
        return incomingThreats.size() > 0;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return TRANSLATION_GUI_NAME;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityEnergy.ENERGY)
        {
            return (T) energyStorage;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing)
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
        if(nbt.hasKey(NBTConstants.FREQUENCY)) {
            this.radio.setChannel(Integer.toString(nbt.getInteger(NBTConstants.FREQUENCY)));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {   SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileRadarStation> SAVE_LOGIC = new NbtSaveHandler<TileRadarStation>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBT_INVENTORY, TileRadarStation::getInventory)
        /* */.nodeINBTSerializable(NBT_RADIO, TileRadarStation::getRadio)
        /* */.nodeBoolean(NBT_OUTPUT_REDSTONE, TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone)
        /* */.nodeInteger(NBT_DETECTION_RANGE, TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange)
        /* */.nodeInteger(NBT_TRIGGER_RANGE, TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        .base();

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
