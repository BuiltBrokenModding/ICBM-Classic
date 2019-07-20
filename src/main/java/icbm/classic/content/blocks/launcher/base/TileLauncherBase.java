package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.caps.IMissileLauncher;
import icbm.classic.api.events.LauncherEvent;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.multiblock.MultiBlockHelper;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.BlockICBM;
import icbm.classic.api.EnumTier;
import icbm.classic.prefab.tile.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This tile entity is for the base of the missile launcher
 *
 * @author Calclavia, DarkGuardsman
 */
public class TileLauncherBase extends TileMachine implements IMultiTileHost, IInventoryProvider<ExternalInventory>
{

    public static List<BlockPos> northSouthMultiBlockCache = new ArrayList();
    public static List<BlockPos> eastWestMultiBlockCache = new ArrayList();

    private static EulerAngle angle = new EulerAngle(0, 0, 0);

    static
    {
        northSouthMultiBlockCache.add(new BlockPos(1, 0, 0));
        northSouthMultiBlockCache.add(new BlockPos(1, 1, 0));
        northSouthMultiBlockCache.add(new BlockPos(1, 2, 0));
        northSouthMultiBlockCache.add(new BlockPos(-1, 0, 0));
        northSouthMultiBlockCache.add(new BlockPos(-1, 1, 0));
        northSouthMultiBlockCache.add(new BlockPos(-1, 2, 0));

        eastWestMultiBlockCache.add(new BlockPos(0, 0, 1));
        eastWestMultiBlockCache.add(new BlockPos(0, 1, 1));
        eastWestMultiBlockCache.add(new BlockPos(0, 2, 1));
        eastWestMultiBlockCache.add(new BlockPos(0, 0, -1));
        eastWestMultiBlockCache.add(new BlockPos(0, 1, -1));
        eastWestMultiBlockCache.add(new BlockPos(0, 2, -1));
    }

    // The connected missile launcher frame
    public TileLauncherFrame supportFrame = null;
    public TileLauncherScreen launchScreen = null;

    /**
     * Fake entity to allow player to mount the missile without using the missile entity itself
     */
    public EntityPlayerSeat seat;

    // The tier of this launcher base
    private boolean _destroyingStructure = false;

    ExternalInventory inventory;

    /**
     * Client's render cached object, used in place of inventory to avoid affecting GUIs
     */
    public ItemStack cachedMissileStack;

