package icbm.classic.content.missile.logic.source.cause;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * General purpose block cause
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockCause extends MissileCause implements IMissileCause.IBlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block");

    private World world;
    private BlockPos blockPos;
    private IBlockState blockState;

    private int worldId;

    public BlockCause(World world, BlockPos pos, IBlockState state) {
        this.world = world;
        this.worldId = world.provider.getDimension();
        this.blockPos = pos;
        this.blockState = state;
    }

    public World getWorld() {
        if(world == null) {
            world = DimensionManager.getWorld(worldId);
        }
        return world;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return SAVE_LOGIC.save(this, super.serializeNBT());
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        super.deserializeNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<BlockCause> SAVE_LOGIC = new NbtSaveHandler<BlockCause>()
        .mainRoot()
        /* */.nodeInteger("level", BlockCause::getWorldId, BlockCause::setWorldId)
        /* */.nodeBlockPos("pos", BlockCause::getBlockPos, BlockCause::setBlockPos)
        /* */.nodeBlockState("state", BlockCause::getBlockState, BlockCause::setBlockState)
        .base();
}
