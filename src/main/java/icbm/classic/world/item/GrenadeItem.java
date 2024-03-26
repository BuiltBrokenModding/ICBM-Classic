package icbm.classic.world.item;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.item.ItemBase;
import icbm.classic.world.block.explosive.ItemBlockExplosive;
import icbm.classic.world.entity.GrenadeEntity;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityLivingBase;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class GrenadeItem extends ItemBase {
    public static final int MAX_USE_DURATION = 3 * 20; //TODO config

    public GrenadeItem(Properties properties) {
        super(properties);
        this.setMaxStackSize(16);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @Nullable
    public net.neoforged.common.capabilities.ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        final CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(stack);
        if (nbt != null) {
            capabilityExplosive.deserializeNBT(nbt);
        }
        return capabilityExplosive;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack par1ItemStack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack par1ItemStack) {
        return MAX_USE_DURATION;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(Level levelIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getHeldItem(handIn);
        playerIn.setActiveHand(handIn);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemstack);
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack itemStack, Level level, EntityLivingBase entityLiving, int timeLeft) {
        if (!world.isClientSide()) {
            //Play throw sound
            world.playSound(null, entityLiving.getX(), entityLiving.getY(), entityLiving.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 0.5F, 0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

            //Calculate energy based on player hold time
            final float throwEnergy = (float) (this.getMaxItemUseDuration(itemStack) - timeLeft) / (float) this.getMaxItemUseDuration(itemStack);

            //Create generate entity
            new GrenadeEntity(world)
                .setItemStack(itemStack)
                .setThrower(entityLiving)
                .aimFromThrower()
                .setThrowMotion(throwEnergy).spawn();

            //Consume item
            if (!(entityLiving instanceof Player) || !((Player) entityLiving).capabilities.isCreativeMode) {
                itemStack.shrink(1);
            }
        }
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        final ExplosiveType data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(itemstack.getItemDamage());
        if (data != null) {
            return "grenade." + data.getRegistryName();
        }
        return "grenade";
    }

    @Override
    public String getUnlocalizedName() {
        return "grenade";
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, Player player) {
        return true;
    }

    @Override
    protected void getDetailedInfo(ItemStack stack, Player player, List list) {
        ((ItemBlockExplosive) Item.getItemFromBlock(BlockReg.blockExplosive)).getDetailedInfo(stack, player, list);
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH) {
            for (int id : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosivesIDs()) {
                list.add(new ItemStack(this, 1, id));
            }
        }
    }
}
