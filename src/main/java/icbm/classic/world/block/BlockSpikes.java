package icbm.classic.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("deprecation")
public class BlockSpikes extends Block {

    private final SpikeType spikeType;


    public BlockSpikes(BlockBehaviour.Properties properties, SpikeType spikeType) {
        super(properties);
        this.spikeType = spikeType;
    }

    @Override
    public @NotNull RenderShape getRenderShape(@NotNull BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public void entityInside(BlockState pState, Level level, BlockPos pPos, Entity entity) {
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            entity.hurt(level.damageSources().cactus(), 1);

            switch (spikeType) {
                case POISON -> living.addEffect(new MobEffectInstance(MobEffects.POISON, 7 * 20, 0));
                case FIRE -> living.setSecondsOnFire(7);
            }
        }
    }

    public enum SpikeType {
        NORMAL,
        POISON,
        FIRE
    }
}
