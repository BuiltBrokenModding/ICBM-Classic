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
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBase;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemThrowableProjectile extends ItemBase implements ICapabilityHolderItem, ISubItemProvider {
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config
    public static final float THROW_VELOCITY = 0.5f;

    public static final Component ERROR_THROWING_INTERFACE = Component.translatable("error.icbmclassic:projectile.throwing.interface", IProjectileThrowable.class.getSimpleName());
    public static final Component ERROR_THROWING_TYPE = Component.translatable("error.icbmclassic:projectile.throwing.type", ProjectileTypes.TYPE_THROWABLE.getKey());

    // TODO split into empty crafting item and version holding item
    // TODO add a damaged/used version to drop after deploying cargo

    public ItemThrowableProjectile() {
        super(new Item.Properties().stacksTo(16));
    }

    @Nullable
    @Override
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack) {
        if (capability == ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY) {
            return (T) new ProjectileStack(stack);
        }
        return null;
    }

    @Override
    public UseAnim getUseAnimation(@Nonnull ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(@Nonnull ItemStack stack, @Nonnull LivingEntity entity) {
        return MAX_USE_DURATION;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player playerIn, @Nonnull InteractionHand handIn) {
        final ItemStack itemstack = playerIn.getItemInHand(handIn);
        playerIn.startUsingItem(handIn);
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull LivingEntity entityLiving, int timeLeft) {
        // PORT: 1.12 had an operator precedence bug here (shrunk for any non-creative user even on failed throws, crashed for non-players)
        if (!world.isClientSide() && throwProjectile(stack, world, entityLiving) && !(entityLiving instanceof Player && ((Player) entityLiving).isCreative())) {
            stack.shrink(1);
        }
    }

    // TODO move logic to common helper called `throwProjectile` to better reuse common spawn logic
    public static boolean throwProjectile(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity thrower) {
        final boolean isCreative = thrower instanceof Player && ((Player) thrower).isCreative();
        final InteractionHand hand = thrower instanceof LivingEntity ? ((LivingEntity) thrower).getUsedItemHand() : InteractionHand.MAIN_HAND;
        final IProjectileStack<Entity> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if (projectileStack == null) {
            return false;
        }

        final IProjectileData<Entity> projectileData = projectileStack.getProjectileData();
        if(projectileData == null) {
            return false;
        }

        final Entity parachute = projectileData.newEntity(world, !isCreative);
        if (!projectileData.isType(ProjectileTypes.TYPE_THROWABLE)) {
            if(thrower instanceof Player) {
                ((Player) thrower).displayClientMessage(ERROR_THROWING_TYPE, true);
            }
            ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as type(s) isn't supported. " +
                "This is likely missing implementation on the projectile. " +
                "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
            return false;
        }

        if (parachute instanceof IProjectileThrowable) {
            final float yaw = thrower instanceof LivingEntity ? ((LivingEntity) thrower).getYHeadRot() : thrower.getYRot();
            final float pitch = thrower.getXRot();
            final double x = thrower.getX();
            final double y = thrower.getY() + thrower.getEyeHeight();
            final double z = thrower.getZ();

            final IActionSource source = new ActionSource(world, new Vec3(x, y, z), new EntityCause(thrower));
            ((IProjectileThrowable<Entity>) parachute).throwProjectile(parachute, source, x, y, z, yaw, pitch, THROW_VELOCITY, 0);

        } else {
            if(thrower instanceof Player) {
                ((Player) thrower).displayClientMessage(ERROR_THROWING_INTERFACE, true);
            }
            ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as it doesn't support IProjectileThrowable." +
                "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
            return false;
        }

        // Spawn
        if (world.addFreshEntity(parachute)) {

            // Run post spawn logic
            projectileData.onEntitySpawned(parachute, thrower, hand);

            return true;
        }
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> list, TooltipFlag flag) {
        final IProjectileStack projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);

        // Only show basic info if we have no projectile data
        if(projectileStack == null || projectileStack.getProjectileData() == null) {
            final String key = getDescriptionId(stack) + ".info";
            final float gravity = -EntityParachute.GRAVITY * 20;
            final float air = (1 - EntityParachute.AIR_RESISTANCE) * 100;
            LanguageUtility.outputLines(Component.translatable(key, String.format("%.2f", air) + " %", String.format("%.2f", gravity)), line -> list.add(Component.literal(line)));
        }

        // Show projectile information
        if(projectileStack != null && projectileStack.getProjectileData() != null) {
            final IProjectileData projectileData = projectileStack.getProjectileData();
            LanguageUtility.outputLines(projectileData.getTooltip(), line -> list.add(Component.literal(line)));

            if(projectileData instanceof CargoProjectileData) {
                final ItemStack heldItem = ((CargoProjectileData<?, ?>) projectileData).getHeldItem();

                if(heldItem != null && !heldItem.isEmpty()) {
                    list.add(Component.empty());
                    list.add(Component.translatable("projectile.icbmclassic:holder.held"));
                    list.addAll(heldItem.getTooltipLines(context, ItemBase.getClientPlayer(), flag));
                    list.add(Component.empty());

                    if(!CargoHolderHandler.isAllowed(heldItem)) {
                        list.add(Component.translatable("projectile.icbmclassic:holder.disabled.config"));
                    }
                }
                else {
                    list.add(Component.translatable("projectile.icbmclassic:holder.empty"));
                }
            }

        }
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this));

        if(this == ItemReg.itemParachute.get()) {
            items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
            items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(Blocks.FURNACE)).setParachuteMode(ProjectileCargoMode.BLOCK)));
            items.add(parachuteWith(new ParachuteProjectileData().setHeldItem(new ItemStack(Blocks.TNT)).setParachuteMode(ProjectileCargoMode.BLOCK)));
        }
        else if(this == ItemReg.itemBalloon.get()) {
            items.add(parachuteWith(new BalloonProjectileData().setHeldItem(new ItemStack(Items.EGG)).setParachuteMode(ProjectileCargoMode.ITEM)));
        }
    }

    private ItemStack parachuteWith(IProjectileData data) {
        final ItemStack stack = new ItemStack(this);
        final IProjectileStack projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY);
        if(projectileStack instanceof ProjectileStack) {
            ((ProjectileStack) projectileStack).setProjectileData(data);
        }
        return stack;
    }
}
// PORTED
