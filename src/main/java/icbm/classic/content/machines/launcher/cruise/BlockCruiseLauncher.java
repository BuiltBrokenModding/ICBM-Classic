package icbm.classic.content.machines.launcher.cruise;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/15/2018.
 */
public class BlockCruiseLauncher extends BlockICBM
{
    public BlockCruiseLauncher()
    {
        super("cruiseLauncher");
        this.blockHardness = 10f;
        this.blockResistance = 10f;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return null;
    }
}
