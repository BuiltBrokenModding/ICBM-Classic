package icbm.classic.world.missile.entity.anti.item;

import icbm.classic.IcbmConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.world.missile.entity.anti.SurfaceToAirMissileEntity;
import net.minecraft.world.level.Level;

public class CapabilitySAMStack implements ICapabilityMissileStack {
    @Override
    public String getMissileId() {
        return IcbmConstants.PREFIX + "missile.sam";
    }

    @Override
    public IMissile newMissile(Level level) {
        return new SurfaceToAirMissileEntity(world).getMissileCapability();
    }
}