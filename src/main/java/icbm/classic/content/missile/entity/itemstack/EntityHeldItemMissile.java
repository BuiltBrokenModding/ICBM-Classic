package icbm.classic.content.missile.entity.itemstack;

import net.minecraft.world.item.ShearsItem;
import net.neoforged.neoforge.common.IShearable;
import net.minecraft.world.level.block.Block;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.HeldItemMissileHandler;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.EntityCapability;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import net.neoforged.neoforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Missile holding an item, will use the item on impact if possible
 */
public class EntityHeldItemMissile extends EntityMissile<EntityHeldItemMissile> implements IEntityWithComplexSpawn {

    private ItemStack renderStackCache;
    @Getter
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(1); //TODO send packet if inventory changes
    @Getter @Setter
    private HeldActionMode actionMode = HeldActionMode.PRIMARY_FIRST;

    boolean hasUsedAction = false;

    public EntityHeldItemMissile(EntityType<? extends EntityHeldItemMissile> type, Level world) {
        super(type, world);
        // PORT: 1.12 only set the max health, leaving the synced health at its default of 1
        this.initHealth(ConfigMissile.TIER_2_HEALTH);
    }

    public EntityHeldItemMissile(Level world) {
        this(EntityReg.MISSILE_HELD_ITEM.get(), world);
    }

    @Override
    public void tick() {

        if(!level().isClientSide()) {
            // TODO add homing target logic
        }

        //Normal update logic
        super.tick();
    }

    @Override
    protected IProjectileBlockInteraction.EnumHitReactions specialHandleBlock(BlockHitResult hit, double velocity) {
        if(!level().isClientSide()) {
            final ItemStack held = this.itemStackHandler.getStackInSlot(0);

            if (!hasUsedAction && !held.isEmpty() && HeldItemMissileHandler.isAllowed(held)) {
                try {
                    if (actionMode == HeldActionMode.PRIMARY || actionMode == HeldActionMode.PRIMARY_FIRST) {
                        usePrimaryOnPosition(held, hit, velocity);
                        if (actionMode == HeldActionMode.PRIMARY_FIRST) {
                            useSecondaryOnPosition(held, hit);
                        }
                    } else {
                        useSecondaryOnPosition(held, hit);
                        if (actionMode == HeldActionMode.SECONDARY_FIRST) {
                            usePrimaryOnPosition(held, hit, velocity);
                        }
                    }
                }
                catch (Exception e) {
                    hasUsedAction = true;
                    ICBMClassic.logger().error("Failed to mimic player interaction during missile impact. Missile: {}", this);
                    ICBMClassic.logger().error("Impact error", e);
                }
            }

            if (hasUsedAction) {
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
            }

            return ProjectileBlockInteraction.handleSpecialInteraction(level(), this.getInGroundData().getPos(), hit.getLocation(), this.getInGroundData().getSide(), this.getInGroundData().getState(), this);
        }
        return IProjectileBlockInteraction.EnumHitReactions.PASS;
    }

    @Override
    protected void actionOnImpact(HitResult hit) {
        if(!this.level().isClientSide()) {
            if (isAlive()) {
                this.spawnAtLocation(toStack(), 0);
            }
            this.destroy();
        }
    }

    private static Vec3 offsetBySide(BlockHitResult hit) {
        final Direction side = hit.getDirection();
        return hit.getLocation().add(side.getStepX(), side.getStepY(), side.getStepZ());
    }

