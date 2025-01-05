package icbm.classic.content.missile.entity.anti.item;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.anti.EntitySurfaceToAirMissile;
import icbm.classic.content.reg.EntityReg;
import net.minecraft.world.World;

public class CapabilitySAMStack implements ICapabilityMissileStack
{
    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile.sam";
    }

    @Override
    public IMissile newMissile(World world)
    {
        return EntityReg.MISSILE_SURFACE_TO_AIR.get().create(world).getMissileCapability();
    }
}