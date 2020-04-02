package icbm.classic.lib.capability.launcher;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.NBTConstants;
import icbm.classic.api.caps.IMissileHolder;
import icbm.classic.api.caps.IMissileLauncher;
import icbm.classic.api.missiles.LaunchStatus;
import icbm.classic.lib.CapabilityPrefab;
import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityMissileLauncher extends CapabilityPrefab implements IMissileLauncher
{
    private double target_x;
    private double target_y;
    private double target_z;
    private BlockPos targetPos;

    public IMissileHolder holder;
    public Pos missileSpawnOffset;
    public TileEntity host;
    //TODO we need to figure out how to define spawn position (launcher vs cruise launcher)

    public CapabilityMissileLauncher(IMissileHolder holder, Pos missileSpawnOffset)
    {
        this.holder = holder;
        this.missileSpawnOffset = missileSpawnOffset;
    }

    @Nonnull
    @Override
    public IMissileHolder getMissileHolder()
    {
        return holder;
    }

    @Override
    public LaunchStatus launchMissile(@Nullable Entity cause)
    {
        return null;
    }

    @Override
    public LaunchStatus getLauncherStatus()
    {
        return null;
    }

    @Override
    public void setTarget(double x, double y, double z)
    {
        target_x = x;
        target_y = y;
        target_z = z;
        targetPos = new BlockPos(x, y, z);
    }

    @Nullable
    @Override
    public BlockPos getTarget()
    {
        return targetPos;
    }

    @Override
    public double getTargetX()
    {
        return target_x;
    }

    @Override
    public double getTargetY()
    {
        return target_y;
    }

    @Override
    public double getTargetZ()
    {
        return target_z;
    }

    @Override
    public boolean isCapability(@Nonnull Capability<?> capability)
    {
        return capability == ICBMClassicAPI.MISSILE_LAUNCHER_CAPABILITY;
    }

    @Override
    protected void save(NBTTagCompound tag)
    {
        tag.setDouble(NBTConstants.TARGET_X, target_x);
        tag.setDouble(NBTConstants.TARGET_Y, target_y);
        tag.setDouble(NBTConstants.TARGET_Z, target_z);
    }

    @Override
    protected void load(NBTTagCompound nbt)
    {
        target_x = nbt.getDouble(NBTConstants.TARGET_X);
        target_y = nbt.getDouble(NBTConstants.TARGET_Y);
        target_z = nbt.getDouble(NBTConstants.TARGET_Z);
    }


    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMissileLauncher.class, new Capability.IStorage<IMissileLauncher>()
        {
            @Nullable
            @Override
            public NBTBase writeNBT(Capability<IMissileLauncher> capability, IMissileLauncher instance, EnumFacing side)
            {
                return null;
            }

            @Override
            public void readNBT(Capability<IMissileLauncher> capability, IMissileLauncher instance, EnumFacing side, NBTBase nbt)
            {

            }
        },
        () -> new CapabilityMissileLauncher(null, null));
    }
}
