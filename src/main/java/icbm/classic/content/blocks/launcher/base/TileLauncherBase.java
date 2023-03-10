package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.content.missile.logic.targeting.BallisticTargetingData;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
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
    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    protected boolean checkMissileCollision = true;
    private boolean hasMissileCollision = false;

    private final LauncherInventory inventory = new LauncherInventory(this);

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);
    public final IMissileLauncher missileLauncher = new LauncherCapability(this);

    private final LauncherNode launcherNode = new LauncherNode(this, true);

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
        return new TextComponentTranslation("gui.launcherBase.name");
    }


    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        inventory.deserializeNBT(nbt.getCompoundTag(NBTConstants.INVENTORY));
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag(NBTConstants.INVENTORY, inventory.serializeNBT());
        return super.writeToNBT(nbt);
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
}
