package icbm.classic.content.blocks.launcher.cruise;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioHeaders;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.tile.IGuiTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;

public class TileCruiseLauncher extends TileLauncherPrefab implements IPacketIDReceiver, IGuiTile
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
    private final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, CruiseInventory.SLOT_MISSILE);

    private boolean doLaunchNext = false;

    /**
     * Gets the translation to use for showing status to the user. Should
     * only be used for long format displays.
     *
     * @return The string to be displayed
     */
    public String getStatusTranslation()
    {
        if (!hasChargeToFire())
        {
            return ERROR_NO_POWER;
        }
        // Checks for empty slot
        else if (!missileHolder.hasMissile())
        {
            return ERROR_NO_MISSILE;
        }
        else if (!hasTarget())
        {
            return ERROR_NO_TARGET;
        }
        else if (this.isTooClose(getTarget()))
        {
           return ERROR_MIN_RANGE;
        }
        else if (!canSpawnMissileWithNoCollision())
        {
            return ERROR_MISSILE_SPACE;
        }

        return READY_TRANSLATION;
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
        if (isServer() && isAimed() && shouldFire())
        {
            this.launch();
        }
    }

    private boolean shouldFire() {
        return doLaunchNext || this.ticks % REDSTONE_CHECK_RATE == 0 && this.world.getStrongPower(getPos()) > 0;
    }

    @Override
    public void setTarget(Pos target)
    {
        super.setTarget(target);
        updateAimAngle();
    }

    protected boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle()
    {
        if (getTarget() != null && !getTarget().isZero())
        {
            Pos aimPoint = getTarget();
            Pos center = new Pos((IPos3D) this).add(0.5);
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
        return new PacketTile("gui", DESCRIPTION_PACKET_ID, this).addData(getEnergy(), this.getFrequency(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
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
                    this.setFrequency(data.readInt());
                    return true;
                }
                //Set target packet from GUI
                case SET_TARGET_PACKET_ID:
                {
                    this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                    return true;
                }
                //launch missile
                case LAUNCH_PACKET_ID:
                {
                    launch();
                    return true;
                }
                case DESCRIPTION_PACKET_ID:
                {
                    if (isClient())
                    {
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
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

        buf.writeInt(getTarget().xi());
        buf.writeInt(getTarget().yi());
        buf.writeInt(getTarget().zi());

        buf.writeDouble(currentAim.yaw());
        buf.writeDouble(currentAim.pitch());
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        cachedMissileStack = ByteBufUtils.readItemStack(buf);

        setTarget(new Pos(buf.readInt(), buf.readInt(), buf.readInt()));

        currentAim.setYaw(buf.readDouble());
        currentAim.setPitch(buf.readDouble());
    }

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        inventory.deserializeNBT(nbt.getCompoundTag(NBTConstants.INVENTORY));
        currentAim.readFromNBT(nbt.getCompoundTag(NBTConstants.CURRENT_AIM));
        initFromLoad();
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag(NBTConstants.INVENTORY, inventory.serializeNBT());
        nbt.setTag(NBTConstants.CURRENT_AIM, currentAim.writeNBT(new NBTTagCompound()));
        return super.writeToNBT(nbt);
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
        return getTarget() != null && !getTarget().isZero();
    }

    protected boolean hasChargeToFire()
    {
        return this.checkExtract();
    }

    protected boolean canSpawnMissileWithNoCollision()
    {
        //Make sure there is noting above us to hit when spawning the missile
        for (int x = -1; x < 2; x++)
        {
            for (int z = -1; z < 2; z++)
            {
                BlockPos pos = getPos().add(x, 1, z);
                IBlockState state = world.getBlockState(pos);
                Block block = state.getBlock();
                if (!block.isAir(state, world, pos))
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Launches the missile
     *
     * @return true if launched, false if not
     */
    //@Override
    public boolean launch()
    {
        this.doLaunchNext = false;
        if (this.canLaunch())
        {
            final ItemStack inventoryStack = missileHolder.getMissileStack();

            if(inventoryStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null)) {
                final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
                if(capabilityMissileStack != null) {
                    final IMissile missile = capabilityMissileStack.newMissile(world);
                    final Entity entity = missile.getMissileEntity();
                    if(entity instanceof IMissileAiming) {

                        //Aim missile
                        ((IMissileAiming) entity).initAimingPosition(xi() + 0.5, yi() + 1.5, zi() + 0.5,
                            -(float) currentAim.yaw() - 180, -(float) currentAim.pitch(), 1, ConfigMissile.DIRECT_FLIGHT_SPEED);

                        //Setup missile
                        missile.setFlightLogic(new DeadFlightLogic(ConfigMissile.CRUISE_FUEL));
                        missile.launch();

                        if(world.spawnEntity(entity)) {
                            this.extractEnergy();
                            inventory.setStackInSlot(0, capabilityMissileStack.consumeMissile());
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(getPos()).add(0.5).distance(new Pos(target.x() + .5, target.y() + .5, target.z() + .5)) < 20;
    }

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data) //TODO pack as message object
    {
        //Floor frequency as we do not care about sub ranges
        final int frequency = (int) Math.floor(hz);
        if (isServer() && frequency == this.getFrequency())
        {
            //Laser detonator signal
            if (messageHeader.equals(RadioHeaders.FIRE_AT_TARGET.header))
            {
                final Pos pos = (Pos) data[0];
                if (!isTooClose(pos))
                {
                    setTarget(pos);
                    this.doLaunchNext = true;
                    ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + pos));
                }
            }
            //Remote detonator signal
            else if (messageHeader.equals(RadioHeaders.FIRE_LAUNCHER.header))
            {
                ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Firing missile at " + getTarget()));
                this.doLaunchNext = true;
            }
        }
    }

    @Override
    public boolean targetWithYValue()
    {
        return true;
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
        //Run before screen check to prevent looping
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY && (facing == EnumFacing.DOWN || facing == null) || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY)
        {
            return true;
        }
        return super.hasCapability(capability, facing);
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
        return super.getCapability(capability, facing);
    }
}
