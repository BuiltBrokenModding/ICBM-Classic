package icbm.classic.content.machines.launcher.cruise;

import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import cpw.mods.fml.common.registry.GameRegistry;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
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
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.explosion.*;

import java.util.List;

public class TileCruiseLauncher extends TileLauncherPrefab implements IInventory, IPacketIDReceiver, ILauncherController, ILauncherContainer, IRecipeContainer
{
    // The missile that this launcher is holding
    public EntityMissile daoDan = null;

    public float rotationYaw = 0;

    public float rotationPitch = 0;

    public TileCruiseLauncher()
    {
        super("cruiseLauncher", Material.iron);
        this.setTarget(new Pos());
    }

    @Override
    public Tile newTile()
    {
        return new TileCruiseLauncher();
    }

    @Override
    protected IInventory createInventory()
    {
        return new TileModuleInventory(this, 2);
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from)
    {
        return 100000000;
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
        else if (this.getTarget() == null)
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
        setInventorySlotContents(0, itemStack);
    }

    public void setMissile()
    {
        if (!this.worldObj.isRemote)
        {
            if (this.getStackInSlot(0) != null)
            {
                if (this.getStackInSlot(0).getItem() instanceof ItemMissile)
                {
                    int haoMa = this.getStackInSlot(0).getItemDamage();

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
                                    this.daoDan = new EntityMissile(this.worldObj);
                                    this.daoDan.setPosition(startingPosition.x(), startingPosition.y(), startingPosition.z());
                                    this.daoDan.launcherPos = new Pos(this);
                                    this.daoDan.explosiveID = Explosives.get(haoMa);
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
        return new PacketTile(this, 0, getEnergyStored(ForgeDirection.UNKNOWN), this.getFrequency(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
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
                this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                return true;
            }
            case 1:
            {
                this.setFrequency(data.readInt());
                return true;
            }
            case 2:
            {
                this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                return true;
            }
        }
        return false;
    }

    private float getPitchFromTarget()
    {
        double distance = Math.sqrt((this.getTarget().x() - this.xCoord) * (this.getTarget().x() - this.xCoord) + (this.getTarget().z() - this.zCoord) * (this.getTarget().z() - this.zCoord));
        return (float) Math.toDegrees(Math.atan((this.getTarget().y() - (this.yCoord + 0.5F)) / distance));
    }

    private float getYawFromTarget()
    {
        double xDifference = this.getTarget().x() - (this.xCoord + 0.5F);
        double yDifference = this.getTarget().z() - (this.zCoord + 0.5F);
        return (float) Math.toDegrees(Math.atan2(yDifference, xDifference));
    }

    @Override
    public boolean canLaunch()
    {
        if (this.daoDan != null && this.getStackInSlot(0) != null)
        {
            Explosion missile = (Explosion) Explosives.get(this.getStackInSlot(0).getItemDamage()).handler;

            if (missile != null && missile.getID() == daoDan.getExplosiveType().getID() && missile.isCruise() && missile.getTier() <= 3)
            {
                if (this.checkExtract())
                {
                    if (!this.isTooClose(this.getTarget()))
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
            this.daoDan.launch(this.getTarget());
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
    }

    /** Writes a tile entity to NBT. */
    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        NBTTagList var2 = new NBTTagList();

        nbt.setTag("Items", var2);
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
                if (Explosives.get(itemStack.getItemDamage()).handler instanceof Explosion)
                {
                    Explosion missile = (Explosion) Explosives.get(itemStack.getItemDamage()).handler;

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
    public ILauncherController getController()
    {
        return this;
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
                "@@@", "!?!", "@@@",
                '@', UniversalRecipe.PRIMARY_PLATE.get(),
                '!', new ItemStack(ICBMClassic.blockRadarStation),
                '?', new ItemStack(ICBMClassic.blockRadarStation)));
    }
}
