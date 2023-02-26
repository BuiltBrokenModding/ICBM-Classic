package icbm.classic.content.blocks.launcher.screen;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.api.tile.IRadioWaveReceiver;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.config.ConfigLauncher;
import icbm.classic.content.blocks.launcher.ILauncherComponent;
import icbm.classic.content.blocks.launcher.LauncherReference;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioHeaders;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.saving.NbtSaveNode;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.lib.NBTConstants;
import icbm.classic.prefab.tile.TileMachine;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.MinecraftForge;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Optional;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileMachine implements IPacketIDReceiver, IRadioWaveReceiver, ILauncherComponent
{

    public static final int DESCRIPTION_PACKET_ID = 0;
    public static final int SET_FREQUENCY_PACKET_ID = 1;
    public static final int SET_TARGET_PACKET_ID = 2;
    public static final int LOCK_HEIGHT_PACKET_ID = 3;
    public static final int LAUNCH_PACKET_ID = 4;

    /** Height to wait before missile curves */
    public int lockHeight = 3;

    /** Target position of the launcher */
    private BlockPos _targetPos = BlockPos.ORIGIN;

    /** Frequency of the device */
    private int frequency = 0;

    public int clientEnergyBar = 0;

    private final LauncherReference launcherReference = new LauncherReference(this);

    @Override
    public LauncherReference getReference() {
        return launcherReference;
    }

    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            //Only launch if redstone
            if (ticks % 3 == 0 && world.getStrongPower(getPos()) > 0)
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
        final int energy = Optional.ofNullable(getLauncher()).map(TilePoweredMachine::getEnergy).orElse(0);
        return new PacketTile("desc", 0, this).addData(energy, this.getFrequency(), this.lockHeight, this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
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
                        this.clientEnergyBar = data.readInt();
                        this.setFrequency(data.readInt());
                        this.lockHeight = data.readShort();
                        this.setTarget(new BlockPos(data.readInt(), data.readInt(), data.readInt()));
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
                    this.setTarget(new BlockPos(data.readInt(), data.readInt(), data.readInt()));
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
        final TileLauncherBase launcher = getLauncher();
        return launcher != null
            && launcher.getMissileStack() != null
            && launcher.checkExtract()
            && launcher.isInRange(this.getTarget());
    }

    /**
     * Calls the missile launcher base to launch it's missile towards a targeted location
     * @return true if launched, false if not
     */
    public boolean launch()
    {
        final TileLauncherBase launcher = getLauncher();
        return this.canLaunch() && launcher.launchMissile(this.getTarget(), this.lockHeight);
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    public String getStatus()
    {
        final TileLauncherBase launcher = getLauncher();

        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");

        if (launcher == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusMissing");
        }
        else if (!launcher.checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusNoPower");
        }
        else if (launcher.getMissileStack().isEmpty())
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusEmpty");
        }
        else if (this.getTarget() == null)
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusInvalid");
        }
        else if (launcher.isTargetTooClose(this.getTarget()))
        {
            status = LanguageUtility.getLocal("gui.launcherscreen.statusClose");
        }
        else if (launcher.isTargetTooFar(this.getTarget()))
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
    {
        // TODO make sure other launchers don't trigger when a laser designator is used
        if (isServer())
        {
            final TileLauncherBase launcher = getLauncher();

            //Floor frequency as we do not care about sub ranges
            final int frequency = (int) Math.floor(hz);
            if (frequency == getFrequency() && launcher != null)
            {
                //Laser detonator signal
                if (messageHeader.equals(RadioHeaders.FIRE_AT_TARGET.header))
                {
                    // Validate data
                    if(data == null || data.length == 0 || !(data[0] instanceof BlockPos)) {
                        ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("BUG! Target Data: " + Arrays.toString(data)));
                        return;
                    }

                    // Get data
                    final BlockPos pos = (BlockPos) data[0];
                    if (new Pos((IPos3D) this).distance(pos) <= ConfigLauncher.RANGE)
                    {
                        setTarget(pos);
                        launch();
                        ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + pos)); //TODO translate
                    }
                }
                //Remote detonator signal
                else if (messageHeader.equals(RadioHeaders.FIRE_LAUNCHER.header))
                {
                    ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + getTarget())); //TODO translate
                    launch();
                }
            }
        }
    }

    @Override
    public Cube getRadioReceiverRange() {
        return RadioRegistry.INFINITE;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (isServer())
        {
            RadioRegistry.add(this);
        }
    }

    @Override
    public void invalidate()
    {
        RadioRegistry.remove(this);
        super.invalidate();
    }

    public BlockPos getTarget()
    {
        return this._targetPos;
    }

    /**
     * Called to set the target
     *
     * @param target
     */
    @Deprecated //TODO switch with blockPos
    public void setTarget(BlockPos target)
    {
        final LauncherSetTargetEvent event = new LauncherSetTargetEvent(world, pos, target);
        if(!MinecraftForge.EVENT_BUS.post(event))
        {
            this._targetPos = event.target == null ? target : event.target;
            updateClient = true;
        }
    }

    /**
     * What is the frequency of the device
     *
     * @return Hz value
     */
    public int getFrequency()
    {
        return this.frequency;
    }

    /**
     * Called to se the frequency of the device
     *
     * @param frequency - Hz value
     */
    public void setFrequency(int frequency)
    {
        this.frequency = frequency;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileLauncherScreen> SAVE_LOGIC = new NbtSaveHandler<TileLauncherScreen>()
        .mainRoot()
        /* */.nodeInteger(NBTConstants.TARGET_HEIGHT, launcher -> launcher.lockHeight, (launcher, h) -> launcher.lockHeight = h)
        /* */.nodeInteger(NBTConstants.FREQUENCY, launcher -> launcher.frequency, (launcher, f) -> launcher.frequency = f)
        /* */.nodeBlockPos(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        .base();
}
