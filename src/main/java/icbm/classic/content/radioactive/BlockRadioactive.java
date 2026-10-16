package icbm.classic.content.radioactive;

import icbm.classic.ICBMConstants;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.blocks.ConfigBlocks;
import icbm.classic.content.blast.ICBMDamageTypes;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Radioactive dirt and stone, flattened from the 1.12 meta variants (radioactive_dirt, radioactive_stone).
 * <p>
 * PORT: 1.12 used Material.ROCK with hardness and explosion resistance copied from vanilla dirt/stone per
 * state, those values are set through the block properties now. The blockstate type property is removed,
 * the type is passed in the constructor.
 */
public class BlockRadioactive extends Block
{
    // PORT: 1.12 static DamageSource "icbmclassic:radioactive_block", now ICBMDamageTypes.RADIOACTIVE_BLOCK

    private final EnumType type;

    public BlockRadioactive(EnumType type)
    {
        super(Properties.of()
            .mapColor(type == EnumType.DIRT ? MapColor.DIRT : MapColor.STONE)
            // 1.12 hardness matched vanilla dirt (0.5) / stone (1.5), resistance matched dirt / stone too
            .strength(type == EnumType.DIRT ? 0.5f : 1.5f, type == EnumType.DIRT ? 0.5f : 6.0f)
            .sound(SoundType.STONE)
            .requiresCorrectToolForDrops() // 1.12 Material.ROCK needed a pickaxe, needs mineable/pickaxe tag
            .randomTicks());
        this.type = type;
    }

    public EnumType getType()
    {
        return type;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "radioactive." + type.ordinal();
    }

    /** 1.12 tickRate */
    public int tickRate()
    {
        return ConfigBlocks.radioactive.decayDelay;
    }

    @Override
    public boolean isRandomlyTicking(BlockState state)
    {
        // 1.12 only enabled random ticks when decayDelay > 0, config is read at runtime now
        return ConfigBlocks.radioactive.decayDelay > 0;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
    {
        tick(state, worldIn, pos, random);
    }

    /** 1.12 updateTick */
    @Override
    protected void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random)
    {
        final boolean stop = this.tryDecaySelf(worldIn, pos, state, random);
        this.tryDecayEntities(worldIn, pos, state, random);

        if (!stop && !ConfigBlocks.radioactive.randomTickOnly && tickRate() > 0)
        {
            worldIn.scheduleTick(pos, this, random.nextInt(tickRate()) + 1);
        }
    }

    private boolean tryDecaySelf(Level worldIn, BlockPos pos, BlockState state, RandomSource random)
    {
        if (ConfigBlocks.radioactive.decayBlockChance <= 0.000000001)
        {
            return false;
        }
        // Block death chance
        if (random.nextFloat() < ConfigBlocks.radioactive.decayBlockChance)
        {
            // TODO fire action event
            this.doDecaySelf(worldIn, pos, state);
            return true;
        }
        return false;
    }

    private void doDecaySelf(Level worldIn, BlockPos pos, BlockState state)
    {
        switch (type)
        {
            case DIRT:
                worldIn.setBlock(pos, Blocks.DIRT.defaultBlockState(), 3);
                break;
            case STONE:
                worldIn.setBlock(pos, Blocks.STONE.defaultBlockState(), 3);
                break;
            default:
                break;
        }
    }

    private void tryDecayEntities(Level worldIn, BlockPos pos, BlockState state, RandomSource random)
    {
        if (ConfigBlocks.radioactive.decayEffectRange <= 0 || ConfigBlocks.radioactive.decayEffectChance <= 0)
        {
            return;
        }

        final AABB bounds = new AABB(pos).inflate(ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange);
        final List<LivingEntity> entities = worldIn.getEntitiesOfClass(LivingEntity.class, bounds);
        for (LivingEntity entity : entities)
        {
            if (random.nextFloat() < ConfigBlocks.radioactive.decayEffectChance)
            {
                float protection = ProtectiveArmorHandler.getProtectionRating(entity);
                if (protection < ConfigMain.protectiveArmor.minProtectionRadiation || protection < random.nextFloat())
                {
                    //TODO randomize damage
                    //TODO scale damage by protection percentage
                    //TODO scale damage by range

                    entity.hurt(ICBMDamageTypes.source(worldIn, ICBMDamageTypes.RADIOACTIVE_BLOCK), ConfigBlocks.radioactive.decayEffectDamage);
                    if (ConfigBlocks.radioactive.decayWitherDuration > 0)
                    {
                        // PORT: 1.12 hard-coded 20 ticks and ignored the configured duration
                        entity.addEffect(new MobEffectInstance(MobEffects.WITHER, ConfigBlocks.radioactive.decayWitherDuration));
                    }
                }
            }
        }
    }

    /** 1.12 randomDisplayTick */
    @Override
    public void animateTick(BlockState stateIn, Level worldIn, BlockPos pos, RandomSource rand)
    {
        if (rand.nextInt(12) == 0)
        {
            ICBMSounds.RADIOACTIVE_TICK.play(worldIn, ((float) pos.getX() + 0.5F), ((float) pos.getY() + 0.5F), ((float) pos.getZ() + 0.5F),
                1.0F + rand.nextFloat() * 0.5f, 1 + rand.nextFloat() * 0.2f,
                true);
        }

        //TODO spawn particles showing AOE
    }

    public static enum EnumType implements StringRepresentable
    {
        DIRT,
        STONE;

        @Override
        public String getSerializedName()
        {
            return name().toLowerCase();
        }

        /** 1.12 name */
        public String getName()
        {
            return getSerializedName();
        }

        public static EnumType get(int meta)
        {
            return meta >= 0 && meta < values().length ? values()[meta] : STONE;
        }
    }
}
// PORTED
