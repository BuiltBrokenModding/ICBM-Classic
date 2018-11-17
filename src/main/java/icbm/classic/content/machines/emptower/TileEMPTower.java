package icbm.classic.content.machines.emptower;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import icbm.classic.api.tile.multiblock.IMultiTile;
import icbm.classic.api.tile.multiblock.IMultiTileHost;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.explosive.blast.BlastEMP;
import icbm.classic.content.multiblock.MultiBlockHelper;
import icbm.classic.lib.IGuiTile;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.prefab.inventory.ExternalInventory;
import icbm.classic.prefab.inventory.IInventoryProvider;
import icbm.classic.prefab.item.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class TileEMPTower extends TilePoweredMachine implements IMultiTileHost, IPacketIDReceiver, IGuiTile, IInventoryProvider<ExternalInventory>, IPeripheral
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150;

    public static List<BlockPos> tileMapCache = new ArrayList();

    static
    {
        tileMapCache.add(new BlockPos(0, 1, 0));
    }

    public float rotation = 0;
    private float rotationDelta;

    // The EMP mode. 0 = All, 1 = Missiles Only, 2 = Electricity Only
    public byte empMode = 0; //TODO move to enum

    private int cooldownTicks = 0;

    // The EMP explosion radius
    public int empRadius = 60;

    private boolean _destroyingStructure = false;

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

    @Override
    public void update()
    {
        super.update();
        if (isServer())
        {
            if (!isReady())
            {
                cooldownTicks--;
            }
            else
            {
                if (ticks % 20 == 0 && getEnergy() > 0)
                {
                    ICBMSounds.MACHINE_HUM.play(world, xi(), yi(), zi(), 0.5F, 0.85F * getChargePercentage(), true);
                    sendDescPacket();
                }
                if (world.getRedstonePowerFromNeighbors(getPos()) > 0)
                {
                    fire();
                }
            }
        }
        else
        {
            rotationDelta = (float) (Math.pow(getChargePercentage(), 2) * 0.5);
            rotation += rotationDelta;
            if (rotation > 360)
            {
                rotation = 0;
            }
        }
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float) getEnergyConsumption());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type)
    {
        if (!super.read(data, id, player, type))
        {
            switch (id)
            {
                case 1: //TODO constant
                {
                    empRadius = data.readInt();
                    return true;
                }
                case 2://TODO constant
                {
                    empMode = data.readByte();
                    return true;
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
        buf.writeInt(empRadius);
        buf.writeByte(empMode);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        empRadius = buf.readInt();
        empMode = buf.readByte();
    }

    @Override
    public int getEnergyBufferSize()
    {
        return Math.max(3000000 * (this.empRadius / MAX_RADIUS), 1000000);
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);

        this.empRadius = par1NBTTagCompound.getInteger("empRadius");
        this.empMode = par1NBTTagCompound.getByte("empMode");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setInteger("empRadius", this.empRadius);
        par1NBTTagCompound.setByte("empMode", this.empMode);
        return super.writeToNBT(par1NBTTagCompound);
    }

    //@Callback(limit = 1)
    public boolean fire()
    {
        if (this.checkExtract())
        {
            if (isReady())
            {
                switch (this.empMode)
                {
                    default:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectBlocks().setEffectEntities().runBlast();
                        break;
                    case 1:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectEntities().runBlast();
                        break;
                    case 2:
                        new BlastEMP(world, null, this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5, this.empRadius).setEffectBlocks().runBlast();
                        break;
                }
                this.extractEnergy();
                this.cooldownTicks = getMaxCooldown();
                return true;
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback
    public boolean isReady()
    {
        return getCooldown() <= 0;
    }

    //@Callback
    public int getCooldown()
    {
        return cooldownTicks;
    }

    //@Callback
    public int getMaxCooldown()
    {
        return 120;
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                tileMulti.setHost(this);
            }
        }
    }

    @Override
    public boolean onMultiTileBroken(IMultiTile tileMulti, Object source, boolean harvest)
    {
        if (!_destroyingStructure && tileMulti instanceof TileEntity)
        {
            if (getLayoutOfMultiBlock().contains(getPos().subtract(((TileEntity) tileMulti).getPos())))
            {
                MultiBlockHelper.destroyMultiBlockStructure(this, harvest, true, true);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onTileInvalidate(IMultiTile tileMulti)
    {

    }

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        if (isServer())
        {
            openGui(player, 0);
        }
        return true;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public List<BlockPos> getLayoutOfMultiBlock()
    {
        return tileMapCache;
    }

    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player)
    {
        return new ContainerEMPTower(player, this);
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player)
    {
        return new GuiEMPTower(player, this);
    }

    @Nonnull
    @Override
    public String getType() {
        return "ICBM_EMPTower";
    }

    @Nonnull
    @Override
    public String[] getMethodNames() {
        return new String[]{"fire", "isReady", "getStoredEnergy", "getMaxEnergy", "setFireMode", "setRadius"};
    }

    /**
     * Adds LUA control for the EMP tower
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
            case 0:             // canFire
                return new Object[]{this.fire()};
            case 1:             // isReady
                return new Object[]{this.isReady()};
            case 2:             // getStoredEnergy
                return new Object[]{this.getEnergy()};
            case 3:             // getMaxEnergy
                return new Object[]{this.getEnergyBufferSize()};
            case 4:             // setFireMode
                if (arguments.length == 1) {
                    try {
                        byte mode = (byte) Double.parseDouble(String.valueOf(arguments[0]));
                        if (mode == 0 || mode == 1 || mode == 2) {
                            this.empMode = mode;
                            return new Object[0];
                        } else {
                            throw new LuaException("Parameter mode must be either 0 (both), 1 (missiles) or 2 (blocks)");
                        }
                    } catch (NumberFormatException ignored) {
                        throw new LuaException("Parameter mode must be a valid integer");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. 1 required");
                }
            case 5:             // setRadius
                if (arguments.length == 1) {
                    try {
                        int range = (byte) Double.parseDouble(String.valueOf(arguments[0]));
                        if (range >= 10 && range <= 150) {
                            empRadius = range;
                            return new Object[0];
                        } else {
                            throw new LuaException("Parameter range must be between 10 and 150]");
                        }
                    } catch (NumberFormatException ignored) {
                        throw new LuaException("Parameter range must be a valid integer");
                    }
                } else {
                    throw new LuaException("Wrong amount of parameters. 1 required");
                }
        }
        return new Object[0];
    }

    @Override
    public boolean equals(@Nullable IPeripheral other) {
        return false;
    }
}
