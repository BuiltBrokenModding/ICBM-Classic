package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.missile.logic.source.cause.BlockCause;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockScreenCause extends BlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block.screen");

    public BlockScreenCause(World world, BlockPos pos, IBlockState state) {
        super(world, pos, state);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }
}
