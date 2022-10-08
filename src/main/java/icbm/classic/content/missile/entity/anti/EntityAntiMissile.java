package icbm.classic.content.missile.entity.anti;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.lib.capability.emp.CapabilityEMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntityAntiMissile extends EntityMissile<EntityAntiMissile>
{
   protected final RadarScanLogic scanLogic = new RadarScanLogic(this);

    public EntityAntiMissile(World world)
    {
        super(world);
    }

    @Override
    public void onUpdate()
    {
        scanLogic.tick();
        super.onUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        //TODO add AB capability so radars can redirect targets
        return super.hasCapability(capability, facing);
    }
}
