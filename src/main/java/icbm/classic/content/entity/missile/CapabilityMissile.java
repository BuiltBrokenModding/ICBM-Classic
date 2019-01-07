package icbm.classic.content.entity.missile;

import icbm.classic.ICBMClassic;
import icbm.classic.api.caps.IEMPReceiver;
import icbm.classic.api.caps.IMissile;
import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.ILauncherContainer;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public class CapabilityMissile implements IMissile
{

    public final EntityMissile missile;

    public CapabilityMissile(EntityMissile missile)
    {
        this.missile = missile;
    }

    @Override
    public void dropMissileAsItem()
    {
        ItemStack stack = toStack();
        if (stack != null && !stack.isEmpty() && world() != null)
        {
            world().spawnEntity(new EntityItem(world(), x(), y(), z(), stack));
        }
        missile.setDead();
    }

    @Override
    public BlastState doExplosion()
    {
        return missile.doExplosion();
    }

    @Override
    public boolean hasExploded()
    {
        return missile.isExploding;
    }

    @Override
    public ItemStack toStack()
    {
        return new ItemStack(ICBMClassic.itemMissile, 1, missile.explosiveID);
    }

    @Override
    public int getTicksInAir()
    {
        return missile.ticksInAir;
    }

    @Override
    public ILauncherContainer getLauncher()
    {
        return null;
    }

    @Override
    public void launch(BlockPos target)
    {

    }

    @Override
    public World world()
    {
        return missile != null ? missile.world : null;
    }

    @Override
    public double z()
    {
        return missile != null ? missile.posZ : 0;
    }

    @Override
    public double x()
    {
        return missile != null ? missile.posX : 0;
    }

    @Override
    public double y()
    {
        return missile != null ? missile.posY : 0;
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMissile.class, new Capability.IStorage<IMissile>()
                {
                    @Nullable
                    @Override
                    public NBTBase writeNBT(Capability<IMissile> capability, IMissile instance, EnumFacing side)
                    {
                        return null;
                    }

                    @Override
                    public void readNBT(Capability<IMissile> capability, IMissile instance, EnumFacing side, NBTBase nbt)
                    {

                    }
                },
                () -> new CapabilityMissile(null));
    }
}
