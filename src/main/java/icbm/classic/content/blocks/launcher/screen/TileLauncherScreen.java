package icbm.classic.content.blocks.launcher.screen;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.blocks.launcher.TileLauncherPrefab;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.api.EnumTier;
import icbm.classic.lib.NBTConstants;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
@SuppressWarnings("incomplete-switch")
public class TileLauncherScreen extends TileLauncherPrefab implements IPacketIDReceiver, IInventoryProvider<ExternalInventory>
{
    // The missile launcher base in which this
    // screen is connected with
    public TileLauncherBase launcherBase = null;

    public static final int DESCRIPTION_PACKET_ID = 0;
    public static final int SET_FREQUENCY_PACKET_ID = 1;
    public static final int SET_TARGET_PACKET_ID = 2;
    public static final int LOCK_HEIGHT_PACKET_ID = 3;
    public static final int LAUNCH_PACKET_ID = 4;

    /** Height to wait before missile curves */
    public short lockHeight = 3;

    public ExternalInventory inventory;

    public int launchDelay = 0;

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
        if (this.launcherBase == null || this.launcherBase.isInvalid())
        {
            this.launcherBase = null;
            for (EnumFacing rotation : EnumFacing.HORIZONTALS)
            {
                final Pos position = new Pos((IPos3D) this).add(rotation);
                final TileEntity tileEntity = position.getTileEntity(world);
                if (tileEntity != null)
                {
                    if (tileEntity instanceof TileLauncherBase)
                    {
                        this.launcherBase = (TileLauncherBase) tileEntity;
                        if (isServer())
                        {
                            setRotation(rotation.getOpposite());
                            updateClient = true;
                        }
                    }
                }
            }
        }
        if (isServer())
        {
            //Delay launch, basically acts as a reload time
            if (launchDelay > 0)
            {
                launchDelay--;
            }
            //Only launch if redstone
            else if (ticks % 10 == 0 && world.getRedstonePowerFromNeighbors(getPos()) > 0) //TODO replace with countdown
            {
                this.launch();
            }

            //Update packet TODO see if this is needed
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
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (!super.read(data, id, player, packet))
        {
            switch (id)
            {
                case DESCRIPTION_PACKET_ID:
                {
                    if (isClient())
                    {
                        //this.tier = data.readInt();
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.lockHeight = data.readShort();
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                    break;
                }
                case SET_FREQUENCY_PACKET_ID:
                {
                    this.setFrequency(data.readInt());
                    return true;
                }
                case SET_TARGET_PACKET_ID:
                {
                    this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                    return true;
                }
                case LOCK_HEIGHT_PACKET_ID:
                {
                    this.lockHeight = (short) Math.max(Math.min(data.readShort(), Short.MAX_VALUE), 3);
                    return true;
                }
                case LAUNCH_PACKET_ID:
                {
                    launch(); //canLaunch is called by launch
                }
            }
            return false;
        }
        return true;
    }

    // Checks if the missile is launchable
    public boolean canLaunch()
    {
        if (this.launcherBase != null && this.launcherBase.getMissileStack() != null)
        {
            if (this.checkExtract())
            {
                return this.launcherBase.isInRange(this.getTarget());
            }
        }
        return false;
    }

    /**
     * Calls the missile launcher base to launch it's missile towards a targeted location
     * @return true if launched, false if not
     */
    public boolean launch()
    {
        if (this.canLaunch() && this.launcherBase.launchMissile(this.getTarget(), this.lockHeight))
        {
            //Reset delay
            switch (getTier())
            {
                case ONE:
                    launchDelay = ConfigLauncher.LAUNCHER_DELAY_TIER1;
                    break;
                case TWO:
                    launchDelay = ConfigLauncher.LAUNCHER_DELAY_TIER2;
                    break;
                case THREE:
                    launchDelay = ConfigLauncher.LAUNCHER_DELAY_TIER3;
                    break;
            }

            //Remove energy
            this.extractEnergy();

            //Mark client for update
            updateClient = true;
            return true;
        }

        return false;
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

        if (this.launcherBase == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusMissing");
        }
        else if (!checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusNoPower");
        }
        else if (this.launcherBase.getMissileStack().isEmpty())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusEmpty");
        }
        else if (this.getTarget() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusInvalid");
        }
        else if (this.launcherBase.isTargetTooClose(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusClose");
        }
        else if (this.launcherBase.isTargetTooFar(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusFar");
        }
        else
        {
            color = "\u00a72";
            status = LanguageUtility.getLocal("gui.launcherscreen.statusReady");
        }

        return color + status;
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        //this.tier = par1NBTTagCompound.getInteger(NBTConstants.TIER);
        this.lockHeight = par1NBTTagCompound.getShort(NBTConstants.TARGET_HEIGHT);
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        //par1NBTTagCompound.setInteger(NBTConstants.TIER, this.tier);
        par1NBTTagCompound.setShort(NBTConstants.TARGET_HEIGHT, this.lockHeight);
        return super.writeToNBT(par1NBTTagCompound);
    }

    @Override
    public int getEnergyConsumption()
    {
        switch (this.getTier())
        {
            case ONE:
                return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER1;
            case TWO:
                return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER2;
        }
        return ConfigLauncher.LAUNCHER_POWER_USAGE_TIER3;
    }

    @Override
    public int getEnergyBufferSize()
    {
        switch (this.getTier())
        {
            case ONE:
                return ConfigLauncher.LAUNCHER_POWER_CAP_TIER1;
            case TWO:
                return ConfigLauncher.LAUNCHER_POWER_CAP_TIER2;
        }
        return ConfigLauncher.LAUNCHER_POWER_CAP_TIER3;
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
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data) //TODO pack as message object
    {// TODO make sure other launchers don't trigger when a laser designator is used
        if (isServer())
        {
            //Floor frequency as we do not care about sub ranges
            int frequency = (int) Math.floor(hz);
            //Only tier 3 (2 for tier value) can be remotely fired
            if (getTier() == EnumTier.THREE && frequency == getFrequency() && launcherBase != null)
            {
                //Laser detonator signal
                if (messageHeader.equals("activateLauncherWithTarget")) //TODO cache headers somewhere like API references
                {
                    Pos pos = (Pos) data[0];
                    if (new Pos((IPos3D) this).distance(pos) < this.launcherBase.getRange())
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
}
