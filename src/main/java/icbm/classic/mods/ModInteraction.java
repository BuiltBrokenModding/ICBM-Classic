package icbm.classic.mods;

import icbm.classic.mods.cc.CCProxy;
import icbm.classic.mods.ic2.IC2Proxy;
import icbm.classic.mods.mekanism.MekProxy;

public class ModInteraction {

    public static void preInit() {
        IC2Proxy.INSTANCE.preInit();
        MekProxy.INSTANCE.preInit();
        CCProxy.INSTANCE.preInit();
    }

    public static void init() {
        IC2Proxy.INSTANCE.init();
        MekProxy.INSTANCE.init();
        CCProxy.INSTANCE.init();
    }

    public static void postInit() {
        IC2Proxy.INSTANCE.postInit();
        MekProxy.INSTANCE.postInit();
        CCProxy.INSTANCE.postInit();
    }
}
