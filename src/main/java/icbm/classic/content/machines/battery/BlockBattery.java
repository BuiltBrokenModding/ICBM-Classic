package icbm.classic.content.machines.battery;

import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;

import javax.annotation.Nullable;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 3/21/2018.
 */
public class BlockBattery extends BlockICBM
{
    public BlockBattery()
    {
        super("batterybox", Material.WOOD);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
        ItemStack heldStack = playerIn.getHeldItem(hand);
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if (tileEntity instanceof TileEntityBattery)
        {
            if (!heldStack.isEmpty())
            {
                if (heldStack.hasCapability(CapabilityEnergy.ENERGY, null))
                {
                    if (!worldIn.isRemote)
                    {
                        ((TileEntityBattery) tileEntity).getInventory().transferIntoInventory(playerIn, hand, heldStack);
                    }
                }
                else if (heldStack.getItem() == Items.STICK)
                {
                    if (!worldIn.isRemote)
                    {
                        int slotInUse = ((TileEntityBattery) tileEntity).getInventory().getSlots() - ((TileEntityBattery) tileEntity).getInventory().getEmptySlots().size();
                        int energy = ((TileEntityBattery) tileEntity).getEnergyStorage().getEnergyStored();
                        playerIn.sendMessage(new TextComponentString("Batteries: " + slotInUse + " Energy: " + energy));
                    }
                }
            }
            else
            {
                ((TileEntityBattery) tileEntity).openGui(playerIn, 0);
            }
        }

        return true;
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileEntityBattery();
    }
}
