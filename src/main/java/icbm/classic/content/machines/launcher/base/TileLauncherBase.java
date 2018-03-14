package icbm.classic.content.machines.launcher.base;

import cofh.api.energy.IEnergyHandler;
import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.framework.energy.data.EnergyBuffer;
import com.builtbroken.mc.framework.multiblock.EnumMultiblock;
import com.builtbroken.mc.framework.multiblock.MultiBlockHelper;
import com.builtbroken.mc.imp.transform.rotation.EulerAngle;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.prefab.items.ItemBlockSubTypes;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import icbm.classic.Settings;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.entity.EntityPlayerSeat;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.frame.TileLauncherFrame;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.ITier;
import resonant.api.explosion.ILauncherContainer;
import resonant.api.explosion.ILauncherController;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * This tile entity is for the base of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherBase extends TileModuleMachine implements IPacketIDReceiver, IMultiTileHost, ITier, ILauncherContainer, IRecipeContainer, IEnergyHandler
{
    public static HashMap<IPos3D, String> northSouthMultiBlockCache = new HashMap();
    public static HashMap<IPos3D, String> eastWestMultiBlockCache = new HashMap();

    private static EulerAngle accuracyAngleRandomizer_yawCache = new EulerAngle(0, 0, 0);

    static
    {
        northSouthMultiBlockCache.put(new Pos(1, 0, 0), EnumMultiblock.INV_ENERGY.getTileName());
        northSouthMultiBlockCache.put(new Pos(1, 1, 0), EnumMultiblock.INV_ENERGY.getTileName());
        northSouthMultiBlockCache.put(new Pos(1, 2, 0), EnumMultiblock.INV_ENERGY.getTileName());
        northSouthMultiBlockCache.put(new Pos(-1, 0, 0), EnumMultiblock.INV_ENERGY.getTileName());
        northSouthMultiBlockCache.put(new Pos(-1, 1, 0), EnumMultiblock.INV_ENERGY.getTileName());
        northSouthMultiBlockCache.put(new Pos(-1, 2, 0), EnumMultiblock.INV_ENERGY.getTileName());

        eastWestMultiBlockCache.put(new Pos(0, 0, 1), EnumMultiblock.INV_ENERGY.getTileName());
        eastWestMultiBlockCache.put(new Pos(0, 1, 1), EnumMultiblock.INV_ENERGY.getTileName());
        eastWestMultiBlockCache.put(new Pos(0, 2, 1), EnumMultiblock.INV_ENERGY.getTileName());
        eastWestMultiBlockCache.put(new Pos(0, 0, -1), EnumMultiblock.INV_ENERGY.getTileName());
        eastWestMultiBlockCache.put(new Pos(0, 1, -1), EnumMultiblock.INV_ENERGY.getTileName());
        eastWestMultiBlockCache.put(new Pos(0, 2, -1), EnumMultiblock.INV_ENERGY.getTileName());
    }

    // The connected missile launcher frame
    public TileLauncherFrame supportFrame = null;
    public TileLauncherScreen launchScreen = null;

    /** Fake entity to allow player to mount the missile without using the missile entity itself */
    public EntityPlayerSeat seat;

    // The tier of this launcher base
    protected int tier = 0;
    private boolean _destroyingStructure = false;

    public TileLauncherBase()
    {
        super("launcherBase", Material.iron);
        this.itemBlock = ItemBlockSubTypes.class;
        this.hardness = 10f;
        this.resistance = 10f;
        this.isOpaque = false;
    }

    @Override
    protected IInventory createInventory()
    {
        return new TileModuleInventory(this, 1);
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherBase();
    }


    @Override
    public EnergyBuffer getEnergyBuffer(ForgeDirection side)
    {
        if (launchScreen != null)
        {
            return launchScreen.getEnergyBuffer(ForgeDirection.UNKNOWN);
        }
        return null;
    }

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
                if (getMissileStack() != null && seat == null)
                {
                    seat = new EntityPlayerSeat(worldObj);
                    seat.host = this;
                    seat.rideOffset = new Pos(getFacing().offsetX * 0.5, 1, getFacing().offsetZ * 0.5);
                    seat.setPosition(x() + 0.5, y() + 0.5, z() + 0.5);
                    seat.setSize(0.5f, 2.5f);
                    worldObj.spawnEntityInWorld(seat);
                }
                //Destroy seat if no missile
                else if (getMissileStack() == null && seat != null)
                {
                    if (seat.riddenByEntity != null)
                    {
                        seat.riddenByEntity.mountEntity(null);
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
                this.supportFrame = null;
                this.launchScreen = null;

                //Check on all 4 sides
                for (byte i = 2; i < 6; i++)
                {
                    //Get tile entity on side
                    Pos position = new Pos(this.xCoord, this.yCoord, this.zCoord).add(ForgeDirection.getOrientation(i));
                    TileEntity tileEntity = this.worldObj.getTileEntity(position.xi(), position.yi(), position.zi());

                    //If frame update rotation
                    if (tileEntity instanceof TileLauncherFrame)
                    {
                        this.supportFrame = (TileLauncherFrame) tileEntity;
                        if (isServer())
                        {
                            this.supportFrame.setFacing(getDirection());
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
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        if (slot == 0)
        {
            sendDescPacket();
        }
    }

    @Override
    public boolean canStore(ItemStack stack, ForgeDirection side)
    {
        return stack != null && stack.getItem() == ICBMClassic.itemMissile;
    }

    @Override
    public boolean canRemove(ItemStack stack, ForgeDirection side)
    {
        return true;
    }

    @Override
    public String getInventoryName()
    {
        return LanguageUtility.getLocal("gui.launcherBase.name");
    }

    /**
     * Launches the missile
     *
     * @param target     - The target in which the missile will land in
     * @param lockHeight - height to wait before curving the missile
     */
    public boolean launchMissile(Pos target, int lockHeight)
    {
        final ItemStack stack = getMissileStack();
        if (stack != null && stack.getItem() == ICBMClassic.itemMissile)
        {
            Explosive ex = Explosives.get(stack.getItemDamage()).handler;
            if (ex.hasMissileForm())
            {
                target = applyInaccuracy(target, oldWorld().rand, getInaccuracy());

                //TODO add distance check? --- something seems to be missing
                //TODO add distance based inaccuracy addition
                //TODO add tier based inaccuracy, higher tier missiles have a high chance of hitting

                if (isServer())
                {
                    EntityMissile missile = new EntityMissile(oldWorld());

                    //Set data
                    missile.explosiveID = Explosives.get(stack.getItemDamage());
                    missile.launcherPos = new Pos((TileEntity) this);
                    missile.setPosition(xi(), yi() + 3, zi());

                    //Trigger launch event
                    missile.launch(target, lockHeight);

                    //Spawn entity
                    oldWorld().spawnEntityInWorld(missile);

                    //Grab rider
                    if (seat != null && seat.riddenByEntity != null)
                    {
                        Entity entity = seat.riddenByEntity;
                        seat.riddenByEntity.mountEntity(null);
                        entity.mountEntity(missile);
                    }

                    //Remove item
                    this.decrStackSize(0, 1);
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the inaccuracy of the launcher
     *
     * @return
     */
    public int getInaccuracy()
    {
        // Apply inaccuracy
        int inaccuracy = 30; //TODO move to config

        //Get value from support frame
        if (this.supportFrame != null)
        {
            inaccuracy = this.supportFrame.getInaccuracy();
        }

        return inaccuracy;
    }

    /**
     * Called to randomize the target by a given maxInaccuracy
     * <p>
     * Will generate a random distance and angle to apply to the target position. This
     * creates a circle pattern around the original target point
     *
     * @param target        - target to randomize
     * @param random        - random object to use for randomization
     * @param maxInaccuracy - max distance
     * @return
     */
    public static Pos applyInaccuracy(Pos target, Random random, double maxInaccuracy)
    {
        final float degrees = 360;

        //Randomize distance
        double inaccuracy = maxInaccuracy * random.nextFloat();

        //Randomize radius drop
        accuracyAngleRandomizer_yawCache.setYaw(random.nextFloat() * degrees);

        //Update target
        return target.add(accuracyAngleRandomizer_yawCache.x() * inaccuracy, 0, accuracyAngleRandomizer_yawCache.z() * inaccuracy);
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
        return new Pos(this.xCoord, 0, this.zCoord).distance(new Pos(target.x(), 0, target.z())) < 10;
    }

    // Is the target too far?
    public boolean isTargetTooFar(Pos target)
    {
        // Checks if it is greater than the maximum range for the launcher base
        double distance = new Pos(this.xCoord, 0, this.zCoord).distance(new Pos(target.x(), 0, target.z()));


        return distance > getRange();
    }

    public double getRange()
    {
        return getRangeForTier(tier);
    }

    public static double getRangeForTier(int tier)
    {
        if (tier == 0)
        {
            return Settings.LAUNCHER_RANGE_TIER1;
        }
        else if (tier == 1)
        {
            return Settings.LAUNCHER_RANGE_TIER2;
        }
        return Settings.LAUNCHER_RANGE_TIER3;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.tier = nbt.getInteger("tier");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);
        nbt.setInteger("tier", getTier());
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(getTier());
        buf.writeBoolean(getMissileStack() != null);
        if (getMissileStack() != null)
        {
            ByteBufUtils.writeItemStack(buf, getMissileStack());
        }
    }

    public ItemStack getMissileStack()
    {
        return getStackInSlot(0);
    }

    @Override
    public int getTier()
    {
        return this.tier;
    }

    @Override
    public void setTier(int tier)
    {
        this.tier = tier;
    }

    @Override
    protected boolean onPlayerRightClick(EntityPlayer player, int side, Pos hit)
    {
        if (player.inventory.getCurrentItem() != null)
        {
            if (player.inventory.getCurrentItem().getItem() instanceof ItemMissile)
            {
                if (this.getMissileStack() == null)
                {
                    if (isServer())
                    {
                        this.setInventorySlotContents(0, player.inventory.getCurrentItem());
                        if (!player.capabilities.isCreativeMode)
                        {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                            player.inventoryContainer.detectAndSendChanges();
                        }
                    }
                    return true;
                }
            }
            else if (launchScreen != null)
            {
                return launchScreen.onPlayerActivated(player, side, hit);
            }
        }
        else if (this.getMissileStack() != null)
        {
            if (isServer())
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, this.getMissileStack());
                this.setInventorySlotContents(0, null);
                player.inventoryContainer.detectAndSendChanges();
            }
            return true;
        }

        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public boolean canStore(ItemStack stack, int slot, ForgeDirection side)
    {
        return slot == 0 && stack.getItem() instanceof ItemMissile;
    }

    @Override
    public ILauncherController getController()
    {
        for (byte i = 2; i < 6; i++)
        {
            Pos position = new Pos((TileEntity) this).add(ForgeDirection.getOrientation(i));

            TileEntity tileEntity = position.getTileEntity(this.worldObj);

            if (tileEntity instanceof ILauncherController)
            {
                return (ILauncherController) tileEntity;
            }
        }

        return null;
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void firstTick()
    {
        super.firstTick();
        MultiBlockHelper.buildMultiBlock(oldWorld(), this, true, true);
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().containsKey(new Pos((TileEntity) this).sub(new Pos((TileEntity) tileMulti))))
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
            Pos pos = new Pos((TileEntity) tileMulti).sub(new Pos((TileEntity) this));

            if (getLayoutOfMultiBlock().containsKey(pos))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean canPlaceBlockAt()
    {
        return super.canPlaceBlockAt() && oldWorld().getBlock(xi(), yi() + 1, zi()).isReplaceable(oldWorld(), xi(), yi() + 1, zi()) && oldWorld().getBlock(xi(), yi() + 2, zi()).isReplaceable(oldWorld(), xi(), yi() + 2, zi());
    }

    @Override
    public boolean canPlaceBlockOnSide(ForgeDirection side)
    {
        return side == ForgeDirection.UP && canPlaceBlockAt();
    }

    @Override
    public boolean removeByPlayer(EntityPlayer player, boolean willHarvest)
    {
        _destroyingStructure = true;
        MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
        return super.removeByPlayer(player, willHarvest);
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, int side, float xHit, float yHit, float zHit)
    {
        return this.onPlayerRightClick(player, side, new Pos(xHit, yHit, zHit));
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public HashMap<IPos3D, String> getLayoutOfMultiBlock()
    {
        if (getDirection() == ForgeDirection.EAST || getDirection() == ForgeDirection.WEST)
        {
            return eastWestMultiBlockCache;
        }
        return northSouthMultiBlockCache;
    }

    @Override
    public void setFacing(ForgeDirection facingDirection)
    {
        if (facingDirection != getDirection())
        {
            if (isServer())
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
            }
            super.setFacing(facingDirection);
            if (isServer())
            {
                MultiBlockHelper.buildMultiBlock(oldWorld(), this, true, true);
                markDirty();
            }
        }
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        // Missile Launcher Platform
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchBase, 1, 0),
                "! !", "!C!", "!!!",
                '!', UniversalRecipe.SECONDARY_METAL.get(),
                'C', UniversalRecipe.CIRCUIT_T1.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchBase, 1, 1),
                "! !", "!C!", "!@!",
                '@', new ItemStack(ICBMClassic.blockLaunchBase, 1, 0),
                '!', UniversalRecipe.PRIMARY_METAL.get(),
                'C', UniversalRecipe.CIRCUIT_T2.get()));
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockLaunchBase, 1, 2),
                "! !", "!C!", "!@!",
                '@', new ItemStack(ICBMClassic.blockLaunchBase, 1, 1),
                '!', UniversalRecipe.PRIMARY_PLATE.get(),
                'C', UniversalRecipe.CIRCUIT_T3.get()));
    }

    @Override
    public void onPlaced(EntityLivingBase entityLiving, ItemStack itemStack)
    {
        super.onPlaced(entityLiving, itemStack);
        setTier(itemStack.getItemDamage());
    }

    @Override
    public int metadataDropped(int meta, int fortune)
    {
        return getTier();
    }

    @Override
    protected boolean useMetaForFacing()
    {
        return true;
    }
}
