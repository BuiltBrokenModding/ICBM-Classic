package icbm.classic.content.machines;

import com.builtbroken.mc.api.items.ISimpleItemRenderer;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import icbm.classic.client.render.tile.RenderCruiseLauncher;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.ExplosiveRegistry;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.content.explosive.ex.missiles.Missile;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import org.lwjgl.opengl.GL11;
import resonant.api.explosion.*;

import java.util.List;

public class TileCruiseLauncher extends TileLauncherPrefab implements IInventory, IPacketIDReceiver, ILauncherController, ILauncherContainer, IRecipeContainer, ISimpleItemRenderer
{
    // The missile that this launcher is holding
    public EntityMissile daoDan = null;

    public float rotationYaw = 0;

    public float rotationPitch = 0;

    /** The ItemStacks that hold the items currently being used in the missileLauncher */
    private ItemStack[] containingItems = new ItemStack[2];

    public TileCruiseLauncher()
    {
        super("cruiseLauncher", Material.iron);
        this.targetPos = new Pos();
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return 100000000;
    }

    /** Returns the number of slots in the inventory. */
    @Override
    public int getSizeInventory()
    {
        return this.containingItems.length;
    }

    /** Returns the stack in slot i */
    @Override
    public ItemStack getStackInSlot(int par1)
    {
        return this.containingItems[par1];
    }

    /**
     * Decrease the size of the stack in slot (first int arg) by the amount of the second int arg.
     * Returns the new stack.
     */
    @Override
    public ItemStack decrStackSize(int par1, int par2)
    {
        if (this.containingItems[par1] != null)
        {
            ItemStack var3;

            if (this.containingItems[par1].stackSize <= par2)
            {
                var3 = this.containingItems[par1];
                this.containingItems[par1] = null;
                return var3;
            }
            else
            {
                var3 = this.containingItems[par1].splitStack(par2);

                if (this.containingItems[par1].stackSize == 0)
                {
                    this.containingItems[par1] = null;
                }

                return var3;
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * When some containers are closed they call this on each slot, then drop whatever it returns as
     * an EntityItem - like when you close a workbench GUI.
     */
    @Override
    public ItemStack getStackInSlotOnClosing(int par1)
    {
        if (this.containingItems[par1] != null)
        {
            ItemStack var2 = this.containingItems[par1];
            this.containingItems[par1] = null;
            return var2;
        }
        else
        {
            return null;
        }
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor
     * sections).
     */
    @Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack)
    {
        this.containingItems[par1] = par2ItemStack;

        if (par2ItemStack != null && par2ItemStack.stackSize > this.getInventoryStackLimit())
        {
            par2ItemStack.stackSize = this.getInventoryStackLimit();
        }
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    @Override
    public String getStatus()
    {
        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");

        if (!this.checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusNoPower");
        }
        else if (this.daoDan == null)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusEmpty");
        }
        else if (this.targetPos == null)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusInvalid");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.launcherCruise.statusReady");
        }

        return color + status;
    }

