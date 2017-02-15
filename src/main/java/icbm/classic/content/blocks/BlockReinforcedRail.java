package icbm.classic.content.blocks;

import icbm.classic.ICBMClassic;
import net.minecraft.block.BlockRail;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

/** @author DarkGuardsman */
public class BlockReinforcedRail extends BlockRail
{
    public BlockReinforcedRail()
    {
        setBlockName(ICBMClassic.PREFIX + "reinforcedRail");
        setBlockTextureName(ICBMClassic.PREFIX + "reinforcedRail");
        setHardness(10F);
        setResistance(10F);
        setStepSound(soundTypeMetal);
    }

    @Override
    public float getRailMaxSpeed(World world, EntityMinecart cart, int y, int x, int z)
    {
        return 0.6f;
    }
}
