package icbm.classic.content.machines.radarstation;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class BlockRadarStation extends BlockICBM
{
    public static final PropertyBool REDSTONE_PROPERTY = PropertyBool.create("redstone");

    public BlockRadarStation()
    {
        super("radarStation");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileRadarStation();
    }
}
