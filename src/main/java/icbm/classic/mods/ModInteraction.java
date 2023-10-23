package icbm.classic.mods;

import icbm.classic.mods.mekanism.MekProxy;

public class ModInteraction {

    public static void preInit() {
        MekProxy.INSTANCE.preInit();
    }

    public static void init() {
        MekProxy.INSTANCE.init();
    }

    public static void postInit() {
        MekProxy.INSTANCE.postInit();
    }
}
