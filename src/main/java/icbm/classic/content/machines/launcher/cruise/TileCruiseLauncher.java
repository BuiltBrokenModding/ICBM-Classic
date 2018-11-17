package icbm.classic.content.machines.launcher.cruise;

import com.builtbroken.jlib.data.vector.IPos3D;
import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import icbm.classic.content.missile.MissileFlightType;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.IGuiTile;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.transform.region.Cube;
import icbm.classic.lib.transform.rotation.EulerAngle;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.ICBMClassic;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.handlers.Explosion;
import icbm.classic.content.items.ItemMissile;
import icbm.classic.content.machines.launcher.TileLauncherPrefab;
import icbm.classic.prefab.tile.EnumTier;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import icbm.classic.api.explosion.ILauncherContainer;
import icbm.classic.api.explosion.ILauncherController;
import icbm.classic.api.explosion.LauncherType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TileCruiseLauncher extends TileLauncherPrefab implements IPacketIDReceiver, ILauncherController, ILauncherContainer, IGuiTile, IInventoryProvider<ExternalInventory>, IPeripheral
{
    /** Desired aim angle, updated every tick if target != null */
    protected final EulerAngle aim = new EulerAngle(0, 0, 0);
    /** Current aim angle, updated each tick */
    protected final EulerAngle currentAim = new EulerAngle(0, 0, 0);

    protected static double ROTATION_SPEED = 10.0;

    /** Last time rotation was updated, used in {@link EulerAngle#lerp(EulerAngle, double)} function for smooth rotation */
    protected long lastRotationUpdate = System.nanoTime();
    /** Percent of time that passed since last tick, should be 1.0 on a stable server */
    protected double deltaTime;

    ExternalInventory inventory;


    protected ItemStack cachedMissileStack = ItemStack.EMPTY;

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

        if (!this.checkExtract())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusNoPower");
        }
        else if (this.getInventory().getStackInSlot(0).isEmpty())
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.statusEmpty");
        }
        else if (this.getInventory().getStackInSlot(0).getItem() != ICBMClassic.itemMissile)
        {
            status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
        }
        else
        {
            final Explosion missile = (Explosion) Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage()).handler;
            if (missile == null)
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.invalidMissile");
            }
            else if (!missile.isCruise())
            {
                status = LanguageUtility.getLocal("gui.launcherCruise.notCruiseMissile");
            }
            else if (missile.getTier().ordinal() > 3)
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

        if (getTarget() != null && !getTarget().isZero())
        {
            Pos aimPoint = getTarget();
            Pos center = new Pos((IPos3D) this).add(0.5);
            if (ICBMClassic.runningAsDev)
            {
                //Engine.packetHandler.sendToAllAround(new PacketSpawnParticleStream(world.provider.getDimension(), center, aimPoint), this);
            }
            aim.set(center.toEulerAngle(aimPoint).clampTo360());

            currentAim.moveTowards(aim, ROTATION_SPEED, deltaTime).clampTo360();

            if (isServer())
            {
                if (this.ticks % 40 == 0 && this.world.getRedstonePowerFromNeighbors(getPos()) > 0)
                {
                    this.launch();
                }
            }
        }
    }

    @Override
    public void placeMissile(ItemStack itemStack)
    {
        getInventory().setInventorySlotContents(0, itemStack);
    }

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile("gui", 0, this).addData(getEnergy(), this.getFrequency(), this.getTarget().xi(), this.getTarget().yi(), this.getTarget().zi());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, id, player, type))
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
            else
            {
                switch (id)
                {
                    //GUI description packet
                    case 0:
                    {
                        setEnergy(data.readInt());
                        this.setFrequency(data.readInt());
                        this.setTarget(new Pos(data.readInt(), data.readInt(), data.readInt()));
                        return true;
                    }
                }
            }
            return false;
        }
        return true;
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeBoolean(getInventory().getStackInSlot(0) != null);
        if (getInventory().getStackInSlot(0) != null)
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

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        getInventory().load(nbt.getCompoundTag("inventory"));
        currentAim.readFromNBT(nbt.getCompoundTag("currentAim"));
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        nbt.setTag("inventory", getInventory().save(new NBTTagCompound()));
        nbt.setTag("currentAim", currentAim.writeNBT(new NBTTagCompound()));
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean canLaunch()
    {
        if (getTarget() != null && !getTarget().isZero())
        {
            //Validate if we have an item and a target
            if (this.getInventory().getStackInSlot(0) != null && this.getInventory().getStackInSlot(0).getItem() == ICBMClassic.itemMissile)
            {
                //Validate that the item in the slot is a missile we can fire
                final Explosion missile = (Explosion) Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage()).handler;
                if (missile != null && missile.isCruise() && missile.getTier().ordinal() <= 3)
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

            EntityMissile entityMissile = new EntityMissile(world, xi() + 0.5, yi() + 1.5, zi() + 0.5, -(float) currentAim.yaw() - 180, -(float) currentAim.pitch(), 2);
            entityMissile.missileType = MissileFlightType.CRUISE_LAUNCHER;
            entityMissile.explosiveID = Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage());
            entityMissile.acceleration = 1;
            entityMissile.launch(null);
            world.spawnEntity(entityMissile);
            //Clear slot last so we can still access data as needed or roll back changes if a crash happens
            this.getInventory().decrStackSize(0, 1);
        }
    }

    // Is the target too close?
    public boolean isTooClose(Pos target)
    {
        return new Pos(getPos()).add(0.5).distance(new Pos(target.x() + .5, target.z() + .5, target.z() + .5)) < 20;
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
            if (Explosives.get(itemStack.getItemDamage()).handler instanceof Explosion)
            {
                Explosion missile = (Explosion) Explosives.get(itemStack.getItemDamage()).handler;

                if (missile.isCruise() && missile.getTier().ordinal() <= EnumTier.THREE.ordinal())
                {
                    return true;
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
        return new Cube(-2, 0, -2, 2, 3, 2).add(new Pos((IPos3D) this)).toAABB();
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerCruiseLauncher(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiCruiseLauncher(player, this);
    }

    @Nonnull
    @Override
    public String getType() {
        return "ICBM_CruiseLauncher";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"launch", "canLaunch", "getMissile", "getStoredEnergy", "getMaxEnergy", "setTarget", "setFrequency"};
    }

    /**
     * Almost identical to LauncherScreen
     *
     * @param computer  The interface to the computer that is making the call. Remember that multiple
     *                  computers can be attached to a peripheral at once.
     * @param context   The context of the currently running lua thread. This can be used to wait for events
     *                  or otherwise yield.
     * @param method    An integer identifying which of the methods from getMethodNames() the computercraft
     *                  wishes to call. The integer indicates the index into the getMethodNames() table
     *                  that corresponds to the string passed into peripheral.call()
     * @param arguments An array of objects, representing the arguments passed into {@code peripheral.call()}.<br>
     *                  Lua values of type "string" will be represented by Object type String.<br>
     *                  Lua values of type "number" will be represented by Object type Double.<br>
     *                  Lua values of type "boolean" will be represented by Object type Boolean.<br>
     *                  Lua values of type "table" will be represented by Object type Map.<br>
     *                  Lua values of any other type will be represented by a null object.<br>
     *                  This array will be empty if no arguments are passed.
     * @return
     * @throws LuaException
     * @throws InterruptedException
     */
    @Nullable
    @Override
    public Object[] callMethod(@Nonnull IComputerAccess computer, @Nonnull ILuaContext context, int method, @Nonnull Object[] arguments) throws LuaException, InterruptedException {
        switch (method) {
            case 0:             // launch
                this.launch();
                return new Object[0];
            case 1:             // canLaunch
                return new Object[]{this.canLaunch()}; //  && !this.launcherBase.getMissileStack().isEmpty()
            case 2:             // getMissile
                if (this.getInventory().getStackInSlot(0) != null) {
                    if (this.getInventory().getStackInSlot(0).getItem() instanceof ItemMissile) {
                        return new String[]{Explosives.get(this.getInventory().getStackInSlot(0).getItemDamage()).getName()};
                    }
                }
            case 3:             // getStoredEnergy
                return new Object[]{this.getEnergy()};
            case 4:             // getMaxEnergy
                return new Object[]{this.getEnergyBufferSize()};
            case 5:             // setTarget
                if (arguments.length == 3) {
                    try {
                        System.out.println(arguments[0] + " " + arguments[1] + " " + arguments[2]);
                        int x = (int) Double.parseDouble(String.valueOf(arguments[0]));
                        int y = (int) Double.parseDouble(String.valueOf(arguments[1]));
                        int z = (int) Double.parseDouble(String.valueOf(arguments[2]));
                        this.setTarget(new Pos(x, y, z));
                        return new Object[]{true};
                    } catch (NumberFormatException ignored) {
                        throw new LuaException("Parameters x, y, z must be valid integers");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. 3 required");
                }
            case 6:             // setFrequency
                if (arguments.length == 1) {
                    try {
                        int frequency = (int) Double.parseDouble(String.valueOf(arguments[0]));
                        this.setFrequency(frequency);
                    } catch (NumberFormatException ignored) {
                        throw new LuaException("Parameter must be a valid integer");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. One required");
                }
        }
        return new Object[0];
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }
}
