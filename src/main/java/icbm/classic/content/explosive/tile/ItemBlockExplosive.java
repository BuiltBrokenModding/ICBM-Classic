package icbm.classic.content.explosive.tile;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.helper.MathUtility;
import com.builtbroken.mc.prefab.items.ItemBlockAbstract;
import icbm.classic.content.explosive.Explosives;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

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
    public void getDetailedInfo(ItemStack stack, EntityPlayer player, List list)
    {
        ///Shhh!!! tell no one this exists, tss a surprise
        if (stack.getItemDamage() == Explosives.REDMATTER.ordinal())
        {
            boolean taunt = player.getDisplayName().toLowerCase().startsWith("sips_") || player.getDisplayName().toLowerCase().startsWith("sjin");
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
                if(redmatterRandomTranslations == -1)
                {
                    try
                    {
                        redmatterRandomTranslations = Integer.parseInt(LanguageUtility.getLocal("icbm.explosive.redMatter.info.count"));
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

            if(System.currentTimeMillis() - lastTranslationChange > changeOverDelay)
            {
                lastTranslationChange = System.currentTimeMillis();
                if(taunt)
                {
                    tauntCount = MathUtility.rand.nextInt(7);
                }
                else if (redmatterRandomTranslations > 0)
                {
                    tauntCount = MathUtility.rand.nextInt(redmatterRandomTranslations);
                }
            }
        }
        else
        {
            super.getDetailedInfo(stack, player, list);
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
    public String getUnlocalizedName(ItemStack itemstack)
    {
        return this.getUnlocalizedName() + "." + Explosives.get(itemstack.getItemDamage()).handler.getUnlocalizedName();
    }

    @Override
    public String getUnlocalizedName()
    {
        return "icbm.explosive";
    }
}
