package icbm.classic.prefab;

import com.builtbroken.mc.api.abstraction.world.IPosWorld;
import com.builtbroken.mc.api.abstraction.world.IWorld;
import com.builtbroken.mc.api.data.IPacket;
import com.builtbroken.mc.core.Engine;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.core.network.packet.PacketTile;
import com.builtbroken.mc.core.network.packet.PacketType;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public abstract class TileMachine extends TileEntity implements IPacketIDReceiver, IPosWorld
{
    public static final int DESC_PACKET_ID = -1;
    /**
     * Toggle to send a {@link #getUpdatePacket()} on the next tick, keep in mind only do this for render data.
     * if the data is not used by the renderer then send it at the time it is needed. For example, GUI data
     * should be sent to only GUI users and not everyone.
     */
    protected boolean updateClient = false;

    protected int ticks = 0;

    public void update()
    {
        ticks++;
        if (ticks == Integer.MAX_VALUE - 1)
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
            if (ticks % 3 == 0)
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

    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket()
    {
        return Engine.packetHandler.toMCPacket(getDescPacket());
    }

    public PacketTile getDescPacket()
    {
        return null;
    }

    public void sendPacketToGuiUsers(IPacket packet)
    {

    }

    @Override
    public boolean read(ByteBuf buf, int id, EntityPlayer player, PacketType type)
    {
        return false;
    }

    /**
     * Packet sent to GUI users
     *
     * @return
     */
    protected PacketTile getGUIPacket()
    {
        return null;
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
        if(tier != getTier())
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
}
