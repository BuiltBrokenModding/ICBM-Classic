package icbm.classic.content.items;

import icbm.classic.lib.LanguageUtility;
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
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TextComponentString;

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
                if (!entity.world.isRemote)
                {
                    EntityExplosive entityTNT = (EntityExplosive) entity;

                    //TODO add drop handler to explosive
                    EntityItem entityItem = new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(ICBMClassic.blockExplosive, 1, entityTNT.explosiveID));
                    float var13 = 0.05F;
                    Random random = new Random();
                    entityItem.motionX = ((float) random.nextGaussian() * var13);
                    entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
                    entityItem.motionZ = ((float) random.nextGaussian() * var13);
                    entity.world.spawnEntity(entityItem);
                }
                entity.setDead();
            }
            else if (entity instanceof EntityTNTPrimed)
            {
                if (!entity.world.isRemote)
                {
                    EntityItem entityItem = new EntityItem(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(Blocks.TNT));
                    float var13 = 0.05F;
                    Random random = new Random();
                    entityItem.motionX = ((float) random.nextGaussian() * var13);
                    entityItem.motionY = ((float) random.nextGaussian() * var13 + 0.2F);
                    entityItem.motionZ = ((float) random.nextGaussian() * var13);
                    entity.world.spawnEntity(entityItem);
                }
                entity.setDead();
            }
            else if (entity instanceof EntityBombCart)
            {
                ((EntityBombCart) entity).killMinecart(DamageSource.GENERIC);
            }

            this.discharge(itemStack, ENERGY_COST, true);
            return true;
        }
        else
        {
            player.sendMessage(new TextComponentString(LanguageUtility.getLocal("message.defuser.nopower")));
        }

        return false;
    }
}
