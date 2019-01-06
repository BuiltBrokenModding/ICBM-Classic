package icbm.classic.content.explosive.tile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemBlockAbstract;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
    public void getDetailedInfo(ItemStack stack, @Nullable EntityPlayer player, List list)
    {
        if (stack.getItemDamage() == Explosives.REDMATTER.ordinal())
        {
            ///Shhh!!! tell no one this exists, tis a surprise
            boolean taunt = shouldTauntPlayer(player);
            if (taunt)
            {
                switch (tauntCount)
                {
                    case 0:
                        list.add("Place me, you know you want to :)");
                    case 1:
                        list.add("Mine with me, lets get some minerals!!");
                    case 2:
                        list.add("Can you hear the noises in the dark?");
                    case 3:
                        list.add("One does not simply use");
                        list.add("redmatter to cancel redmatter");
                    case 4:
                        list.add("Nice base you have");
                        list.add("be a shame if something");
                        list.add("would happen to it");
                    case 5:
                        list.add("Don't worry i've changed");
                    case 6:
                        list.add("Lets eat a world together");
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
            if (translation.contains(","))
            {
                String[] split = translation.split(",");
                for (String s : split)
                {
                    list.add(s.trim());
                }
            }
            else
            {
                list.add(translation);
            }
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
    public String getTranslationKey(ItemStack itemstack)
    {
        final IExplosiveData data = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(itemstack.getItemDamage());
        if (data != null)
        {
            return super.getTranslationKey() + data.getRegistryName();
        }
        return super.getTranslationKey(itemstack);
    }

    @Override
    public String getTranslationKey()
    {
        return "icbm.explosive";
    }
}
