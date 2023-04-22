package icbm.classic.content.blocks.launcher.base;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.content.blocks.launcher.base.gui.ContainerLaunchBase;
import icbm.classic.content.blocks.launcher.base.gui.GuiLauncherBase;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

/**
 * This tile entity is for the base of the missile launcher
 *
 * @author Calclavia, DarkGuardsman
 */
public class TileLauncherBase extends TilePoweredMachine implements ILauncherComponent //TODO move to cap
{

    public static final int PACKET_LOCK_HEIGHT = 0;
    public static final int PACKET_GROUP_ID = 1;
    public static final int PACKET_GROUP_INDEX = 2;
    public static final int PACKET_GUI = 3;

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    /** Toggle to check collision area above pad for missiles */
    protected boolean checkMissileCollision = true;
    /** True to note a missile is above the launcher */
    private boolean hasMissileCollision = false;

    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile)
            .withInsertCheck((s) -> !this.checkForMissileInBounds())
            .withChangeCallback((stack) -> this.sendDescPacket())
        )
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this::dischargeItem));

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);
    public final IMissileLauncher missileLauncher = new LauncherCapability(this);

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

    @Getter @Setter
    private FiringPackage firingPackage;

    @Override
    public void onLoad()
    {
        launcherNode.connectToTiles();
    }

    @Override
    public void invalidate()
    {
        getNetworkNode().onTileRemoved();
        super.invalidate();
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
            // Handle firing delay
            if(firingPackage != null) {
                firingPackage.setCountDown(firingPackage.getCountDown() - 1);
                if(firingPackage.getCountDown() <= 0) {
                    firingPackage.launch(missileLauncher);
                    firingPackage = null;
                }
            }

            if (ticks % 3 == 0)
            {
                checkMissileCollision = true;

                //Update seat position
                Optional.ofNullable(seat).ifPresent(seat -> seat.setPosition(x() + 0.5, y() + 0.5, z() + 0.5));

                //Create seat if missile
                if (!getMissileStack().isEmpty() && seat == null)  //TODO add hook to disable riding some missiles
                {
                    seat = new EntityPlayerSeat(world);
                    seat.host = this;
                    seat.rideOffset = new Pos(getRotation()).multiply(0.5, 1, 0.5);
                    seat.setPosition(x() + 0.5, y() + 0.5, z() + 0.5);
                    seat.setSize(0.5f, 2.5f);
                    world.spawnEntity(seat);
                }
                //Destroy seat if no missile
                else if (getMissileStack().isEmpty() && seat != null)
                {
                    Optional.ofNullable(seat.getRidingEntity()).ifPresent(Entity::removePassengers);
                    seat.setDead();
                    seat = null;
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return (T) missileHolder;
        }
        else if(capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) missileLauncher;
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
            final AxisAlignedBB collisionCheck = new AxisAlignedBB(xi(), yi(), zi(), xi() + 1, yi() + 5, zi() + 1);
            final List<EntityMissile> entities = world.getEntitiesWithinAABB(EntityMissile.class, collisionCheck);
            hasMissileCollision = entities.size() > 0;
        }
        return hasMissileCollision;
    }

    @Override
    public ITextComponent getDisplayName()
    {
        return new TextComponentTranslation("gui.icbmclassic:launcherbase.name");
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        ByteBufUtils.writeItemStack(buf, getMissileStack());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        cachedMissileStack = ByteBufUtils.readItemStack(buf);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (!super.read(data, id, player, packet))
        {
            switch (id)
            {
                case PACKET_GUI:
                {
                    lockHeight = data.readInt();
                    groupIndex = data.readInt();
                    groupId = data.readInt();
                    return true;
                }
                case PACKET_LOCK_HEIGHT:
                {
                    lockHeight = data.readInt();
                    return true;
                }
                case PACKET_GROUP_INDEX:
                {
                    groupIndex = data.readInt();
                    return true;
                }
                case PACKET_GROUP_ID:
                {
                    groupId = data.readInt();
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    protected PacketTile getGUIPacket()
    {
        PacketTile packetTile = new PacketTile("gui", PACKET_GUI, this);
        packetTile.addData(lockHeight);
        packetTile.addData(groupIndex);
        packetTile.addData(groupId);
        return packetTile;
    }

    public void sendLockHeightPacket(int lockHeight) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("lockHeight_C>S", PACKET_LOCK_HEIGHT, this).addData(lockHeight));
        }
    }

    public void sendGroupIdPacket(int groupId) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("groupId_C>S", PACKET_GROUP_ID, this).addData(groupId));
        }
    }

    public void sendGroupIndexPacket(int groupIndex) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("groupIndex_C>S", PACKET_GROUP_INDEX, this).addData(groupIndex));
        }
    }

    public ItemStack getMissileStack()
    {
        if (isClient() && cachedMissileStack != null)
        {
            return cachedMissileStack;
        }
        return missileHolder.getMissileStack();
    }

    public boolean tryInsertMissile(EntityPlayer player, EnumHand hand, ItemStack heldItem)
    {
        if (this.getMissileStack().isEmpty() && missileHolder.canSupportMissile(heldItem))
        {
            if (isServer())
            {
                final ItemStack stackLeft = inventory.insertItem(0, heldItem, false);
                if (!player.capabilities.isCreativeMode)
                {
                    player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, stackLeft);
                    player.inventoryContainer.detectAndSendChanges();
                }
            }
            return true;
        }
        else if (player.isSneaking() && heldItem.isEmpty() && !this.getMissileStack().isEmpty())
        {
            if (isServer())
            {

                player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, this.getMissileStack());
                inventory.extractItem(0, 1, false);
                player.inventoryContainer.detectAndSendChanges();
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
    public int getEnergyConsumption()
    {
        return ConfigLauncher.POWER_COST;
    }

    @Override
    public int getEnergyBufferSize()
    {
        return ConfigLauncher.POWER_CAPACITY;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerLaunchBase(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiLauncherBase(player, this);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileLauncherBase> SAVE_LOGIC = new NbtSaveHandler<TileLauncherBase>()
        .mainRoot()
        /* */.nodeInteger("lock_height", launcher -> launcher.lockHeight, (launcher, h) -> launcher.lockHeight = h)
        /* */.nodeInteger("group_id", launcher -> launcher.groupId, (launcher, h) -> launcher.groupId = h)
        /* */.nodeInteger("group_index", launcher -> launcher.groupIndex, (launcher, h) -> launcher.groupIndex = h)
        /* */.nodeINBTSerializable("inventory", launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("firing_package", launcher -> launcher.firingPackage)
        .base();
}
