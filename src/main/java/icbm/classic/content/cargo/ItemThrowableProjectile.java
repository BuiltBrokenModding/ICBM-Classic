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
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("projectile", ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, new ProjectileStack());
        return provider;
    }

    @Override
    public UseAction getItemUseAction(@Nonnull ItemStack stack) {
        return UseAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
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
        if (!stack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            return false;
        }
        final IProjectileStack<Entity> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
        if (projectileStack == null) {
            return false;
        }

        final IProjectileData<Entity> projectileData = projectileStack.getProjectileData();
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

            final IActionSource source = new ActionSource(world, new Vec3d(x, y, z), new EntityCause(thrower));
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
        if (world.spawnEntity(parachute)) {

            // Run post spawn logic
            projectileStack.getProjectileData().onEntitySpawned(parachute, thrower, hand);

            return true;
        }
        return false;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<ITextComponent> list, ITooltipFlag flag) {
        final IProjectileStack projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);

        // Only show basic info if we have no projectile data
        if(projectileStack == null || projectileStack.getProjectileData() == null) {
            final String key = getUnlocalizedName(stack) + ".info";
            final float gravity = -EntityParachute.GRAVITY * 20;
            final float air = (1 - EntityParachute.AIR_RESISTANCE) * 100;
            LanguageUtility.outputLines(new TranslationTextComponent(key, String.format("%.2f", air) + " %", String.format("%.2f", gravity)), list::add);
        }

        // Show projectile information
        if(projectileStack != null && projectileStack.getProjectileData() != null) {
            LanguageUtility.outputLines(projectileStack.getProjectileData().getTooltip(), list::add);
        }
    }

    @Override
    public void getSubItems(ItemGroup tab, NonNullList<ItemStack> items)
    {
        if (this.isInCreativeTab(tab))
        {
            items.add(new ItemStack(this));

            if(this == ItemReg.itemParachute) {
                items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(net.minecraft.item.Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
                items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(net.minecraft.block.Blocks.FURNACE)).setParachuteMode(ProjectileCargoMode.BLOCK)));
                items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(Blocks.TNT)).setParachuteMode(ProjectileCargoMode.BLOCK)));
            }
            else if(this == ItemReg.itemBalloon) {
                items.add(parachuteWith(new BalloonProjectileData().setHeldItem(new ItemStack(Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
            }
        }
    }

    private ItemStack parachuteWith(IProjectileData data) {
        final ItemStack stack = new ItemStack(this);
        final IProjectileStack projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
        if(projectileStack instanceof ProjectileStack) {
            ((ProjectileStack) projectileStack).setProjectileData(data);
        }
        return stack;
    }
}
