package icbm.classic.content.items;

import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.events.ExplosiveDefuseEvent;
import icbm.classic.content.entity.EntityBombCart;
import icbm.classic.prefab.item.ItemICBMElectrical;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForge;

//Explosive Defuser
public class ItemDefuser extends ItemICBMElectrical
{
    private static final int ENERGY_COST = 2000;

    public ItemDefuser()
    {
        super("defuser", new Item.Properties());
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
    public boolean onLeftClickEntity(ItemStack itemStack, Player player, Entity entity)
    {
        if (this.getEnergy(itemStack) >= ENERGY_COST)
        {
            if (ICBMClassicHelpers.isExplosive(entity))
            {
                if (!entity.level().isClientSide())
                {
                    final IExplosive explosive = ICBMClassicHelpers.getExplosive(entity);
                    if (explosive != null)
                    {
                        if (NeoForge.EVENT_BUS.post(new ExplosiveDefuseEvent.ICBMExplosive(player, entity, explosive)).isCanceled())
                        {
                            return false;
                        }

                        explosive.onDefuse();
                    }
                    entity.discard();
                }
            }
            else if (entity instanceof PrimedTnt)
            {
                if (NeoForge.EVENT_BUS.post(new ExplosiveDefuseEvent.TNTExplosive(player, entity)).isCanceled())
                {
                    return false;
                }

                if (!entity.level().isClientSide())
                {
                    entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(Blocks.TNT)));
                }
                entity.discard();
            }
            else if (entity instanceof EntityBombCart)
            {
                if (NeoForge.EVENT_BUS.post(new ExplosiveDefuseEvent.ICBMBombCart(player, entity)).isCanceled())
                {
                    return false;
                }

                ((EntityBombCart) entity).killMinecart(entity.damageSources().generic());
            }

            this.discharge(itemStack, ENERGY_COST, true);
            return true;
        }
        else
        {
            player.sendSystemMessage(Component.translatable("message.defuser.nopower"));
        }

        return false;
    }
}
// PORTED
