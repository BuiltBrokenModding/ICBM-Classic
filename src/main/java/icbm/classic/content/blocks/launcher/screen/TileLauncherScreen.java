package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.launcher.IMissileLauncherStatus;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.NBTConstants;
import icbm.classic.prefab.tile.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TileMachine implements IPacketIDReceiver, ILauncherComponent
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

    public int clientEnergyStored = 0;
    public int clientEnergyCapacity = 0;

    private final LauncherNode launcherNode = new LauncherNode(this, false);
    public final RadioScreen radioCap = new RadioScreen(this);

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
            //Only launch if redstone
            if (ticks % 3 == 0 && world.getStrongPower(getPos()) > 0)
            {
                fireAllLaunchers();
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
        final int energy = Optional.ofNullable(getNetworkNode().getNetwork()).map(network -> network.energyStorage.getEnergyStored()).orElse(0);
        final int energyCap = Optional.ofNullable(getNetworkNode().getNetwork()).map(network -> network.energyStorage.getMaxEnergyStored()).orElse(0);
        return new PacketTile("desc", 0, this).addData(energy, energyCap, radioCap.getChannel(), this.lockHeight, this.getTarget().getX(), this.getTarget().getY(), this.getTarget().getZ());
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
                        this.clientEnergyStored = data.readInt();
                        this.clientEnergyCapacity = data.readInt();
                        this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                        this.lockHeight = data.readInt();
                        this.setTarget(new BlockPos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                    break;
                }
                case SET_FREQUENCY_PACKET_ID:
                {
                    this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                    return true;
                }
                case SET_TARGET_PACKET_ID:
                {
                    this.setTarget(new BlockPos(data.readInt(), data.readInt(), data.readInt()));
                    return true;
                }
                case LOCK_HEIGHT_PACKET_ID:
                {
                    this.lockHeight = data.readInt();
                    return true;
                }
                case LAUNCH_PACKET_ID:
                {
                    fireAllLaunchers();
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Calls the missile launcher base to launch it's missile towards a targeted location
     * @return true if launched, false if not
     */
    public IMissileLauncherStatus launch(IMissileLauncher launcher, int launcherCount, boolean simulate)
    {
        final BlockScreenCause cause = new BlockScreenCause(world, pos, getBlockState(), launcherCount, lockHeight);
        return launcher.launch(new BasicTargetData(this.getTarget()), cause, simulate); //TODO move lockHeight to launchPad
    }

    public boolean fireAllLaunchers() {
        // TODO add chain fire delay settings to screen
        boolean hasFired = false;
        for(IMissileLauncher launcher : getLaunchers()) {
            final IMissileLauncherStatus status = launch(launcher, getLaunchers().size(), false); // TODO output status to users
            if(!status.isError()) {
                hasFired = true;
            }
        }
        return hasFired;
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    public String getStatus()
    {
        if (getNetworkNode().getNetwork() == null)
        {
            return LanguageUtility.getLocal("gui.launcherscreen.noNetwork");
        }
        return getNetworkNode().getLaunchers().stream().map(launcher -> {
            if (launcher == null)
            {
                return LanguageUtility.getLocal("gui.launcherscreen.statusMissing");
            }

            final IMissileLauncherStatus status = launch(launcher, getNetworkNode().getLaunchers().size(), true);
            if(status.isError()) {
                return status.message();
            }
            return "";
        }).filter(s -> !s.isEmpty()).findFirst().orElse(LanguageUtility.getLocal("gui.launcherscreen.statusReady"));
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
    public void onLoad()
    {
        super.onLoad();
        getNetworkNode().connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void invalidate()
    {
        RadioRegistry.remove(radioCap);
        getNetworkNode().onTileRemoved();
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
        final LauncherSetTargetEvent event = new LauncherSetTargetEvent(world, pos, new Vec3d(target));
        if(!MinecraftForge.EVENT_BUS.post(event))
        {
            this._targetPos = event.target == null ? target : new BlockPos(event.target);
            updateClient = true;
        }
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing)
            || capability == ICBMClassicAPI.RADIO_CAPABILITY
            || Optional.ofNullable(getNetworkNode().getNetwork()).map(network -> network.hasCapability(capability, facing)).orElse(false);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == ICBMClassicAPI.RADIO_CAPABILITY) {
            return (T) radioCap;
        }
        else if (getNetworkNode().getNetwork() != null)
        {
            final T cap = getNetworkNode().getNetwork().getCapability(capability, facing);
            if(cap != null) {
                return cap;
            }
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);

        // Legacy handling TODO data fixer
        if(nbt.hasKey(NBTConstants.FREQUENCY)) {
            this.radioCap.setChannel(Integer.toString(nbt.getInteger(NBTConstants.FREQUENCY)));
        }
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
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radioCap)
        /* */.nodeBlockPos(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        .base();
}
