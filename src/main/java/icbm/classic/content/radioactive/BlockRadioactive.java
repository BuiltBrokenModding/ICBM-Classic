package icbm.classic.content.radioactive;

import icbm.classic.config.ConfigMain;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.SoundEvents;
import net.minecraft.potion.Effects;
import net.minecraft.potion.EffectInstance;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

public class BlockRadioactive extends Block {

    public static final DamageSource damageSource = new DamageSource("icbmclassic:radioactive_block");

    public float decayChance = 0.85f; //TODO configs
    public int areaOfEffect = 5;
    public int tickRate = 5;
    public float damage = 2;

    public BlockRadioactive(Block.Properties properties) {
        super(properties.tickRandomly());
    }

    @Override
    public int tickRate(IWorldReader worldIn)
    {
        return tickRate;
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote)
        {
            final AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(areaOfEffect, areaOfEffect, areaOfEffect);
            final List<LivingEntity> entities = worldIn.getEntitiesWithinAABB(LivingEntity.class, bounds);
            for(LivingEntity entity : entities) {
                if(random.nextFloat() < decayChance) {
                    float protection = ProtectiveArmorHandler.getProtectionRating(entity);
                    if (protection < ConfigMain.protectiveArmor.minProtectionRadiation || protection < random.nextFloat()) {
                        entity.attackEntityFrom(damageSource, damage); //TODO consider reducing damage by random amount of protection found. This way iron armor can reduce damage every so often
                        entity.addPotionEffect(new EffectInstance(Effects.WITHER, 20));
                    }
                }
            }

            worldIn.getPendingBlockTicks().scheduleTick(pos, this, Math.max(1, random.nextInt(tickRate)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(12) == 0)
        {
            worldIn.playSound(((float)pos.getX() + 0.5F), ((float)pos.getY() + 0.5F), ((float)pos.getZ() + 0.5F),
                SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, //TODO get custom audio
                1.0F + rand.nextFloat(), rand.nextFloat() * 1.7F + 0.3F, false);
        }

        //TODO spawn particles showing AOE
    }
}