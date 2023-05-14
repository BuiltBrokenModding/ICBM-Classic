package icbm.classic.content.blocks.radarstation;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.config.ConfigMain;
import icbm.classic.content.blocks.emptower.RadioEmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.content.blocks.radarstation.data.RadarRenderData;
import icbm.classic.content.blocks.radarstation.data.RadarRenderDot;
import icbm.classic.content.blocks.radarstation.gui.ContainerRadarStation;
import icbm.classic.content.blocks.radarstation.gui.GuiRadarStation;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.lambda.*;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radar.RadarRegistry;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.imp.Radio;
import icbm.classic.lib.radio.messages.IncomingMissileMessage;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.lib.tile.TickDoOnce;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
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
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.GameRegistry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

public class TileRadarStation extends TileMachine implements IMachineInfo, IGuiTile, IPlayerUsing
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "radarstation");

    /** Max range the radar station will attempt to find targets inside */
    public final static int MAX_DETECTION_RANGE = 500; //TODO config
    public final static int ENERGY_COST = 1000;
    public final static int ENERGY_CAPACITY = 20000;

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
        .withOnChange((p,c,s) -> this.markDirty());
    @Getter
    private final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    @Getter
    private final RadioRadar radio = new RadioRadar(this);

    private EnumRadarState radarVisualState = EnumRadarState.OFF;
    private EnumRadarState preRadarVisualState = EnumRadarState.OFF;

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<EntityPlayer> playersUsing = new LinkedList<>();

    public TileRadarStation() {
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.openContainer instanceof ContainerRadarStation));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if(isServer()) {
            descriptionPacketSender.doNext();
        }
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER);
        consumer.accept(ENERGY_COST_TICK, getEnergyCost());
        consumer.accept("MAX_RANGE", MAX_DETECTION_RANGE);
    }

    @Override
    public void update()
    {
        super.update();

        if (isServer())
        {
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
        this.descriptionPacketSender.doNext();
        this.radarRenderData.clear();

        final List<Entity> entities = RadarRegistry.getAllLivingObjectsWithin(world, getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5, Math.min(detectionRange, MAX_DETECTION_RANGE));

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
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerRadarStation(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiRadarStation(player, this);
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
    public static void register() {
        GameRegistry.registerTileEntity(TileRadarStation.class, REGISTRY_NAME);
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_GUI, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_DETECTION_RANGE, PACKET_TRIGGER_RANGE, PACKET_REDSTONE_OUTPUT);
    }

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_DESCRIPTION = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeEnum(EnumRadarState.class, (t) -> t.radarVisualState, (t, e) -> t.radarVisualState = e);

    public static final PacketCodex<TileRadarStation, IRadioChannelAccess> PACKET_RADIO_HZ = GeneralCodexs.radioChannel(REGISTRY_NAME, (tile) -> tile.radio);
    public static final PacketCodex<TileRadarStation, Radio> PACKET_RADIO_DISABLE = GeneralCodexs.radioToggleDisable(REGISTRY_NAME, (tile) -> tile.radio);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_TRIGGER_RANGE = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "range.trigger")
        .fromClient()
        .nodeInt(TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_DETECTION_RANGE = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "range.detection")
        .fromClient()
        .nodeInt(TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_REDSTONE_OUTPUT = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "redstone.output")
        .fromClient()
        .toggleBoolean(TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone);

    public static final PacketCodexTile<TileRadarStation, TileRadarStation> PACKET_GUI = (PacketCodexTile<TileRadarStation, TileRadarStation>) new PacketCodexTile<TileRadarStation, TileRadarStation>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt(TileRadarStation::getTriggerRange, TileRadarStation::setTriggerRange)
        .nodeInt(TileRadarStation::getDetectionRange, TileRadarStation::setDetectionRange)
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeBoolean(TileRadarStation::isOutputRedstone, TileRadarStation::setOutputRedstone)
        .node( (t) -> t.radarRenderData.getDots(), (t, dots) -> t.radarRenderData.setDots(dots), RadarRenderData::encodeDots, RadarRenderData::decodeDots);
}
