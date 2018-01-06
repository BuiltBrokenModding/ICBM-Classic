package icbm.classic.content.potion;

import icbm.classic.ICBMClassic;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public abstract class CustomPotion extends Potion
{
    /**
     * Creates a new type of potion
     *
     * @param isBadEffect - Is this potion a good potion or a bad one?
     * @param color       - The color of this potion.
     * @param name        - The name of this potion.
     */
    public CustomPotion(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect, color);
        this.setPotionName("potion." + name);
        REGISTRY.register(id, new ResourceLocation(ICBMClassic.PREFIX + name), this);
    }

    @Override
    public Potion setIconIndex(int par1, int par2)
    {
        super.setIconIndex(par1, par2);
        return this;
    }

    @Override
    protected Potion setEffectiveness(double par1)
    {
        super.setEffectiveness(par1);
        return this;
    }
}
