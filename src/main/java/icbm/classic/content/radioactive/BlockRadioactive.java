package icbm.classic.content.radioactive;

import com.builtbroken.jlib.lang.StringHelpers;
import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.client.ICBMSounds;
import icbm.classic.config.ConfigMain;
import icbm.classic.config.blocks.ConfigBlocks;
import icbm.classic.content.gas.ProtectiveArmorHandler;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class BlockRadioactive extends Block {
    public static final PropertyType TYPE_PROP = new PropertyType();

    public static final DamageSource damageSource = new DamageSource("icbmclassic:radioactive_block");

    public BlockRadioactive() {
        super(Material.ROCK);
        this.setDefaultState(getDefaultState().withProperty(TYPE_PROP, EnumType.STONE));
        this.setRegistryName(ICBMConstants.PREFIX + "radioactive");
        this.setUnlocalizedName(ICBMConstants.PREFIX + "radioactive");
        this.setCreativeTab(ICBMClassic.CREATIVE_TAB);
        this.setHardness(0.5f);
        if (ConfigBlocks.radioactive.decayDelay > 0) {
            this.setTickRandomly(true);
        }
    }

    @Override
    public int tickRate(@Nonnull World worldIn) {
        return ConfigBlocks.radioactive.decayDelay;
    }

    @Override
    public void randomTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        updateTick(worldIn, pos, state, random);
    }

    @Override
    public void updateTick(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Random random) {
        if (!worldIn.isRemote) {
            final boolean stop = this.tryDecaySelf(worldIn, pos, state, random);
            this.tryDecayEntities(worldIn, pos, state, random);

            if(!stop && !ConfigBlocks.radioactive.randomTickOnly) {
                worldIn.scheduleUpdate(pos, this, random.nextInt(tickRate(worldIn)) + 1);
            }
        }
    }

    private boolean tryDecaySelf(World worldIn, BlockPos pos, IBlockState state, Random random) {
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

    private void doDecaySelf(World worldIn, BlockPos pos, IBlockState state) {
        switch (state.getValue(TYPE_PROP)) {
            case DIRT:
                worldIn.setBlockState(pos, Blocks.DIRT.getDefaultState());
                break;
            case STONE:
                worldIn.setBlockState(pos, Blocks.STONE.getDefaultState());
                break;
            default:
                break;
        }
    }

    private void tryDecayEntities(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (ConfigBlocks.radioactive.decayEffectRange <= 0 || ConfigBlocks.radioactive.decayEffectChance <= 0) {
            return;
        }

        final AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange, ConfigBlocks.radioactive.decayEffectRange);
        final List<EntityLivingBase> entities = worldIn.getEntitiesWithinAABB(EntityLivingBase.class, bounds);
        for (EntityLivingBase entity : entities) {
            if (random.nextFloat() < ConfigBlocks.radioactive.decayEffectChance) {
                float protection = ProtectiveArmorHandler.getProtectionRating(entity);
                if (protection < ConfigMain.protectiveArmor.minProtectionRadiation || protection < random.nextFloat()) {
                    //TODO randomize damage
                    //TODO scale damage by protection percentage
                    //TODO scale damage by range

                    entity.attackEntityFrom(damageSource, ConfigBlocks.radioactive.decayEffectDamage);
                    if (ConfigBlocks.radioactive.decayWitherDuration > 0) {
                        entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20));
                    }
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(@Nonnull IBlockState stateIn, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Random rand) {
        if (rand.nextInt(12) == 0) {
            ICBMSounds.RADIOACTIVE_TICK.play(worldIn, ((float) pos.getX() + 0.5F), ((float) pos.getY() + 0.5F), ((float) pos.getZ() + 0.5F),
                1.0F + rand.nextFloat() * 0.5f, 1 + rand.nextFloat() * 0.2f,
                true);
        }

        //TODO spawn particles showing AOE
    }

    @Deprecated
    public float getBlockHardness(@Nonnull IBlockState blockState, @Nonnull World worldIn, @Nonnull BlockPos pos) {
        if (blockState.getProperties().containsKey(TYPE_PROP)) {
            final EnumType type = (EnumType) blockState.getProperties().get(TYPE_PROP);
            switch (type) {
                case DIRT:
                    return Blocks.DIRT.getBlockHardness(Blocks.DIRT.getDefaultState(), worldIn, pos);
                case STONE:
                    return Blocks.STONE.getBlockHardness(Blocks.STONE.getDefaultState(), worldIn, pos);

            }
        }
        return this.blockHardness;
    }

    @Override
    public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, @Nullable Entity exploder, @Nonnull Explosion explosion) {
        final IBlockState blockState = world.getBlockState(pos);
        if (blockState.getProperties().containsKey(TYPE_PROP)) {
            final EnumType type = (EnumType) blockState.getProperties().get(TYPE_PROP);
            switch (type) {
                case DIRT:
                    return Blocks.DIRT.getExplosionResistance(world, pos, exploder, explosion);
                case STONE:
                    return Blocks.STONE.getExplosionResistance(world, pos, exploder, explosion);
            }
        }
        return getExplosionResistance(exploder);
    }

    @Override
    public int damageDropped(@Nonnull IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE_PROP);
    }

    @Override
    public IBlockState getStateForPlacement(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EnumFacing facing,
                                            float hitX, float hitY, float hitZ, int meta,
                                            @Nonnull EntityLivingBase placer, @Nonnull EnumHand hand) {
        return getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE_PROP).ordinal();
    }

    @Nonnull
    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
        if (tab == this.getCreativeTabToDisplayOn()) {
            for (EnumType type : EnumType.values()) {
                items.add(new ItemStack(this, 1, type.ordinal()));
            }
        }
    }

    public static class PropertyType extends PropertyEnum<EnumType> {
        public PropertyType() {
            super("type", EnumType.class, Lists.newArrayList(EnumType.values()));
        }
    }

    public static enum EnumType implements IStringSerializable {
        DIRT,
        STONE;

        @Override
        public String getName() {
            return name().toLowerCase();
        }

        public static EnumType get(int meta) {
            return meta >= 0 && meta < values().length ? values()[meta] : STONE;
        }
    }
}