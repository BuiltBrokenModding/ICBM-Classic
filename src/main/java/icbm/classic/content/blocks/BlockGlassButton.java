package icbm.classic.content.blocks;

import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockButton;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockGlassButton extends BlockButton
{
    public BlockGlassButton()
    {
        super(false);
        this.setTickRandomly(true);
        this.setUnlocalizedName(ICBMClassic.PREFIX + "glassButton");
        this.setSoundType(SoundType.GLASS);
        this.setHardness(0.5F);
    }

    @Override
    public int quantityDropped(Random par1Random)
    {
        return 0;
    }

    @Override
    protected void playClickSound(@Nullable EntityPlayer player, World worldIn, BlockPos pos)
    {
        worldIn.playSound(player, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_ON, SoundCategory.BLOCKS, 0.3F, 0.6F);
    }

    @Override
    protected void playReleaseSound(World worldIn, BlockPos pos)
    {
        worldIn.playSound((EntityPlayer)null, pos, SoundEvents.BLOCK_STONE_BUTTON_CLICK_OFF, SoundCategory.BLOCKS, 0.3F, 0.5F);
    }
}
