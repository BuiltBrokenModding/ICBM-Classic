package icbm.classic.content.blocks.launcher.cruise;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.events.LauncherSetTargetEvent;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.machines.ConfigLauncher;
import icbm.classic.content.blocks.launcher.FiringPackage;
import icbm.classic.content.blocks.launcher.LauncherLangs;
import icbm.classic.content.blocks.launcher.cruise.gui.ContainerCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.content.blocks.launcher.network.ILauncherComponent;
import icbm.classic.content.blocks.launcher.network.LauncherNode;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.source.cause.RedstoneCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.lib.NBTConstants;
import icbm.classic.lib.capability.launcher.CapabilityMissileHolder;
import icbm.classic.lib.data.IMachineInfo;
import icbm.classic.lib.energy.storage.EnergyBuffer;
import icbm.classic.lib.energy.system.EnergySystem;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import icbm.classic.lib.network.lambda.tile.PacketCodexTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.TickAction;
import icbm.classic.lib.tile.TickDoOnce;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.gui.IPlayerUsing;
import icbm.classic.prefab.inventory.InventorySlot;
import icbm.classic.prefab.inventory.InventoryWithSlots;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.TileMachine;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;

public class TileCruiseLauncher extends TileMachine implements IGuiTile, ILauncherComponent, IMachineInfo, IPlayerUsing
{
    public static final ResourceLocation REGISTRY_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "cruiseLauncher");

    private static final int REDSTONE_CHECK_RATE = 40;
    private static final double ROTATION_SPEED = 10.0;

    public static final double MISSILE__HOLDER_Y = 2.0;

    /** Target position of the launcher */
    private Vec3d _targetPos = Vec3d.ZERO;

    /** Desired aim angle, updated every tick if target != null */
    @Getter
    protected final EulerAngle aim = new EulerAngle(0, 0, 0); //TODO change UI to only have yaw and pitch, drop xyz but still allow tools to auto fill from xyz

    /** Current aim angle, updated each tick */
    @Getter
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();

    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

    public final EnergyBuffer energyStorage = new EnergyBuffer(() -> ConfigLauncher.POWER_CAPACITY)
        .withOnChange((p,c,s) -> this.markDirty());
    public final InventoryWithSlots inventory = new InventoryWithSlots(2)
        .withChangeCallback((s, i) -> markDirty())
        .withSlot(new InventorySlot(0, ICBMClassicHelpers::isMissile).withChangeCallback((stack) -> this.markDirty()))
        .withSlot(new InventorySlot(1, EnergySystem::isEnergyItem).withTick(this.energyStorage::dischargeItem));

    @Getter
    protected final CapabilityMissileHolder missileHolder = new CapabilityMissileHolder(inventory, 0);

    @Getter
    protected final CLauncherCapability launcher = new CLauncherCapability(this);

    @Getter @Setter
    protected FiringPackage firingPackage;

    private final LauncherNode launcherNode = new LauncherNode(this, true);
    public final RadioCruise radio = new RadioCruise(this);

    private final TickDoOnce descriptionPacketSender = new TickDoOnce((t) -> PACKET_DESCRIPTION.sendToAllAround(this));

    @Getter
    private final List<EntityPlayer> playersUsing = new LinkedList<>();

    public TileCruiseLauncher() {
        tickActions.add(descriptionPacketSender);
        tickActions.add(new TickAction(3, true, (t) -> PACKET_GUI.sendPacketToGuiUsers(this, playersUsing)));
        tickActions.add(new TickAction(20, true, (t) -> {
            playersUsing.removeIf((player) -> !(player.openContainer instanceof ContainerCruiseLauncher));
        }));
        tickActions.add(inventory);
    }

    @Override
    public void markDirty()
    {
        super.markDirty();
        if(isServer()) {
            descriptionPacketSender.doNext();
        }
    }

    @Override
    public void provideInformation(BiConsumer<String, Object> consumer) {
        consumer.accept("NEEDS_POWER", ConfigMain.REQUIRES_POWER);
        consumer.accept("ENERGY_COST_ACTION", getFiringCost());
    }

    public int getFiringCost() {
        return ConfigLauncher.POWER_COST;
    }

    @Override
    public void onLoad()
    {
        super.onLoad();
        launcherNode.connectToTiles();
        if (isServer())
        {
            RadioRegistry.add(radio);
        }
    }

    @Override
    public void invalidate()
    {
        if (isServer()) {
            RadioRegistry.remove(radio);
        }
        super.invalidate();
    }

    @Override
    public LauncherNode getNetworkNode() {
        return launcherNode;
    }

    @Override
    public void update()
    {
        super.update();

        //TODO add a per tick energy consumption or at least while aiming

        deltaTime = (System.nanoTime() - lastRotationUpdate) / 100000000.0; // time / time_tick, client uses different value
        lastRotationUpdate = System.nanoTime();

        if(isServer()) {

            // Update current aim
            currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

            // Check redstone
            if (this.ticks % REDSTONE_CHECK_RATE == 0) {
                for (EnumFacing side : EnumFacing.VALUES) {
                    final int power = world.getRedstonePower(getPos().offset(side), side);
                    if (power > 1) {
                        firingPackage = new FiringPackage(new BasicTargetData(getTarget()), new RedstoneCause(getWorld(), getPos(), getBlockState(), side), 0);
                    }
                }
            }

            if(firingPackage != null && isAimed()) {
                firingPackage.setCountDown(firingPackage.getCountDown() - 1);
                if(firingPackage.getCountDown() <= 0) {
                    firingPackage.launch(launcher);
                    firingPackage = null;
                }
            }
        }
    }


    public void setTarget(Vec3d target)
    {
        if(!Objects.equals(target, this._targetPos)) {

            // Only fire packet server side to avoid description packet triggering events
            if(isServer()) {
                final LauncherSetTargetEvent event = new LauncherSetTargetEvent(world, getPos(), target);

                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    this._targetPos = event.target == null ? Vec3d.ZERO : event.target;
                    this.markDirty();
                }
            }
            else {
                this._targetPos = target;
            }
            updateAimAngle();
        }
    }

    public boolean isAimed() {
        return currentAim.isWithin(aim, 0.01);
    }

    protected void updateAimAngle()
    {
        if (hasTarget())
        {
            final Vec3d aimPoint = getTarget();
            final Pos center = new Pos(this).add(0.5, MISSILE__HOLDER_Y, 0.5);
            aim.set(center.toEulerAngle(aimPoint).clampTo360());
            aim.setYaw(EulerAngle.clampPos360(aim.yaw()));
        }
        else
        {
            aim.set(0, 0, 0);
        }
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

    protected boolean hasTarget()
    {
        return getTarget() != null && getTarget() != Vec3d.ZERO;
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
                     final Block block = state.getBlock();
                     if (!block.isAir(state, world, pos)) {
                         final AxisAlignedBB box = block.getCollisionBoundingBox(state, world, pos);
                         return box == null || box.maxY < 0.5;
                     }
                 }
             }
         }
        return true;
    }

    // Is the target too close?
    public boolean isTooClose(Vec3d target)
    {
        return new Pos(getPos()).add(0.5).distance(target) < 20; //TODO remove pos usage
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new AxisAlignedBB(getPos().add(-2, 0, -2), getPos().add(2, 3, 2));
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
            || capability == CapabilityEnergy.ENERGY && ConfigMain.REQUIRES_POWER
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_HOLDER_CAPABILITY
            || capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if(capability == CapabilityEnergy.ENERGY) {
            return (T) energyStorage;
        }
        else if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
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
            return (T) radio;
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
            this.radio.setChannel(Integer.toString(nbt.getInteger(NBTConstants.FREQUENCY)));
        }
        initFromLoad();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileCruiseLauncher> SAVE_LOGIC = new NbtSaveHandler<TileCruiseLauncher>()
        .mainRoot()
        /* */.nodeINBTSerializable(NBTConstants.INVENTORY, launcher -> launcher.inventory)
        /* */.nodeINBTSerializable("radio", launcher -> launcher.radio)
        /* */.nodeVec3d(NBTConstants.TARGET, launcher -> launcher._targetPos, (launcher, pos) -> launcher._targetPos = pos)
        /* */.nodeEulerAngle(NBTConstants.CURRENT_AIM, launcher -> launcher.currentAim, (launcher, pos) -> launcher.currentAim.set(pos))
        /* */.nodeINBTSerializable("firing_package", launcher -> launcher.firingPackage)
        /* */.nodeInteger("energy", tile -> tile.energyStorage.getEnergyStored(), (tile, i) -> tile.energyStorage.setEnergyStored(i))
        /* */.nodeINBTSerializable("launcher", launcher -> launcher.launcher)
        .base();

    public static void register() {
        GameRegistry.registerTileEntity(TileCruiseLauncher.class, REGISTRY_NAME);
        PacketCodexReg.register(PACKET_DESCRIPTION, PACKET_RADIO_HZ, PACKET_RADIO_DISABLE, PACKET_TARGET, PACKET_GUI, PACKET_LAUNCH);
    }

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_DESCRIPTION = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "description")
        .fromServer()
        .nodeItemStack((t) -> t.missileHolder.getMissileStack(), (t, f) -> t.cachedMissileStack = f)
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .nodeDouble(t -> t.currentAim.yaw(), (t, f) -> t.currentAim.setYaw(f))
        .nodeDouble(t -> t.currentAim.pitch(), (t, f) -> t.currentAim.setPitch(f));

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_HZ = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.frequency", (t) -> t.radio)
        .fromClient()
        .nodeString(RadioCruise::getChannel, RadioCruise::setChannel)
        .onFinished((r, t, p) -> r.markDirty());

    public static final PacketCodexTile<TileCruiseLauncher, RadioCruise> PACKET_RADIO_DISABLE = (PacketCodexTile<TileCruiseLauncher, RadioCruise>) new PacketCodexTile<TileCruiseLauncher, RadioCruise>(REGISTRY_NAME, "radio.disable", (t) -> t.radio)
        .fromClient()
        .toggleBoolean(RadioCruise::isDisabled, RadioCruise::setDisabled)
        .onFinished((r, t, p) -> r.markDirty());

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_TARGET = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "target")
        .fromClient()
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget)
        .onFinished((r, t, p) -> r.markDirty());;

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_GUI = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "gui")
        .fromClient()
        .nodeInt((t) -> t.energyStorage.getEnergyStored(), (t, i) -> t.energyStorage.setEnergyStored(i))
        .nodeString((t) -> t.radio.getChannel(), (t, s) -> t.radio.setChannel(s))
        .nodeBoolean((t) -> t.radio.isDisabled(), (t, b) -> t.radio.setDisabled(b))
        .nodeVec3d(TileCruiseLauncher::getTarget, TileCruiseLauncher::setTarget);

    public static final PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher> PACKET_LAUNCH = (PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>) new PacketCodexTile<TileCruiseLauncher, TileCruiseLauncher>(REGISTRY_NAME, "launch")
        .fromClient()
        .onFinished((tile, target, player) -> {
            final EntityCause cause = new EntityCause(player); // TODO note was UI interaction
            tile.getLauncher().launch(new BasicTargetData(tile.getTarget()), cause, false); // TODO send feedback to UI as part of a event list or history
            tile.markDirty();
        });
}