    private void usePrimaryOnPosition(ItemStack held, BlockHitResult hit, double velocity) {
        final FakePlayer player = getFakePlayer(offsetBySide(hit));
        player.setItemInHand(InteractionHand.MAIN_HAND, held.copy());

        // TODO mimic player.interactionManager.onBlockClicked(hit.getBlockPos(), hit.sideHit);
        final Level world = level();
        final BlockPos pos = hit.getBlockPos();
        final BlockState state = world.getBlockState(pos);

        // Shear use this to instant break leaves (via IShearable)
        // PORT: 1.12 ItemShears#onBlockStartBreak sheared IShearable blocks; NeoForge 21.1 has no such hook, so do it directly
        if(held.getItem() instanceof ShearsItem && state.getBlock() instanceof IShearable shearable && shearable.isShearable(player, held, world, pos)) {
            if(!world.isClientSide()) {
                for(ItemStack drop : shearable.onSheared(player, held, world, pos)) {
                    Block.popResource(world, pos, drop);
                }
                world.removeBlock(pos, false);
            }
            hasUsedAction = true;
        }
        // TODO silk touch
        else if(state.canHarvestBlock(world, pos, player)) {
            // Player default dig speed with hand is 1.0, wood shovel is 2.0, stone is 4.0, diamond is 8.0
            //  block HP (technically progress) is hardness * 10, hardness = digSpeed / blockHardness / (30 if can harvest | 100 if can't)
            //  tool strike irl can be 5m/s to 10m/s depending on method... we want to scale by velocity with a bonus to mass of missile
            // Damage should be scaled on single impact and shouldn't account for some enchantments.

            // Prevent non-tools from breaking blocks, keeps a stick from killing a dirt block
            // PORT: 1.12 getToolClasses().isEmpty() -> item has no TOOL data component
            if(!player.getMainHandItem().has(DataComponents.TOOL)) {
                resetFakePlayer(player);
                return;
            }

            final float toolSpeed = player.getMainHandItem().getDestroySpeed(state);
            final float hardness = state.getDestroySpeed(world, pos);
            final float digSpeed = toolSpeed * (float)velocity * ConfigMissile.HELD_ITEM_MISSILE.BLOCK_DAMAGE_MULTIPLIER;

            if(hardness <= digSpeed) {
                this.hasUsedAction = true;

                // Special handling for dynamic tree mod, due to async tool isn't handled correctly for fake player
                final String clazzName = state.getBlock().getClass().getName();
                if(clazzName.startsWith("com.ferreusveritas.dynamictrees.blocks.") && clazzName.contains("Branch")) {
                    //TODO replace with their API and create system for dynamically registering special handlers
                   this.handleDynamicTreeBreak(state, world, pos, player);
                }
                // Break block TODO trigger events with shooter if player
                else {
                    final BlockEntity blockEntity = world.getBlockEntity(pos);
                    if (state.onDestroyedByPlayer(world, pos, player, true, world.getFluidState(pos))) {
                        state.getBlock().destroy(world, pos, state);
                        state.getBlock().playerDestroy(world, player, pos, state, blockEntity, player.getMainHandItem());
                    }
                }
            }
        }

        if(hasUsedAction) {
            this.itemStackHandler.setStackInSlot(0, player.getMainHandItem());
        }

        resetFakePlayer(player);
    }

    private void handleDynamicTreeBreak(BlockState state, Level world, BlockPos pos, LivingEntity player) {
        try {
            // PORT-CHECK: reflective hook into Dynamic Trees, method name/signature from the 1.12 mod
            Class clazz = state.getBlock().getClass();
            Method method = clazz.getMethod("futureBreak", BlockState.class, Level.class, BlockPos.class, LivingEntity.class);
            method.invoke(state.getBlock(), state, world, pos, player);
        }
        catch (Exception e) {
            ICBMClassic.logger().error("Failed to handle breaking dynamic tree. Missile: " + this, e);
        }
    }

    private void useSecondaryOnPosition(ItemStack held, BlockHitResult hit) {
        final FakePlayer player = getFakePlayer(offsetBySide(hit));

        player.setItemInHand(InteractionHand.MAIN_HAND, held.copy());

        // Right click
        // PORT: 1.12 passed hit offsets of 0,0,0; UseOnContext uses the real hit and the fake player's held copy
        final UseOnContext context = new UseOnContext(player, InteractionHand.MAIN_HAND, hit);
        final ItemStack used = player.getMainHandItem();
        InteractionResult ret = used.onItemUseFirst(context);
        if (ret == InteractionResult.PASS) {
            used.useOn(context);
        }

        //TODO handle offhand

        this.itemStackHandler.setStackInSlot(0, player.getItemInHand(InteractionHand.MAIN_HAND));
        resetFakePlayer(player);
    }

    @Override
    protected void onImpactEntity(@Nonnull Entity entityHit, float velocity, @Nonnull HitResult hit) {
        if (!level().isClientSide()) {
            final ItemStack held = this.itemStackHandler.getStackInSlot(0);
            final FakePlayer player = getFakePlayer(hit.getLocation());

            if(!held.isEmpty() && HeldItemMissileHandler.isAllowed(held)) {
                if(actionMode == HeldActionMode.PRIMARY || actionMode == HeldActionMode.PRIMARY_FIRST) {
                    useItemPrimaryOnEntity(player, held, entityHit, velocity);
                    if(actionMode == HeldActionMode.PRIMARY_FIRST) {
                        useItemSecondaryOnEntity(player, held, entityHit);
                    }
                }
                else {
                    useItemSecondaryOnEntity(player, held, entityHit);
                    if(actionMode == HeldActionMode.SECONDARY_FIRST) {
                        useItemPrimaryOnEntity(player, held, entityHit, velocity);
                    }
                }
            }
            onImpact(hit);
        }
    }

    private void useItemSecondaryOnEntity(FakePlayer player, ItemStack held, Entity entityHit) {

        // Setup player
        player.setItemInHand(InteractionHand.MAIN_HAND, held.copy());

        if(entityHit instanceof LivingEntity) {
            // PORT-CHECK: 1.12 compared == SUCCESS, consumesAction also accepts CONSUME
            hasUsedAction = player.interactOn(entityHit, InteractionHand.MAIN_HAND).consumesAction();

            if(!hasUsedAction) {

                // Try off-hand
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                player.setItemInHand(InteractionHand.OFF_HAND, held.copy());

                hasUsedAction = player.interactOn(entityHit, InteractionHand.OFF_HAND).consumesAction();

                // If used store impacted item, this may not be the same item
                if(hasUsedAction) {
                    this.itemStackHandler.setStackInSlot(0, player.getItemInHand(InteractionHand.OFF_HAND));
                }
            }
            else {
                this.itemStackHandler.setStackInSlot(0, player.getItemInHand(InteractionHand.MAIN_HAND));
            }
        }

        resetFakePlayer(player);
    }

