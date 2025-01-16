package icbm.classic.content.missile.entity.itemstack;

import com.google.common.collect.Multimap;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.HeldItemMissileHandler;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;
import java.lang.reflect.Method;

/**
 * Missile holding an item, will use the item on impact if possible
 */
public class EntityHeldItemMissile extends EntityMissile<EntityHeldItemMissile> implements IEntityAdditionalSpawnData {

    @Getter
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(1); //TODO send packet if inventory changes
    private final LazyOptional<ItemStackHandler> itemStackHandlerLazy = LazyOptional.of(() -> itemStackHandler);

    @Getter @Setter
    private HeldActionMode actionMode = HeldActionMode.PRIMARY_FIRST;

    boolean hasUsedAction = false;

    private final LazyOptional<ItemStack> itemstack;

    public EntityHeldItemMissile(EntityType<EntityHeldItemMissile> type, World world, NonNullSupplier<ItemStack> stackSupplier) {
        super(type, world);
        this.itemstack = LazyOptional.of(stackSupplier);
        this.setMaxHealth(ConfigMissile.TIER_2_HEALTH);
    }

    @Override
    public void tick() {

        if(!world.isRemote) {
            // TODO add homing target logic
        }

        //Normal update logic
        super.tick();
    }

    @Override
    protected IProjectileBlockInteraction.EnumHitReactions specialHandleBlock(BlockRayTraceResult hit, double velocity) {
        if(!world.isRemote) {
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

            return ProjectileBlockInteraction.handleSpecialInteraction(world, this.getInGroundData().getPos(), hit.getHitVec(), this.getInGroundData().getSide(), this.getInGroundData().getState(), this);
        }
        return IProjectileBlockInteraction.EnumHitReactions.PASS;
    }

    @Override
    protected void actionOnImpact(RayTraceResult hit) {
        if(!this.world.isRemote) {
            if (isAlive()) {
                this.entityDropItem(toStack(), 0);
            }
            this.destroy();
        }
    }

    private void usePrimaryOnPosition(ItemStack held, BlockRayTraceResult hit, double velocity) {
        final FakePlayer player = getFakePlayer(hit.getHitVec()
            .add(hit.getFace().getDirectionVec().getX(), hit.getFace().getDirectionVec().getY(), hit.getFace().getDirectionVec().getZ()));
        player.setHeldItem(Hand.MAIN_HAND, held.copy());

        // TODO mimic player.interactionManager.onBlockClicked(hit.getBlockPos(), hit.sideHit);
        final BlockPos pos = hit.getPos();
        final BlockState state = world.getBlockState(pos);

        // Shear use this to instant break leaves (via IShearable)
        if(held.getItem().onBlockStartBreak(held, pos, player)) {
            hasUsedAction = true;
        }
        // TODO state.getBlock().canSilkHarvest(world, pos, state, player) && EnchantmentHelper.getEnchantmentLevel(Enchantments.SILK_TOUCH, held) > 0
        else if(state.getBlock().canHarvestBlock(state, world, pos, player)) {
            // Player default dig speed with hand is 1.0, wood shovel is 2.0, stone is 4.0, diamond is 8.0
            //  block HP (technically progress) is hardness * 10, hardness = digSpeed / blockHardness / (30 if can harvest | 100 if can't)
            //  tool strike irl can be 5m/s to 10m/s depending on method... we want to scale by velocity with a bonus to mass of missile
            // Damage should be scaled on single impact and shouldn't account for some enchantments.

            // Prevent non-tools from breaking blocks, keeps a stick from killing a dirt block
            if(player.getHeldItemMainhand().getItem().getToolTypes(held).isEmpty()) {
                resetFakePlayer(player);
                return;
            }

            final float toolSpeed = player.getHeldItemMainhand().getDestroySpeed(state);
            final float hardness = state.getBlockHardness(world, pos);
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
                else if (state.getBlock().removedByPlayer(state, world, pos, player, true, this.world.getFluidState(pos)))
                {
                    state.getBlock().harvestBlock(this.world, player, pos, state, world.getTileEntity(pos), player.getHeldItem(Hand.MAIN_HAND));
                    state.getBlock().onPlayerDestroy(this.world, pos, state);
                }
            }
        }

        if(hasUsedAction) {
            this.itemStackHandler.setStackInSlot(0, player.getHeldItemMainhand());
        }

