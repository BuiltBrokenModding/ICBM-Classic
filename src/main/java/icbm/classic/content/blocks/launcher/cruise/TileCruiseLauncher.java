package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.source.cause.RedstoneCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
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
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TileMachine;
import lombok.Getter;
import lombok.Setter;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class TileCruiseLauncher extends TileMachine implements IGuiTile, ILauncherComponent, IMachineInfo, IPlayerUsing, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "cruiselauncher"); // PORT: 1.12 "cruiseLauncher", resource paths must be lower case

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    public static final double MISSILE__HOLDER_Y = 2.0;

    /** Target position of the launcher */
    private Vec3 _targetPos = Vec3.ZERO;

    /** Desired aim angle, updated every tick if target != null */
    @Getter
    protected final EulerAngle aim = new EulerAngle(0, 0, 0); //TODO change UI to only have yaw and pitch, drop xyz but still allow tools to auto fill from xyz

    /** Current aim angle, updated each tick */
    @Getter
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();

    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p,c,s) -> this.setChanged());
    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> setChanged())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile).withChangeCallback((stack) -> this.setChanged()))
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    @Getter
    protected final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);

    @Getter
    protected final CLauncherCapability launcher = new CLauncherCapability(this);

    @Getter @Setter
    protected FiringPackage firingPackage;

    private final LauncherNode launcherNode = new LauncherNode(this, true);
    public final RadioCruise radio = new RadioCruise(this);

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileCruiseLauncher(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_CRUISE_LAUNCHER.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerCruiseLauncher));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void setChanged()
    {
        super.setChanged();
        if(isServer()) {
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
    public void onLoad()
    {
        super.onLoad();
        launcherNode.connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radio);
        }
    }

    @Override
    public void setRemoved()
    {
        if (isServer()) {
            RadioRegistry.remove(radio);
        }
        // PORT: 1.21 calls setRemoved on chunk unload too, leave the launcher network like the other components
        //  so a stale node (and its inventory/launcher entry) isn't left behind
        getNetworkNode().onTileRemoved();
        super.setRemoved();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        super.update();

        //TODO add a per tick energy consumption or at least while aiming

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        if(isServer()) {

            // Update current aim
            // PORT: keep the turret synced/saved while it rotates (no longer relies on the per tick inventory setChanged)
            final boolean wasAimed = isAimed();
            currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();
            if (!wasAimed) {
                setChanged();
            }

            // Check redstone
            if (this.ticks % REDSTONE_CHECK_RATE == 0) {
                for (Direction side : Direction.values()) {
                    final int power = level.getSignal(getBlockPos().relative(side), side);
                    if (power > 1) {
                        firingPackage = new FiringPackage(new BasicTargetData(getTarget()), new RedstoneCause(level, getBlockPos(), getBlockState(), side), 0);
                    }
                }
            }

            if(firingPackage != null && isAimed()) {
                firingPackage.setCountDown(firingPackage.getCountDown() - 1);
                if(firingPackage.getCountDown() <= 0) {
                    firingPackage.launch(launcher);
                    firingPackage = null;
                }
            }
        }
    }


    public void setTarget(Vec3 target)
    {
        if(!Objects.equals(target, this._targetPos)) {

            // Only fire packet server side to avoid description packet triggering events
            if(isServer()) {
                final LauncherSetTargetEvent event = new LauncherSetTargetEvent(level, getBlockPos(), target);

                if (!NeoForge.EVENT_BUS.post(event).isCanceled()) {
                    this._targetPos = event.target == null ? Vec3.ZERO : event.target;
                    this.setChanged();
                }
            }
            else {
                this._targetPos = target;
            }
            updateAimAngle();
        }
    }

    public boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle()
    {
        if (hasTarget())
        {
            final Vec3 aimPoint = getTarget();
            final Pos center = new Pos(this).add(0.5, MISSILE__HOLDER_Y, 0.5);
            aim.set(center.toEulerAngle(aimPoint).clampTo360());
            aim.setYaw(EulerAngle.clampPos360(aim.yaw()));
        }
        else
        {
            aim.set(0, 0, 0);
        }
    }

    protected void initFromLoad()
    {
        cachedMissileStack = inventory.getStackInSlot(0);
        updateAimAngle();
        currentAim.set(aim);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries)
    {
        super.onDataPacket(net, pkt, registries);
        initFromLoad();
    }

    protected boolean hasTarget()
    {
        return getTarget() != null && getTarget() != Vec3.ZERO;
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        // TODO use raytrace to detect collision so we can fire out of holes
         for (int y = 1; y <= 2; y++) {
             for (int x = -1; x < 2; x++) {
                 for (int z = -1; z < 2; z++) {
                     final BlockPos pos = getBlockPos().offset(x, y, z);
                     final BlockState state = level.getBlockState(pos);
                     if (!state.isAir()) {
                         final VoxelShape box = state.getCollisionShape(level, pos);
                         return box.isEmpty() || box.max(Direction.Axis.Y) < 0.5;
                     }
                 }
             }
         }
        return true;
    }

    // Is the target too close?
    public boolean isTooClose(Vec3 target)
    {
        return new Pos(getBlockPos()).add(0.5).distance(target) < 20; //TODO remove pos usage
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.launcher.cruise.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerCruiseLauncher(windowId, player, this);
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        if(capability == Capabilities.EnergyStorage.BLOCK) {
            return ConfigMain.REQUIRES_POWER ? (T) energyStorage : null;
        }
        else if (capability == Capabilities.ItemHandler.BLOCK)
        {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return (T) missileHolder;
        }
        else if(capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) launcher;
        }
        else if(capability == ICBMClassicAPI.RADIO_CAPABILITY)
        {
            return (T) radio;
        }
        return null;
    }

    public Vec3 getTarget()
    {
        if (this._targetPos == null)
        {
            this._targetPos = Vec3.ZERO;
        }
        return this._targetPos;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        SAVE_LOGIC.load(this, nbt);
        if(nbt.contains(NBTConstants.FREQUENCY)) {
            this.radio.setChannel(Integer.toString(nbt.getInt(NBTConstants.FREQUENCY)));
        }
        initFromLoad();
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        SAVE_LOGIC.save(this, nbt);
        super.saveAdditional(nbt, registries);
    }

    private static final NbtSaveHandler<TileCruiseLauncher> SAVE_LOGIC = new NbtSaveHandler<TileCruiseLauncher>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBTConstants.INVENTORY, launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radio)
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        /* */.nodeEulerAngle(NBTConstants.CURRENT_AIM, launcher -> launcher.currentAim, (launcher, pos) -> launcher.currentAim.set(pos))
        // PORT: 1.12 used the accessor-only node, which never restored the package as the field is null on load
        /* */.nodeINBTSerializable("firing_package", TileCruiseLauncher::getFiringPackage, (launcher, firingPackage) -> {
            // Client gets this via the update tag but never ticks it, don't keep a stale copy that affects the GUI status
            if (launcher.getLevel() == null || !launcher.getLevel().isClientSide()) {
                launcher.firingPackage = firingPackage;
            }
        }, () -> new FiringPackage(null, null))
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.launcher)
        .base();

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_DESCRIPTION = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack((t) -> t.missileHolder.getMissileStack(), (t, f) -> t.cachedMissileStack = f)
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .nodeDouble(t -> t.currentAim.yaw(), (t, f) -> t.currentAim.setYaw(f))
        .nodeDouble(t -> t.currentAim.pitch(), (t, f) -> t.currentAim.setPitch(f));

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_HZ = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.frequency", (t) -> t.radio)
        .fromClient()
        .nodeString(RadioCruise::getChannel, RadioCruise::setChannel)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_DISABLE = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.disable", (t) -> t.radio)
        .fromClient()
        .toggleBoolean(RadioCruise::isDisabled, RadioCruise::setDisabled)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_TARGET = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .onFinished((r, t, p) -> r.setChanged());

    // PORT: 1.12 marked this server to client GUI sync as fromClient(); with the port's direction check that let a
    //  client with the GUI open push energy/radio/target values to the server
    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_GUI = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget);

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_LAUNCH = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((tile, target, player) -> {
            final EntityCause cause = new EntityCause(player); // TODO note was UI interaction
            tile.getLauncher().launch(new BasicTargetData(tile.getTarget()), cause, false); // TODO send feedback to UI as part of a event list or history
            tile.setChanged();
        });
}
// PORTED
