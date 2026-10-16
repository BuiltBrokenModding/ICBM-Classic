package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.missiles.IMissileAiming;
import icbm.classic.api.missiles.parts.IMissileFlightLogicStep;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.cluster.action.ActionDataCluster;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import icbm.classic.content.missile.logic.flight.ArcFlightLogic;
import icbm.classic.content.missile.logic.flight.DeadFlightLogic;
import icbm.classic.content.missile.logic.flight.move.MoveByVec3Logic;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.content.missile.logic.targeting.BasicTargetData;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Rocket Launcher
 * <p>
 * PORT: the "pulling" item property is registered client side in {@code ClientReg}.
 *
 * @author Calclavia
 */

public class ItemRocketLauncher extends ItemICBMElectrical
{
    private static final int ENERGY = 1000000;
    private static final int firingDelay = 1000;

    private static final double minDistance = 20;
    private static final double ballisticBurstY = 20;

    private final boolean fireUpDown;

    public ItemRocketLauncher(boolean fireUpDown)
    {
        // 1.12 never set a max stack size for launchers, so they stack to 64 like any other item
        super(fireUpDown ? "ballisticLauncher" : "rocketLauncher", new Item.Properties()); //TODO move to set name
        this.fireUpDown = fireUpDown;
    }

    /** @return true if this launcher fires in an arc */
    public boolean isBallistic()
    {
        return fireUpDown;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected)
    {

    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity)
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack)
    {
        return UseAnim.BOW;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity shooter, int timeLeft)
    {
        if (shooter instanceof Player player)
        {
            if (this.getEnergy(stack) >= ENERGY || player.isCreative())
            {
                // Check the player's inventory and look for missiles.
                for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++) //TODO add ammo wheel to select missile to use
                {
                    final ItemStack inventoryStack = player.getInventory().getItem(slot);

                    final ICapabilityMissileStack capabilityMissileStack = inventoryStack.isEmpty() ? null : inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
                    if (capabilityMissileStack != null)
                    {
                        if (!world.isClientSide())
                        {
                            final IMissile missile = capabilityMissileStack.newMissile(world);
                            final Entity missileEntity = missile.getMissileEntity();

                            if (missileEntity instanceof IMissileAiming) //TODO convert to actionData that will use causeBy to trigger init
                            {
                                //Setup aiming and offset from player
                                ((IMissileAiming) missileEntity).initAimingPosition(player, 1, ConfigMissile.DIRECT_FLIGHT_SPEED);

                                // Get player aim
                                final Vec3 eyePos = player.getEyePosition(1.0F);
                                final Vec3 lookVector = player.getViewVector(1.0F);

                                // Javelin style launching
                                if(fireUpDown) {
                                    //TODO check min distance
                                    final IMissileFlightLogicStep stepLockHeight = new MoveByVec3Logic()
                                        .setDistance(3)
                                        .setRelative(false)
                                        .setDirection(lookVector)
                                        .setAcceleration(0.2);
                                    missile.setFlightLogic(stepLockHeight.addStep(new ArcFlightLogic()));
                                }
                                // Dummy RPG firing
                                else {
                                    missile.setFlightLogic(new DeadFlightLogic(ConfigMissile.HANDHELD_FUEL));
                                }

                                // Setup source of missile for later cause by TODO include item used
                                missile.setMissileSource(new ActionSource(world, missileEntity.position(), new EntityCause(player)));

                                // Raytrace to set a default target for air-burst missiles
                                final double traceDistance = 500;
                                final Vec3 rayEnd = eyePos.add(lookVector.x * traceDistance, lookVector.y * traceDistance, lookVector.z * traceDistance);
                                // 1.12: rayTraceBlocks(start, end, stopOnLiquid=false, ignoreNoBox=true, returnLast=false)
                                final HitResult rayTraceResult = world.clip(new ClipContext(eyePos, rayEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                                if(rayTraceResult != null && rayTraceResult.getType() != HitResult.Type.MISS) {

                                    if(fireUpDown && rayTraceResult.getLocation().distanceTo(player.position()) < minDistance) { //TODO customize
                                        player.displayClientMessage(Component.translatable("item.icbmclassic:rocketLauncher.error.distance.min", minDistance), true);
                                        return;
                                    }
                                    missile.setTargetData(new BasicTargetData(rayTraceResult.getLocation()));
                                }
                                else if(fireUpDown) {
                                    player.displayClientMessage(Component.translatable("item.icbmclassic:rocketLauncher.error.targeting"), true);
                                    return;
                                }

                                // Move aim position up if cluster TODO expose this to a user so it can be set for any missile
                                if(fireUpDown && missileEntity instanceof EntityMissileActionable && ((EntityMissileActionable) missileEntity).getMainAction() != null)
                                {
                                    final IPotentialAction potentialAction = ((EntityMissileActionable) missileEntity).getMainAction();
                                    if(potentialAction.getActionData() instanceof ActionDataCluster) {
                                        ((BasicTargetData)missile.getTargetData()).setPosition(missile.getTargetData().getPosition().add(0, ballisticBurstY, 0));
                                    }
                                }

                                missile.launch();

                                //Spawn entity into world
                                if(world.addFreshEntity(missileEntity))
                                {
                                    // PORT: 1.12 kept the reload delay in a client side map of click times, which a dedicated
                                    // server never saw. The item cooldown is server driven and synced to the client.
                                    player.getCooldowns().addCooldown(this, firingDelay / 50);

                                    if (player.isShiftKeyDown()) //TODO allow missile to have control of when riding is allowed
                                    {
                                        player.startRiding(missileEntity);
                                        player.setShiftKeyDown(false);
                                    }

                                    else if(player.getItemInHand(InteractionHand.OFF_HAND).getItem() == Items.LEAD) {

                                        final double x = shooter.getX();
                                        final double y = shooter.getY();
                                        final double z = shooter.getZ();

                                        for (Mob victim : world.getEntitiesOfClass(Mob.class, new AABB(
                                            x - 7.0D, y - 7.0D, z - 7.0D,
                                            x + 7.0D, y + 7.0D, z + 7.0D))
                                        )
                                        {
                                            if (victim.getLeashHolder() == player)
                                            {
                                                //victim.setLeashHolder(missileEntity, true);
                                                victim.startRiding(missileEntity);
                                                break;
                                            }
                                        }
                                    }

                                    if (!player.isCreative())
                                    {
                                        player.getInventory().setItem(slot, capabilityMissileStack.consumeMissile());
                                        player.inventoryMenu.broadcastChanges();
                                        this.discharge(stack, ENERGY, true);
                                    }
                                }
                                else
                                {
                                    player.displayClientMessage(Component.translatable("item.icbmclassic:rocketLauncher.error.spawning"), true);
                                }
                            }
                            else
                            {
                                player.displayClientMessage(Component.translatable("item.icbmclassic:rocketLauncher.error.IMissileAiming", inventoryStack.getHoverName()), true);
                            }

                            //Exit loop to prevent firing all missiles in inventory
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player player, InteractionHand handIn)
    {
        // Reload delay is the item cooldown set in releaseUsing, vanilla skips use() while it runs
        //TODO play weapon empty click audio to note the gun is reloading
        final ItemStack itemstack = player.getItemInHand(handIn);
        player.startUsingItem(handIn);
        // PORT: consume instead of success, in 1.21 success swings the arm before the aim pose
        return InteractionResultHolder.consume(itemstack);
    }
}
// PORTED
