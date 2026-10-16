package icbm.classic.content.blocks.launcher.base;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.capability.holder.ICapabilityHolderTile;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.lib.tile.TickDoOnce;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TileMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * This tile entity is for the base of the missile launcher
 *
 * @author Calclavia, DarkGuardsman
 */
public class TileLauncherBase extends TileMachine implements ILauncherComponent, IMachineInfo, IGuiTile, IPlayerUsing, ICapabilityHolderTile
{
    public static final ResourceLocation REGISTRY_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "launcherbase");

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    /** Toggle to check collision area above pad for missiles */
    protected boolean checkMissileCollision = true;
    /** True to note a missile is above the launcher */
    private boolean hasMissileCollision = false;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p,c,s) -> this.setChanged());

    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> setChanged())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile)
            .withInsertCheck((s) -> !this.checkForMissileInBounds())
            .withChangeCallback((stack) -> this.setChanged())
        )
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);
    public final LauncherCapability missileLauncher = new LauncherCapability(this);

    private final LauncherNode launcherNode = new LauncherNode(this, true);

    /** User defined: Time in ticks to wait before firing a missile */
    @Getter @Setter
    private int firingDelay = 0;
    /** User defined: Height to move before changing direction */
    @Getter @Setter
    private int lockHeight = 3;
    /** User defined: Group of missiles */
    @Getter @Setter
    private int groupId = -1;
    /** User defined: Index in the group, can be shared and works more like priority */
    @Getter @Setter
    private int groupIndex = -1;

    @Setter
    private Direction seatSide = null;

    @Getter @Setter
    private FiringPackage firingPackage;

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public TileLauncherBase(BlockPos pos, BlockState state) {
        super(BlockReg.TILE_LAUNCHER_BASE.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3,true,  (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20,true,  (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerLaunchBase));
        }));
        tickActions.add(new TickAction(() -> isServer() && this.firingPackage != null, this::handleFirePackage));
        tickActions.add(new TickAction(3, true, this::updateSeat));
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
        consumer.accept(NEEDS_POWER, ConfigMain.REQUIRES_POWER);
        consumer.accept(ENERGY_COST_ACTION, getFiringCost());
        consumer.accept("MAX_RANGE", ConfigLauncher.RANGE); //TODO min range
        consumer.accept("INACCURACY_BASE", ConfigLauncher.MIN_INACCURACY);
        consumer.accept("INACCURACY_RANGE", ConfigLauncher.SCALED_INACCURACY_DISTANCE);
        consumer.accept("INACCURACY_LAUNCHERS", ConfigLauncher.SCALED_INACCURACY_LAUNCHERS);
    }


    /**
     * Direction the launcher is facing to deploy missiles
     *
     * @return direction
     */
    public Direction getLaunchDirection() {
        final BlockState state = getBlockState();
        if (state.hasProperty(BlockLauncherBase.ROTATION_PROP))
        {
            return state.getValue(BlockLauncherBase.ROTATION_PROP);
        }
        return Direction.UP;
    }

    public Direction getSeatSide() {
        if(seatSide == null) {
            switch (getLaunchDirection()) {
                case UP: seatSide = Direction.NORTH; break;
                case DOWN: seatSide = Direction.NORTH; break;
                case EAST: seatSide = Direction.UP; break;
                case WEST: seatSide = Direction.UP; break;
                case NORTH: seatSide = Direction.UP; break;
                case SOUTH: seatSide = Direction.UP; break;
            }
        }
        return seatSide;
    }

    public float getMissileYaw(boolean render) {
        if(render) {
            switch (getLaunchDirection()) {
                case NORTH: return 0;
                case SOUTH: return -180;
                case WEST: return 90;
                case EAST: return -90;
                default: return 0;
            }
        }
        switch (getLaunchDirection()) {
            case NORTH: return -180;
            case SOUTH: return 0;
            case WEST: return -90;
            case EAST: return 90;
            default: return 0;
        }
    }

    public float getMissilePitch(boolean render) {
        if(render) {
            switch (getLaunchDirection()) {
                case UP: return 0;
                case DOWN: return -180;
                default: return -90;
            }
        }
        switch (getLaunchDirection()) {
            case UP: return 90;
            case DOWN: return -90;
            default: return 0;
        }
    }

    public int getFiringCost() {
        return ConfigLauncher.POWER_COST;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        launcherNode.connectToTiles();
    }

    @Override
    public void setRemoved()
    {
        getNetworkNode().onTileRemoved();
        super.setRemoved();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    private void handleFirePackage() {
        firingPackage.setCountDown(firingPackage.getCountDown() - 1);
        if(firingPackage.getCountDown() <= 0) {
            firingPackage.launch(missileLauncher);
            firingPackage = null;
        }
    }

    private void updateSeat() {
        checkMissileCollision = true;

        // PORT: forget a seat that was removed (discarded or unloaded with its chunk) so a new one can be created;
        // a saved seat that loads later yields to the new one (see EntityPlayerSeat#tick)
        if (seat != null && seat.isRemoved()) {
            seat = null;
        }

        //Create seat if missile
        if (!getMissileStack().isEmpty() && seat == null)  //TODO add hook to disable riding some missiles
        {
            seat = new EntityPlayerSeat(level);
            seat.setPos(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5);
            seat.setHost(this);
            level.addFreshEntity(seat);
        }
        //Destroy seat if no missile
        else if (getMissileStack().isEmpty() && seat != null)
        {
            Optional.ofNullable(seat.getVehicle()).ifPresent(Entity::ejectPassengers);
            seat.discard();
            seat = null;
        }
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
            return (T) missileLauncher;
        }
        return null;
    }

    public boolean checkForMissileInBounds()
    {
        //Limit how often we check for collision
        if (checkMissileCollision && level != null)
        {
            checkMissileCollision = false;

            //Validate the space above the launcher is free of entities, mostly for smooth reload visuals
            final BlockPos pos = getBlockPos();
            final AABB collisionCheck = new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 5, pos.getZ() + 1); //TODO magic numbers
            final List<EntityMissile> entities = level.getEntitiesOfClass(EntityMissile.class, collisionCheck);
            hasMissileCollision = entities.size() > 0;
        }
        return hasMissileCollision;
    }

    @Override
    public Component getDisplayName()
    {
        return Component.translatable("gui.icbmclassic:launcherbase.name");
    }

    public ItemStack getMissileStack()
    {
        if (isClient() && cachedMissileStack != null)
        {
            return cachedMissileStack;
        }
        return missileHolder.getMissileStack();
    }

    public boolean tryInsertMissile(Player player, InteractionHand hand, ItemStack heldItem) // TODO consider moving to inventory code as a generic insert/extract slot logic
    {
        // Add missile
        if (this.getMissileStack().isEmpty() && missileHolder.canSupportMissile(heldItem))
        {
            if (isServer())
            {
                final ItemStack stackLeft = inventory.insertItem(0, heldItem, false);
                if (!player.getAbilities().instabuild)
                {
                    player.setItemInHand(hand, stackLeft);
                    player.inventoryMenu.broadcastChanges();
                }
            }
            return true;
        }
        // Remove missile
        else if (player.isShiftKeyDown() && heldItem.isEmpty() && !this.getMissileStack().isEmpty())
        {
            if (isServer())
            {
                player.setItemInHand(hand, this.getMissileStack());
                inventory.extractItem(0, 1, false);
                player.inventoryMenu.broadcastChanges();
            }
            return true;
        }
        return false;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory playerInventory, Player player)
    {
        return new ContainerLaunchBase(windowId, player, this);
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        super.loadAdditional(nbt, registries);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries)
    {
        SAVE_LOGIC.save(this, nbt);
        super.saveAdditional(nbt, registries);
    }

    private static final NbtSaveHandler<TileLauncherBase> SAVE_LOGIC = new NbtSaveHandler<TileLauncherBase>()
        .mainRoot()
        /* */.nodeInteger("lock_height", TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight)
        /* */.nodeInteger("group_id", TileLauncherBase::getGroupId, TileLauncherBase::setGroupId)
        /* */.nodeInteger("group_index", TileLauncherBase::getGroupIndex, TileLauncherBase::setGroupIndex)
        /* */.nodeInteger("firing_delay", TileLauncherBase::getFiringDelay, TileLauncherBase::setFiringDelay)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("inventory", launcher -> launcher.inventory)
        // PORT: 1.12 used the accessor-only node, which never restored the package as the field is null on load
        /* */.nodeINBTSerializable("firing_package", TileLauncherBase::getFiringPackage, (launcher, firingPackage) -> {
            // Client gets this via the update tag but never ticks it, don't keep a stale copy
            if (launcher.getLevel() == null || !launcher.getLevel().isClientSide()) {
                launcher.setFiringPackage(firingPackage);
            }
        }, () -> new FiringPackage(null, null))
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.missileLauncher)
        /* */.nodeFacing("seat_side", TileLauncherBase::getSeatSide, TileLauncherBase::setSeatSide)
        .base();

    /** Registers packet codexes, block entity type lives in BlockReg */
    public static void register() {
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_GUI, PACKET_LOCK_HEIGHT, PACKET_GROUP_ID, PACKET_GROUP_INDEX, PACKET_FIRING_DELAY, PACKET_SEAT_ROTATION);
    }

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_DESCRIPTION = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack(TileLauncherBase::getMissileStack, (t, f) -> t.cachedMissileStack = f)
        .nodeFacing(TileLauncherBase::getSeatSide, TileLauncherBase::setSeatSide);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_GUI = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt(TileLauncherBase::getGroupIndex, TileLauncherBase::setGroupIndex)
        .nodeInt(TileLauncherBase::getGroupId, TileLauncherBase::setGroupId)
        .nodeInt(TileLauncherBase::getFiringDelay, TileLauncherBase::setFiringDelay)
        .nodeInt(TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight)
        // PORT: 1.12 never synced energy for the GUI, so the energy bar only showed the value from the chunk load
        .nodeInt(t -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i));

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_LOCK_HEIGHT = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "lock_height")
        .fromClient()
        .nodeInt(TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight)
        .onFinished((tile, target, player) -> tile.setChanged());

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_GROUP_INDEX = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "group.index")
        .fromClient()
        .nodeInt(TileLauncherBase::getGroupIndex, TileLauncherBase::setGroupIndex)
        .onFinished((tile, target, player) -> tile.setChanged());

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_GROUP_ID = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "group.id")
        .fromClient()
        .nodeInt(TileLauncherBase::getGroupId, TileLauncherBase::setGroupId)
        .onFinished((tile, target, player) -> tile.setChanged());

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_SEAT_ROTATION = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "rotation.seat")
        .fromClient()
        .nodeFacing(TileLauncherBase::getSeatSide, TileLauncherBase::setSeatSide)
        .onFinished((tile, target, player) -> tile.setChanged());

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_FIRING_DELAY = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "firing.delay")
        .fromClient()
        .nodeInt(TileLauncherBase::getFiringDelay, TileLauncherBase::setFiringDelay)
        .onFinished((tile, target, player) -> tile.setChanged());
}
// PORTED
