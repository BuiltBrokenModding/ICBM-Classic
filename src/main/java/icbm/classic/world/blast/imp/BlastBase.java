package icbm.classic.world.blast.imp;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.explosion.responses.BlastNullResponses;
import icbm.classic.api.explosion.responses.BlastResponse;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

/**
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public abstract class BlastBase implements IBlastInit {
    private Level level;
    private double x, y, z;
    private boolean locked;
    private double blastSize;

    @Override
    public float getBlastRadius() {
        return (float) blastSize;
    }

    @Override
    public IBlastInit setBlastSize(double size) {
        this.blastSize = size;
        return this;
    }

    @Override
    public void clearBlast() {

    }

    @Nonnull
    @Override
    public BlastResponse runBlast() {
        final Level level = level();
        if (world != null) {
            if (!world.isClientSide()) {
                return triggerBlast();
            }
            return BlastState.TRIGGERED.genericResponse;
        }
        return BlastNullResponses.WORLD.get();
    }

    protected abstract BlastResponse triggerBlast();

    //<editor-fold desc="pos-data">
    @Override
    public Level level() {
        return world;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }
    //</editor-fold>

    //<editor-fold desc="blast-init">
    @Override
    public IBlastInit setBlastLevel(Level level) {
        if (!locked) {
            this.level = level;
        }
        return this;
    }

    @Override
    public IBlastInit setBlastPosition(double x, double y, double z) {
        if (!locked) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
        return this;
    }

    @Override
    public IBlastInit buildBlast() {
        locked = true;
        return this;
    }
    //</editor-fold>
}
