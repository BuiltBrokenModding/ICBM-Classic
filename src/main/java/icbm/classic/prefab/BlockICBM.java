package icbm.classic.prefab;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

public class BlockICBM extends Block
{
    public BlockICBM(String name, Material mat)
    {
        super(mat);
    }

    public BlockICBM(String name)
    {
        this(name, Material.IRON);
    }
}
