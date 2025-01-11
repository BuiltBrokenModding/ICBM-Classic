package icbm.classic.content.blocks.launcher.base;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.TileReg;
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
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

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
public class TileLauncherBase extends TileMachine implements ILauncherComponent, IMachineInfo, IGuiTile, IPlayerUsing
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "launcher_base");

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    /** Toggle to check collision area above pad for missiles */
    protected boolean checkMissileCollision = true;
    /** True to note a missile is above the launcher */
    private boolean hasMissileCollision = false;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p,c,s) -> this.markDirty());

    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, (stack) -> stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY).isPresent())
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


    private LazyOptional<IEnergyStorage> lazyEnergy = LazyOptional.of(() -> energyStorage);
    private LazyOptional<IMissileHolder> lazyMissileHolder = LazyOptional.of(() -> missileHolder);
    private LazyOptional<IMissileLauncher> lazyMissileLauncher = LazyOptional.of(() -> missileLauncher);
    private LazyOptional<IItemHandler> lazyInventory = LazyOptional.of(() -> inventory);

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
    private final List<PlayerEntity> playersUsing = new LinkedList<>();

    public TileLauncherBase() {
        super(TileReg.LAUNCHER_BASE.get());
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3,true,  (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20,true,  (t) -> {
            playersUsing.removeIf((player) -> !(player.openContainer instanceof ContainerLaunchBase));
        }));
        tickActions.add(new TickAction(() -> isServer() && this.firingPackage != null, this::handleFirePackage));
        tickActions.add(new TickAction(3, true, this::updateSeat));
        tickActions.add(inventory);
    }

    @Override
    public void tick()
    {
        // whatever reason onLoad can't be used for tile checks
        if(isServer() && this.ticks == 0) {
            launcherNode.connectToTiles();
        }
        super.tick();
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
        if (state.getProperties().contains(BlockLauncherBase.FACING))
        {
            return state.get(BlockLauncherBase.FACING);
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
    public void remove()
    {
        getNetworkNode().onTileRemoved();
        super.remove();
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

        //Create seat if missile
        if (!getMissileStack().isEmpty() && seat == null)  //TODO add hook to disable riding some missiles
        {
            seat = EntityReg.HOLDER_SEAT.get().create(world);
            seat.setPosition(getPos().getX() + 0.5, getPos().getY() + 0.5, getPos().getZ() + 0.5);
            seat.setHost(this);
            world.addEntity(seat);
        }
        //Destroy seat if no missile
        else if (getMissileStack().isEmpty() && seat != null)
        {
            Optional.ofNullable(seat.getRidingEntity()).ifPresent(Entity::removePassengers);
            seat.remove();
            seat = null;
        }
    }

    @Override
    @Nullable
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing)
    {
        if(capability == CapabilityEnergy.ENERGY) {
            return lazyEnergy.cast();
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return lazyInventory.cast();
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return lazyMissileHolder.cast();
        }
        else if(capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return lazyMissileLauncher.cast();
        }
        return super.getCapability(capability, facing);
    }

    public boolean checkForMissileInBounds()
    {
        //Limit how often we check for collision
        if (checkMissileCollision)
        {
            checkMissileCollision = false;

            //Validate the space above the launcher is free of entities, mostly for smooth reload visuals
            final AxisAlignedBB collisionCheck = new AxisAlignedBB(getPos().getX(), getPos().getY(), getPos().getZ(), getPos().getX() + 1, getPos().getY() + 5, getPos().getZ() + 1); //TODO magic numbers
            final List<EntityMissile> entities = world.getEntitiesWithinAABB(EntityMissile.class, collisionCheck);
            hasMissileCollision = entities.size() > 0;
        }
        return hasMissileCollision;
    }

    /*@Override
    public ITextComponent getDisplayName()
    {
        return new TranslationTextComponent("gui.icbmclassic:launcherbase.name");
    }*/

    public ItemStack getMissileStack()
    {
        if (isClient() && cachedMissileStack != null)
        {
            return cachedMissileStack;
        }
        return missileHolder.getMissileStack();
    }

    public boolean tryInsertMissile(PlayerEntity player, Hand hand, ItemStack heldItem) // TODO consider moving to inventory code as a generic insert/extract slot logic
    {
        // Add missile
        if (this.getMissileStack().isEmpty() && missileHolder.canSupportMissile(heldItem))
        {
            if (isServer())
            {
                final ItemStack stackLeft = inventory.insertItem(0, heldItem, false);
                if (!player.isCreative())
                {
                    player.setItemStackToSlot(hand == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, stackLeft);
                    player.container.detectAndSendChanges();
                }
            }
            return true;
        }
        // Remove missile
        else if (player.isSneaking() && heldItem.isEmpty() && !this.getMissileStack().isEmpty())
        {
            if (isServer())
            {

                player.setItemStackToSlot(hand == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND, this.getMissileStack());
                inventory.extractItem(0, 1, false);
                player.container.detectAndSendChanges();
            }
            return true;
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public Object getServerGuiElement(int ID, PlayerEntity player)
    {
        //return new ContainerLaunchBase(player, this);
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, PlayerEntity player)
    {
        //return new GuiLauncherBase(player, this);
        return null;
    }

    @Override
    public void read(CompoundNBT nbt)
    {
        super.read(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        return super.write(nbt);
    }

    private static final NbtSaveHandler<TileLauncherBase> SAVE_LOGIC = new NbtSaveHandler<TileLauncherBase>()
        .mainRoot()
        /* */.nodeInteger("lock_height", TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight)
        /* */.nodeInteger("group_id", TileLauncherBase::getGroupId, TileLauncherBase::setGroupId)
        /* */.nodeInteger("group_index", TileLauncherBase::getGroupIndex, TileLauncherBase::setGroupIndex)
        /* */.nodeInteger("firing_delay", TileLauncherBase::getFiringDelay, TileLauncherBase::setFiringDelay)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("inventory", launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("firing_package", launcher -> launcher.firingPackage)
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.missileLauncher)
        /* */.nodeFacing("seat_side", TileLauncherBase::getSeatSide, TileLauncherBase::setSeatSide)
        .base();

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
        .nodeInt(TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_LOCK_HEIGHT = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "lock_height")
        .fromClient()
        .nodeInt(TileLauncherBase::getLockHeight, TileLauncherBase::setLockHeight);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_GROUP_INDEX = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "group.index")
        .fromClient()
        .nodeInt(TileLauncherBase::getGroupIndex, TileLauncherBase::setGroupIndex);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_GROUP_ID = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "group.id")
        .fromClient()
        .nodeInt(TileLauncherBase::getGroupId, TileLauncherBase::setGroupId);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_SEAT_ROTATION = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "rotation.seat")
        .fromClient()
        .nodeFacing(TileLauncherBase::getSeatSide, TileLauncherBase::setSeatSide);

    public static final PacketCodexTile<TileLauncherBase, TileLauncherBase> PACKET_FIRING_DELAY = (PacketCodexTile<TileLauncherBase, TileLauncherBase>) new PacketCodexTile<TileLauncherBase, TileLauncherBase>(REGISTRY_NAME, "firing.delay")
        .fromClient()
        .nodeInt(TileLauncherBase::getFiringDelay, TileLauncherBase::setFiringDelay);
}
