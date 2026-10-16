package icbm.classic;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.lib.actions.ActionSystem;
import icbm.classic.lib.capability.ex.CapabilityExplosive;

public class ICBMClassicMock extends ICBMClassic {

    public static void init() {
        // Register capabilities
        ICBMClassicAPI.EXPLOSIVE_CAPABILITY = TestBase.getCapOrCreate(IExplosive.class, CapabilityExplosive::register);

        // Setup ICBM instance and register content
        ICBMClassic.INSTANCE = new ICBMClassicMock();
        ICBMClassic.INSTANCE.handleExRegistry(null);
        ICBMClassic.INSTANCE.handleMissileFlightRegistry();
        ActionSystem.setup();
        ICBMClassic.INSTANCE.handleMissileTargetRegistry();
    }
}
