package icbm.classic.content.blocks.launcher.cruise;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.source.cause.RedstoneCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.IGuiTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TileCruiseLauncher extends TileLauncherPrefab implements IPacketIDReceiver, IGuiTile, ILauncherComponent
{
    public static final String ERROR_TRANSLATION = "launcher.cruise.error";
    public static final String ERROR_NO_POWER = ERROR_TRANSLATION + ".power";
    public static final String ERROR_NO_MISSILE = ERROR_TRANSLATION + ".missile.none";
    public static final String ERROR_MISSILE_SPACE = ERROR_TRANSLATION + ".missile.space";
    public static final String ERROR_NO_TARGET = ERROR_TRANSLATION + ".target.none";
    public static final String ERROR_MIN_RANGE = ERROR_TRANSLATION + ".target.min";
    public static final String READY_TRANSLATION = "launcher.cruise.ready";

    public static final int DESCRIPTION_PACKET_ID = 0;
    public static final int SET_FREQUENCY_PACKET_ID = 1;
    public static final int SET_TARGET_PACKET_ID = 2;
    public static final int LAUNCH_PACKET_ID = 3;

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    public static final double MISSILE__HOLDER_Y = 2.0;

    /** Target position of the launcher */
    private Vec3d _targetPos = Vec3d.ZERO;

    /** Desired aim angle, updated every tick if target != null */
    protected final EulerAngle aim = new EulerAngle(0, 0, 0); //TODO change UI to only have yaw and pitch, drop xyz but still allow tools to auto fill from xyz

    /** Current aim angle, updated each tick */
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();

    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    public final CruiseInventory inventory = new CruiseInventory(this);
    protected final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, CruiseInventory.SLOT_MISSILE);
    protected final CLauncherCapability launcher = new CLauncherCapability(this);

    protected boolean doLaunchNext = false;
    protected IMissileCause nextFireCause;

    private final LauncherNode launcherNode = new LauncherNode(this, true);
    public final RadioCruise radioCap = new RadioCruise(this);

    @Override
    public void onLoad()
    {
        super.onLoad();
        launcherNode.connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void invalidate()
    {
        if (isServer()) {
            RadioRegistry.remove(radioCap);
        }
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    /**
     * Gets the translation to use for showing status to the user. Should
     * only be used for long format displays.
     *
     * @return The string to be displayed
     */
    public ITextComponent getStatusTranslation()
    {
        if (!hasChargeToFire())
        {
            return new TextComponentTranslation(ERROR_NO_POWER);
        }
        // Checks for empty slot
        else if (!missileHolder.hasMissile())
        {
            return new TextComponentTranslation(ERROR_NO_MISSILE);
        }
        else if (!hasTarget())
        {
            return new TextComponentTranslation(ERROR_NO_TARGET);
        }
        else if (this.isTooClose(getTarget()))
        {
           return new TextComponentTranslation(ERROR_MIN_RANGE);
        }
        else if (!canSpawnMissileWithNoCollision())
        {
            return new TextComponentTranslation(ERROR_MISSILE_SPACE);
        }

        return new TextComponentTranslation(READY_TRANSLATION);
    }

    @Override
    public void update()
    {
        super.update();

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        // Fill internal battery
        this.dischargeItem(inventory.getEnergySlot());

        // Update current aim
        currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

        // Check redstone
        if(this.ticks % REDSTONE_CHECK_RATE == 0) {
            for(EnumFacing side : EnumFacing.VALUES) {
                final int power = world.getRedstonePower(getPos().offset(side), side);
                if(power > 1) {
                    nextFireCause = new RedstoneCause(world(), getPos(), getBlockState(), side);
                    doLaunchNext = true;
                }
            }
        }

        // Check redstone
        if (isServer() && isAimed() && doLaunchNext)
        {
            launcher.launch(new BasicTargetData(getTarget()), nextFireCause, false);
        }
    }


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
            updateAimAngle();
        }
    }

    protected boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle()
    {
        if (hasTarget())
        {
            final Vec3d aimPoint = getTarget();
            final Pos center = new Pos((IPos3D) this).add(0.5, MISSILE__HOLDER_Y, 0.5);
            aim.set(center.toEulerAngle(aimPoint).clampTo360());
            aim.setYaw(EulerAngle.clampPos360(aim.yaw()));
        }
        else
        {
            aim.set(0, 0, 0);
        }
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile("gui", DESCRIPTION_PACKET_ID, this).addData(getEnergy(), this.radioCap.getChannel(), this.getTarget().x, this.getTarget().y, this.getTarget().z);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, id, player, type))
        {
            switch (id)
            {
                //set frequency packet from GUI
                case SET_FREQUENCY_PACKET_ID:
                {
                    this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                    return true;
                }
                //Set target packet from GUI
                case SET_TARGET_PACKET_ID:
                {
                    this.setTarget(new Vec3d(data.readDouble(), data.readDouble(), data.readDouble()));
                    return true;
                }
                //launch missile
                case LAUNCH_PACKET_ID:
                {
                    final EntityCause cause = new EntityCause(player); // TODO note was UI interaction
                    launcher.launch(new BasicTargetData(getTarget()), cause, false);
                    return true;
                }
                case DESCRIPTION_PACKET_ID:
                {
                    if (isClient())
                    {
                        setEnergy(data.readInt());
                        this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                        this.setTarget(new Vec3d(data.readDouble(), data.readDouble(), data.readDouble()));
                    }
                    return true;
                }
                default:
                    return false;
            }
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        ByteBufUtils.writeItemStack(buf, this.missileHolder.getMissileStack());

        buf.writeDouble(getTarget().x);
        buf.writeDouble(getTarget().y);
        buf.writeDouble(getTarget().z);

        buf.writeDouble(currentAim.yaw());
        buf.writeDouble(currentAim.pitch());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        cachedMissileStack = ByteBufUtils.readItemStack(buf);

        setTarget(new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));

        currentAim.setYaw(buf.readDouble());
        currentAim.setPitch(buf.readDouble());
    }

    protected void initFromLoad()
    {
        cachedMissileStack = inventory.getStackInSlot(0);
        updateAimAngle();
        currentAim.set(aim);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        super.onDataPacket(net, pkt);
        initFromLoad();
    }

    public void sendHzPacket(String channel) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("frequency_C>S", TileCruiseLauncher.SET_FREQUENCY_PACKET_ID, this).addData(channel));
        }
    }

    public void sendTargetPacket(Vec3d data) {
        if(isClient()) {
            ICBMClassic.packetHandler.sendToServer(new PacketTile("target_C>S", TileCruiseLauncher.SET_TARGET_PACKET_ID, this).addData(data));
        }
    }

    //@Override
    public boolean canLaunch()
    {
        return hasTarget()
                && isAimed()
                && missileHolder.hasMissile()
                && hasChargeToFire()
                && !this.isTooClose(this.getTarget())
                && canSpawnMissileWithNoCollision();
    }

    protected boolean hasTarget()
    {
        return getTarget() != null && getTarget() != Vec3d.ZERO;
    }

    protected boolean hasChargeToFire()
    {
        return this.checkExtract();
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        // TODO use raytrace to detect collision so we can fire out of holes
         for (int y = 1; y <= 2; y++) {
             for (int x = -1; x < 2; x++) {
                 for (int z = -1; z < 2; z++) {
                     final BlockPos pos = getPos().add(x, y, z);
                     final IBlockState state = world.getBlockState(pos);
                     Block block = state.getBlock();
                     if (!block.isAir(state, world, pos)) {
                         return false;
                     }
                 }
             }
         }
        return true;
    }

    // Is the target too close?
    public boolean isTooClose(Vec3d target)
    {
        return new Pos(getPos()).add(0.5).distance(target) < 20;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-2, 0, -2, 2, 3, 2).add(new Pos((IPos3D) this)).toAABB();
    }

    @Override
    public Object getServerGuiElement(int id, EntityPlayer player)
    {
        return new ContainerCruiseLauncher(player, this);
    }

    @Override
    public Object getClientGuiElement(int id, EntityPlayer player)
    {
        return new GuiCruiseLauncher(player, this);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing)
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) inventory;
        } else if (capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return (T) missileHolder;
        }
        else if(capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY) {
            return (T) launcher;
        }
        else if(capability == ICBMClassicAPI.RADIO_CAPABILITY)
        {
            return (T) radioCap;
        }
        return super.getCapability(capability, facing);
    }

    public Vec3d getTarget()
    {
        if (this._targetPos == null)
        {
            this._targetPos = Vec3d.ZERO;
        }
        return this._targetPos;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
        if(nbt.hasKey(NBTConstants.FREQUENCY)) {
            this.radioCap.setChannel(Integer.toString(nbt.getInteger(NBTConstants.FREQUENCY)));
        }
        initFromLoad();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {   SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileCruiseLauncher> SAVE_LOGIC = new NbtSaveHandler<TileCruiseLauncher>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBTConstants.INVENTORY, launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radioCap)
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        /* */.nodeEulerAngle(NBTConstants.CURRENT_AIM, launcher -> launcher.currentAim, (launcher, pos) -> launcher.currentAim.set(pos))
        .base();
}
