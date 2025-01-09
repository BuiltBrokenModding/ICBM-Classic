package icbm.classic.content.blocks;

import icbm.classic.config.ConfigMain;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlockSpikes extends Block
{
    public static final DamageSource DAMAGE_SOURCE = new DamageSource("icbmclassic:spikes");

    @Setter @Accessors(chain = true)
    public boolean fire;
    @Setter @Accessors(chain = true)
    public boolean poison;

    public BlockSpikes()
    {
        super(
            Block.Properties.create(Material.IRON)
                .hardnessAndResistance(1)
                .doesNotBlockMovement()
        );
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity)
    {
        // If the entity is a living entity
        if (entity instanceof LivingEntity)
        {
            if (poison) //TODO replace with state
            {
                entity.attackEntityFrom(DAMAGE_SOURCE, ConfigMain.spikes.poisonDamage);
                ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.POISON, 7 * 20, 0));
            }
            else if (fire)
            {
                entity.attackEntityFrom(DAMAGE_SOURCE, ConfigMain.spikes.fireDamage);
                entity.setFire(7);
            }
            else {
                entity.attackEntityFrom(DAMAGE_SOURCE, ConfigMain.spikes.normalDamage);
            }
        }
    }
}
