package icbm.classic.content.blocks.launcher.frame;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public class TileLauncherFrame extends TileEntity {
    // TODO do inventory pass through to launcher

    private WeakReference<TileLauncherBase> host;
    private boolean isSearchingForHost = false;

    /**
     * Attempts to find the launcher or returns if already found
     *
     * @param shouldIgnore function to prevent referencing the same tiles again
     * @return launcher, if found
     */
    public TileLauncherBase findHost(Function<TileEntity, Boolean> shouldIgnore) {
        // Return host if we already found it
        if(host != null && host.get() != null) {
            return host.get();
        }

        // Prevent loops
        if(isSearchingForHost) {
            return null;
        }

        // Start search
        isSearchingForHost = true;

        // Attempt to locate host
        for(EnumFacing side : EnumFacing.values()) {
            final TileEntity tile = world.getTileEntity(getPos().offset(side));
            if(!shouldIgnore.apply(tile)) {

                // If launcher store and return
                if (tile instanceof TileLauncherBase) {
                    host = new WeakReference(host);
                    return (TileLauncherBase) tile;
                }
                // If frame, try to ask it for the host
                else if (tile instanceof TileLauncherFrame) {
                    return findHost((t) -> shouldIgnore.apply(t) || t == this);
                }
            }
        }

        // End search
        isSearchingForHost = false;

        return null;
    }
}
