package icbm.classic.mods;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.fml.ModList;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 5/22/2018.
 */
public class ModProxy
{
    public void preInit()
    {

    }

    public void init()
    {

    }

    public void postInit()
    {

    }

    public void onTileValidate(BlockEntity tile)
    {

    }

    public void onTileInvalidate(BlockEntity tile)
    {

    }

    /** PORT: replaces 1.12 {@code @Optional.Method(modid)} */
    protected static boolean isModLoaded(String modId)
    {
        return ModList.get() != null && ModList.get().isLoaded(modId);
    }
}
// PORTED
