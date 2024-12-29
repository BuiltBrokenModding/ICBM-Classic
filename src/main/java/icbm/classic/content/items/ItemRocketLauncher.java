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
import net.minecraft.entity.*;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;

/**
 * Rocket Launcher
 *
 * @author Calclavia
 */

public class ItemRocketLauncher extends ItemICBMElectrical
{
    private static final int ENERGY = 1000000;
    private static final int firingDelay = 1000;
    private final HashMap<String, Long> clickTimePlayer = new HashMap<String, Long>();

    private static final double minDistance = 20;
    private static final double ballisticBurstY = 20;

    private final boolean fireUpDown;

    public ItemRocketLauncher(Item.Properties properties, boolean fireUpDown)
    {
        super(properties); //TODO move to set name
        this.fireUpDown = fireUpDown;
        this.addPropertyOverride(new ResourceLocation("pulling"), new IItemPropertyGetter()
        {
            @OnlyIn(Dist.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable LivingEntity entityIn)
            {
                return entityIn != null && entityIn.isHandActive() && entityIn.getActiveItemStack() == stack ? 1.0F : 0.0F;
            }
        });
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected)
    {

    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack)
    {
        return Integer.MAX_VALUE;
    }

    @Override
    public UseAction getItemUseAction(ItemStack par1ItemStack)
    {
        return UseAction.BOW;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, LivingEntity shooter, int timeLeft)
    {
        if (shooter instanceof PlayerEntity)
        {
            final PlayerEntity player = (PlayerEntity) shooter;
            if (this.getEnergy(stack) >= ENERGY || player.capabilities.isCreativeMode)
            {
                // Check the player's inventory and look for missiles.
                for (int slot = 0; slot < player.inventory.getSizeInventory(); slot++) //TODO add ammo wheel to select missile to use
                {
                    final ItemStack inventoryStack = player.inventory.getStackInSlot(slot);

                    if (inventoryStack.hasCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null))
                    {
                        final ICapabilityMissileStack capabilityMissileStack = inventoryStack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
                        if (capabilityMissileStack != null)
                        {
                            if (!world.isRemote)
                            {
                                final IMissile missile = capabilityMissileStack.newMissile(world);
                                final Entity missileEntity = missile.getMissileEntity();

                                if (missileEntity instanceof IMissileAiming) //TODO convert to actionData that will use causeBy to trigger init
                                {
                                    //Setup aiming and offset from player
                                    ((IMissileAiming) missileEntity).initAimingPosition(player, 1, ConfigMissile.DIRECT_FLIGHT_SPEED);

                                    // Get player aim
                                    final Vec3d eyePos = player.getPositionEyes(1.0F);
                                    final Vec3d lookVector = player.getLook(1.0F);

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
                                    missile.setMissileSource(new ActionSource(world, missileEntity.getPositionVector(), new EntityCause(player)));

                                    // Raytrace to set a default target for air-burst missiles
                                    final double traceDistance = 500;
                                    final Vec3d rayEnd = eyePos.addVector(lookVector.x * traceDistance, lookVector.y * traceDistance, lookVector.z * traceDistance);
                                    final RayTraceResult rayTraceResult = world.rayTraceBlocks(eyePos, rayEnd, false, true, false);

                                    if(rayTraceResult != null && rayTraceResult.hitVec != null) {

                                        if(fireUpDown && rayTraceResult.hitVec.distanceTo(player.getPositionVector()) < minDistance) { //TODO customize
                                            player.sendStatusMessage(new TranslationTextComponent("item.icbmclassic:rocketLauncher.error.distance.min", minDistance), true);
                                            return;
                                        }
                                        missile.setTargetData(new BasicTargetData(rayTraceResult.hitVec));
                                    }
                                    else if(fireUpDown) {
                                        player.sendStatusMessage(new TranslationTextComponent("item.icbmclassic:rocketLauncher.error.targeting"), true);
                                        return;
                                    }

                                    // Move aim position up if cluster TODO expose this to a user so it can be set for any missile
                                    if(fireUpDown && missileEntity instanceof EntityMissileActionable && ((EntityMissileActionable) missileEntity).getMainAction() != null)
                                    {
                                        final IPotentialAction potentialAction = ((EntityMissileActionable) missileEntity).getMainAction();
                                        if(potentialAction.getActionData() instanceof ActionDataCluster) {
                                            ((BasicTargetData)missile.getTargetData()).setPosition(missile.getTargetData().getPosition().addVector(0, ballisticBurstY, 0));
                                        }
                                    }

                                    missile.launch();

                                    //Spawn entity into world
                                    if(world.spawnEntity(missileEntity))
                                    {
                                        if (player.isSneaking()) //TODO allow missile to have control of when riding is allowed
                                        {
                                            player.startRiding(missileEntity);
                                            player.setSneaking(false);
                                        }

                                        else if(player.getHeldItem(Hand.OFF_HAND).getItem() == Items.LEAD) {

                                            final double x = shooter.posX;
                                            final double y = shooter.posY;
                                            final double z = shooter.posZ;

                                            for (MobEntity victim : world.getEntitiesWithinAABB(MobEntity.class, new AxisAlignedBB(
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

                                        if (!player.capabilities.isCreativeMode)
                                        {
                                            player.inventory.setInventorySlotContents(slot, capabilityMissileStack.consumeMissile());
                                            player.inventoryContainer.detectAndSendChanges();
                                            this.discharge(stack, ENERGY, true);
                                        }
                                    }
                                    else
                                    {
                                        player.sendStatusMessage(new TranslationTextComponent("item.icbmclassic:rocketLauncher.error.spawning"), true);
                                    }
                                }
                                else
                                {
                                    player.sendStatusMessage(new TranslationTextComponent("item.icbmclassic:rocketLauncher.error.IMissileAiming", inventoryStack), true);
                                }

                                //Exit loop to prevent firing all missiles in inventory
                                return;
                            }

                            //Store last time player launched a rocket
                            clickTimePlayer.put(player.getName(), System.currentTimeMillis());
                        }
                    }
                }
            }
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity player, Hand handIn)
    {
        ItemStack itemstack = player.getHeldItem(handIn);

        long clickMs = System.currentTimeMillis();
        if (clickTimePlayer.containsKey(player.getName()))
        {
            if (clickMs - clickTimePlayer.get(player.getName()) < firingDelay)
            {
                //TODO play weapon empty click audio to note the gun is reloading
                return new ActionResult<ItemStack>(ActionResultType.FAIL, itemstack);
            }
        }

        player.setActiveHand(handIn);
        return new ActionResult<ItemStack>(ActionResultType.SUCCESS, itemstack);
    }
}
