package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.ILauncherComponent;
import icbm.classic.content.blocks.launcher.LauncherReference;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;
import java.util.Optional;

public class TileLauncherFrame extends TileEntity implements ILauncherComponent {

    private final LauncherReference launcherReference = new LauncherReference(this);
    @Override
    public LauncherReference getReference() {
        return launcherReference;
    }
    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing)
    {
        return super.hasCapability(capability, facing) || Optional.ofNullable(getLauncher()).map(launcher -> launcher.hasCapability(capability, facing)).orElse(false);
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing)
    {
        if (getLauncher() != null)
        {
            final T cap = getLauncher().getCapability(capability, facing);
            if(cap != null) {
                return cap;
            }
        }
        return super.getCapability(capability, facing);
    }
}
