package icbm.classic.content.missile.source;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MissileSourceEntity extends MissileSource
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "entity");

    private Vec3d position;

    public MissileSourceEntity(World world, Vec3d position, EntitySourceData sourceData) {
        super(world, sourceData);
        this.position = position;
    }

    @Override
    public MissileSourceType getType()
    {
        return MissileSourceType.ENTITY;
    }

    @Override
    public Vec3d getFiredPosition()
    {
        return position;
    }

    @Override
    public BlockPos getBlockPos()
    {
        return new BlockPos(position);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
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

    private static final NbtSaveHandler<MissileSourceEntity> SAVE_LOGIC = new NbtSaveHandler<MissileSourceEntity>()
        .mainRoot()
        /* */.nodeVec3d("pos", MissileSourceEntity::getFiredPosition, MissileSourceEntity::setPosition)
        .base();
}
