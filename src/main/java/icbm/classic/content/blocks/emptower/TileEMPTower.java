package icbm.classic.content.blocks.emptower;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.energy.IEnergyBuffer;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.content.blocks.emptower.gui.ContainerEMPTower;
import icbm.classic.content.blocks.emptower.gui.GuiEMPTower;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.client.ICBMSounds;
import icbm.classic.content.blast.BlastEMP;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.network.packet.PacketTile;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.prefab.tile.IGuiTile;
import icbm.classic.prefab.tile.PowerBuffer;
import icbm.classic.prefab.tile.TilePoweredMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.items.CapabilityItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/** Logic side of the EMP tower block */
public class TileEMPTower extends TilePoweredMachine implements IPacketIDReceiver, IGuiTile
{
    // The maximum possible radius for the EMP to strike
    public static final int MAX_RADIUS = 150; //TODO move to config with a min & max
    public static final int BONUS_RADIUS = 20;
    public static final int ENERGY_COST_AREA = 100;
    public static final int COOLDOWN = 10  * 20;
    public static final int ENERGY_INPUT = 1000;

    public static final int CHANGE_RADIUS_PACKET_ID = 1; //TODO migrate to its own handler
    public static final int CHANGE_HZ_PACKET_ID = 2;
    public static final int GUI_PACKET_ID = 3;
    public static final int FIRE_PACKET_ID = 4;

    /** Tick synced rotation */
    public float rotation = 0;

    /** Client side use in render */
    public float prevRotation = 0;

    /** Delay before EMP can be fired again */
    protected int cooldownTicks = 0;

    /** Radius of the EMP tower */
    public int range = 60;

    public final EmpTowerInventory inventory = new EmpTowerInventory();
    public final RadioEmpTower radioCap = new RadioEmpTower(this);

