package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.source.MissileSourceBlock;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.BallisticFlightLogic;
import icbm.classic.content.missile.targeting.BallisticTargetingData;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.caps.IMissileLauncher;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.TileMachine;
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
public class TileLauncherBase extends TilePoweredMachine implements ILauncherComponent
{
    private static final EulerAngle angle = new EulerAngle(0, 0, 0);

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    private boolean checkMissileCollision = true;
    private boolean hasMissileCollision = false;

    private final LauncherInventory inventory = new LauncherInventory(this);

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);
    public final IMissileLauncher missileLauncher = null; //TODO implement, screen will now only set data instead of being the launcher

    private final LauncherNode launcherNode = new LauncherNode(this);

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

    protected Vec3d applyInaccuracy(BlockPos target, int launcherCount)
    {
        // Apply inaccuracy
        double inaccuracy = ConfigLauncher.MIN_INACCURACY;

        // Add inaccuracy based on range
        double distance = getDistanceSq(target.getX(), target.getY(), target.getZ());
        double scale = distance / ConfigLauncher.RANGE;
        inaccuracy += scale * ConfigLauncher.SCALED_INACCURACY;

        // Add inaccuracy for each launcher fired in circuit
        if(launcherCount > 1) {
            inaccuracy += (launcherCount - 1) * ConfigLauncher.SCALED_LAUNCHER_COST;
        }

        //Randomize distance
        inaccuracy = inaccuracy * getWorld().rand.nextFloat();

        //Randomize radius drop
        angle.setYaw(getWorld().rand.nextFloat() * 360); //TODO fix to use a normal distribution from ICBM 2

        //Apply inaccuracy to target position and return
        return new Vec3d((target.getX() + 0.5) + angle.x() * inaccuracy, 0, (target.getZ() + 0.5) + angle.z() * inaccuracy);
    }

    /**
     * Launches the missile
     *
     * @param targetPos     - The target in which the missile will land in
     * @param lockHeight - height to wait before curving the missile
     */
    public boolean launchMissile(BlockPos targetPos, int lockHeight, int launcherCount)
    {
        //Allow canceling missile launches
        if (MinecraftForge.EVENT_BUS.post(new LauncherEvent.PreLaunch(missileLauncher, missileHolder)))
        {
            return false;
        }

        final ItemStack stack = getMissileStack();
        if (stack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
        {
            final ICapabilityMissileStack missileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);

            if (missileStack != null)
            {
                final Vec3d target = applyInaccuracy(targetPos, launcherCount);

                //TODO add distance check? --- something seems to be missing

                if (isServer())
                {
                    final IMissile missile = missileStack.newMissile(world());
                    final Entity entity = missile.getMissileEntity();
                    entity.setPosition(xi() + 0.5, yi() + 3.1, zi() + 0.5);  //TODO store offset as variable, sync with missile height

                    //Trigger launch event
                    missile.setTargetData(new BallisticTargetingData(target, 1));
                    missile.setFlightLogic(new BallisticFlightLogic(lockHeight));
                    missile.setMissileSource( new MissileSourceBlock(world, getPos(), getBlockState(), null)); //TODO encode player that built launcher, firing method (laser, remote, redstone), and other useful data
                    missile.launch();

                    //Spawn entity
                    ((WorldServer) getWorld()).addScheduledTask(() -> getWorld().spawnEntity(entity));

                    //Grab rider
                    if (seat != null && !seat.getPassengers().isEmpty()) //TODO add hook to disable riding some missiles
                    {
                        final List<Entity> riders = seat.getPassengers();
                        riders.forEach(r -> {
                            entity.dismountRidingEntity();
                            r.startRiding(entity);
                        });
                    }

                    //Remove item
                    inventory.extractItem(0, 1, false);
                    checkMissileCollision = true;
                }
                return true;
            }
        }
        return false;
    }

    // Checks if the missile target is in range
    public boolean isInRange(BlockPos target)
    {
        if (target != null)
        {
            return !isTargetTooFar(target) && !isTargetTooClose(target);
        }
        return false;
    }

    /**
     * Checks to see if the target is too close.
     *
     * @param target
     * @return
     */
    public boolean isTargetTooClose(BlockPos target)
    {
        final int minDistance = 10;
        final int deltaX = Math.abs(target.getX() - getPos().getX());
        final int deltaZ = Math.abs(target.getZ() - getPos().getZ());
        return deltaX < minDistance || deltaZ < minDistance;
    }

    public boolean isTargetTooFar(BlockPos target)
    {
        final int deltaX = Math.abs(target.getX() - getPos().getX());
        final int deltaZ = Math.abs(target.getZ() - getPos().getZ());
        return deltaX > ConfigLauncher.RANGE || deltaZ > ConfigLauncher.RANGE;
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
