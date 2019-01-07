package icbm.classic.content.blocks.explosive;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.tile.IRotatable;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.content.items.ItemRemoteDetonator;
import icbm.classic.lib.explosive.ExplosiveHandler;
import icbm.classic.lib.explosive.cap.CapabilityExplosiveStack;
import icbm.classic.lib.network.IPacket;
import icbm.classic.lib.network.IPacketIDReceiver;
import icbm.classic.lib.transform.vector.Pos;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.network.ByteBufUtils;

public class TileEntityExplosive extends TileEntity implements IPacketIDReceiver, IRotatable
{

    /**
     * Is the tile currently exploding
     */
    public boolean exploding = false;

    public CapabilityExplosiveStack capabilityExplosive;

    /**
     * Reads a tile entity from NBT.
     */
    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        capabilityExplosive = new CapabilityExplosiveStack(nbt.getCompoundTag(CapabilityExplosiveStack.NBT_STACK));
        //this.explosive = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(nbt.getInteger("explosiveID")); TODO data fixer
        //this.nbtData = nbt.getCompoundTag("data");
    }

    /**
     * Writes a tile entity to NBT.
     */
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (capabilityExplosive != null)
        {
            nbt.setTag(CapabilityExplosiveStack.NBT_STACK, capabilityExplosive.serializeNBT());
        }
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean read(ByteBuf data, int id, EntityPlayer player, IPacket packet)
    {
        if (id == 1)
        {
            ItemStack exStack = ByteBufUtils.readItemStack(data);
            if (capabilityExplosive == null)
            {
                capabilityExplosive = new CapabilityExplosiveStack(exStack);
            }
            else
            {
                capabilityExplosive.stack = exStack;
                capabilityExplosive.initData();
            }
            world.markBlockRangeForRenderUpdate(pos, pos);
            return true;
        }
        else if (id == 2 && !this.world.isRemote)
        {
            // Packet explode command
            if (player.inventory.getCurrentItem().getItem() instanceof ItemRemoteDetonator)
            {
                ItemStack itemStack = player.inventory.getCurrentItem();

                ((ItemRemoteDetonator) ICBMClassic.itemRemoteDetonator).discharge(itemStack, ItemRemoteDetonator.ENERGY, true);
            }
            return true;
        }
        return false;
    }

    public void trigger(boolean setFire)
    {
        exploding = true;
        EntityExplosive entityExplosive = new EntityExplosive(world, new Pos(pos).add(0.5), getDirection(), capabilityExplosive.stack);

        if (setFire)
        {
            entityExplosive.setFire(100);
        }

        world.spawnEntity(entityExplosive);
        world.setBlockToAir(pos);
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

    @Override
    public EnumFacing getDirection()
    {
        return EnumFacing.byIndex(this.getBlockMetadata());
    }

    @Override
    public void setDirection(EnumFacing facingDirection)
    {
        this.world.setBlockState(pos, getBlockType().getDefaultState().withProperty(BlockExplosive.ROTATION_PROP, facingDirection), 2);
    }
}
