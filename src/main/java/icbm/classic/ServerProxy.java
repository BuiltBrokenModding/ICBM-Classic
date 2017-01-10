package icbm.classic;

import icbm.classic.content.radarstation.TileRadarStation;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/9/2017.
 */
public class ServerProxy extends CommonProxy
{
    @Override
    public void preInit()
    {
        super.preInit();
        ICBMClassic.blockRadarStation = ICBMClassic.INSTANCE.getManager().newBlock("icbmCRadarStation", new TileRadarStation());
    }
}
