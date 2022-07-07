package icbm.classic.content.blocks.launcher;

import icbm.classic.ICBMClassic;
import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.IMissileSource;
import icbm.classic.content.entity.missile.explosive.CapabilityMissile;
import icbm.classic.lib.saving.NbtSaveHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.EntitySpawnHandler;

import java.lang.ref.WeakReference;
import java.util.UUID;

public class LauncherMissileSource implements IMissileSource
{
    public static final ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "block.launcher");
    private World world;
    private BlockPos position;

    public LauncherMissileSource(World world, BlockPos pos) {
        this.world = world;
        this.position = pos;
    }

    public LauncherMissileSource()
    {
        //Used for save/load only
    }

    @Override
    public World getWorld()
    {
        return world;
    }

    @Override
    public Entity getFiringEntity()
    {
        return null;
    }

    @Override
    public MissileSourceType getType()
    {
        return MissileSourceType.BLOCK;
    }

    @Override
    public Vec3d getFiredPosition()
    {
        return new Vec3d(position.getX() + 0.5, position.getY() + 0.5, position.getZ() + 0.5);
    }

    @Override
    public BlockPos getBlockPos()
    {
        return position;
    }

    @Override
    public NBTTagCompound save() {
        return SAVE_LOGIC.save(this, new NBTTagCompound());
    }

    @Override
    public void load(NBTTagCompound save) {
        SAVE_LOGIC.load(this, save);
    }

    @Override
    public ResourceLocation getRegistryName()
    {
        return REG_NAME;
    }

    private static final NbtSaveHandler<LauncherMissileSource> SAVE_LOGIC = new NbtSaveHandler<LauncherMissileSource>()
        .mainRoot()
        /* */.nodeBlockPos("block_pos", LauncherMissileSource::getBlockPos, (source, pos) -> source.position = pos)
        /* */.nodeWorldDim("dimension", (source) -> source.world, (source, world) -> source.world = world)
        .base();


}
