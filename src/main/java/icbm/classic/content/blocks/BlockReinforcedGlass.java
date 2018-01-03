package icbm.classic.content.blocks;

import icbm.classic.ICBMClassic;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockReinforcedGlass extends Block
{
    public BlockReinforcedGlass()
    {
        super(Material.GLASS);
        this.setUnlocalizedName(ICBMClassic.PREFIX + "reinforcedGlass");
        this.setHardness(10);
        this.setResistance(48);
    }

    @Override
    protected boolean canSilkHarvest()
    {
        return true;
    }
}
