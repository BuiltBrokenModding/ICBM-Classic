package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.launcher.ILauncherSolution;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.LauncherSolution;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherEntry;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.blocks.launcher.screen.gui.ContainerLaunchScreen;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TileMachine;
import lombok.Getter;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.common.NeoForge;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileMachine implements ILauncherComponent, IMachineInfo, IPlayerUsing, IGuiTile, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "launcherscreen");

    /** Target position of the launcher */
    private Vec3 _targetPos = Vec3.ZERO;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p,c,s) -> this.setChanged());
    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> setChanged())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));
    private final LauncherNode launcherNode = new LauncherNode(this, false);
    public final RadioScreen radioCap = new RadioScreen(this);

    @Getter
    private float launcherInaccuracy = 0;

    private final List<LauncherPair> statusList = new ArrayList<>();
    private boolean refreshStatus = false;

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileLauncherScreen(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_LAUNCHER_SCREEN.get(), pos, state);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20,true,  (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerLaunchScreen));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER);
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        super.update();

        if (isServer())
        {
            if(ticks % 5 == 0 || refreshStatus) {
                refreshStatus = false;
                statusList.clear();

                final List<LauncherEntry> launchers = getLaunchersInGroup();
                if(!launchers.isEmpty()) {
                    final int launcherCount = launchers.size();
                    launcherInaccuracy = launchers.stream().map(LauncherEntry::getLauncher).map(l -> {

                        // Collect status
                        final IActionStatus status = preCheck(l);
                        statusList.add(new LauncherPair(l.getLauncherGroup(), l.getLaunchIndex(), status));

                        // Get accuracy for compare
                        return l.getInaccuracy(getTarget(), launcherCount);

                    }).max(Float::compareTo).orElse(0f);
                }
            }

            //Only launch if redstone
            if (ticks % 3 == 0 && level.getDirectSignalTo(getBlockPos()) > 0) //TODO check for pulse instead of high only & add delay
            {
                fireAllLaunchers(false);
            }
        }
    }

    @Nonnull
    public List<LauncherEntry> getLaunchersInGroup() {
        if(getNetworkNode().getNetwork() != null) {
            return getNetworkNode().getNetwork().getLaunchers(getFiringGroup());
        }
        return Collections.emptyList();
    }

    public int getFiringGroup() {
        return -1;
    }

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_GUI = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .nodeString(t -> t.radioCap.getChannel(), (t, f) -> t.radioCap.setChannel(f))
        .nodeBoolean(t -> t.radioCap.isDisabled(), (t, f) -> t.radioCap.setDisabled(f))
        .nodeFloat(t -> t.launcherInaccuracy, (t, f) -> t.launcherInaccuracy = f)
        // PORT: 1.12 never synced energy for the GUI, so the energy bar only showed the value from the chunk load
        .nodeInt(t -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .nodeNbtCompound(t -> {
            final CompoundTag tag = new CompoundTag(); //TODO find a way to send bytes
            final ListTag list = new ListTag();
            t.statusList.forEach((pair) -> {
                final CompoundTag save = new CompoundTag();
                save.putInt("g", pair.getGroup());
                save.putInt("i", pair.getIndex());
                save.put("p", ICBMClassicAPI.ACTION_STATUS_REGISTRY.save(pair.getStatus()));
                list.add(save);
            });
            tag.put("p", list);
            return tag;
        }, (t, tag) -> {
            final ListTag list = tag.getList("p", 10);
            final List<LauncherPair> status = new ArrayList<>(list.size());
            for(int i = 0; i < list.size(); i++) {
                final CompoundTag save = (CompoundTag) list.get(i);
                final int group = save.getInt("g");
                final int index = save.getInt("i");
                final CompoundTag partSave = save.getCompound("p");
                final IActionStatus part = ICBMClassicAPI.ACTION_STATUS_REGISTRY.load(partSave);
                if(part != null) {
                    status.add(new LauncherPair(group, index, part));
                }
            }

            t.statusList.clear();
            t.statusList.addAll(status);
        });

    public static final PacketCodexTile<TileLauncherScreen, RadioScreen> PACKET_RADIO_HZ = (PacketCodexTile<TileLauncherScreen, RadioScreen>) new PacketCodexTile<TileLauncherScreen, RadioScreen>(REGISTRY_NAME, "radio.frequency", (t) -> t.radioCap)
        .fromClient()
        .nodeString(RadioScreen::getChannel, RadioScreen::setChannel)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, RadioScreen> PACKET_RADIO_DISABLE = (PacketCodexTile<TileLauncherScreen, RadioScreen>) new PacketCodexTile<TileLauncherScreen, RadioScreen>(REGISTRY_NAME, "radio.disable", (t) -> t.radioCap)
        .fromClient()
        .toggleBoolean(RadioScreen::isDisabled, RadioScreen::setDisabled)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_TARGET = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3d(TileLauncherScreen::getTarget, TileLauncherScreen::setTarget)
        .onFinished((r, t, p) -> r.setChanged());

    public static final PacketCodexTile<TileLauncherScreen, TileLauncherScreen> PACKET_LAUNCH = (PacketCodexTile<TileLauncherScreen, TileLauncherScreen>) new PacketCodexTile<TileLauncherScreen, TileLauncherScreen>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((r, t, p) -> {
            r.fireAllLaunchers(false);
            r.setChanged();
        });

    /**
     * Pre-check of the launch process
     *
     * @param launcher to use
     * @return status
     */
    public IActionStatus preCheck(IMissileLauncher launcher)
    {
        return launcher.preCheckLaunch(new BasicTargetData(this.getTarget()), createCause());
    }

    private IActionCause createCause() {
        return new BlockScreenCause(level, getBlockPos(), getBlockState()); //TODO cache?
    }

    public boolean fireAllLaunchers(boolean simulate) {
        refreshStatus = true;

        if(getNetworkNode().getNetwork() == null) {
            return false; //TODO return error status
        }

        final List<LauncherEntry> launchers = getLaunchersInGroup();

        final IActionCause cause = createCause();
        final IMissileTarget target = new BasicTargetData(getTarget());
        final ILauncherSolution solution = new LauncherSolution(target, getFiringGroup(), launchers.size());
        return getNetworkNode().getNetwork()
            .launch(solution, cause, simulate)
            .filter(entry -> !entry.getLastFiringStatus().isBlocking())
            // count is required, as anyMatch() or similar will short-circuit before running all
            .count() > 0;
    }

    /**
     * Client side check for canLaunch
     *
     * @return true if current status list contains no blocking
     */
    public boolean canLaunch() {
        return !statusList.isEmpty() && statusList.stream().map(LauncherPair::getStatus).noneMatch(IActionStatus::isBlocking);
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    public Component getStatusTranslation()
    {
        // Network isn't setup
        if (getNetworkNode().getNetwork() == null)
        {
            return LauncherLangs.TRANSLATION_ERROR_NO_NETWORK;
        }
        // No launcher is connected yet
        else if(getLaunchersInGroup().isEmpty()) {
            return LauncherLangs.TRANSLATION_ERROR_NO_LAUNCHER;
        }
        // Generally only fails client side when status list is missing
        else if(statusList.isEmpty()) {
            return LauncherLangs.TRANSLATION_ERROR_NO_NETWORK_STATUS;
        }
        final List<LauncherPair> errors = statusList.stream().filter(pair -> pair.getStatus().isBlocking()).collect(Collectors.toList());
        if(errors.isEmpty()) {
            return LauncherLangs.TRANSLATION_READY;
        }
        final MutableComponent status = Component.translatable(LauncherLangs.ERROR_MISSILE_MULTI, errors.size(), statusList.size());
        for(int i = 0; i < errors.size() && i < 5; i++) {
            status.append(" \n \t ");
            status.append(errors.get(i).getStatus().message());
        }
        return status;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.launcherscreen.name");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerLaunchScreen(windowId, player, this);
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        getNetworkNode().connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void setRemoved()
    {
        if(isServer()) {
            RadioRegistry.remove(radioCap);
        }
        getNetworkNode().onTileRemoved();
        super.setRemoved();
    }

    public Vec3 getTarget()
    {
        return this._targetPos;
    }

    /**
     * Called to set the target
     *
     * @param target
     */
    public void setTarget(Vec3 target)
    {
        if(target != this._targetPos) {

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
        }
    }

    @Override
    @Nullable
    public <T> T getCapability(BlockCapability<T, Direction> capability, @Nullable Direction facing)
    {
        //TODO add IEnergyStorage wrapper to ensure this tile and the network both get power
        if(capability == ICBMClassicAPI.RADIO_CAPABILITY) {
            return (T) radioCap;
        }
        else if (getNetworkNode().getNetwork() != null)
        {
            return getNetworkNode().getNetwork().getCapability(capability, facing);
        }
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        SAVE_LOGIC.load(this, nbt);

        // Legacy handling TODO data fixer
        if(nbt.contains(NBTConstants.FREQUENCY)) {
            this.radioCap.setChannel(Integer.toString(nbt.getInt(NBTConstants.FREQUENCY)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        SAVE_LOGIC.save(this, nbt);
        super.saveAdditional(nbt, registries);
    }

    private static final NbtSaveHandler<TileLauncherScreen> SAVE_LOGIC = new NbtSaveHandler<TileLauncherScreen>()
        .mainRoot()
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radioCap)
        /* */.nodeINBTSerializable("inventory", launcher -> launcher.inventory)
        // PORT: 1.12 never saved the screen's own buffer, so energy discharged from its battery slot was lost on reload
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        .base();
}
// PORTED
