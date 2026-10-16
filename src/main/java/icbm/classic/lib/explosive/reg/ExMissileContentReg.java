package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.data.EntityInteractionFunction;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.content.IExMissileRegistry;
import icbm.classic.content.reg.ItemReg;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import icbm.classic.lib.explosive.ExplosiveStacks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.NeoForge;

import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public class ExMissileContentReg extends ExplosiveContentRegistry implements IExMissileRegistry
{

    public ExMissileContentReg()
    {
        super(ICBMClassicAPI.EX_MISSILE);
    }

    @Override
    public void setLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback)
    {

    }

    @Override
    public void setMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback)
    {

    }

    @Override
    public void setInteractionListener(ResourceLocation exName, EntityInteractionFunction function)
    {

    }

    @Override
    public void triggerLaunch(IMissile missile)
    {
        NeoForge.EVENT_BUS.post(new MissileEvent.PostLaunch(missile, missile.getMissileEntity()));
    }

    @Override
    public void triggerFlightUpdate(IMissile missile)
    {

    }

    @Override
    public boolean onInteraction(Entity entity, Player player, InteractionHand hand)
    {
        return false;
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName)
    {
        return ExplosiveStacks.create(ItemReg.itemExplosiveMissile.get(), ICBMClassicHelpers.getExplosive(regName, false));
    }
}
// PORTED
