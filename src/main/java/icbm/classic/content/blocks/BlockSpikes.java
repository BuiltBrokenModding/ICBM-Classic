package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import icbm.classic.config.blocks.ConfigBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Spikes, flattened from the 1.12 meta variants (spikes, spikes_poison, spikes_fire).
 * <p>
 * PORT: cutout render layer comes from the block model ("render_type": "cutout").
 */
public class BlockSpikes extends Block
{
    /** PORT: 1.12 DamageSource("icbmclassic:spikes") is a data driven damage type, needs data/icbmclassic/damage_type/spikes.json */
    public static final ResourceKey<DamageType> DAMAGE_TYPE = ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "spikes"));

    private final EnumSpikes spikeType;

    public BlockSpikes(EnumSpikes spikeType)
    {
        // PORT: 1.12 Material.IRON hardness 1 (resistance 5 internal = 1), IRON needed a pickaxe to drop
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .requiresCorrectToolForDrops()
            .strength(1.0F, 1.0F)
            .sound(SoundType.METAL)
            .noCollission()
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY));
        this.spikeType = spikeType;
    }

    public EnumSpikes getSpikeType()
    {
        return spikeType;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "spikes." + spikeType.ordinal();
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        return Shapes.empty();
    }

    @Override
    protected void entityInside(BlockState state, Level world, BlockPos pos, Entity entity)
    {
        // If the entity is a living entity
        if (entity instanceof LivingEntity)
        {
            if (spikeType == EnumSpikes.POISON)
            {
                entity.hurt(world.damageSources().source(DAMAGE_TYPE), ConfigBlocks.spikes.poisonDamage);
                ((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, 7 * 20, 0));
            }
            else if (spikeType == EnumSpikes.FIRE)
            {
                entity.hurt(world.damageSources().source(DAMAGE_TYPE), ConfigBlocks.spikes.fireDamage);
                entity.igniteForSeconds(7);
            }
            else {
                entity.hurt(world.damageSources().source(DAMAGE_TYPE), ConfigBlocks.spikes.normalDamage);
            }
        }
    }

    public static enum EnumSpikes implements StringRepresentable
    {
        NORMAL,
        POISON,
        FIRE;

        @Override
        public String toString()
        {
            return this.getSerializedName();
        }

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

        public static EnumSpikes get(int meta)
        {
            return meta >= 0 && meta < values().length ? values()[meta] : NORMAL;
        }
    }
}
// PORTED