    public final IMissileHolder missileHolder = null; //TODO wrapper to inventory or wrapper inventory to holder (likely better option)
    public final IMissileLauncher missileLauncher = null; //TODO implement, screen will now only set data instead of being the launcher

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner
     * uses this to count ticks and creates a new spawn inside its implementation.
     */
    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            if (ticks % 3 == 0)
            {
                //Update seat position
                if (seat != null)
                {
                    seat.setPosition(x() + 0.5, y() + 0.5, z() + 0.5);
                }

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
                    if (seat.getRidingEntity() != null)
                    {
                        seat.getRidingEntity().startRiding(null);
                    }
                    seat.setDead();
                    seat = null;
                }
            }
        }
        //1 second update
        if (ticks % 20 == 0)
        {
            //Only update if frame or screen is invalid
            if (this.supportFrame == null || launchScreen == null || launchScreen.isInvalid() || this.supportFrame.isInvalid())
            {
                //Reset data
                if (this.supportFrame != null)
                {
                    this.supportFrame.launcherBase = null;
                }
                this.supportFrame = null;
                this.launchScreen = null;

                //Check on all 4 sides
                for (EnumFacing rotation : EnumFacing.HORIZONTALS)
                {
                    //Get tile entity on side
                    Pos position = new Pos(getPos()).add(rotation);
                    TileEntity tileEntity = this.world.getTileEntity(position.toBlockPos());

                    //If frame update rotation
                    if (tileEntity instanceof TileLauncherFrame)
                    {
                        this.supportFrame = (TileLauncherFrame) tileEntity;
                        this.supportFrame.launcherBase = this;
                        if (isServer())
                        {
                            this.supportFrame.setRotation(getRotation());
                        }
                    }
                    //If screen, tell the screen the base exists
                    else if (tileEntity instanceof TileLauncherScreen)
                    {
                        this.launchScreen = (TileLauncherScreen) tileEntity;
                    }
                }
            }
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return getInventory() != null;
        }
        else if (launchScreen != null)
        {
            return launchScreen.hasCapability(capability, facing);
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) getInventory().itemHandlerWrapper;
        }
        else if (launchScreen != null)
        {
            return launchScreen.getCapability(capability, facing);
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        if (slot == 0)
        {
            sendDescPacket();
        }
    }

    @Override
    public boolean canStore(ItemStack stack, EnumFacing side)
    {
        return stack != null && stack.getItem() == ItemReg.itemMissile;
    }

    @Override
    public boolean canRemove(ItemStack stack, EnumFacing side)
    {
        return true;
    }

    //@Override TODO ?
    public String getInventoryName()
    {
        return LanguageUtility.getLocal("gui.launcherBase.name");
    }

    protected Pos applyInaccuracy(Pos target)
    {
        // Apply inaccuracy
        int inaccuracy = 30; //TODO customize more

        //Get value from support frame
        if (this.supportFrame != null)
        {
            inaccuracy = this.supportFrame.getInaccuracy();
        }

        //TODO add distance based inaccuracy addition
        //TODO add tier based inaccuracy, higher tier missiles have a high chance of hitting

        //Randomize distance
        inaccuracy = getWorld().rand.nextInt(inaccuracy);

        //Randomize radius drop
        angle.setYaw(getWorld().rand.nextFloat() * 360);

        //Apply inaccuracy to target position and return
        return target.add(angle.x() * inaccuracy, 0, angle.z() * inaccuracy);
    }

    /**
     * Launches the missile
     *
     * @param target     - The target in which the missile will land in
     * @param lockHeight - height to wait before curving the missile
     */
    public boolean launchMissile(Pos target, int lockHeight)
    {
        //Allow canceling missile launches
        if (MinecraftForge.EVENT_BUS.post(new LauncherEvent.OnLaunch(missileLauncher, missileHolder)))
        {
            return false;
        }

        final ItemStack stack = getMissileStack();
        if (stack.getItem() == ItemReg.itemMissile) //TODO capability
        {
            IExplosiveData explosiveData = ICBMClassicHelpers.getExplosive(stack.getItemDamage(), true);
            if (explosiveData != null)
            {
                target = applyInaccuracy(target);

                //TODO add distance check? --- something seems to be missing

                if (isServer())
                {
                    EntityMissile missile = new EntityMissile(getWorld()); //TODO generate entity from item using handler

                    //Set data
                    missile.explosiveID = explosiveData.getRegistryID();
                    missile.launcherPos = new Pos((TileEntity) this); //TODO store our launcher instance or UUID
                    missile.setPosition(xi() + 0.5, yi() + 3, zi() + 0.5); //TODO store offset

                    //Trigger launch event
                    missile.capabilityMissile.launch(target.x(), target.y(), target.z(), lockHeight);

                    //Spawn entity
                    getWorld().spawnEntity(missile);

                    //Grab rider
                    if (seat != null && seat.getRidingEntity() != null) //TODO add hook to disable riding some missiles
                    {
                        Entity entity = seat.getRidingEntity();
                        seat.getRidingEntity().startRiding(null);
                        entity.startRiding(missile);
                    }

                    //Remove item
                    getInventory().decrStackSize(0, 1);
                }
                return true;
            }
        }
        return false;
    }

    // Checks if the missile target is in range
    public boolean isInRange(Pos target)
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
    public boolean isTargetTooClose(Pos target)
    {
        // Check if it is greater than the minimum range
        return new Pos(this.x(), 0, this.z()).distance(new Pos(target.x(), 0, target.z())) < 10;
    }

    // Is the target too far?
    public boolean isTargetTooFar(Pos target)
    {
        // Checks if it is greater than the maximum range for the launcher base
        double distance = new Pos(this.x(), 0, this.z()).distance(new Pos(target.x(), 0, target.z()));


        return distance > getRange();
    }

    public double getRange()
    {
        return getRangeForTier(getTier());
    }

    public static double getRangeForTier(EnumTier tier)
    {
        if (tier == EnumTier.ONE)
        {
            return ConfigLauncher.LAUNCHER_RANGE_TIER1;
        }
        else if (tier == EnumTier.TWO)
        {
            return ConfigLauncher.LAUNCHER_RANGE_TIER2;
        }
        return ConfigLauncher.LAUNCHER_RANGE_TIER3;
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        getInventory().load(nbt.getCompoundTag("inventory")); //TODO datafixer to replace inventory
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("inventory", getInventory().save(new NBTTagCompound()));
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
        return getInventory().getStackInSlot(0);
    }

    public boolean onPlayerRightClick(EntityPlayer player, EnumHand hand, ItemStack heldItem)
    {

        if (!tryInsertMissile(player, hand, heldItem) && launchScreen != null)
        {
            return BlockReg.blockLaunchScreen.onBlockActivated(world, launchScreen.getPos(), world.getBlockState(launchScreen.getPos()), player, hand, EnumFacing.NORTH, 0, 0, 0);
            //return launchScreen.onPlayerActivated(player, side, hit);
        }

        return true;
    }

    public boolean tryInsertMissile(EntityPlayer player, EnumHand hand, ItemStack heldItem)
    {
        if (heldItem.getItem() instanceof ItemMissile && this.getMissileStack().isEmpty())
        {
            if (heldItem.getItem() instanceof ItemMissile)
            {
                if (this.getMissileStack().isEmpty())
                {
                    if (isServer())
                    {
                        getInventory().setInventorySlotContents(0, heldItem);
                        if (!player.capabilities.isCreativeMode)
                        {
                            player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    }
                    return true;
                }
            }
        }
        else if (player.isSneaking() && heldItem.isEmpty() && !this.getMissileStack().isEmpty())
        {
            if (isServer())
            {

                player.setItemStackToSlot(hand == EnumHand.MAIN_HAND ? EntityEquipmentSlot.MAINHAND : EntityEquipmentSlot.OFFHAND, this.getMissileStack());
                getInventory().setInventorySlotContents(0, ItemStack.EMPTY);
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
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 1);
        }
        return inventory;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, EnumFacing side)
    {
        return slot == 0 && stack.getItem() instanceof ItemMissile;
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            BlockPos pos = ((TileEntity) tileMulti).getPos().subtract(getPos());
            if (getLayoutOfMultiBlock().contains(pos))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            BlockPos pos = ((TileEntity) tileMulti).getPos().subtract(getPos());
            if (getLayoutOfMultiBlock().contains(pos))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        return this.onPlayerRightClick(player, hand, player.getHeldItem(hand));
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public List<BlockPos> getLayoutOfMultiBlock()
    {
        return getLayoutOfMultiBlock(getRotation());
    }

    public static List<BlockPos> getLayoutOfMultiBlock(EnumFacing facing)
    {
        if (facing == EnumFacing.EAST || facing == EnumFacing.WEST)
        {
            return eastWestMultiBlockCache;
        }
        return northSouthMultiBlockCache;
    }

    @Override
    public void setRotation(EnumFacing facingDirection)
    {
        //Only update if state has changed
        if (facingDirection != getRotation()

                //Prevent up and down placement
                && facingDirection != EnumFacing.UP
                && facingDirection != EnumFacing.DOWN)
        {
            //Clear old structure
            if (isServer())
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
            }

            //Update block state
            world.setBlockState(pos, getBlockState().withProperty(BlockICBM.ROTATION_PROP, facingDirection));

            //Create new structure
            if (isServer())
            {
                MultiBlockHelper.buildMultiBlock(getWorld(), this, true, true);
                markDirty();
            }
        }
    }
}
