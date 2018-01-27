package icbm.classic.content.machines.launcher.screen;

import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.api.energy.IEnergyBufferProvider;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.api.tile.access.IGuiTile;
import com.builtbroken.mc.api.tile.provider.IInventoryProvider;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.data.Direction;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.prefab.hz.FakeRadioSender;
import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.prefab.BlockICBM;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;
import resonant.api.explosion.ILauncherController;
import resonant.api.explosion.LauncherType;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileLauncherPrefab implements IPacketIDReceiver, ILauncherController, IGuiTile, IEnergyBufferProvider, IInventoryProvider<ExternalInventory>
{
    // The missile launcher base in which this
    // screen is connected with
    public TileLauncherBase laucherBase = null;

    /** Height to wait before missile curves */
    public short lockHeight = 3;

    public ExternalInventory inventory;

    @Override
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2); //TODO figure out what these 2 slots did
        }
        return inventory;
    }

    @Override
    public void update()
    {
        super.update();
        if (this.laucherBase == null || this.laucherBase.isInvalid())
        {
            this.laucherBase = null;
            for (byte i = 2; i < 6; i++)
            {
                final Pos position = toPos().add(Direction.getOrientation(i));
                final TileEntity tileEntity = position.getTileEntity(world);
                if (tileEntity != null)
                {
                    if (tileEntity instanceof TileLauncherBase)
                    {
                        this.laucherBase = (TileLauncherBase) tileEntity;
                        if (isServer())
                        {
                            setRotation(EnumFacing.getFront(i).getOpposite());
                            updateClient = true;
                        }
                    }
                }
            }
        }
        if (isServer())
        {
            if (this.ticks % 100 == 0 && world.isBlockIndirectlyGettingPowered(getPos()) > 0)
            {
                this.launch();
            }
            if (ticks % 3 == 0)
            {
                sendDescPacket();
            }
        }
    }

    @Override
    public PacketTile getDescPacket()
    {
        return new PacketTile("desc", 0, this).addData(getEnergy(), this.getFrequency(), this.lockHeight, this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return getDescPacket();
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        if (this.laucherBase != null)
        {
            if (!this.laucherBase.isInvalid())
            {
                this.laucherBase.getInventory().setInventorySlotContents(0, itemStack);
            }
        }
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (!super.read(data, id, player, packet))
        {
            switch (id)
            {
                case 0:
                {
                    if(isClient())
                    {
                        //this.tier = data.readInt();
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.lockHeight = data.readShort();
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
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
                case 3:
                {
                    this.lockHeight = (short) Math.max(Math.min(data.readShort(), Short.MAX_VALUE), 3);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    // Checks if the missile is launchable
    @Override
    public boolean canLaunch()
    {
        if (this.laucherBase != null && this.laucherBase.getMissileStack() != null)
        {
            if (this.checkExtract())
            {
                return this.laucherBase.isInRange(this.getTarget());
            }
        }
        return false;
    }

    /** Calls the missile launcher base to launch it's missile towards a targeted location */
    @Override
    public void launch()
    {
        if (this.canLaunch() && this.laucherBase.launchMissile(this.getTarget(), this.lockHeight))
        {
            this.extractEnergy();
            updateClient = true;
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

        if (this.laucherBase == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusMissing");
        }
        else if (!checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusNoPower");
        }
        else if (this.laucherBase.getMissileStack() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusEmpty");
        }
        else if (this.getTarget() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusInvalid");
        }
        else if (this.laucherBase.isTargetTooClose(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusClose");
        }
        else if (this.laucherBase.isTargetTooFar(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherScreen.statusFar");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.launcherScreen.statusReady");
        }

        return color + status;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        //this.tier = par1NBTTagCompound.getInteger("tier");
        this.lockHeight = par1NBTTagCompound.getShort("targetHeight");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        //par1NBTTagCompound.setInteger("tier", this.tier);
        par1NBTTagCompound.setShort("targetHeight", this.lockHeight);
        return super.writeToNBT(par1NBTTagCompound);
    }

    @Override
    public int getEnergyConsumption()
    {
        switch (this.getTier())
        {
            case ONE:
                return 50000;
            case TWO:
                return 80000;
        }
        return 100000;
    }

    @Override
    public int getEnergyBufferSize()
    {
        return getEnergyConsumption() * 2;
    }

    @Override
    public LauncherType getLauncherType()
    {
        return LauncherType.TRADITIONAL;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerLaunchScreen(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiLauncherScreen(player, this);
    }

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data)
    {
        //Floor frequency as we do not care about sub ranges
        int frequency = (int) Math.floor(hz);
        //Only tier 3 (2 for tier value) can be remotely fired
        if (getTier() == BlockICBM.EnumTier.THREE && frequency == getFrequency() && laucherBase != null)
        {
            //Laser detonator signal
            if (messageHeader.equals("activateLauncherWithTarget"))
            {
                Pos pos = (Pos) data[0];
                if (toPos().distance(pos) < this.laucherBase.getRange())
                {
                    setTarget(pos);
                    launch();
                    ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + pos));
                }
            }
            //Remote detonator signal
            else if (messageHeader.equals("activateLauncher"))
            {
                ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + getTarget()));
                launch();
            }
        }
    }
}
