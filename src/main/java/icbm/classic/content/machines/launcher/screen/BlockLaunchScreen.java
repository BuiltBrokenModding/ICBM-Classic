package icbm.classic.content.machines.launcher.screen;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/16/2018.
 */
public class BlockLaunchScreen extends BlockICBM
{
    public BlockLaunchScreen()
    {
        super("launcherScreen");
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileLauncherScreen();
    }


    @Override
    public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items)
    {
        items.add(new ItemStack(this, 1, 0));
        items.add(new ItemStack(this, 1, 1));
        items.add(new ItemStack(this, 1, 2));
    }
}
