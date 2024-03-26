package icbm.classic.prefab.tile;

import icbm.classic.lib.tile.ITick;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class IcbmBlockEntity extends BlockEntity //implements ITickable
{
    protected int ticks = -1;

    protected final List<ITick> tickActions = new ArrayList();

    public IcbmBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public void update() {
        //Increase tick
        ticks++;
        if (ticks >= Integer.MAX_VALUE - 1) {
            ticks = 0;
        }

        tickActions.forEach(action -> {
            action.update(ticks, isServer());
        });
    }

    @Override
    public boolean shouldRefresh(Level level, BlockPos pos, BlockState oldState, BlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public SPacketUpdateBlockEntity getUpdatePacket() {
        return new SPacketUpdateBlockEntity(pos, 0, getUpdateTag());
    }

    @Override
    public CompoundTag getUpdateTag() {
        return writeToNBT(new CompoundTag());
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateBlockEntity pkt) {
        readFromNBT(pkt.getNbtCompound());
    }

    public boolean isServer() {
        return world != null && !world.isClientSide();
    }

    public boolean isClient() {
        return world != null && world.isClientSide();
    }

    @Deprecated
    public Direction getRotation() {
        BlockState state = getBlockState();
        if (state.getProperties().containsKey(IcbmBlock.ROTATION_PROP)) {
            return state.getValue(IcbmBlock.ROTATION_PROP);
        }
        return Direction.NORTH;
    }

    public BlockState getBlockState() {
        return world.getBlockState(getPos());
    }
}
