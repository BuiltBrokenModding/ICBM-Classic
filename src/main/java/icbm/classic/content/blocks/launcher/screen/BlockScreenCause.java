package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.ICBMConstants;
import icbm.classic.content.missile.logic.source.cause.CausedByBlock;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class BlockScreenCause extends CausedByBlock {

    public static final ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "block.screen");

    public BlockScreenCause(Level world, BlockPos pos, BlockState state) {
        super(world, pos, state);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }
}
// PORTED