        resetFakePlayer(player);
    }

    private void handleDynamicTreeBreak(BlockState state, World world, BlockPos pos, LivingEntity player) {
        try {
            // https://github.com/DynamicTreesTeam/DynamicTrees/blob/release/1.12.2/src/main/java/com/ferreusveritas/dynamictrees/blocks/BlockBranch.java#L396
            Class clazz = state.getBlock().getClass();
            Method method = clazz.getMethod("futureBreak", BlockState.class, World.class, BlockPos.class, LivingEntity.class);
            method.invoke(state.getBlock(), state, world, pos, player);
        }
        catch (Exception e) {
            ICBMClassic.logger().error("Failed to handle breaking dynamic tree. Missile: " + this, e);
        }
    }

    private void useSecondaryOnPosition(ItemStack held, BlockRayTraceResult hit) {
        final FakePlayer player = getFakePlayer(hit.getHitVec()
            .add(hit.getFace().getDirectionVec().getX(), hit.getFace().getDirectionVec().getY(), hit.getFace().getDirectionVec().getZ()));

        player.setHeldItem(Hand.MAIN_HAND, held.copy());

        // Right click
        final ItemUseContext context = new ItemUseContext(player, Hand.MAIN_HAND, hit);
        final ActionResultType ret = held.onItemUseFirst(context);
        if (ret == ActionResultType.PASS) {
            held.onItemUse(context);
        }

        //TODO handle offhand

        this.itemStackHandler.setStackInSlot(0, player.getHeldItem(Hand.MAIN_HAND));
        resetFakePlayer(player);
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity, EntityRayTraceResult hit) {
        if (!world.isRemote) {
            final ItemStack held = this.itemStackHandler.getStackInSlot(0);
            final FakePlayer player = getFakePlayer(hit.getHitVec()); //TODO move slightly away from target

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
        player.setHeldItem(Hand.MAIN_HAND, held.copy());

        if(entityHit instanceof LivingEntity) {
            hasUsedAction = player.interactOn(entityHit, Hand.MAIN_HAND) == ActionResultType.SUCCESS;

            if(!hasUsedAction) {

                // Try off-hand
                player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                player.setHeldItem(Hand.OFF_HAND, held.copy());

                hasUsedAction = player.interactOn(entityHit, Hand.OFF_HAND) == ActionResultType.SUCCESS;

                // If used store impacted item, this may not be the same item
                if(hasUsedAction) {
                    this.itemStackHandler.setStackInSlot(0, player.getHeldItem(Hand.OFF_HAND));
                }
            }
            else {
                this.itemStackHandler.setStackInSlot(0, player.getHeldItem(Hand.MAIN_HAND));
            }
        }

        resetFakePlayer(player);
    }

    private FakePlayer getFakePlayer(Vec3d pos) {
        final FakePlayer player = FakePlayerFactory.getMinecraft((ServerWorld) world);
        player.setPosition(pos.x, pos.y, pos.z);
        //TODO get shooter and use them as the player for protections & death logs
        resetFakePlayer(player);
        return player;
    }

    private void resetFakePlayer(FakePlayer player) {
        player.setHeldItem(Hand.OFF_HAND, ItemStack.EMPTY);
        player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
        player.stopRiding();
        player.removePassengers();
    }

    private void useItemPrimaryOnEntity(FakePlayer player, ItemStack held, Entity entityHit, float velocity) {

        // Setup player
        player.setHeldItem(Hand.MAIN_HAND, held.copy());

        // Left click entity
        if(entityHit instanceof LivingEntity) {

            final Multimap<String, AttributeModifier> attributes = held.getAttributeModifiers(EquipmentSlotType.MAINHAND);
            if(attributes.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
                final AbstractAttributeMap attributeMap = new AttributeMap();
                attributeMap.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
                attributeMap.applyAttributeModifiers(attributes);

                final IAttributeInstance attributeInstance =  attributeMap.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
                double damage = attributeInstance.getValue();

                damage += EnchantmentHelper.getModifierForCreature(held, ((LivingEntity) entityHit).getCreatureAttribute());
                damage *= velocity;

                if(damage > 0) {
                    final LivingEntity attacker = this.getOwner() instanceof LivingEntity ? (LivingEntity) this.getOwner() : player;
                    entityHit.attackEntityFrom(DamageSource.causeIndirectDamage(this, attacker), (float)damage);
                }
                //TODO if entity dies keep moving with sword

                // TODO pull knockback from item

                final int j = EnchantmentHelper.getFireAspectModifier(player);
                if (j > 0)
                {
                    entityHit.setFire(j * 4);
                }

                this.applyEnchantments(player, entityHit);
            }

            hasUsedAction = held.getItem().hitEntity(held, (LivingEntity) entityHit, player);
            if (hasUsedAction) {
                this.itemStackHandler.setStackInSlot(0, player.getHeldItem(Hand.MAIN_HAND));
            }
        }

        // Reset
        resetFakePlayer(player);
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return 0;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return itemStackHandlerLazy.cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public ItemStack toStack() { //TODO replace with more specific callbacks to decouple render side from drop-logic from pick-logic
        if(world.isRemote) {
            return this.itemstack.orElse(ItemStack.EMPTY);
        }
        return genItem();
    }

    private ItemStack genItem() {
        final ItemStack stack = this.itemstack.map(ItemStack::copy).orElse(ItemStack.EMPTY);
        stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null).ifPresent(capabilityMissileStack -> {
            if(capabilityMissileStack instanceof CapabilityHeldItemMissile) {
                ((CapabilityHeldItemMissile) capabilityMissileStack).setHeldItem(itemStackHandler.getStackInSlot(0));
                ((CapabilityHeldItemMissile) capabilityMissileStack).setActionMode(getActionMode());
            }
        });
        return stack;
    }

    @Override
    public void readAdditional(CompoundNBT nbt)
    {
        super.readAdditional(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeAdditional(CompoundNBT nbt)
    {
        super.writeAdditional(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    @Override
    public void writeSpawnData(PacketBuffer additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        additionalMissileData.writeCompoundTag(SAVE_LOGIC.save(this));
    }

    @Override
    public void readSpawnData(PacketBuffer additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        SAVE_LOGIC.load(this, additionalMissileData.readCompoundTag());
    }

    private static final NbtSaveHandler<EntityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<EntityHeldItemMissile>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", EntityHeldItemMissile::getItemStackHandler)
        /* */.nodeEnumString("action_mode", EntityHeldItemMissile::getActionMode, EntityHeldItemMissile::setActionMode, HeldActionMode::valueOf)
        .base();
}
