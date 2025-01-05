package icbm.classic.content.cargo;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.missiles.projectile.IProjectileThrowable;
import icbm.classic.api.missiles.projectile.ProjectileTypes;
import icbm.classic.content.cargo.balloon.BalloonProjectileData;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cargo.parachute.ParachuteProjectileData;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.entity.Entity;
import net.minecraft.block.Blocks;
import net.minecraft.item.UseAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemThrowableProjectile extends ItemBase {
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config
    public static final float THROW_VELOCITY = 0.5f;

    public static final TranslationTextComponent ERROR_THROWING_INTERFACE = new TranslationTextComponent("error.icbmclassic:projectile.throwing.interface", IProjectileThrowable.class.getSimpleName());
    public static final TranslationTextComponent ERROR_THROWING_TYPE = new TranslationTextComponent("error.icbmclassic:projectile.throwing.type", ProjectileTypes.TYPE_THROWABLE.getKey());

    public ItemThrowableProjectile(Properties p_i48487_1_) {
        super(p_i48487_1_);
    }

    // TODO split into empty crafting item and version holding item
    // TODO add a damaged/used version to drop after deploying cargo


    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ItemStackCapProvider(stack)
            .with(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, ProjectileStack::new);
    }

    @Override
    public UseAction getUseAction(@Nonnull ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack) {
        return MAX_USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, PlayerEntity playerIn, @Nonnull Hand handIn) {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull LivingEntity entityLiving, int timeLeft) {
        if (!world.isRemote && throwProjectile(stack, world, entityLiving) && !(entityLiving instanceof PlayerEntity) || !((PlayerEntity) entityLiving).isCreative()) {
            stack.shrink(1);
        }
    }

    // TODO move logic to common helper called `throwProjectile` to better reuse common spawn logic
    public static boolean throwProjectile(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity thrower) {
        final boolean isCreative = thrower instanceof PlayerEntity && ((PlayerEntity) thrower).isCreative();
        final Hand hand = thrower instanceof LivingEntity ? ((LivingEntity) thrower).getActiveHand() : Hand.MAIN_HAND;
        final LazyOptional<IProjectileStack> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if (!projectileStack.isPresent()) {
            return false;
        }

        final IProjectileData projectileData = projectileStack.map(IProjectileStack::getProjectileData).orElseThrow(IllegalStateException::new);
        if(projectileData == null) {
            return false;
        }

        final Entity parachute = projectileData.newEntity(world, !isCreative);
        if (!projectileData.isType(ProjectileTypes.TYPE_THROWABLE)) {
            if(thrower instanceof PlayerEntity) {
                ((PlayerEntity) thrower).sendStatusMessage(ERROR_THROWING_TYPE, true);
            }
            ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as type(s) isn't supported. " +
                "This is likely missing implementation on the projectile. " +
                "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
            return false;
        }

        if (parachute instanceof IProjectileThrowable) {
            final float yaw = thrower instanceof LivingEntity ? ((LivingEntity) thrower).rotationYawHead : thrower.rotationYaw;
            final float pitch = thrower.rotationPitch;
            final double x = thrower.posX;
            final double y = thrower.posY + thrower.getEyeHeight()  ;
            final double z = thrower.posZ;

            final IActionSource source = new ActionSource(DimensionType.getKey(world.getDimension().getType()), new Vec3d(x, y, z), new EntityCause(thrower));
            ((IProjectileThrowable<Entity>) parachute).throwProjectile(parachute, source, x, y, z, yaw, pitch, THROW_VELOCITY, 0);

        } else {
            if(thrower instanceof PlayerEntity) {
                ((PlayerEntity) thrower).sendStatusMessage(ERROR_THROWING_INTERFACE, true);
            }
            ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as it doesn't support IProjectileThrowable." +
                "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
            return false;
        }

        // Spawn
        if (world.addEntity(parachute)) {

            // Run post spawn logic
            projectileData.onEntitySpawned(parachute, thrower, hand);

            return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        final LazyOptional<IProjectileStack> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);

        // Only show basic info if we have no projectile data
        if(!projectileStack.isPresent() || projectileStack.orElseThrow(IllegalStateException::new).getProjectileData() == null) {
            final String key = getTranslationKey(stack) + ".info";
            final float gravity = -EntityParachute.GRAVITY * 20;
            final float air = (1 - EntityParachute.AIR_RESISTANCE) * 100;
            LanguageUtility.outputComponents(new TranslationTextComponent(key, String.format("%.2f", air) + " %", String.format("%.2f", gravity)), list::add);
        }

        // Show projectile information
        if(projectileStack.isPresent() && projectileStack.orElseThrow(IllegalStateException::new).getProjectileData() != null) {
            LanguageUtility.outputComponents(projectileStack.orElseThrow(IllegalStateException::new).getProjectileData().getTooltip(), list::add);
        }
    }

    @Override
    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
        if (this.isInGroup(group)) {
            items.add(new ItemStack(this));

            if(this == ItemReg.PARACHUTE.get()) {
                items.add(parachuteWith(new ParachuteProjectileData(EntityReg.CARGO_PARACHUTE_SIZE_1::get).setHeldItem(new ItemStack(net.minecraft.item.Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
                items.add(parachuteWith(new ParachuteProjectileData(EntityReg.CARGO_PARACHUTE_SIZE_2::get).setHeldItem(new ItemStack(net.minecraft.block.Blocks.FURNACE)).setParachuteMode(ProjectileCargoMode.BLOCK)));
                items.add(parachuteWith(new ParachuteProjectileData(EntityReg.CARGO_PARACHUTE_SIZE_2::get).setHeldItem(new ItemStack(Blocks.TNT)).setParachuteMode(ProjectileCargoMode.BLOCK)));
            }
            else if(this == ItemReg.BALLON.get()) {
                items.add(parachuteWith(new BalloonProjectileData(EntityReg.CARGO_BALLOON::get).setHeldItem(new ItemStack(Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
            }
        }
    }

    private ItemStack parachuteWith(IProjectileData data) {
        final ItemStack stack = new ItemStack(this);
        final LazyOptional<IProjectileStack> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
        if(projectileStack.isPresent() && projectileStack.orElseThrow(IllegalStateException::new) instanceof ProjectileStack) {
            ((ProjectileStack) projectileStack.orElseThrow(IllegalStateException::new)).setProjectileData(data);
        }
        return stack;
    }
}
