package icbm.classic.world.block.launcher.screen;

import icbm.classic.IcbmConstants;
import icbm.classic.world.missile.logic.source.cause.BlockCause;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockScreenCause extends BlockCause {

    public static final ResourceLocation REG_NAME = new ResourceLocation(IcbmConstants.MOD_ID, "block.screen");

    public BlockScreenCause(Level level, BlockPos pos, BlockState state) {
        super(world, pos, state);
    }

    @Override
    public ResourceLocation getRegistryName() {
        return REG_NAME;
    }
}
