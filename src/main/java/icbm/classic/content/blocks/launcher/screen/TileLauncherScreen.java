package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.launcher.IActionStatus;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.blocks.launcher.screen.gui.ContainerLaunchScreen;
import icbm.classic.content.blocks.launcher.screen.gui.GuiLauncherScreen;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.NBTConstants;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherScreen extends TilePoweredMachine implements IPacketIDReceiver, ILauncherComponent
{
    public static final int SET_FREQUENCY_PACKET_ID = 0;
    public static final int SET_TARGET_PACKET_ID = 1;
    public static final int LAUNCH_PACKET_ID = 2;

    /** Height to wait before missile curves */
    public int lockHeight = 3;

    /** Target position of the launcher */
    private Vec3d _targetPos = Vec3d.ZERO;

    public final InventoryWithSlots inventory = new InventoryWithSlots(1)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, EnergySystem::isEnergyItem).withTick(this::dischargeItem));
    private final LauncherNode launcherNode = new LauncherNode(this, false);
    public final RadioScreen radioCap = new RadioScreen(this);

    @Getter
    private float launcherInaccuracy = 0;

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        super.update();

        // Tick inventory
        inventory.onTick();

        if (isServer())
        {
            if(getNetworkNode().getNetwork() != null) {
                final List<IMissileLauncher> launchers = getNetworkNode().getLaunchers();
                launcherInaccuracy = launchers.stream().map(l -> l.getInaccuracy(getTarget(), launchers.size())).max(Float::compareTo).orElse(0f);
            }

            //Only launch if redstone
            if (ticks % 3 == 0 && world.getStrongPower(getPos()) > 0)
            {
                fireAllLaunchers(false);
            }

            //Update packet TODO see if this is needed
            if (ticks % 3 == 0)
            {
                sendDescPacket();
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
                case SET_FREQUENCY_PACKET_ID:
                {
                    this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                    return true;
                }
                case SET_TARGET_PACKET_ID:
                {
                    this.setTarget(new Vec3d(data.readDouble(), data.readDouble(), data.readDouble()));
                    return true;
                }
                case LAUNCH_PACKET_ID:
                {
                    fireAllLaunchers(false);
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf data)
    {
        super.writeDescPacket(data);
        ByteBufUtils.writeUTF8String(data, radioCap.getChannel());
        data.writeFloat(launcherInaccuracy);
        data.writeDouble(this.getTarget().x);
        data.writeDouble(this.getTarget().y);
        data.writeDouble(this.getTarget().z);
    }

    @Override
    public void readDescPacket(ByteBuf data)
    {
        super.readDescPacket(data);
        this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
        this.launcherInaccuracy = data.readFloat();
        this.setTarget(new Vec3d(data.readDouble(), data.readDouble(), data.readDouble()));
    }

    public void sendHzPacket(String channel) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", SET_FREQUENCY_PACKET_ID, this).addData(channel));
        }
    }

    public void sendTargetPacket(Vec3d data) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("target_C>S", SET_TARGET_PACKET_ID, this).addData(data));
        }
    }

    public void sendLaunchPacket() {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("launch_C>S", SET_TARGET_PACKET_ID, this));
        }
    }

    /**
     * Calls the missile launcher base to launch it's missile towards a targeted location
     * @return true if launched, false if not
     */
    public IActionStatus launch(IMissileLauncher launcher, int launcherCount, boolean simulate)
    {
        final BlockScreenCause cause = new BlockScreenCause(world, pos, getBlockState(), launcherCount, lockHeight);
        return launcher.launch(new BasicTargetData(this.getTarget()), cause, simulate); //TODO move lockHeight to launchPad
    }

    public boolean fireAllLaunchers(boolean simulate) {
        // TODO add chain fire delay settings to screen
        boolean hasFired = false;
        for(IMissileLauncher launcher : getLaunchers()) {
            final IActionStatus status = launch(launcher, getLaunchers().size(), simulate); // TODO output status to users
            if(!status.isError()) {
                hasFired = true;
            }
        }
        return hasFired;
    }

    public boolean canLaunch() {
        return fireAllLaunchers(true);
    }

    /**
     * Gets the display status of the missile launcher
     *
     * @return The string to be displayed
     */
    public ITextComponent getStatusTranslation()
    {
        if (getNetworkNode().getNetwork() == null)
        {
            return new TextComponentTranslation("gui.icbmclassic:error.no_network");
        }
        return getNetworkNode().getLaunchers().stream().map(launcher -> {
            if (launcher == null)
            {
                return new TextComponentTranslation("gui.launcherscreen.statusMissing");
            }

            final IActionStatus status = launch(launcher, getNetworkNode().getLaunchers().size(), true);
            if(status.isError()) {
                return status.message();
            }
            return null;
        }).filter(Objects::nonNull).findFirst().orElse(new TextComponentTranslation("gui.launcherscreen.statusReady"));
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
        if(isServer()) {
            RadioRegistry.remove(radioCap);
        }
        getNetworkNode().onTileRemoved();
        super.invalidate();
    }

    public Vec3d getTarget()
    {
        return this._targetPos;
    }

    /**
     * Called to set the target
     *
     * @param target
     */
    public void setTarget(Vec3d target)
    {
        if(target != this._targetPos) {

            // Only fire packet server side to avoid description packet triggering events
            if(isServer()) {
                final LauncherSetTargetEvent event = new LauncherSetTargetEvent(world, getPos(), target);

                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    this._targetPos = event.target == null ? Vec3d.ZERO : event.target;
                    updateClient = true;
                }
            }
            else {
                this._targetPos = target;
            }
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
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        .base();
}
