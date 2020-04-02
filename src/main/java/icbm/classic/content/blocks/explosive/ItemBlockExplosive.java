package icbm.classic.content.blocks.explosive;

import icbm.classic.api.EnumTier;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.lib.capability.ex.CapabilityExplosiveStack;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;

public class ItemBlockExplosive extends ItemBlockAbstract
{
    private final String count_key = "icbm.explosive.redMatter.info.count";

    private int tauntCount = 0;
    private int redmatterRandomTranslations = -1;

    private Long lastTranslationChange = 0L;

    private static int changeOverDelay = 1000 * 60;

    public ItemBlockExplosive(Block block)
    {
        super(block);
        this.setMaxDamage(0);
        this.setHasSubtypes(true);
    }

    @Override
    @Nullable
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt)
    {
        final CapabilityExplosiveStack capabilityExplosive = new CapabilityExplosiveStack(stack);
        if(nbt != null)
        {
            capabilityExplosive.deserializeNBT(nbt);
        }
        return capabilityExplosive;
    }

    @Override
    public void getDetailedInfo(ItemStack stack, @Nullable EntityPlayer player, List list)
    {
        final IExplosiveData data = ICBMClassicHelpers.getExplosive(stack.getItemDamage(), true);
        if (data != null)
        {
            final EnumTier tierdata = data.getTier();
            list.add(TextFormatting.DARK_RED + LanguageUtility.getLocal("info.misc.tier") + ": " + tierdata.getTooltipColor() + tierdata.getLocalizedName());
        }

        if (stack.getItemDamage() == ICBMExplosives.REDMATTER.getRegistryID()) //TODO add hook for any explosive via content reg
        {
            ///Shhh!!! tell no one this exists, tis a surprise
            boolean taunt = shouldTauntPlayer(player);
            if (taunt)
            {
                switch (tauntCount)
                {
                    case 0:
                        list.add("Place me, you know you want to :)");
                        //$FALL-THROUGH$
                    case 1:
                        list.add("Mine with me, lets get some minerals!!");
                        //$FALL-THROUGH$
                    case 2:
                        list.add("Can you hear the noises in the dark?");
                        //$FALL-THROUGH$
                    case 3:
                        list.add("One does not simply use");
                        list.add("redmatter to cancel redmatter");
                        //$FALL-THROUGH$
                    case 4:
                        list.add("Nice base you have");
                        list.add("be a shame if something");
                        list.add("would happen to it");
                        //$FALL-THROUGH$
                    case 5:
                        list.add("Don't worry i've changed");
                        //$FALL-THROUGH$
                    case 6:
                        list.add("Lets eat a world together");
                        //$FALL-THROUGH$
                    case 7:
                        list.add("I'm back for you");
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
                    tauntCount = player.world.rand.nextInt(7);
                }
                else if (redmatterRandomTranslations > 0)
                {
                    tauntCount = player.world.rand.nextInt(redmatterRandomTranslations);
                }
            }
        }
        else
        {
            super.getDetailedInfo(stack, player, list);
        }
    }

    protected boolean shouldTauntPlayer(@Nullable EntityPlayer player)
    {
        return player != null && player.getName() != null
                && (player.getName().toLowerCase().startsWith("sips_") || player.getName().toLowerCase().startsWith("sjin"));
    }

    protected void normalDetailedInfo(List list)
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
        String translationKey = "icbm.explosive.redMatter.info." + tauntCount;
        String translation = LanguageUtility.getLocal(translationKey);
        if (!translation.isEmpty() && !translation.equals(translationKey))
        {
            list.addAll(LanguageUtility.splitByLine(translation));
        }
    }

    @Override
    protected boolean hasDetailedInfo(ItemStack stack, EntityPlayer player)
    {
        return true;
    }

    @Override
    public int getMetadata(int damage)
    {
        return damage;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items)
    {
        if (tab == getCreativeTab() || tab == CreativeTabs.SEARCH)
        {
            for (int id : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosivesIDs())
            {
                items.add(new ItemStack(this, 1, id));
            }
        }
    }

    @Override
    public String getTranslationKey(ItemStack itemstack)
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(itemstack.getItemDamage());
        if (data != null)
        {
            return "explosive." + data.getRegistryName();
        }
        return "explosive";
    }

    @Override
    public String getTranslationKey()
    {
        return "explosive";
    }
}
