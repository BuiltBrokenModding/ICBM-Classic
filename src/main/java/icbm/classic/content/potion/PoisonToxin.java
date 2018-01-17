package icbm.classic.content.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public class PoisonToxin extends CustomPotion
{
    public static Potion INSTANCE;

    public PoisonToxin(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect, color, id, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase par1EntityLiving, int amplifier)
    {
        if (!(par1EntityLiving instanceof EntityZombie) && !(par1EntityLiving instanceof EntityPigZombie))
        {
            par1EntityLiving.attackEntityFrom(DamageSource.MAGIC, 1);
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        if (duration % (20 * 2) == 0)
        {
            return true;
        }

        return false;
    }
}
