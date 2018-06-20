package icbm.classic.content.machines.emptower;

import com.builtbroken.mc.framework.computer.DataSystemHandler;
import com.builtbroken.mc.framework.computer.DataSystemLambda;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/20/2018.
 */
public class DSEmpTower
{
    public static void load()
    {
        DataSystemLambda dataSystemLambda = DataSystemHandler.createNewDataSystem("icbm.empTower", new TileEMPTower());
        dataSystemLambda.addMethod("isEmpReady", tile -> ((TileEMPTower)tile).isReady());
        dataSystemLambda.addMethod("getEmpCooldown", tile -> ((TileEMPTower)tile).getCooldown());
        dataSystemLambda.addMethod("getEmpMaxCooldown", tile -> ((TileEMPTower)tile).getMaxCooldown());
        dataSystemLambda.addMethod("getEmpMaxCooldown", tile -> ((TileEMPTower)tile).getMaxCooldown());
        dataSystemLambda.addMethod("fireEmp", tile -> ((TileEMPTower)tile).fire());
    }
}