    /** Returns the name of the inventory. */
    @Override
    public String getInventoryName()
    {
        return LanguageUtility.getLocal("gui.launcherCruise.name");
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public void update()
    {
        super.update();

        //this.discharge(this.containingItems[1]);

        // Rotate the yaw
        if (this.getYawFromTarget() - this.rotationYaw != 0)
        {
            this.rotationYaw += (this.getYawFromTarget() - this.rotationYaw) * 0.1;
        }
        if (this.getPitchFromTarget() - this.rotationPitch != 0)
        {
            this.rotationPitch += (this.getPitchFromTarget() - this.rotationPitch) * 0.1;
        }

        if (!this.worldObj.isRemote)
        {
            this.setMissile();

            if (this.ticks % 100 == 0 && this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
            {
                this.launch();
            }
        }
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        this.containingItems[0] = itemStack;
    }

    public void setMissile()
    {
        if (!this.worldObj.isRemote)
        {
            if (this.containingItems[0] != null)
            {
                if (this.containingItems[0].getItem() instanceof ItemMissile)
                {
                    int haoMa = this.containingItems[0].getItemDamage();

                    if (Explosives.get(haoMa).handler instanceof Missile)
                    {
                        Missile missile = (Missile) Explosives.get(haoMa).handler;

                        ExplosionEvent.ExplosivePreDetonationEvent evt = new ExplosionEvent.ExplosivePreDetonationEvent(this.worldObj, this.xCoord, this.yCoord, this.zCoord, ExplosiveType.AIR, missile);
                        MinecraftForge.EVENT_BUS.post(evt);

                        if (!evt.isCanceled())
                        {
                            if (this.daoDan == null)
                            {

                                if (missile.isCruise() && missile.getTier() <= 3)
                                {
                                    Pos startingPosition = new Pos((this.xCoord + 0.5f), (this.yCoord + 1f), (this.zCoord + 0.5f));
                                    this.daoDan = new EntityMissile(this.worldObj, startingPosition, new Pos(this), Explosives.get(haoMa));
                                    this.worldObj.spawnEntityInWorld((Entity) this.daoDan);
                                    return;
                                }
                            }

                            if (this.daoDan != null)
                            {
                                if (this.daoDan.explosiveID == Explosives.get(haoMa))
                                {
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            if (this.daoDan != null)
            {
                ((Entity) this.daoDan).setDead();
            }

            this.daoDan = null;
        }
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile(this, 0, getEnergyStored(ForgeDirection.UNKNOWN), this.getFrequency(), this.targetPos.xi(), this.targetPos.yi(), this.targetPos.zi());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
    {
        switch (id)
        {
            case 0:
            {
                this.energy = data.readInt();
                this.setFrequency(data.readInt());
                this.targetPos = new Pos(data.readInt(), data.readInt(), data.readInt());
                return true;
            }
            case 1:
            {
                this.setFrequency(data.readInt());
                return true;
            }
            case 2:
            {
                this.targetPos = new Pos(data.readInt(), data.readInt(), data.readInt());
                return true;
            }
        }
        return false;
    }

    private float getPitchFromTarget()
    {
        double distance = Math.sqrt((this.targetPos.x() - this.xCoord) * (this.targetPos.x() - this.xCoord) + (this.targetPos.z() - this.zCoord) * (this.targetPos.z() - this.zCoord));
        return (float) Math.toDegrees(Math.atan((this.targetPos.y() - (this.yCoord + 0.5F)) / distance));
    }

    private float getYawFromTarget()
    {
        double xDifference = this.targetPos.x() - (this.xCoord + 0.5F);
        double yDifference = this.targetPos.z() - (this.zCoord + 0.5F);
        return (float) Math.toDegrees(Math.atan2(yDifference, xDifference));
    }

    @Override
    public boolean canLaunch()
    {
        if (this.daoDan != null && this.containingItems[0] != null)
        {
            Explosion missile = (Explosion) ExplosiveRegistry.get(this.containingItems[0].getItemDamage());

            if (missile != null && missile.getID() == daoDan.getExplosiveType().getID() && missile.isCruise() && missile.getTier() <= 3)
            {
                if (this.checkExtract())
                {
                    if (!this.isTooClose(this.targetPos))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public LauncherType getLauncherType()
    {
        return LauncherType.CRUISE;
    }

    /**
     * Launches the missile
     */
    @Override
    public void launch()
    {
        if (this.canLaunch())
        {
            this.decrStackSize(0, 1);
            this.extractEnergy();
            this.daoDan.launch(this.targetPos);
            this.daoDan = null;
        }
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(this.xCoord, 0, this.zCoord).distance(new Pos(target.x(), 0, target.z())) < 8;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);

        NBTTagList var2 = nbt.getTagList("Items", 10);

        this.containingItems = new ItemStack[this.getSizeInventory()];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = var2.getCompoundTagAt(var3);
            byte var5 = var4.getByte("Slot");

            if (var5 >= 0 && var5 < this.containingItems.length)
            {
                this.containingItems[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < this.containingItems.length; ++var3)
        {
            if (this.containingItems[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte) var3);
                this.containingItems[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        nbt.setTag("Items", var2);
    }

    /**
     * Returns the maximum stack size for a inventory slot. Seems to always be 64, possibly will be
     * extended. *Isn't this more of a set than a get?*
     */
    @Override
    public int getInventoryStackLimit()
    {
        return 1;
    }

    /** Do not make give this method the name canInteractWith because it clashes with Container */
    @Override
    public boolean isUseableByPlayer(EntityPlayer par1EntityPlayer)
    {
        return this.worldObj.getTileEntity(this.xCoord, this.yCoord, this.zCoord) != this ? false : par1EntityPlayer.getDistanceSq(this.xCoord + 0.5D, this.yCoord + 0.5D, this.zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory()
    {

    }

    @Override
    public void closeInventory()
    {

    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        if (player.inventory.getCurrentItem() != null)
        {
            if (player.inventory.getCurrentItem().getItem() instanceof ItemMissile)
            {
                if (this.getStackInSlot(0) == null)
                {

                    this.setInventorySlotContents(0, player.inventory.getCurrentItem());
                    if (!player.capabilities.isCreativeMode)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                    }
                    return true;
                }
                else
                {
                    ItemStack player_held = player.inventory.getCurrentItem();
                    if (!player.capabilities.isCreativeMode)
                    {
                        player.inventory.setInventorySlotContents(player.inventory.currentItem, this.getStackInSlot(0));
                    }
                    this.setInventorySlotContents(0, player_held);
                    return true;
                }
            }
        }
        else if (this.getStackInSlot(0) != null)
        {
            player.inventory.setInventorySlotContents(player.inventory.currentItem, this.getStackInSlot(0));
            this.setInventorySlotContents(0, null);
            return true;
        }

        player.openGui(ICBMClassic.INSTANCE, 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
        return true;
    }

    @Override
    public boolean targetWithYValue()
    {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slotID, ItemStack itemStack)
    {
        if (itemStack != null)
        {
            if (itemStack.getItem() instanceof ItemMissile && this.getStackInSlot(slotID) == null)
            {
                if (ExplosiveRegistry.get(itemStack.getItemDamage()) instanceof Explosion)
                {
                    Explosion missile = (Explosion) ExplosiveRegistry.get(itemStack.getItemDamage());

                    if (missile.isCruise() && missile.getTier() <= 3)
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void setContainingMissile(IMissile missile)
    {
        this.daoDan = (EntityMissile) missile;
    }

    @Override
    public ILauncherController getController()
    {
        return this;
    }

    @Override
    public IMissile getMissile()
    {
        return this.daoDan;
    }

    @Override
    public IMissile getContainingMissile()
    {
        return this.daoDan;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return AxisAlignedBB.getBoundingBox(xCoord - 1, yCoord, zCoord - 1, xCoord + 1, yCoord + 1, zCoord + 1);
    }

    @Override
    public void genRecipes(List<IRecipe> recipes)
    {
        GameRegistry.addRecipe(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockEmpTower),
                "@@@", "!?! ", "@@@",
                '@', UniversalRecipe.PRIMARY_PLATE.get(),
                '!', new ItemStack(ICBMClassic.blockRadarStation),
                '?', new ItemStack(ICBMClassic.blockRadarStation)));
    }

    @Override
    public void renderInventoryItem(IItemRenderer.ItemRenderType type, ItemStack itemStack, Object... data)
    {
        GL11.glPushMatrix();
        GL11.glTranslatef(0f, 0.4f, 0f);
        GL11.glRotatef(180f, 0f, 0f, 1f);
        GL11.glScalef(0.55f, 0.5f, 0.55f);

        FMLClientHandler.instance().getClient().renderEngine.bindTexture(RenderCruiseLauncher.TEXTURE_FILE);

        RenderCruiseLauncher.MODEL0.render(0.0625F);
        RenderCruiseLauncher.MODEL1.render(0.0625F);
        GL11.glPopMatrix();
    }
}
