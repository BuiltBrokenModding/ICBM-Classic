package icbm.classic.world.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.player.Player;

public class PoisonFrostBite extends CustomPotion {
    public static MobEffect INSTANCE;

    public PoisonFrostBite(boolean isBadEffect, int color, int id, String name) {
        super(isBadEffect, color, id, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier) {
        if (entity.isBurning()) {
            entity.extinguish();
        }

        if (!(entity instanceof Player) || !((Player) entity).capabilities.isCreativeMode) {
            if (entity instanceof Player) {
                ((Player) entity).addExhaustion(3F * (amplifier + 1));
            }

            // Check to see if it's on ice
            if (entity.world.getBlockState(new BlockPos(entity)).getBlock() == Blocks.ICE) {
                entity.attackEntityFrom(DamageSource.MAGIC, 2);
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier) {
        if (duration % 20 == 0) {
            return true;
        }
        return false;
    }
}
