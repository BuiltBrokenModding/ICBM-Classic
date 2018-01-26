package icbm.classic.prefab;

import com.builtbroken.mc.api.abstraction.world.IPosWorld;
import com.builtbroken.mc.api.abstraction.world.IWorld;
import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.api.energy.IEnergyBuffer;
import com.builtbroken.mc.api.tile.IPlayerUsing;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import com.builtbroken.mc.data.Direction;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public abstract class TileMachine extends TileEntity implements IPacketIDReceiver, IPosWorld, IPlayerUsing, ITickable
{
    public static final int DESC_PACKET_ID = -1;
    /**
     * Toggle to send a {@link #getUpdatePacket()} on the next tick, keep in mind only do this for render data.
     * if the data is not used by the renderer then send it at the time it is needed. For example, GUI data
     * should be sent to only GUI users and not everyone.
     */
    protected boolean updateClient = false;

    protected int ticks = 0;

    List<EntityPlayer> playersWithGUI = new ArrayList();

    @Override
    public void  update()
    {
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1)
        {
            ticks = 0;
        }

        if (isServer())
        {
            //Sync client(s) if needed
            if (updateClient)
            {
                updateClient = false;
                sendDescPacket();
            }
            //Sync GUI data to client(s)
            if (ticks % 3 == 0 && getPlayersUsing().size() > 0)
            {
                PacketTile packet = getGUIPacket();
                if (packet != null)
                {
                    sendPacketToGuiUsers(packet);
                }
            }
        }
    }

    public void sendDescPacket()
    {
        PacketTile packetTile = getDescPacket();
        if (packetTile != null)
        {
            Engine.packetHandler.sendToAllAround(packetTile, this);
        }
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return new SPacketUpdateTileEntity(pos, 0, getUpdateTag());
    }

    @Override
    public NBTTagCompound getUpdateTag()
    {
        return writeToNBT(new NBTTagCompound());
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt)
    {
        readFromNBT(pkt.getNbtCompound());
    }

    public PacketTile getDescPacket()
    {
        PacketTile packetTile = new PacketTile("desc",this);
        packetTile.data().writeInt(DESC_PACKET_ID);
        writeDescPacket(packetTile.data());
        return packetTile;
    }

    public void sendPacketToGuiUsers(IPacket packet)
    {
        if (packet != null)
        {
            Iterator<EntityPlayer> it = getPlayersUsing().iterator();
            while (it.hasNext())
            {
                final EntityPlayer player = it.next();
                if (player instanceof EntityPlayerMP && isValidGuiUser(player))
                {
                    Engine.packetHandler.sendToPlayer(packet, (EntityPlayerMP) player);
                }
                else
                {
                    it.remove();
                }
            }
        }
    }

    protected boolean isValidGuiUser(EntityPlayer player)
    {
        return player.openContainer != null;
    }

    @Override
    public final List<EntityPlayer> getPlayersUsing()
    {
        return playersWithGUI;
    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        if (isClient())
        {
            if (id == DESC_PACKET_ID)
            {
                readDescPacket(buf);
                return true;
            }
        }
        return false;
    }


    public void writeDescPacket(ByteBuf buf)
    {

    }

    public void readDescPacket(ByteBuf buf)
    {

    }

    /**
     * Packet sent to GUI users
     *
     * @return
     */
    protected PacketTile getGUIPacket()
    {
        return getDescPacket();
    }

    public boolean isServer()
    {
        return !world.isRemote;
    }

    public boolean isClient()
    {
        return world.isRemote;
    }

    public EnumFacing getRotation()
    {
        return getBlockState().getValue(BlockICBM.ROTATION_PROP);
    }

    public void setRotation(EnumFacing facingDirection)
    {
        //Only update if state has changed
        if (facingDirection != getRotation())
        {
            //Update block state
            world.setBlockState(pos, getBlockState().withProperty(BlockICBM.ROTATION_PROP, facingDirection));
        }
    }

    public BlockICBM.EnumTier getTier()
    {
        return getBlockState().getValue(BlockICBM.TIER_PROP);
    }

    public void setTier(BlockICBM.EnumTier tier)
    {
        if (tier != getTier())
        {
            world.setBlockState(pos, getBlockState().withProperty(BlockICBM.TIER_PROP, tier));
        }
    }

    public IBlockState getBlockState()
    {
        return world.getBlockState(getPos());
    }

    @Override
    public IWorld world()
    {
        return Engine.getWorld(world.provider.getDimension());
    }

    @Override
    public double z()
    {
        return getPos().getZ();
    }

    @Override
    public double x()
    {
        return getPos().getX();
    }

    @Override
    public double y()
    {
        return getPos().getY();
    }

    public boolean hasPower()
    {
        return true;
    }

    public void setEnergy(int energy)
    {

    }

    public int getEnergy()
    {
        return 0;
    }

    public IEnergyBuffer getEnergyBuffer(Direction side)
    {
        return null;
    }

    public int getEnergyConsumption()
    {
        return 100000;
    }

    public int getEnergyBufferSize()
    {
        return getEnergyConsumption() * 2;
    }

    public boolean checkExtract()
    {
        return getEnergy() >= getEnergyConsumption();
    }

    public void extractEnergy()
    {

    }
}
