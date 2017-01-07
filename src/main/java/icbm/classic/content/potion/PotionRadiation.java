package icbm.classic.content.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import resonant.lib.prefab.potion.CustomPotion;

public class PotionRadiation extends CustomPotion
{
    public static final PotionRadiation INSTANCE = new PotionRadiation(21, true, 5149489, "radiation");

    public PotionRadiation(int id, boolean isBadEffect, int color, String name)
    {
        super(id, isBadEffect, color, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase par1EntityLiving, int amplifier)
    {
        if (par1EntityLiving.worldObj.rand.nextFloat() > 0.9 - (amplifier * 0.07))
        {
            par1EntityLiving.attackEntityFrom(PoisonRadiation.damageSource, 1);

            if (par1EntityLiving instanceof EntityPlayer)
            {
                ((EntityPlayer) par1EntityLiving).addExhaustion(0.010F * (amplifier + 1));
            }
        }

    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        if (duration % 10 == 0)
        {
            return true;
        }

        return false;
    }
}
