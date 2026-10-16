package icbm.classic.content.blocks;

import icbm.classic.ICBMConstants;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;

/**
 * Concrete blocks, flattened from the 1.12 meta variants (concrete, concrete_compact, concrete_reinforced).
 * <p>
 * PORT: explosion resistance is now set per block through properties (1.12 overrode getExplosionResistance per state).
 */
public class BlockConcrete extends Block
{
    private final EnumType type;

    public BlockConcrete(EnumType type)
    {
        // PORT: 1.12 Material.ROCK needed a pickaxe to drop -> requiresCorrectToolForDrops plus the mineable/pickaxe tag.
        // BASEDRUM instrument + requiresCorrectToolForDrops is what BlastBlockHelpers#isRockMaterial treats as 1.12 rock
        super(Properties.of()
            .mapColor(MapColor.STONE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(10, type.getExplosionResistance())
            .sound(SoundType.STONE));
        this.type = type;
    }

    public EnumType getType()
    {
        return type;
    }

    @Override
    public String getDescriptionId()
    {
        return "tile." + ICBMConstants.PREFIX + "concrete." + type.ordinal();
    }

    public static enum EnumType implements StringRepresentable
    {
        NORMAL(28),
        COMPACT(280),
        REINFORCED(2800); //obsidian is 2000 (1.12 value, 1.21 obsidian is 1200)

        private final float explosionResistance;

        EnumType(float explosionResistance)
        {
            this.explosionResistance = explosionResistance;
        }

        public float getExplosionResistance()
        {
            return explosionResistance;
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

        public static EnumType get(int meta)
        {
            return meta >= 0 && meta < values().length ? values()[meta] : NORMAL;
        }
    }
}
// PORTED
