package icbm.classic.prefab.tile;

import icbm.classic.lib.tile.ITick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/9/2017.
 * <p>
 * PORT: 1.12 ITickable#update is now driven by the block's {@code getTicker} which calls {@link #update()}.
 * 1.12 {@code shouldRefresh} (keep tile while block is the same) is the 1.21 default behaviour.
 */
public class TileMachine extends BlockEntity
{
    protected int ticks = -1;

    protected final List<ITick> tickActions = new ArrayList<>();

    public TileMachine(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    /** Called each tick by the block entity ticker */
    public void update()
    {
        //Increase tick
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1)
        {
            ticks = 0;
        }

        tickActions.forEach(action -> {
            action.update(ticks, isServer());
        });
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveWithoutMetadata(registries);
    }

    public boolean isServer()
    {
        return level != null && !level.isClientSide();
    }

    public boolean isClient()
    {
        return level != null && level.isClientSide();
    }

    @Deprecated
    public Direction getRotation()
    {
        final BlockState state = getBlockState();
        if (state.hasProperty(BlockICBM.ROTATION_PROP))
        {
            return state.getValue(BlockICBM.ROTATION_PROP);
        }
        return Direction.NORTH;
    }

    /**
     * Distance squared from the center of this tile, replaces 1.12 TileEntity#getDistanceSq
     */
    public double getDistanceSq(double x, double y, double z)
    {
        final double dx = getBlockPos().getX() + 0.5D - x;
        final double dy = getBlockPos().getY() + 0.5D - y;
        final double dz = getBlockPos().getZ() + 0.5D - z;
        return dx * dx + dy * dy + dz * dz;
    }
}
// PORTED
