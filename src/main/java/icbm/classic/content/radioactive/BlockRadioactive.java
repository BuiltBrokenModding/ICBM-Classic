package icbm.classic.content.radioactive;

import com.google.common.collect.Lists;
import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
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
        if(ConfigBlocks.radioactive.decayDelay > 0) {
            this.setTickRandomly(true);
        }
    }

    @Override
    public int tickRate(World worldIn) {
        return ConfigBlocks.radioactive.decayDelay;
    }

    @Override
    public void randomTick(World worldIn, BlockPos pos, IBlockState state, Random random) {
        if (!worldIn.isRemote) {
            if (ConfigBlocks.radioactive.decayRange <= 0 || ConfigBlocks.radioactive.decayChance <= 0) {
                return;
            }

            final AxisAlignedBB bounds = new AxisAlignedBB(pos).grow(ConfigBlocks.radioactive.decayRange, ConfigBlocks.radioactive.decayRange, ConfigBlocks.radioactive.decayRange);
            final List<EntityLivingBase> entities = worldIn.getEntitiesWithinAABB(EntityLivingBase.class, bounds);
            for (EntityLivingBase entity : entities) {
                if (random.nextFloat() < ConfigBlocks.radioactive.decayChance) {
                    float protection = ProtectiveArmorHandler.getProtectionRating(entity);
                    if (protection < ConfigMain.protectiveArmor.minProtectionRadiation || protection < random.nextFloat()) {
                        entity.attackEntityFrom(damageSource, ConfigBlocks.radioactive.decayDamage); //TODO consider reducing damage by random amount of protection found. This way iron armor can reduce damage every so often
                        entity.addPotionEffect(new PotionEffect(MobEffects.WITHER, 20));
                    }
                }
            }

            worldIn.scheduleUpdate(pos, this, Math.max(1, random.nextInt(ConfigBlocks.radioactive.decayDelay)));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        if (rand.nextInt(12) == 0) {
            worldIn.playSound(((float) pos.getX() + 0.5F), ((float) pos.getY() + 0.5F), ((float) pos.getZ() + 0.5F),
                SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, //TODO get custom audio
                1.0F + rand.nextFloat(), rand.nextFloat() * 1.7F + 0.3F, false);
        }

        //TODO spawn particles showing AOE
    }

    @Deprecated
    public float getBlockHardness(IBlockState blockState, World worldIn, BlockPos pos) {
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
    public float getExplosionResistance(World world, BlockPos pos, @Nullable Entity exploder, Explosion explosion) {
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
    public int damageDropped(IBlockState state) {
        return getMetaFromState(state);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE_PROP);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE_PROP).ordinal();
    }

    @Deprecated
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE_PROP, EnumType.get(meta));
    }

    @Override
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
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