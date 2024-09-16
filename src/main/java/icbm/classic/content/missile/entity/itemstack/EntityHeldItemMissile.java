package icbm.classic.content.missile.entity.itemstack;

import com.google.common.collect.Multimap;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.entity.itemstack.item.CapabilityHeldItemMissile;
import icbm.classic.content.missile.entity.itemstack.item.HeldItemMissileHandler;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.saving.NbtSaveHandler;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * Missile holding an item, will use the item on impact if possible
 */
public class EntityHeldItemMissile extends EntityMissile<EntityHeldItemMissile> implements IEntityAdditionalSpawnData {

    private ItemStack renderStackCache;
    @Getter
    private final ItemStackHandler itemStackHandler = new ItemStackHandler(1); //TODO send packet if inventory changes
    @Getter @Setter
    private boolean primaryAction = true;

    boolean hasUsedAction = false;

    public EntityHeldItemMissile(World world) {
        super(world);
        this.setMaxHealth(ConfigMissile.TIER_2_HEALTH);
    }

    @Override
    public void onUpdate() {

        if(!world.isRemote) {
            // TODO add homing target logic
        }

        //Normal update logic
        super.onUpdate();
    }

    @Override
    protected void actionOnImpact(RayTraceResult hit) {
        if(!this.world.isRemote) {
            final ItemStack held = this.itemStackHandler.getStackInSlot(0);

            if(!hasUsedAction && !held.isEmpty() && HeldItemMissileHandler.isAllowed(held)) {
                useItemOnPosition(held, hit);
            }

            if (isEntityAlive()) {
                this.entityDropItem(toStack(), 0);
            }
            this.destroy();
        }
    }

    private void useItemOnPosition(ItemStack held, RayTraceResult hit) {
        if (!primaryAction) {
            final FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer) world); //TODO get shooter and use them as the player for protections & death logs

            held = held.copy();
            player.setHeldItem(EnumHand.MAIN_HAND, held);

            // Right click
            EnumActionResult ret = held.onItemUseFirst(player, world, hit.getBlockPos(), EnumHand.MAIN_HAND, hit.sideHit, 0, 0, 0);
            if (ret == EnumActionResult.PASS) {
                held.onItemUse(player, world, hit.getBlockPos(), EnumHand.MAIN_HAND, hit.sideHit, 0, 0, 0);
            }

            this.itemStackHandler.setStackInSlot(0, held);
        }
    }

    @Override
    protected void onImpactEntity(Entity entityHit, float velocity, RayTraceResult hit) {
        if (!world.isRemote) {
            final ItemStack held = this.itemStackHandler.getStackInSlot(0);

            if(!held.isEmpty() && HeldItemMissileHandler.isAllowed(held)) {
                if(primaryAction) {
                    useItemPrimaryOnEntity(held, entityHit, velocity);
                }
                else {
                    //TODO activate item on entity
                }
            }
            onImpact(hit);
        }
    }

    private void useItemPrimaryOnEntity(ItemStack held, Entity entityHit, float velocity) {
        final FakePlayer player = FakePlayerFactory.getMinecraft((WorldServer) world); //TODO get shooter and use them as the player for protections & death logs

        held = held.copy();
        player.setHeldItem(EnumHand.MAIN_HAND, held);

        // Left click entity
        if(entityHit instanceof EntityLivingBase) {

            final Multimap<String, AttributeModifier> attributes = held.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
            if(attributes.containsKey(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
                final AbstractAttributeMap attributeMap = new AttributeMap();
                attributeMap.registerAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(1.0D);
                attributeMap.applyAttributeModifiers(attributes);

                final IAttributeInstance attributeInstance =  attributeMap.getAttributeInstance(SharedMonsterAttributes.ATTACK_DAMAGE);
                double damage = attributeInstance.getAttributeValue();

                damage += EnchantmentHelper.getModifierForCreature(held, ((EntityLivingBase) entityHit).getCreatureAttribute());
                damage *= velocity;

                if(damage > 0) {
                    final EntityLivingBase attacker = this.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) this.shootingEntity : player;
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

            hasUsedAction = held.getItem().hitEntity(held, (EntityLivingBase) entityHit, player);
            if (hasUsedAction) {
                this.itemStackHandler.setStackInSlot(0, held);
            }
        }
    }

    @Override
    protected float getImpactDamage(Entity entityHit, float velocity, RayTraceResult hit) {
        return 0;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if(capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return (T) itemStackHandler;
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
            || super.hasCapability(capability, facing);
    }

    @Override
    public ItemStack toStack() { //TODO replace with more specific callbacks to decouple render side from drop-logic from pick-logic
        if(world.isRemote) {
            if(renderStackCache == null) {
                renderStackCache = genItem();
            }
            return renderStackCache;
        }
        return genItem();
    }

    private ItemStack genItem() {
        final ItemStack stack = new ItemStack(ItemReg.heldItemMissile);
        final ICapabilityMissileStack capabilityMissileStack = stack.getCapability(ICBMClassicAPI.MISSILE_STACK_CAPABILITY, null);
        if(capabilityMissileStack instanceof CapabilityHeldItemMissile) {
            ((CapabilityHeldItemMissile) capabilityMissileStack).setHeldItem(itemStackHandler.getStackInSlot(0));
            ((CapabilityHeldItemMissile) capabilityMissileStack).setPrimaryAction(primaryAction);
        }
        return stack;
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound nbt)
    {
        super.readEntityFromNBT(nbt);
        SAVE_LOGIC.load(this, nbt);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound nbt)
    {
        super.writeEntityToNBT(nbt);
        SAVE_LOGIC.save(this, nbt);
    }

    @Override
    public void writeSpawnData(ByteBuf additionalMissileData)
    {
        super.writeSpawnData(additionalMissileData);
        final NBTTagCompound saveData = SAVE_LOGIC.save(this, new NBTTagCompound());
        ByteBufUtils.writeTag(additionalMissileData, saveData);
    }

    @Override
    public void readSpawnData(ByteBuf additionalMissileData)
    {
        super.readSpawnData(additionalMissileData);
        final NBTTagCompound saveData = ByteBufUtils.readTag(additionalMissileData);
        SAVE_LOGIC.load(this, saveData);
    }

    private static final NbtSaveHandler<EntityHeldItemMissile> SAVE_LOGIC = new NbtSaveHandler<EntityHeldItemMissile>()
        .mainRoot()
        /* */.nodeINBTSerializable("inventory", EntityHeldItemMissile::getItemStackHandler)
        /* */.nodeBoolean("primary_action", EntityHeldItemMissile::isPrimaryAction, EntityHeldItemMissile::setPrimaryAction)
        .base();
}
