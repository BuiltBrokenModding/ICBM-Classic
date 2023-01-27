package icbm.classic.content.missile.source;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Missiles fire from a block/tile
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MissileSourceBlock extends MissileSource
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block");

    private BlockPos blockPos;
    private IBlockState state;

    public MissileSourceBlock(World world, BlockPos blockPos, IBlockState state, EntitySourceData sourceData) {
        super(world, sourceData);
        this.blockPos = blockPos;
        this.state = state;
    }

    @Override
    public MissileSourceType getType()
    {
        return MissileSourceType.BLOCK;
    }

    @Override
    public Vec3d getFiredPosition()
    {
        //TODO implement lazy
        return new Vec3d(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
    }

    @Override
    public NBTTagCompound save() {
        return SAVE_LOGIC.save(this, super.save());
    }

    @Override
    public void load(NBTTagCompound save) {
        super.load(save);
        SAVE_LOGIC.load(this, save);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    private static final NbtSaveHandler<MissileSourceBlock> SAVE_LOGIC = new NbtSaveHandler<MissileSourceBlock>()
        .mainRoot()
        /* */.nodeBlockPos("block_pos", MissileSourceBlock::getBlockPos, MissileSourceBlock::setBlockPos)
        .base();


}
