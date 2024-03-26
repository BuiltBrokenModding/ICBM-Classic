package icbm.classic.lib.explosive.reg;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.EntityInteractionFunction;
import icbm.classic.api.events.MissileEvent;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.api.reg.content.IExMissileRegistry;
import icbm.classic.world.IcbmItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.common.MinecraftForge;

import java.util.function.Consumer;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class ExMissileContentReg extends ExplosiveContentRegistry implements IExMissileRegistry {

    public ExMissileContentReg() {
        super(ICBMClassicAPI.EX_MISSILE);
    }

    @Override
    public void setLaunchListener(ResourceLocation exName, Consumer<IMissile> eventCallback) {

    }

    @Override
    public void setMissileUpdateListener(ResourceLocation exName, Consumer<IMissile> eventCallback) {

    }

    @Override
    public void setInteractionListener(ResourceLocation exName, EntityInteractionFunction function) {

    }

    @Override
    public void triggerLaunch(IMissile missile) {
        MinecraftForge.EVENT_BUS.post(new MissileEvent.PostLaunch(missile, missile.getMissileEntity()));
    }

    @Override
    public void triggerFlightUpdate(IMissile missile) {

    }

    @Override
    public boolean onInteraction(Entity entity, Player player, InteractionHand hand) {
        return false;
    }

    @Override
    public ItemStack getDeviceStack(ResourceLocation regName) {
        ExplosiveType ex = getExplosive(regName);
        if (ex != null) {
            return new ItemStack(IcbmItems.itemExplosiveMissile, 1, ex.getRegistryID());
        }
        return null;
    }
}
