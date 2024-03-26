package icbm.classic.world.blast.redmatter.caps;

import icbm.classic.api.explosion.BlastState;
import icbm.classic.api.explosion.IBlast;
import icbm.classic.api.explosion.responses.BlastResponse;
import icbm.classic.world.blast.redmatter.RedmatterEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Wrapper for exposing the {@link RedmatterEntity} as a Blast
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 4/19/2020.
 */
public class BlastRedmatterWrapper implements IBlast {
    private final RedmatterEntity host;

    public BlastRedmatterWrapper(RedmatterEntity host) {
        this.host = host;
    }

    @Nonnull
    @Override
    public BlastResponse runBlast() {
        return BlastState.ALREADY_TRIGGERED.genericResponse;
    }

    @Override
    public void clearBlast() {
        host.setDead();
    }

    //<editor-fold desc="properties">
    @Override
    public boolean isCompleted() {
        return host.isDead;
    }

    @Override
    @Nullable
    public Entity getEntity() {
        return host;
    }

    @Override
    @Nullable
    public Entity getBlastSource() {
        return host;
    }
    //</editor-fold>

    //<editor-fold desc="position-data">
    @Override
    public Level level() {
        return host.world;
    }

    @Override
    public double z() {
        return host.getZ();
    }

    @Override
    public double x() {
        return host.getX();
    }

    @Override
    public double y() {
        return host.getY();
    }
    //</editor-fold>
}
