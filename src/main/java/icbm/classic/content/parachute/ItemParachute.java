package icbm.classic.content.parachute;

import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.cause.IMissileSource;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.missiles.projectile.IProjectileThrowable;
import icbm.classic.api.missiles.projectile.ProjectileType;
import icbm.classic.content.missile.logic.source.MissileSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.projectile.ProjectileStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.prefab.item.ItemStackCapProvider;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class ItemParachute extends ItemBase {
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config
    public static final float THROW_VELOCITY = 0.1f;

    public ItemParachute() {
        this.setName("parachute");
        this.setMaxStackSize(16);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @Nullable
    public net.minecraftforge.common.capabilities.ICapabilityProvider initCapabilities(@Nonnull ItemStack stack, @Nullable NBTTagCompound nbt) {
        final ItemStackCapProvider provider = new ItemStackCapProvider(stack);
        provider.add("projectile", ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, new ProjectileStack());
        return provider;
    }

    @Override
    public EnumAction getItemUseAction(@Nonnull ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(@Nonnull ItemStack stack) {
        return MAX_USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(@Nonnull World world, EntityPlayer playerIn, @Nonnull EnumHand handIn) {
        final ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull EntityLivingBase entityLiving, int timeLeft) {
        if (throwProjectile(stack, world, entityLiving) && !(entityLiving instanceof EntityPlayer) || !((EntityPlayer) entityLiving).isCreative()) {
            stack.shrink(1);
        }
    }

    // TODO move logic to common helper called `throwProjectile` to better reuse common spawn logic
    public static boolean throwProjectile(@Nonnull ItemStack stack, @Nonnull World world, @Nonnull Entity thrower) {
        final boolean isCreative = thrower instanceof EntityPlayer && ((EntityPlayer) thrower).isCreative();
        if (!world.isRemote && stack.hasCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null)) {
            final IProjectileStack<Entity> projectileStack = stack.getCapability(ICBMClassicAPI.PROJECTILE_STACK_CAPABILITY, null);
            if (projectileStack != null) {
                final IProjectileData<Entity> projectileData = projectileStack.getProjectileData();
                final Entity parachute = projectileData.newEntity(world, !isCreative);
                if (projectileData.isType(ProjectileType.TYPE_THROWABLE)) {
                    if (parachute instanceof IProjectileThrowable) {
                        final double yaw = thrower instanceof EntityLivingBase ? ((EntityLivingBase) thrower).rotationYawHead : thrower.rotationYaw;
                        final double pitch = thrower.rotationPitch;
                        final double x = thrower.posX;
                        final double y = thrower.posY;
                        final double z = thrower.posZ;

                        final IMissileSource source = new MissileSource(world, new Vec3d(x, y, z), new EntityCause(thrower));
                        ((IProjectileThrowable<Entity>) parachute).throwProjectile(parachute, source, x, y, z, yaw, pitch, THROW_VELOCITY, 0);

                    } else {
                        ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as it doesn't support IProjectileThrowable." +
                            "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
                        return false;
                    }
                } else {
                    ICBMClassic.logger().warn("ItemParachute: Couldn't throw projectile as type(s) isn't supported. " +
                        "This is likely missing implementation on the projectile. " +
                        "Stack: {}, Data: {}, Entity: {}", projectileStack, projectileData, thrower);
                    return false;
                }

                // Spawn
                if (world.spawnEntity(parachute)) {

                    // Run post spawn logic
                    projectileStack.getProjectileData().onEntitySpawned(parachute, thrower);

                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flag) {
        final String key = getUnlocalizedName(stack) + ".info";
        final float gravity = -EntityParachute.GRAVITY * 20;
        final float air = (1 - EntityParachute.AIR_RESISTANCE) * 100;
        LanguageUtility.outputLines(new TextComponentTranslation(key, String.format("%.2f", air) + " %", String.format("%.2f", gravity)), list::add);
        //TODO passenger item
    }
}
