package icbm.classic.world.block.launcher.base;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
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
import icbm.classic.prefab.tile.IcbmBlockEntity;
import icbm.classic.world.IcbmBlockEntityTypes;
import icbm.classic.world.block.launcher.FiringPackage;
import icbm.classic.world.block.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.world.block.launcher.base.gui.GuiLauncherBase;
import icbm.classic.world.block.launcher.network.ILauncherComponent;
import icbm.classic.world.block.launcher.network.LauncherNode;
import icbm.classic.world.entity.PlayerSeatEntity;
import icbm.classic.world.missile.entity.EntityMissile;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.energy.CapabilityEnergy;
import net.neoforged.fml.common.registry.GameRegistry;
import net.neoforged.items.CapabilityItemHandler;
import net.neoforged.neoforge.capabilities.BaseCapability;

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
public class LauncherBaseBlockEntity extends IcbmBlockEntity implements ILauncherComponent, IMachineInfo, IGuiTile, IPlayerUsing {
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "launcherbase");

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public PlayerSeatEntity seat;

    /**
     * Toggle to check collision area above pad for missiles
     */
    protected boolean checkMissileCollision = true;
    /**
     * True to note a missile is above the launcher
     */
    private boolean hasMissileCollision = false;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p, c, s) -> this.markDirty());

    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile)
            .withInsertCheck((s) -> !this.checkForMissileInBounds())
            .withChangeCallback((stack) -> this.markDirty())
        )
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);
    public final LauncherCapability missileLauncher = new LauncherCapability(this);

    private final LauncherNode launcherNode = new LauncherNode(this, true);

    /**
     * User defined: Time in ticks to wait before firing a missile
     */
    @Getter
    @Setter
    private int firingDelay = 0;
    /**
     * User defined: Height to move before changing direction
     */
    @Getter
    @Setter
    private int lockHeight = 3;
    /**
     * User defined: Group of missiles
     */
    @Getter
    @Setter
    private int groupId = -1;
    /**
     * User defined: Index in the group, can be shared and works more like priority
     */
    @Getter
    @Setter
    private int groupIndex = -1;

    @Setter
    private Direction seatSide = null;

    @Getter
    @Setter
    private FiringPackage firingPackage;

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<Player> playersUsing = new LinkedList<>();

    public LauncherBaseBlockEntity(BlockPos pos, BlockState state) {
        super(IcbmBlockEntityTypes.LAUNCHER_BASE.get(), pos, state);
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.containerMenu instanceof ContainerLaunchBase));
        }));
        tickActions.add(new TickAction(() -> isServer() && this.firingPackage != null, this::handleFirePackage));
        tickActions.add(new TickAction(3, true, this::updateSeat));
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
        BlockState state = getBlockState();
        if (state.getProperties().containsKey(LauncherBaseBlock.ROTATION_PROP)) {
            return state.getValue(LauncherBaseBlock.ROTATION_PROP);
        }
        return Direction.UP;
    }

    public Direction getSeatSide() {
        if (seatSide == null) {
            switch (getLaunchDirection()) {
                case UP:
                    seatSide = Direction.NORTH;
                    break;
                case DOWN:
                    seatSide = Direction.NORTH;
                    break;
                case EAST:
                    seatSide = Direction.UP;
                    break;
                case WEST:
                    seatSide = Direction.UP;
                    break;
                case NORTH:
                    seatSide = Direction.UP;
                    break;
                case SOUTH:
                    seatSide = Direction.UP;
                    break;
            }
        }
        return seatSide;
    }

    public float getMissileYaw(boolean render) {
        if (render) {
            switch (getLaunchDirection()) {
                case NORTH:
                    return 0;
                case SOUTH:
                    return -180;
                case WEST:
                    return 90;
                case EAST:
                    return -90;
                default:
                    return 0;
            }
        }
        switch (getLaunchDirection()) {
            case NORTH:
                return -180;
            case SOUTH:
                return 0;
            case WEST:
                return -90;
            case EAST:
                return 90;
            default:
                return 0;
        }
    }

    public float getMissilePitch(boolean render) {
        if (render) {
            switch (getLaunchDirection()) {
                case UP:
                    return 0;
                case DOWN:
                    return -180;
                default:
                    return -90;
            }
        }
        switch (getLaunchDirection()) {
            case UP:
                return 90;
            case DOWN:
                return -90;
            default:
                return 0;
        }
    }

    public int getFiringCost() {
        return ConfigLauncher.POWER_COST;
    }

    @Override
    public void onLoad() {
        launcherNode.connectToTiles();
    }

    @Override
    public void invalidate() {
        getNetworkNode().onTileRemoved();
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    private void handleFirePackage() {
        firingPackage.setCountDown(firingPackage.getCountDown() - 1);
        if (firingPackage.getCountDown() <= 0) {
            firingPackage.launch(missileLauncher);
            firingPackage = null;
        }
    }

    private void updateSeat() {
        checkMissileCollision = true;

        //Create seat if missile
        if (!getMissileStack().isEmpty() && seat == null)  //TODO add hook to disable riding some missiles
        {
            seat = new PlayerSeatEntity(world);
            seat.setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            seat.setHost(this);
            world.spawnEntity(seat);
        }
        //Destroy seat if no missile
        else if (getMissileStack().isEmpty() && seat != null) {
            Optional.ofNullable(seat.getRidingEntity()).ifPresent(Entity::removePassengers);
            seat.setDead();
            seat = null;
        }
    }

    @Override
    public boolean hasCapability(BaseCapability<?, ?> capability, @Nullable Direction facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER
            || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(BaseCapability<T, ?> capability, @Nullable Direction facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        } else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY) {
            return (T) missileHolder;
        } else if (capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) missileLauncher;
        }
        return super.getCapability(capability, facing);
    }

    public boolean checkForMissileInBounds() {
        //Limit how often we check for collision
        if (checkMissileCollision) {
            checkMissileCollision = false;

            //Validate the space above the launcher is free of entities, mostly for smooth reload visuals
            final AxisAlignedBB collisionCheck = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 5, getPos().getZ() + 1); //TODO magic numbers
            final List<EntityMissile> entities = world.getEntitiesWithinAABB(EntityMissile.class, collisionCheck);
            hasMissileCollision = entities.size() > 0;
        }
        return hasMissileCollision;
    }

    @Override
    public Component getDisplayName() {
        return new TextComponentTranslation("gui.icbmclassic:launcherbase.name");
    }

    public ItemStack getMissileStack() {
        if (isClient() && cachedMissileStack != null) {
            return cachedMissileStack;
        }
        return missileHolder.getMissileStack();
    }

    public boolean tryInsertMissile(Player player, InteractionHand hand, ItemStack heldItem) // TODO consider moving to inventory code as a generic insert/extract slot logic
    {
        // Add missile
        if (this.getMissileStack().isEmpty() && missileHolder.canSupportMissile(heldItem)) {
            if (isServer()) {
                final ItemStack stackLeft = inventory.insertItem(0, heldItem, false);
                if (!player.capabilities.isCreativeMode) {
                    player.setItemStackToSlot(hand == InteractionHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, stackLeft);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
            return true;
        }
        // Remove missile
        else if (player.isSneaking() && heldItem.isEmpty() && !this.getMissileStack().isEmpty()) {
            if (isServer()) {

                player.setItemStackToSlot(hand == InteractionHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, this.getMissileStack());
                inventory.extractItem(0, 1, false);
                player.inventoryContainer.detectAndSendChanges();
            }
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public Object getServerGuiElement(int ID, Player player) {
        return new ContainerLaunchBase(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, Player player) {
        return new GuiLauncherBase(player, this);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public CompoundTag writeToNBT(CompoundTag nbt) {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<LauncherBaseBlockEntity> SAVE_LOGIC = new NbtSaveHandler<LauncherBaseBlockEntity>()
        .mainRoot()
        /* */.nodeInteger("lock_height", LauncherBaseBlockEntity::getLockHeight, LauncherBaseBlockEntity::setLockHeight)
        /* */.nodeInteger("group_id", LauncherBaseBlockEntity::getGroupId, LauncherBaseBlockEntity::setGroupId)
        /* */.nodeInteger("group_index", LauncherBaseBlockEntity::getGroupIndex, LauncherBaseBlockEntity::setGroupIndex)
        /* */.nodeInteger("firing_delay", LauncherBaseBlockEntity::getFiringDelay, LauncherBaseBlockEntity::setFiringDelay)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("inventory", launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("firing_package", launcher -> launcher.firingPackage)
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.missileLauncher)
        /* */.nodeFacing("seat_side", LauncherBaseBlockEntity::getSeatSide, LauncherBaseBlockEntity::setSeatSide)
        .base();

    public static void register() {
        GameRegistry.registerBlockEntity(LauncherBaseBlockEntity.class, REGISTRY_NAME);
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_GUI, PACKET_LOCK_HEIGHT, PACKET_GROUP_ID, PACKET_GROUP_INDEX, PACKET_FIRING_DELAY, PACKET_SEAT_ROTATION);
    }

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_DESCRIPTION = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack(LauncherBaseBlockEntity::getMissileStack, (t, f) -> t.cachedMissileStack = f)
        .nodeFacing(LauncherBaseBlockEntity::getSeatSide, LauncherBaseBlockEntity::setSeatSide);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_GUI = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "gui")
        .fromServer()
        .nodeInt(LauncherBaseBlockEntity::getGroupIndex, LauncherBaseBlockEntity::setGroupIndex)
        .nodeInt(LauncherBaseBlockEntity::getGroupId, LauncherBaseBlockEntity::setGroupId)
        .nodeInt(LauncherBaseBlockEntity::getFiringDelay, LauncherBaseBlockEntity::setFiringDelay)
        .nodeInt(LauncherBaseBlockEntity::getLockHeight, LauncherBaseBlockEntity::setLockHeight);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_LOCK_HEIGHT = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "lock_height")
        .fromClient()
        .nodeInt(LauncherBaseBlockEntity::getLockHeight, LauncherBaseBlockEntity::setLockHeight);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_GROUP_INDEX = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "group.index")
        .fromClient()
        .nodeInt(LauncherBaseBlockEntity::getGroupIndex, LauncherBaseBlockEntity::setGroupIndex);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_GROUP_ID = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "group.id")
        .fromClient()
        .nodeInt(LauncherBaseBlockEntity::getGroupId, LauncherBaseBlockEntity::setGroupId);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_SEAT_ROTATION = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "rotation.seat")
        .fromClient()
        .nodeFacing(LauncherBaseBlockEntity::getSeatSide, LauncherBaseBlockEntity::setSeatSide);

    public static final PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity> PACKET_FIRING_DELAY = (PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>) new PacketCodexTile<LauncherBaseBlockEntity, LauncherBaseBlockEntity>(REGISTRY_NAME, "firing.delay")
        .fromClient()
        .nodeInt(LauncherBaseBlockEntity::getFiringDelay, LauncherBaseBlockEntity::setFiringDelay);
}
