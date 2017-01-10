package icbm.classic.content.blocks;

import icbm.classic.prefab.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

//@BlockInfo(tileEntity = "icbm.core.blocks.TileCamouflage")
public class BlockCamouflage extends BlockICBM
{
    public BlockCamouflage()
    {
        super("camouflage", Material.cloth);
        this.setHardness(0.3F);
        this.setResistance(1F);
        this.setStepSound(soundTypeCloth);
    }


}