    private FakePlayer getFakePlayer(Vec3 pos) {
        final FakePlayer player = FakePlayerFactory.getMinecraft((ServerLevel) level());
        player.setPos(pos.x, pos.y, pos.z);
        //TODO get shooter and use them as the player for protections & death logs
        resetFakePlayer(player);
        return player;
    }

    private void resetFakePlayer(FakePlayer player) {
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        player.stopRiding();
    }

    private void useItemPrimaryOnEntity(FakePlayer player, ItemStack held, Entity entityHit, float velocity) {

        // Setup player
        player.setItemInHand(InteractionHand.MAIN_HAND, held.copy());

        // Left click entity
        if(entityHit instanceof LivingEntity livingHit) {

            // PORT-CHECK: 1.21 attribute modifiers are a data component; built a standalone instance like 1.12's AttributeMap
            final AttributeInstance attributeInstance = new AttributeInstance(Attributes.ATTACK_DAMAGE, (inst) -> {});
            attributeInstance.setBaseValue(1.0D);
            final boolean[] hasAttackDamage = {false};
            held.getAttributeModifiers().forEach(EquipmentSlotGroup.MAINHAND, (attribute, modifier) -> {
                if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                    hasAttackDamage[0] = true;
                    if (!attributeInstance.hasModifier(modifier.id())) {
                        attributeInstance.addTransientModifier(modifier);
                    }
                }
            });

            if(hasAttackDamage[0]) {
                final LivingEntity attacker = this.shootingEntity instanceof LivingEntity ? (LivingEntity) this.shootingEntity : player;
                final DamageSource damageSource = damageSources().mobProjectile(this, attacker); // 1.12 causeIndirectDamage
                double damage = attributeInstance.getValue();

                // PORT-CHECK: 1.12 getModifierForCreature -> EnchantmentHelper#modifyDamage (adds sharpness/smite/etc)
                damage = EnchantmentHelper.modifyDamage((ServerLevel) level(), held, entityHit, damageSource, (float) damage);
                damage *= velocity;

                if(damage > 0) {
                    entityHit.hurt(damageSource, (float)damage);
                }
                //TODO if entity dies keep moving with sword

                // TODO pull knockback from item

                // PORT-CHECK: 1.12 fire aspect + applyEnchantments are both post-attack enchantment effects in 1.21
                EnchantmentHelper.doPostAttackEffectsWithItemSource((ServerLevel) level(), entityHit, damageSource, held);
            }

            hasUsedAction = held.getItem().hurtEnemy(held, livingHit, player);
            if (hasUsedAction) {
                this.itemStackHandler.setStackInSlot(0, player.getItemInHand(InteractionHand.MAIN_HAND));
            }
        }

        // Reset
        resetFakePlayer(player);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, HitResult hit) {
        return 0;
    }

    @Nullable
    @Override
    public <T> T getSidedCapability(EntityCapability<T, @org.jetbrains.annotations.Nullable Direction> capability, @Nullable Direction side) {
        if(capability == Capabilities.ItemHandler.ENTITY_AUTOMATION) {
            return (T) itemStackHandler;
        }
        return super.getSidedCapability(capability, side);
    }

    @Override
    public ItemStack toStack() { //TODO replace with more specific callbacks to decouple render side from drop-logic from pick-logic
        if(level().isClientSide()) {
            if(renderStackCache == null) {
                renderStackCache = genItem();
            }
            return renderStackCache;
        }
        return genItem();
    }

    private ItemStack genItem() {
        final ItemStack stack = new ItemStack(ItemReg.heldItemMissile.get());
        final ICapabilityMissileStack capabilityMissileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY);
        if(capabilityMissileStack instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) capabilityMissileStack).setHeldItem(itemStackHandler.getStackInSlot(0));
            ((CapabilityHeldItemMissile) capabilityMissileStack).setActionMode(getActionMode());
        }
        return stack;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final CompoundTag saveData = SAVE_LOGIC.save(this, new CompoundTag());
        additionalMissileData.writeNbt(saveData);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final CompoundTag saveData = additionalMissileData.readNbt();
        if (saveData != null) {
            SAVE_LOGIC.load(this, saveData);
        }
    }

    private static final NbtSaveHandler<EntityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<EntityHeldItemMissile>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", EntityHeldItemMissile::getItemStackHandler)
        /* */.nodeEnumString("action_mode", EntityHeldItemMissile::getActionMode, EntityHeldItemMissile::setActionMode, HeldActionMode::valueOf)
        .base();
}
// PORTED
