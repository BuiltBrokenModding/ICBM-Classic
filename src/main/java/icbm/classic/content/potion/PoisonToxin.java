package icbm.classic.content.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityPigZombie;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.potion.Potion;
import net.minecraft.util.DamageSource;

public class PoisonToxin extends CustomPotion
{
    public static Potion INSTANCE;
    public static int tickRate = 20 * 2; //TODO config
    public static int damage = 1;

    public PoisonToxin(boolean isBadEffect, int color, int id, String name)
    {
        super(isBadEffect, color, id, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier)
    {
        if (!(entity instanceof EntityZombie) && !(entity instanceof EntityPigZombie))
        {
            entity.attackEntityFrom(DamageSource.MAGIC, damage); //TODO change to use a custom damage source
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        if (duration % tickRate == 0)
        {
            return true;
        }

        return false;
    }
}