    private List<TileEmpTowerFake> subBlocks = new ArrayList();

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (isServer())
        {
            RadioRegistry.add(radioCap);
        }
    }

    @Override
    public void invalidate()
    {
        super.invalidate();
        subBlocks.forEach(tile -> tile.setHost(null));
        subBlocks.clear();
        if (isServer()) {
            RadioRegistry.remove(radioCap);
        }
    }

    @Override
    public void update()
    {
        super.update();
        if (ticks % 3 == 0) {
            //Find tower blocks TODO find a better solution
            subBlocks.clear();
            BlockPos above = getPos().up();
            while(world.getBlockState(above).getBlock() == getBlockType()) {
                final TileEntity tile = world.getTileEntity(above);
                if(tile instanceof TileEmpTowerFake) {
                    ((TileEmpTowerFake) tile).setHost(this);
                    subBlocks.add((TileEmpTowerFake) tile);
                }
                above = above.up();
            }
        }

        // Fill internal battery
        this.dischargeItem(inventory.getEnergySlot());

        if (isServer())
        {
            if (ticks % 20 == 0 && getEnergy() > 0 && getCooldown() <= 0) //TODO convert to a mix of a timer and/or event handler
            {
                ICBMSounds.MACHINE_HUM.play(world, xi(), yi(), zi(), 0.5F, 0.85F * getChargePercentage(), true);
            }

            // TODO if in cooldown play pop sound of metal and smoke/steam particles

            // Sync every so often to keep clients matching on animations
            if(ticks % 3 == 0) {
                sendDescPacket();
            }

            if (isReady() && world.getStrongPower(getPos()) > 0) //TODO convert to a state handler
            {
                fire();
            }
        }

        prevRotation = rotation;

        if(cooldownTicks > 0) {
            cooldownTicks--;
        }
        else
        {
            rotation += (float) (Math.pow(getChargePercentage(), 2) * 10);

            while(this.rotation > 180.0F) {
                this.rotation -= 360F;
            }

            while (this.rotation - this.prevRotation< -180.0F)
            {
                this.prevRotation -= 360.0F;
            }

            while (this.rotation - this.prevRotation >= 180.0F)
            {
                this.prevRotation += 360.0F;
            }
        }
    }

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        buf.writeFloat(rotation);
        buf.writeInt(cooldownTicks);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
        rotation = buf.readFloat();
        cooldownTicks = buf.readInt();
    }

    public float getChargePercentage()
    {
        return Math.min(1, getEnergy() / (float) getEnergyConsumption());
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket type) //TODO migrate to a packet handler
    {
        if (!super.read(data, id, player, type))
        {
            if (id == CHANGE_RADIUS_PACKET_ID) {
                range = data.readInt();
                return true;
            }
            else if(id == CHANGE_HZ_PACKET_ID) {
                radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                return true;
            }
            else if(id == FIRE_PACKET_ID) {
                fire();
                return true;
            }
            else if(id == GUI_PACKET_ID && isClient()) {
                this.setEnergy(data.readInt());
                this.radioCap.setChannel(ByteBufUtils.readUTF8String(data));
                this.range = data.readInt();
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public int getEnergyBufferSize()
    {
        return getEnergyConsumption(); //TODO add configs for min-max energy state
    }

    @Override
    public int getEnergyConsumption()
    {
        return range * range * ENERGY_COST_AREA;
    }

    public int getMaxRadius() {
        return MAX_RADIUS + (subBlocks.size() * BONUS_RADIUS);
    }

    protected IBlast buildBlast()
    {
        return ((BlastEMP)ICBMExplosives.EMP.create()
                .setBlastWorld(world)
                .setBlastPosition(this.xi() + 0.5, this.yi() + 1.2, this.zi() + 0.5)
                .setBlastSize(range))
                .clearSetEffectBlocksAndEntities()
                .setEffectBlocks().setEffectEntities()
                .buildBlast();
    }

    //@Callback(limit = 1) TODO add CC support
    public boolean fire()
    {
        if (this.isReady())
        {
            //Finish and trigger
            if (buildBlast().runBlast().state == BlastState.TRIGGERED)
            {
                //Consume energy
                this.extractEnergy();

                //Reset timer
                this.cooldownTicks = getMaxCooldown();

                return true;
            }
            else
            {
                ICBMClassic.logger().warn("TileEmpTower( DIM: " + world.provider.getDimension() + ", " + getPos() + ") EMP did not trigger, likely was blocked.");
                //TODO display some info to player to explain why blast failed and more detailed debug
            }
        }
        return false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return INFINITE_EXTENT_AABB;
    }

    //@Callback TODO add CC support
    public boolean isReady()
    {
        return getCooldown() <= 0 && this.checkExtract();
    }

    //@Callback TODO add CC support
    public int getCooldown()
    {
        return cooldownTicks;
    }

    public float getCooldownPercentage()
    {
        return 1f - (cooldownTicks / (float)getMaxCooldown());
    }

    //@Callback TODO add CC support
    public int getMaxCooldown()
    {
        return COOLDOWN; //TODO add to config
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

    @Override
    public PacketTile getGUIPacket()
    {
        return new PacketTile("gui", GUI_PACKET_ID, this).addData(getEnergy(), this.radioCap.getChannel(), this.range);
    }


    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing)
            || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || capability == ICBMClassicAPI.RADIO_CAPABILITY;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
        {
            return (T) inventory;
        }
        else if(capability == ICBMClassicAPI.RADIO_CAPABILITY)
        {
            return (T) radioCap;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public IEnergyBuffer getEnergyBuffer(EnumFacing side)
    {
        if (buffer == null)
        {
            buffer = new PowerBuffer<TileEMPTower>(this) {
                @Override
                public int receiveEnergy(int maxReceive, boolean simulate)
                {
                    // Can't take energy while in cooldown
                    if(machine.cooldownTicks > 0) {
                        return 0;
                    }
                    return addEnergyToStorage(Math.min(maxReceive, ENERGY_INPUT), !simulate);
                }
            };
        }
        return buffer;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {   SAVE_LOGIC.save(this, nbt);
        return super.writeToNBT(nbt);
    }

    private static final NbtSaveHandler<TileEMPTower> SAVE_LOGIC = new NbtSaveHandler<TileEMPTower>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", tile -> tile.inventory)
        /* */.nodeINBTSerializable("radio", tile -> tile.radioCap)
        /* */.nodeInteger("range", tile -> tile.range, (launcher, pos) -> launcher.range = pos)
        /* */.nodeInteger("cooldown", tile -> tile.cooldownTicks, (launcher, pos) -> launcher.cooldownTicks = pos)
        /* */.nodeFloat("rotation", tile -> tile.rotation, (launcher, pos) -> launcher.rotation = pos)
        /* */.nodeFloat("prev_rotation", tile -> tile.prevRotation, (launcher, pos) -> launcher.prevRotation = pos)
        .base();

}
