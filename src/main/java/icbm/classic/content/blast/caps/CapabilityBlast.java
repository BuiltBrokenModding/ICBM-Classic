package icbm.classic.content.blast.caps;

import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.blast.BlastStatus;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 5/22/2021.
 * @deprecated
 */
public class CapabilityBlast implements IBlast
{
    @Nonnull
    @Override
    public IActionStatus doAction()
    {
        return BlastStatus.SETUP_ERROR;
    }

    @Nonnull
    @Override
    public IActionSource getSource() {
        return null;
    }

    @Nonnull
    @Override
    public IExplosiveData getActionData() {
        return null;
    }

    @Override
    public World getWorld() {
        return null;
    }

    @Override
    public Vec3d getPosition() {
        return null;
    }

    @Override
    public void clearBlast()
    {

    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IBlast.class, new Capability.IStorage<IBlast>()
                {
                    @Nullable
                    @Override
                    public INBT writeNBT(Capability<IBlast> capability, IBlast instance, Direction side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IBlast> capability, IBlast instance, Direction side, INBT nbt)
                    {

                    }
                },
                CapabilityBlast::new);
    }
}
