package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockScreenCause extends BlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block.screen");

    private int launcherCount;
    private int lockHeight; // TODO move to launcher itself

    public BlockScreenCause(World world, BlockPos pos, IBlockState state, int launcherCount, int lockHeight) {
        super(world, pos, state);
        this.launcherCount = launcherCount;
        this.lockHeight = lockHeight;
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

    private static final NbtSaveHandler<BlockScreenCause> SAVE_LOGIC = new NbtSaveHandler<BlockScreenCause>()
        .mainRoot()
        /* */.nodeInteger("launcher_count", BlockScreenCause::getLauncherCount, BlockScreenCause::setLauncherCount)
        /* */.nodeInteger("lock_height", BlockScreenCause::getLockHeight, BlockScreenCause::setLockHeight)
        .base();
}
