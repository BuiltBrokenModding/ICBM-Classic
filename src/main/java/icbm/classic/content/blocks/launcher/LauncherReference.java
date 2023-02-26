package icbm.classic.content.blocks.launcher;

import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import java.lang.ref.WeakReference;
import java.util.function.Function;

public class LauncherReference {

    private static final EnumFacing[] SEARCH_ORDER = new EnumFacing[] {EnumFacing.DOWN, EnumFacing.UP, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.SOUTH, EnumFacing.WEST};

    private WeakReference<TileLauncherBase> launcherRef;
    private boolean isSearchingForHost = false;

    private final TileEntity host;

    public LauncherReference(TileEntity host) {
        this.host = host;
    }

    /**
     * Attempts to find the launcher or returns if already found
     *
     * @param shouldIgnore function to prevent referencing the same tiles again
     * @return launcher, if found
     */
    public TileLauncherBase findHost(Function<TileEntity, Boolean> shouldIgnore) {
        // Return host if we already found it
        if (launcherRef != null) {
            final TileLauncherBase launcherBase = launcherRef.get();
            if (launcherBase != null && !launcherBase.isInvalid()) {
                return launcherBase;
            }
        }

        // Prevent loops
        if (isSearchingForHost) {
            return null;
        }

        // Start search
        isSearchingForHost = true;
        TileLauncherBase launcherBase = null;

        // Attempt to locate host
        for (EnumFacing side : SEARCH_ORDER) {
            final TileEntity tile = host.getWorld().getTileEntity(host.getPos().offset(side));
            if (shouldIgnore == null || !shouldIgnore.apply(tile)) {

                // If launcher store and return
                if (tile instanceof TileLauncherBase) {
                    launcherBase = (TileLauncherBase) tile;
                    break;
                }
                // If component: attempt to get host from it
                else if (tile instanceof ILauncherComponent) {
                    final LauncherReference reference = ((ILauncherComponent) tile).getReference();
                    if (!reference.isSearchingForHost) {
                       launcherBase = reference.findHost((t) -> shouldIgnore != null && shouldIgnore.apply(t) || t == host);
                       break;
                    }
                }
            }
        }

        if(launcherBase != null) {
            launcherRef = new WeakReference<TileLauncherBase>(launcherBase);
        }

        // End search
        isSearchingForHost = false;

        return null;
    }
}
