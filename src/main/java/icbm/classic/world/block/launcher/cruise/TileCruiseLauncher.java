package icbm.classic.world.block.launcher.cruise;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.lib.tile.TickDoOnce;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.IcbmBlockEntity;
import icbm.classic.world.block.launcher.FiringPackage;
import icbm.classic.world.block.launcher.LauncherLangs;
import icbm.classic.world.block.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.world.block.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNode;
import icbm.classic.world.missile.logic.source.cause.EntityCause;
import icbm.classic.world.missile.logic.source.cause.RedstoneCause;
import icbm.classic.world.missile.logic.targeting.BasicTargetData;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.play.server.SPacketUpdateBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.common.MinecraftForge;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.energy.CapabilityEnergy;
import net.neoforged.fml.common.registry.GameRegistry;
import net.neoforged.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class TileCruiseLauncher extends IcbmBlockEntity implements IGuiTile, ILauncherComponent, IMachineInfo, IPlayerUsing {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "cruiseLauncher");

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    public static final double MISSILE__HOLDER_Y = 2.0;

    /**
     * Target position of the launcher
     */
    private Vec3 _targetPos = Vec3.ZERO;

    /**
     * Desired aim angle, updated every tick if target != null
     */
    @Getter
    protected final EulerAngle aim = new EulerAngle(0, 0, 0); //TODO change UI to only have yaw and pitch, drop xyz but still allow tools to auto fill from xyz

    /**
     * Current aim angle, updated each tick
     */
    @Getter
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /**
     * Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation
     */
    protected long lastRotationUpdate = System.nanoTime();

    /**
     * Percent of time that passed since last tick, should be 1.0 on a stable server
     */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p, c, s) -> this.markDirty());
    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile).withChangeCallback((stack) -> this.markDirty()))
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    @Getter
    protected final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);

    @Getter
    protected final CLauncherCapability launcher = new CLauncherCapability(this);

    @Getter
    @Setter
    protected FiringPackage firingPackage;

    private final LauncherNode launcherNode = new LauncherNode(this, true);
    public final RadioCruise radio = new RadioCruise(this);

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileCruiseLauncher() {
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.openContainer instanceof ContainerCruiseLauncher));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void markDirty() {
        super.markDirty();
        if (isServer()) {
            descriptionPacketSender.doNext();
        }
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept("NEEDS_POWER", ConfigMain.REQUIRES_POWER);
        consumer.accept("ENERGY_COST_ACTION", getFiringCost());
    }

    public int getFiringCost() {
        return ConfigLauncher.POWER_COST;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        launcherNode.connectToTiles();
        if (isServer()) {
            RadioRegistry.add(radio);
        }
    }

    @Override
    public void invalidate() {
        if (isServer()) {
            RadioRegistry.remove(radio);
        }
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    /**
     * Gets the translation to use for showing status to the user. Should
     * only be used for long format displays.
     *
     * @return The string to be displayed
     */
    public Component getStatusTranslation() {
        if (!hasChargeToFire()) {
            return new TextComponentTranslation(LauncherLangs.ERROR_NO_POWER);
        }
        // Checks for empty slot
        else if (!missileHolder.hasMissile()) {
            return new TextComponentTranslation(LauncherLangs.ERROR_MISSILE_NONE);
        } else if (!hasTarget()) {
            return new TextComponentTranslation(LauncherLangs.ERROR_TARGET_NONE);
        } else if (this.isTooClose(getTarget())) {
            return new TextComponentTranslation(LauncherLangs.ERROR_TARGET_MIN);
        } else if (!canSpawnMissileWithNoCollision()) {
            return new TextComponentTranslation(LauncherLangs.ERROR_MISSILE_SPACE);
        }

        // TODO check angle limits

        return new TextComponentTranslation(LauncherLangs.STATUS_READY);
    }

    @Override
    public void update() {
        super.update();

        //TODO add a per tick energy consumption or at least while aiming

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();


        if (isServer()) {

            // Update current aim
            currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

            // Check redstone
            if (this.ticks % REDSTONE_CHECK_RATE == 0) {
                for (Direction side : Direction.VALUES) {
                    final int power = world.getRedstonePower(getPos().offset(side), side);
                    if (power > 1) {
                        firingPackage = new FiringPackage(new BasicTargetData(getTarget()), new RedstoneCause(getLevel(), getPos(), getBlockState(), side), 0);
                    }
                }
            }

            if (firingPackage != null && isAimed()) {
                firingPackage.setCountDown(firingPackage.getCountDown() - 1);
                if (firingPackage.getCountDown() <= 0) {
                    firingPackage.launch(launcher);
                    firingPackage = null;
                }
            }
        }
    }


    public void setTarget(Vec3 target) {
        if (!Objects.equals(target, this._targetPos)) {

            // Only fire packet server side to avoid description packet triggering events
            if (isServer()) {
                final LauncherSetTargetEvent event = new LauncherSetTargetEvent(world, getPos(), target);

                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    this._targetPos = event.target == null ? Vec3.ZERO : event.target;
                    this.markDirty();
                }
            } else {
                this._targetPos = target;
            }
            updateAimAngle();
        }
    }

    public boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle() {
        if (hasTarget()) {
            final Vec3 aimPoint = getTarget();
            final Pos center = new Pos(this).add(0.5, MISSILE__HOLDER_Y, 0.5);
            aim.set(center.toEulerAngle(aimPoint).clampTo360());
            aim.setYaw(EulerAngle.clampPos360(aim.yaw()));
        } else {
            aim.set(0, 0, 0);
        }
    }

    protected void initFromLoad() {
        cachedMissileStack = inventory.getStackInSlot(0);
        updateAimAngle();
        currentAim.set(aim);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateBlockEntity pkt) {
        super.onDataPacket(net, pkt);
        initFromLoad();
    }

    //@Override
    public boolean canLaunch() {
        return hasTarget()
            && isAimed()
            && missileHolder.hasMissile()
            && hasChargeToFire()
            && !this.isTooClose(this.getTarget())
            && canSpawnMissileWithNoCollision();
    }

    protected boolean hasTarget() {
        return getTarget() != null && getTarget() != Vec3.ZERO;
    }

    protected boolean hasChargeToFire() {
        return this.energyStorage.consumePower(getFiringCost(), true);
    }

    protected boolean canSpawnMissileWithNoCollision() {
        //Make sure there is noting above us to hit when spawning the missile
        // TODO use raytrace to detect collision so we can fire out of holes
        for (int y = 1; y <= 2; y++) {
            for (int x = -1; x < 2; x++) {
                for (int z = -1; z < 2; z++) {
                    final BlockPos pos = getPos().add(x, y, z);
                    final BlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    if (!block.isAir(state, world, pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    // Is the target too close?
    public boolean isTooClose(Vec3 target) {
        return new Pos(getPos()).add(0.5).distance(target) < 20; //TODO remove pos usage
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(getPos().add(-2, 0, -2), getPos().add(2, 3, 2));
    }

    @Override
    public Object getServerGuiElement(int id, Player player) {
        return new ContainerCruiseLauncher(player, this);
    }

    @Override
    public Object getClientGuiElement(int id, Player player) {
        return new GuiCruiseLauncher(player, this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable Direction facing) {
        return super.hasCapability(capability, facing)
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY) {
            return (T) missileHolder;
        } else if (capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) launcher;
        } else if (capability == ICBMClassicAPI.RADIO_CAPABILITY) {
            return (T) radio;
        }
        return super.getCapability(capability, facing);
    }

    public Vec3 getTarget() {
        if (this._targetPos == null) {
            this._targetPos = Vec3.ZERO;
        }
        return this._targetPos;
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
        if (nbt.contains(NBTConstants.FREQUENCY)) {
            this.radio.setChannel(Integer.toString(nbt.getInteger(NBTConstants.FREQUENCY)));
        }
        initFromLoad();
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileCruiseLauncher> SAVE_LOGIC = new NbtSaveHandler<TileCruiseLauncher>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBTConstants.INVENTORY, launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radio)
        /* */.nodeVec3(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        /* */.nodeEulerAngle(NBTConstants.CURRENT_AIM, launcher -> launcher.currentAim, (launcher, pos) -> launcher.currentAim.set(pos))
        /* */.nodeINBTSerializable("firing_package", launcher -> launcher.firingPackage)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.launcher)
        .base();

    public static void register() {
        GameRegistry.registerBlockEntity(TileCruiseLauncher.class, REGISTRY_NAME);
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_DESCRIPTION = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack((t) -> t.missileHolder.getMissileStack(), (t, f) -> t.cachedMissileStack = f)
        .nodeVec3(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .nodeDouble(t -> t.currentAim.yaw(), (t, f) -> t.currentAim.setYaw(f))
        .nodeDouble(t -> t.currentAim.pitch(), (t, f) -> t.currentAim.setPitch(f));

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_HZ = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.frequency", (t) -> t.radio)
        .fromClient()
        .nodeString(RadioCruise::getChannel, RadioCruise::setChannel)
        .onFinished((r, t, p) -> r.markDirty());

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_DISABLE = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.disable", (t) -> t.radio)
        .fromClient()
        .toggleBoolean(RadioCruise::isDisabled, RadioCruise::setDisabled)
        .onFinished((r, t, p) -> r.markDirty());

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_TARGET = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .onFinished((r, t, p) -> r.markDirty());
    ;

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_GUI = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "gui")
        .fromClient()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeVec3(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget);

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_LAUNCH = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((tile, target, player) -> {
            final EntityCause cause = new EntityCause(player); // TODO note was UI interaction
            tile.getLauncher().launch(new BasicTargetData(tile.getTarget()), cause, false); // TODO send feedback to UI as part of a event list or history
            tile.markDirty();
        });
}
