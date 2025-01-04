package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.events.ExplosiveDefuseEvent;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.lib.LanguageUtility;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.MinecraftForge;

//Explosive Defuser
public class ItemDefuser extends ItemICBMElectrical
{
    private static final int ENERGY_COST = 2000;

    public ItemDefuser(Properties properties) {
        super(properties);
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
    public boolean onLeftClickEntity(ItemStack itemStack, PlayerEntity player, Entity entity)
    {
        if (this.getEnergy(itemStack) >= ENERGY_COST)
        {
            if (entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null).isPresent())
            {
                if (!entity.world.isRemote)
                {
                    final IExplosive explosive = entity.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null).orElseThrow(IllegalStateException::new);
                    if (MinecraftForge.EVENT_BUS.post(new ExplosiveDefuseEvent.ICBMExplosive(player, entity, explosive)))
                    {
                        return false;
                    }

                    explosive.onDefuse();
                    entity.remove();
                }
            }
            else if (entity instanceof TNTEntity)
            {
                if (MinecraftForge.EVENT_BUS.post(new ExplosiveDefuseEvent.TNTExplosive(player, entity)))
                {
                    return false;
                }

                if (!entity.world.isRemote)
                {
                    entity.world.addEntity(new ItemEntity(entity.world, entity.posX, entity.posY, entity.posZ, new ItemStack(Blocks.TNT)));
                }
                entity.remove();
            }
            else if (entity instanceof EntityBombCart)
            {
                if (MinecraftForge.EVENT_BUS.post(new ExplosiveDefuseEvent.ICBMBombCart(player, entity)))
                {
                    return false;
                }

                ((EntityBombCart) entity).killMinecart(DamageSource.GENERIC);
            }

            this.discharge(itemStack, ENERGY_COST, true);
            return true;
        }
        else
        {
            player.sendMessage(new StringTextComponent(LanguageUtility.getLocal("message.defuser.nopower")));
        }

        return false;
    }
}
