package icbm.classic.mods;

import icbm.classic.mods.mekanism.MekProxy;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.ModList;

public class ModInteraction {

    private static LazyOptional<ModProxy> MEK = LazyOptional.of(MekProxy::new);

    public static void preInit() {
        if(ModList.get().isLoaded("mekanism")) {
            MEK.ifPresent(ModProxy::preInit);
        }
    }

    public static void init() {
        if(ModList.get().isLoaded("mekanism")) {
            MEK.ifPresent(ModProxy::init);
        }
    }

    public static void postInit() {
        if(ModList.get().isLoaded("mekanism")) {
            MEK.ifPresent(ModProxy::postInit);
        }
    }
}
