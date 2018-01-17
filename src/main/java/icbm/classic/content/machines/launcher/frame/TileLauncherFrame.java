package icbm.classic.content.machines.launcher.frame;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.api.tile.multiblock.IMultiTile;
import com.builtbroken.mc.api.tile.multiblock.IMultiTileHost;
import com.builtbroken.mc.core.network.IPacketIDReceiver;
import com.builtbroken.mc.framework.multiblock.EnumMultiblock;
import com.builtbroken.mc.framework.multiblock.MultiBlockHelper;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import icbm.classic.prefab.TileMachine;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.HashMap;

/**
 * This tile entity is for the screen of the missile launcher
 *
 * @author Calclavia
 */
public class TileLauncherFrame extends TileMachine implements IPacketIDReceiver, IMultiTileHost
{
    public static HashMap<IPos3D, String> tileMapCache = new HashMap();

    static
    {
        tileMapCache.put(new Pos(0, 1, 0), EnumMultiblock.TILE.getTileName());
        tileMapCache.put(new Pos(0, 2, 0), EnumMultiblock.TILE.getTileName());
    }

    // The tier of this screen
    private int tier = 0;

    private boolean _destroyingStructure = false;

    @Override
    public void writeDescPacket(ByteBuf buf)
    {
        super.writeDescPacket(buf);
        //buf.writeInt(tier);
    }

    @Override
    public void readDescPacket(ByteBuf buf)
    {
        super.readDescPacket(buf);
       // this.setTier(buf.readInt());
    }

    /** Gets the inaccuracy of the missile based on the launcher support frame's tier */
    public int getInaccuracy()
    {
        switch (this.tier)
        {
            default:
                return 15;
            case 1:
                return 7;
            case 2:
                return 1;
        }
    }

    /** Reads a tile entity from NBT. */
    @Override
    public void readFromNBT(NBTTagCompound par1NBTTagCompound)
    {
        super.readFromNBT(par1NBTTagCompound);
        this.tier = par1NBTTagCompound.getInteger("tier");
    }

    /** Writes a tile entity to NBT. */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound par1NBTTagCompound)
    {
        par1NBTTagCompound.setInteger("tier", this.tier);
        return super.writeToNBT(par1NBTTagCompound);
    }

    //==========================================
    //==== Multi-Block code
    //=========================================

    //@Override
    public void firstTick()
    {
        //super.firstTick();
        MultiBlockHelper.buildMultiBlock(world, this, true, true);
    }

    @Override
    public void onMultiTileAdded(IMultiTile tileMulti)
    {
        if (tileMulti instanceof TileEntity)
        {
            if (tileMapCache.containsKey(new Pos((TileEntity) this).sub(new Pos((TileEntity) tileMulti))))
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
            Pos pos = new Pos((TileEntity) tileMulti).sub(new Pos((TileEntity) this));

            if (tileMapCache.containsKey(pos))
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

    /* TODO
    @Override
    public boolean canPlaceBlockAt()
    {
        return super.canPlaceBlockAt() && oldWorld().getBlock(xi(), yi() + 1, zi()).isReplaceable(oldWorld(), xi(), yi() + 1, zi()) && oldWorld().getBlock(xi(), yi() + 2, zi()).isReplaceable(oldWorld(), xi(), yi() + 2, zi());
    }

    @Override
    public boolean canPlaceBlockOnSide(ForgeDirection side)
    {
        return side == ForgeDirection.UP && canPlaceBlockAt();
    }

    @Override
    public boolean removeByPlayer(EntityPlayer player, boolean willHarvest)
    {
        MultiBlockHelper.destroyMultiBlockStructure(this, false, true, false);
        return super.removeByPlayer(player, willHarvest);
    }
    */

    @Override
    public boolean onMultiTileActivated(IMultiTile tile, EntityPlayer player, EnumHand hand, EnumFacing side, float xHit, float yHit, float zHit)
    {
        //TODO return this.onPlayerRightClick(player, side, new Pos(xHit, yHit, zHit));
        return true;
    }

    @Override
    public void onMultiTileClicked(IMultiTile tile, EntityPlayer player)
    {

    }

    @Override
    public HashMap<IPos3D, String> getLayoutOfMultiBlock()
    {
        return tileMapCache;
    }


    @Override
    public AxisAlignedBB getRenderBoundingBox()
    {
        return new Cube(-1, 0, -1, 1, 3, 1).add(toPos()).toAABB();
    }
}

