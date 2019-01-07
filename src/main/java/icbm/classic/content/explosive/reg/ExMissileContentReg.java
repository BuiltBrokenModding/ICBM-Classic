package icbm.classic.content.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.EntityInteractionFunction;
import icbm.classic.api.explosion.IMissile;
import icbm.classic.api.reg.content.IExMissileRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;

import java.util.function.Consumer;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
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

    }

    @Override
    public void triggerFlightUpdate(IMissile missile)
    {

    }

    @Override
    public boolean onInteraction(Entity entity, EntityPlayer player, EnumHand hand)
    {
        return false;
    }
}
