package icbm.classic.content.machines.coordinator;

import com.builtbroken.mc.prefab.inventory.ExternalInventory;
import com.builtbroken.mc.prefab.inventory.InventoryIterator;
import com.builtbroken.mc.prefab.inventory.InventoryUtility;
import icbm.classic.ICBMClassic;
import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/7/2018.
 */
public class BlockMissileCoordinator extends BlockICBM implements ITileEntityProvider
{
    public BlockMissileCoordinator()
    {
        super("icbmCMissileCoordinator");
        this.blockHardness = 10f;
        this.blockHardness = 10f;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        if (!worldIn.isRemote)
        {
            //this.worldObj.playSoundEffect(this.xCoord, this.yCoord, this.zCoord, ICBMClassic.PREFIX + "interface", 1, (float) (this.worldObj.rand.nextFloat() * 0.2 + 0.9F));
            playerIn.openGui(ICBMClassic.INSTANCE, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
    {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileMissileCoordinator)
        {
            ExternalInventory inventory = ((TileMissileCoordinator) tile).inventory;
            if (inventory != null)
            {
                for (ItemStack stack : new InventoryIterator(inventory, true))
                {
                    InventoryUtility.dropItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), stack, 1, 0);
                }
            }
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileMissileCoordinator();
    }
}
