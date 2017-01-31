package icbm.classic.content.items;

import com.builtbroken.mc.lib.helper.LanguageUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.DamageSource;

import java.util.Random;

//Explosive Defuser
public class ItemDefuser extends ItemICBMElectrical
{
    private static final int ENERGY_COST = 2000;

    public ItemDefuser()
    {
        super("defuser");
    }

    /**
     * Called when the player Left Clicks (attacks) an entity. Processed before damage is done, if
     * return value is true further processing is canceled and the entity is not attacked.
     *
     * @param itemStack The Item being used
     * @param player    The player that is attacking
     * @param entity    The entity being attacked
     * @return True to cancel the rest of the interaction.
     */
    @Override
    public boolean onLeftClickEntity(ItemStack itemStack, EntityPlayer player, Entity entity)
    {
        if (this.getEnergy(itemStack) >= ENERGY_COST)
        {
            if (entity instanceof EntityExplosive)
            {
                if (!entity.worldObj.isRemote)
                {
                    EntityExplosive entityTNT = (EntityExplosive) entity;
                    EntityItem entityItem = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, new ItemStack(ICBMClassic.blockExplosive, 1, entityTNT.explosiveID.ordinal()));
                    float var13 = 0.05F;
                    Random random = new Random();
                    entityItem.motionX = ((float) random.nextGaussian() * var13);
                    entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
                    entityItem.motionZ = ((float) random.nextGaussian() * var13);
                    entity.worldObj.spawnEntityInWorld(entityItem);
                }
                entity.setDead();
            }
            else if (entity instanceof EntityTNTPrimed)
            {
                if (!entity.worldObj.isRemote)
                {
                    EntityItem entityItem = new EntityItem(entity.worldObj, entity.posX, entity.posY, entity.posZ, new ItemStack(Blocks.tnt));
                    float var13 = 0.05F;
                    Random random = new Random();
                    entityItem.motionX = ((float) random.nextGaussian() * var13);
                    entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
                    entityItem.motionZ = ((float) random.nextGaussian() * var13);
                    entity.worldObj.spawnEntityInWorld(entityItem);
                }
                entity.setDead();
            }
            else if (entity instanceof EntityBombCart)
            {
                ((EntityBombCart) entity).killMinecart(DamageSource.generic);
            }

            this.discharge(itemStack, ENERGY_COST, true);
            return true;
        }
        else
        {
            player.addChatMessage(new ChatComponentText(LanguageUtility.getLocal("message.defuser.nopower")));
        }

        return false;
    }
}
