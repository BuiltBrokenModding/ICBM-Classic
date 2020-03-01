package icbm.classic.content.blocks.launcher.cruise;

import com.builtbroken.jlib.data.vector.IPos3D;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.tile.IRadioWaveSender;
import icbm.classic.content.blocks.launcher.TileLauncherPrefab;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.missile.MissileFlightType;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.tile.IGuiTile;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileCruiseLauncher extends TileLauncherPrefab implements IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>
{
    public static final int DESCRIPTION_PACKET_ID = 0;
    public static final int SET_FREQUENCY_PACKET_ID = 1;
    public static final int SET_TARGET_PACKET_ID = 2;
    public static final int LAUNCH_PACKET_ID = 3;

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    /** Desired aim angle, updated every tick if target != null */
    protected final EulerAngle aim = new EulerAngle(0, 0, 0);

    /** Current aim angle, updated each tick */
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();

    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    private ExternalInventory inventory;

    @Override
    public ExternalInventory getInventory()
    {
        if (inventory == null)
        {
            inventory = new ExternalInventory(this, 2);
        }
        return inventory;
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

        if (!hasChargeToFire())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusNoPower");
        }
        else if (this.getInventory().getStackInSlot(0).isEmpty())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusEmpty");
        }
        else if (this.getInventory().getStackInSlot(0).getItem() != ItemReg.itemMissile)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
        }
        else
        {
            final IExplosive explosive = ICBMClassicHelpers.getExplosive(this.getInventory().getStackInSlot(0));
            if (explosive == null)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
            }
            else if (!hasMissile())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.notCruiseMissile");
            }
            else if (!hasTarget())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.statusInvalid");
            }
            else if (this.isTooClose(getTarget()))
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.targetToClose");
            }
            else if (!canSpawnMissileWithNoCollision())
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

    @Override
    public void update()
    {
        super.update();

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        //this.discharge(this.containingItems[1]); TODO

        //Update current aim
        currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

        //Check redstone
        if (isServer() && this.ticks % REDSTONE_CHECK_RATE == 0 && this.world.getRedstonePowerFromNeighbors(getPos()) > 0)
        {
            this.launch();
        }
    }

    @Override
    public void setTarget(Pos target)
    {
        super.setTarget(target);
        updateAimAngle();
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
        buf.writeBoolean(!getInventory().getStackInSlot(0).isEmpty());
        if (getInventory().getStackInSlot(0).isEmpty())
        {
            ByteBufUtils.writeItemStack(buf, getInventory().getStackInSlot(0));
        }

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
        if (buf.readBoolean())
        {
            cachedMissileStack = ByteBufUtils.readItemStack(buf);
        }
        else
        {
            cachedMissileStack = ItemStack.EMPTY;
        }
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
        getInventory().load(nbt.getCompoundTag(NBTConstants.INVENTORY));
        currentAim.readFromNBT(nbt.getCompoundTag(NBTConstants.CURRENT_AIM));
        initFromLoad();
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag(NBTConstants.INVENTORY, getInventory().save(new NBTTagCompound()));
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
                && hasMissile()
                && hasChargeToFire()
                && !this.isTooClose(this.getTarget())
                && canSpawnMissileWithNoCollision();
    }

    protected boolean hasMissile()
    {
        final ItemStack stackInSlot = this.getInventory().getStackInSlot(0);
        return stackInSlot.getItem() == ItemReg.itemMissile && ICBMClassicAPI.EX_MISSILE_REGISTRY.isEnabled(stackInSlot.getItemDamage());
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
        if (this.canLaunch())
        {
            this.extractEnergy();

            EntityMissile entityMissile = new EntityMissile(world, xi() + 0.5, yi() + 1.5, zi() + 0.5, -(float) currentAim.yaw() - 180, -(float) currentAim.pitch(), 2);
            entityMissile.missileType = MissileFlightType.CRUISE_LAUNCHER;
            entityMissile.explosiveID = this.getInventory().getStackInSlot(0).getItemDamage(); //TODO encode entire itemstack
            entityMissile.acceleration = 1;
            entityMissile.capabilityMissile.launchNoTarget();
            world.spawnEntity(entityMissile);

            //TODO we are missing the item NBT, this will prevent encoding data before using the missile

            //Clear slot last so we can still access data as needed or roll back changes if a crash happens
            this.getInventory().decrStackSize(0, 1);
            return true;
        }

        return false;
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(getPos()).add(0.5).distance(new Pos(target.x() + .5, target.z() + .5, target.z() + .5)) < 20;
    }

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data) //TODO pack as message object
    {
        //Floor frequency as we do not care about sub ranges
        final int frequency = (int) Math.floor(hz);
        if (isServer() && frequency == this.getFrequency())
        {
            //Laser detonator signal
            if (messageHeader.equals("activateLauncherWithTarget")) //TODO cache headers somewhere like API references
            {
                final Pos pos = (Pos) data[0];
                if (!isTooClose(pos))
                {
                    setTarget(pos);
                    ((FakeRadioSender) sender).player.sendMessage(new TextComponentString("Aiming missile at " + pos));
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

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        if (slot == 0)
        {
            updateClient = true;
        }
    }

    @Override
    public boolean targetWithYValue()
    {
        return true;
    }

    @Override
    public boolean canStore(ItemStack itemStack, EnumFacing side)
    {
        if (itemStack != null && itemStack.getItem() instanceof ItemMissile && this.getInventory().getStackInSlot(0) == null)
        {
            return ICBMClassicAPI.EX_MISSILE_REGISTRY.isEnabled(itemStack.getItemDamage());
        }
        return false;
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
}
