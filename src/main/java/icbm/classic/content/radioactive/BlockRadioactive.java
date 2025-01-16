package icbm.classic.content.radioactive;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.blocks.ConfigBlocks;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import icbm.classic.content.reg.BlockReg;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potions;
import net.minecraft.util.DamageSource;
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

    public BlockRadioactive(Properties properties) {
        super(properties);
    }

    @Override
    public int tickRate(IWorldReader worldIn)
    {
        return ConfigBlocks.radioactive.decayDelay;
    }

    @Override
    public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
        tick(state, worldIn, pos, random);
    }

    @Override
    public void tick(BlockState state, World worldIn, BlockPos pos, Random random) {
        if (!worldIn.isRemote) {
            final boolean stop = this.tryDecaySelf(worldIn, pos, state, random);
            this.tryDecayEntities(worldIn, pos, state, random);

            if(!stop && !ConfigBlocks.radioactive.randomTickOnly) {
                worldIn.getPendingBlockTicks().scheduleTick(pos, this, Math.max(1, random.nextInt(tickRate(worldIn))));
            }
        }
    }

    private boolean tryDecaySelf(World worldIn, BlockPos pos, BlockState state, Random random) {
        if(ConfigBlocks.radioactive.decayBlockChance <= 0.000000001) {
            return false;
        }
        // Block death chance
        if (random.nextFloat() < ConfigBlocks.radioactive.decayBlockChance) {
            // TODO fire action event
            this.doDecaySelf(worldIn, pos, state);
            return true;
        }
        return false;
    }

    private void doDecaySelf(World worldIn, BlockPos pos, BlockState state) {
        if(state.getBlock() == BlockReg.RADIOACTIVE_DIRT.get()) {
            worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
        }
        else if(state.getBlock() == BlockReg.RADIOACTIVE_STONE.get()) {
            worldIn.setBlockState(pos, Blocks.STONE.getDefaultState());
        }
    }

    private void tryDecayEntities(World worldIn, BlockPos pos, BlockState state, Random random) {
        if (ConfigBlocks.radioactive.decayEffectRange <= 0 || ConfigBlocks.radioactive.decayEffectChance <= 0) {
            return;
        }

        final AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange);
        final List<LivingEntity> entities = worldIn.getEntitiesWithinAABB(LivingEntity.class, bounds);
        for (LivingEntity entity : entities) {
            if (random.nextFloat() < ConfigBlocks.radioactive.decayEffectChance) {
                float protection = ProtectiveArmorHandler.getProtectionRating(entity);
                if (protection < ConfigMain.protectiveArmor.minProtectionRadiation || protection < random.nextFloat()) {
                    //TODO randomize damage
                    //TODO scale damage by protection percentage
                    //TODO scale damage by range

                    entity.attackEntityFrom(damageSource, ConfigBlocks.radioactive.decayEffectDamage);
                    if (ConfigBlocks.radioactive.decayWitherDuration > 0) {
                        entity.addPotionEffect(new EffectInstance(Effects.WITHER, 20));
                    }
                }
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void animateTick(BlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(12) == 0)
        {
            ICBMSounds.RADIOACTIVE_TICK.play(worldIn, ((float) pos.getX() + 0.5F), ((float) pos.getY() + 0.5F), ((float) pos.getZ() + 0.5F),
                1.0F + rand.nextFloat() * 0.5f, 1 + rand.nextFloat() * 0.2f,
                true);
        }

        //TODO spawn particles showing AOE
    }
}