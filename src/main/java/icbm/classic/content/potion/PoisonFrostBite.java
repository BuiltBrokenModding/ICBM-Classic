package icbm.classic.content.potion;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;

public class PoisonFrostBite extends CustomPotion
{
    public static PoisonFrostBite INSTANCE;

    public PoisonFrostBite(boolean isBadEffect, int color, String name)
    {
        super(isBadEffect, color, name);
        this.setIconIndex(6, 0);
    }

    @Override
    public void performEffect(EntityLivingBase entity, int amplifier)
    {
        if (entity.isBurning())
        {
            entity.extinguish();
        }

        if(!(entity instanceof EntityPlayer) || !((EntityPlayer) entity).capabilities.isCreativeMode)
        {
            if (entity instanceof EntityPlayer)
            {
                ((EntityPlayer) entity).addExhaustion(3F * (amplifier + 1));
            }

            // Check to see if it's on ice
            if (entity.worldObj.getBlock(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY) - 1, MathHelper.floor_double(entity.posZ)) == Blocks.ice)
            {
                entity.attackEntityFrom(DamageSource.magic, 2);
            }
        }
    }

    @Override
    public boolean isReady(int duration, int amplifier)
    {
        if (duration % 20 == 0)
        {
            return true;
        }
        return false;
    }
}
