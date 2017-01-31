package icbm.classic.content.machines.launcher.base;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.TileModuleMachine;
import com.builtbroken.mc.prefab.tile.multiblock.EnumMultiblock;
import com.builtbroken.mc.prefab.tile.multiblock.MultiBlockHelper;
import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import icbm.classic.Settings;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.frame.TileLauncherFrame;
import icbm.classic.prefab.VectorHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

/**
 * This tile entity is for the base of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherBase extends TileModuleMachine implements IPacketIDReceiver, IMultiTileHost, ITier, ILauncherContainer, IRecipeContainer
{
    public static HashMap<IPos3D, String> northSouthMultiBlockCache = new HashMap();
    public static HashMap<IPos3D, String> eastWestMultiBlockCache = new HashMap();

    static
    {
        northSouthMultiBlockCache.put(new Pos(1, 0, 0), EnumMultiblock.TILE.getName());
        northSouthMultiBlockCache.put(new Pos(1, 1, 0), EnumMultiblock.TILE.getName());
        northSouthMultiBlockCache.put(new Pos(1, 2, 0), EnumMultiblock.TILE.getName());
        northSouthMultiBlockCache.put(new Pos(-1, 0, 0), EnumMultiblock.TILE.getName());
        northSouthMultiBlockCache.put(new Pos(-1, 1, 0), EnumMultiblock.TILE.getName());
        northSouthMultiBlockCache.put(new Pos(-1, 2, 0), EnumMultiblock.TILE.getName());

        eastWestMultiBlockCache.put(new Pos(0, 0, 1), EnumMultiblock.TILE.getName());
        eastWestMultiBlockCache.put(new Pos(0, 1, 1), EnumMultiblock.TILE.getName());
        eastWestMultiBlockCache.put(new Pos(0, 2, 1), EnumMultiblock.TILE.getName());
        eastWestMultiBlockCache.put(new Pos(0, 0, -1), EnumMultiblock.TILE.getName());
        eastWestMultiBlockCache.put(new Pos(0, 1, -1), EnumMultiblock.TILE.getName());
        eastWestMultiBlockCache.put(new Pos(0, 2, -1), EnumMultiblock.TILE.getName());
    }

    // The connected missile launcher frame
    public TileLauncherFrame supportFrame = null;

    // The tier of this launcher base
    private int tier = 0;

    private boolean _destroyingStructure = false;

    public TileLauncherBase()
    {
        super("launcherBase", Material.iron);
        addInventoryModule(1);
    }

    @Override
    public Tile newTile()
    {
        return new TileLauncherBase();
    }

    /**
     * Allows the entity to update its state. Overridden in most subclasses, e.g. the mob spawner
     * uses this to count ticks and creates a new spawn inside its implementation.
     */
    @Override
    public void update()
    {
        super.update();
        if (!isServer())
        {
            if (this.supportFrame == null || this.supportFrame.isInvalid())
            {
                this.supportFrame = null;
                for (byte i = 2; i < 6; i++)
                {
                    Pos position = new Pos(this.xCoord, this.yCoord, this.zCoord).add(ForgeDirection.getOrientation(i));

                    TileEntity tileEntity = this.worldObj.getTileEntity(position.xi(), position.yi(), position.zi());

                    if (tileEntity instanceof TileLauncherFrame)
                    {
                        this.supportFrame = (TileLauncherFrame) tileEntity;
                        this.supportFrame.setDirection(VectorHelper.getOrientationFromSide(ForgeDirection.getOrientation(i), ForgeDirection.NORTH));
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
    public String getInventoryName()
    {
        return LanguageUtility.getLocal("gui.launcherBase.name");
    }

    /**
     * Launches the missile
     *
     * @param target - The target in which the missile will land in
     */
    public void launchMissile(Pos target, int gaoDu)
    {
        // Apply inaccuracy
        float inaccuracy;

        if (this.supportFrame != null)
        {
            inaccuracy = this.supportFrame.getInaccuracy();
        }
        else
        {
            inaccuracy = 30f;
        }

        inaccuracy *= (float) Math.random() * 2 - 1;

        target = target.add(inaccuracy, 0, inaccuracy);

        this.decrStackSize(0, 1);

        EntityMissile missile = new EntityMissile(world());
        missile.launcherPos = new Pos(this);
        missile.setPosition(xi(), yi() + 3, zi());
        missile.launch(target, gaoDu);
        world().spawnEntityInWorld(missile);
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
        if (this.tier == 0)
        {
            if (distance < Settings.DAO_DAN_ZUI_YUAN / 10)
            {
                return false;
            }
        }
        else if (this.tier == 1)
        {
            if (distance < Settings.DAO_DAN_ZUI_YUAN / 5)
            {
                return false;
            }
        }
        else if (this.tier == 2)
        {
            if (distance < Settings.DAO_DAN_ZUI_YUAN)
            {
                return false;
            }
        }

        return true;
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
        nbt.setInteger("tier", this.tier);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        this.tier = buf.readInt();
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeInt(tier);
        buf.writeBoolean(getStackInSlot(0) != null);
        if(getStackInSlot(0) != null)
        {
            ByteBufUtils.writeItemStack(buf, getStackInSlot(0));
        }
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
                if (this.getStackInSlot(0) == null)
                {
                    if(isServer())
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
        }
        else if (this.getStackInSlot(0) != null)
        {
            if(isServer())
            {
                player.inventory.setInventorySlotContents(player.inventory.currentItem, this.getStackInSlot(0));
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
            Pos position = new Pos(this).add(ForgeDirection.getOrientation(i));

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
        MultiBlockHelper.buildMultiBlock(world(), this, true, true);
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (northSouthMultiBlockCache.containsKey(new Pos(this).sub(new Pos((TileEntity) tileMulti))))
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
            Pos pos = new Pos((TileEntity) tileMulti).sub(new Pos(this));

            if (northSouthMultiBlockCache.containsKey(pos))
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
        return super.canPlaceBlockAt() && world().getBlock(xi(), yi() + 1, zi()).isReplaceable(world(), xi(), yi() + 1, zi()) && world().getBlock(xi(), yi() + 2, zi()).isReplaceable(world(), xi(), yi() + 2, zi());
    }

    @Override
    public boolean canPlaceBlockOnSide(ForgeDirection side)
    {
        return side == ForgeDirection.UP && canPlaceBlockAt();
    }

    @Override
    public boolean removeByPlayer(EntityPlayer player, boolean willHarvest)
    {
        MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
        return super.removeByPlayer(player, willHarvest);
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, int side, IPos3D hit)
    {
        return this.onPlayerRightClick(player, side, new Pos(hit));
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
            MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
            super.setFacing(facingDirection);
            MultiBlockHelper.buildMultiBlock(world(), this, true, true);
            markDirty();
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
        this.tier = itemStack.getItemDamage();
    }
}
