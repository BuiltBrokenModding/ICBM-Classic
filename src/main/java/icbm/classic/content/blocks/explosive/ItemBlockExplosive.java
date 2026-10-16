package icbm.classic.content.blocks.explosive;

import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.reg.ComponentReg;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.lib.capability.holder.ICapabilityHolderItem;
import icbm.classic.prefab.item.ISubItemProvider;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.ItemCapability;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Item for the explosive block. The explosive type is stored in {@link ComponentReg#EXPLOSIVE}.
 */
public class ItemBlockExplosive extends ItemBlockAbstract implements ICapabilityHolderItem, ISubItemProvider
{
    private final String count_key = "explosive.icbmclassic:redMatter.info.count";

    private int tauntCount = 0;
    private int redmatterRandomTranslations = -1;

    private Long lastTranslationChange = 0L;

    private static int changeOverDelay = 1000 * 60;

    public ItemBlockExplosive(Block block)
    {
        super(block);
    }

    /**
     * Gets the explosive of the stack, 1.12 meta 0 is used when the component is missing
     */
    @Nullable
    protected static IExplosiveData getExplosiveData(ItemStack stack)
    {
        final ResourceLocation id = stack.get(ComponentReg.EXPLOSIVE.get());
        if (id == null)
        {
            return ICBMClassicHelpers.getExplosive(0, true);
        }
        return ICBMClassicHelpers.getExplosive(id, true);
    }

    @Override
    @Nullable
    public <T> T getCapability(ItemCapability<T, Void> capability, ItemStack stack)
    {
        if (capability == ICBMClassicAPI.EXPLOSIVE_ITEM_CAPABILITY)
        {
            // PORT-CHECK: CapabilityExplosiveStack reads customizations from the stack itself (no initCapabilities nbt)
            return (T) new CapabilityExplosiveStack(stack);
        }
        return null;
    }

    @Override
    public void getDetailedInfo(ItemStack stack, @Nullable Player player, List<Component> list)
    {
        final IExplosiveData data = getExplosiveData(stack);
        if (data != null)
        {
            final EnumTier tierdata = data.getTier();
            if(tierdata != EnumTier.NONE) // only show the tier if its not NONE. Tier NONE is currently only assinged to the Missile Module
            {
                list.add(Component.empty()
                    .append(Component.translatable("info.misc.tier").withStyle(ChatFormatting.DARK_RED))
                    .append(Component.literal(": ").withStyle(ChatFormatting.DARK_RED))
                    .append(Component.literal(tierdata.getLocalizedName()).withStyle(tierdata.getTooltipColor())));
            }
        }
        // PORT: unreachable in 1.12 as well (redmatter is always registered), kept for parity
        else if (ICBMExplosives.REDMATTER != null && ICBMExplosives.REDMATTER.getRegistryKey().equals(stack.get(ComponentReg.EXPLOSIVE.get()))) //TODO add hook for any explosive via content reg
        {
            ///Shhh!!! tell no one this exists, tis a surprise
            boolean taunt = shouldTauntPlayer(player);
            if (taunt)
            {
                switch (tauntCount)
                {
                    case 0:
                        list.add(Component.literal("Place me, you know you want to :)"));
                        //$FALL-THROUGH$
                    case 1:
                        list.add(Component.literal("Mine with me, lets get some minerals!!"));
                        //$FALL-THROUGH$
                    case 2:
                        list.add(Component.literal("Can you hear the noises in the dark?"));
                        //$FALL-THROUGH$
                    case 3:
                        list.add(Component.literal("One does not simply use"));
                        list.add(Component.literal("redmatter to cancel redmatter"));
                        //$FALL-THROUGH$
                    case 4:
                        list.add(Component.literal("Nice base you have"));
                        list.add(Component.literal("be a shame if something"));
                        list.add(Component.literal("would happen to it"));
                        //$FALL-THROUGH$
                    case 5:
                        list.add(Component.literal("Don't worry i've changed"));
                        //$FALL-THROUGH$
                    case 6:
                        list.add(Component.literal("Lets eat a world together"));
                        //$FALL-THROUGH$
                    case 7:
                        list.add(Component.literal("I'm back for you"));
                }
            }
            else
            {
                normalDetailedInfo(list);
            }

            //Cycle next message
            if (player != null && System.currentTimeMillis() - lastTranslationChange > changeOverDelay)
            {
                lastTranslationChange = System.currentTimeMillis();
                if (taunt)
                {
                    tauntCount = player.level().random.nextInt(7);
                }
                else if (redmatterRandomTranslations > 0)
                {
                    tauntCount = player.level().random.nextInt(redmatterRandomTranslations);
                }
            }
        }
        else
        {
            super.getDetailedInfo(stack, player, list);
        }
    }

    protected boolean shouldTauntPlayer(@Nullable Player player)
    {
        if (player == null)
        {
            return false;
        }
        final String name = player.getName().getString().toLowerCase();
        return name.startsWith("sips_") || name.startsWith("sjin");
    }

    protected void normalDetailedInfo(List<Component> list)
    {
        if (redmatterRandomTranslations == -1)
        {
            try
            {
                redmatterRandomTranslations = Integer.parseInt(LanguageUtility.getLocal(count_key));
            }
            catch (NumberFormatException e)
            {
                redmatterRandomTranslations = 0;
            }
        }
        String translationKey = "explosive.icbmclassic:redMatter.info." + tauntCount;
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            LanguageUtility.splitByLine(translation).forEach(line -> list.add(Component.literal(line)));
        }
    }

    @Override
    public void getSubItems(NonNullList<ItemStack> items)
    {
        for (int id : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosivesIDs())
        {
            final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(id);
            if (data != null)
            {
                final ItemStack stack = new ItemStack(this);
                stack.set(ComponentReg.EXPLOSIVE.get(), data.getRegistryKey());
                items.add(stack);
            }
        }
    }

    @Override
    public String getDescriptionId(ItemStack itemstack)
    {
        final IExplosiveData data = getExplosiveData(itemstack);
        if (data != null)
        {
            return "explosive." + data.getRegistryKey();
        }
        return "explosive";
    }

    @Override
    public String getDescriptionId()
    {
        return "explosive";
    }
}
// PORTED
