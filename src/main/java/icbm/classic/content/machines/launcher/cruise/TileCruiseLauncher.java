package icbm.classic.content.machines.launcher.cruise;

import com.builtbroken.mc.api.tile.IGuiTile;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.core.registry.implement.IRecipeContainer;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.recipe.UniversalRecipe;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.prefab.tile.Tile;
import com.builtbroken.mc.prefab.tile.module.TileModuleInventory;
import cpw.mods.fml.common.network.ByteBufUtils;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.oredict.ShapedOreRecipe;
import resonant.api.explosion.ILauncherContainer;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.LauncherType;

import java.util.List;

public class TileCruiseLauncher extends TileLauncherPrefab implements IInventory, IPacketIDReceiver, ILauncherController, ILauncherContainer, IRecipeContainer, IGuiTile
{
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
        String status;

        if (!this.checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusNoPower");
        }
        else if (this.getStackInSlot(0) == null)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusEmpty");
        }
        else if (this.getStackInSlot(0).getItem() != ICBMClassic.itemMissile)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
        }
        else
        {
            final Explosion missile = (Explosion) Explosives.get(this.getStackInSlot(0).getItemDamage()).handler;
            if (missile == null)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
            }
            else if (!missile.isCruise())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.notCruiseMissile");
            }
            else if (missile.getTier() > 3)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissileTier");
            }
            else if (this.getTarget() == null || getTarget().isZero())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.statusInvalid");
            }
            else if (this.isTooClose(getTarget()))
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.targetToClose");
            }
            else if(!canSpawnMissileWithNoCollision())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.noRoom");
            }
            else
            {
                color = "\u00a72";
                status = LanguageUtility.getLocal("gui.launcherCruise.statusReady");
            }
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
            if (this.ticks % 40 == 0 && this.worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
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

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile(this, 0, getEnergyStored(ForgeDirection.UNKNOWN), this.getFrequency(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, PacketType type)
    {
        if(!super.read(data, id, player, type))
        {
            if (isServer())
            {
                switch (id)
                {
                    //set frequency packet from GUI
                    case 1:
                    {
                        this.setFrequency(data.readInt());
                        return true;
                    }
                    //Set target packet from GUI
                    case 2:
                    {
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
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
        if(getTarget() != null && !getTarget().isZero())
        {
            //Validate if we have an item and a target
            if (this.getStackInSlot(0) != null && this.getStackInSlot(0).getItem() == ICBMClassic.itemMissile)
            {
                //Validate that the item in the slot is a missile we can fire
                final Explosion missile = (Explosion) Explosives.get(this.getStackInSlot(0).getItemDamage()).handler;
                if (missile != null && missile.isCruise() && missile.getTier() <= 3)
                {
                    //Make sure we have enough energy
                    if (this.checkExtract())
                    {
                        //Make sure we are not going to blow ourselves up
                        //TODO check range by missile type
                        if (!this.isTooClose(this.getTarget()))
                        {
                            //Make sure we can safely spawn the missile
                            return canSpawnMissileWithNoCollision();
                        }
                    }
                }
            }
        }
        return false;
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        for (int x = -1; x < 2; x++)
        {
            for (int z = -1; z < 2; z++)
            {
                if (!world().getBlock(xi() + x, yi() + 1, zi() + z).isAir(world(), xi() + x, yi() + 1, zi() + z))
                {
                    return false;
                }
            }
        }
        return true;
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
            this.extractEnergy();
            this.decrStackSize(0, 1);

            EntityMissile missile = new EntityMissile(world());
            missile.launcherPos = new Pos(this);
            missile.setPositionAndRotation(xi(), yi() + 1, zi(), rotationYaw, rotationPitch);
            missile.launch(getTarget(), 0);
            world().spawnEntityInWorld(missile);
        }
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(this.xCoord + .5, this.yCoord + .5, this.zCoord + .5).distance(new Pos(target.x() + .5, target.z() + .5, target.z() + .5)) < 20;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        if (slot == 0)
        {
            updateClient = true;
        }
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeBoolean(getStackInSlot(0) != null);
        if (getStackInSlot(0) != null)
        {
            ByteBufUtils.writeItemStack(buf, getStackInSlot(0));
        }
    }

    @Override
    public boolean onPlayerActivated(EntityPlayer player, int side, Pos hit)
    {
        if (isServer())
        {
            if (player.getHeldItem() != null && player.getHeldItem().getItem() == Items.redstone)
            {
                if (canLaunch())
                {
                    launch();
                }
                else
                {
                    player.addChatComponentMessage(new ChatComponentText(LanguageUtility.getLocal("chat.launcherCruise.failedToFire")));
                    String translation = LanguageUtility.getLocal("chat.launcherCruise.status");
                    translation = translation.replace("%1", getStatus());
                    player.addChatComponentMessage(new ChatComponentText(translation));
                }
            }
            else
            {
                player.openGui(ICBMClassic.INSTANCE, 0, this.worldObj, this.xCoord, this.yCoord, this.zCoord);
            }
        }
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
        recipes.add(new ShapedOreRecipe(new ItemStack(ICBMClassic.blockCruiseLauncher),
                "RL ", "PPP",
                'R', new ItemStack(ICBMClassic.blockLaunchSupport, 1, 2),
                'L', new ItemStack(ICBMClassic.blockLaunchBase, 1, 2),
                'P', UniversalRecipe.PRIMARY_METAL.get()));
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerCruiseLauncher(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return null;
    }
}
