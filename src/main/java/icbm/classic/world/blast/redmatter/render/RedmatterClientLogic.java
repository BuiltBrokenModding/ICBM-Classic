package icbm.classic.world.blast.redmatter.render;

import icbm.classic.world.blast.redmatter.RedmatterEntity;

/**
 * Handles client size logic for the redmatter
 * <p>
 * Created by Dark(DarkGuardsman, Robert) on 5/23/2020.
 */
public class RedmatterClientLogic {
    private final RedmatterEntity host;

    private float visualSize = 0.0F;

    public RedmatterClientLogic(RedmatterEntity host) {
        this.host = host;
    }

    /**
     * Triggered from the render to smoothly change size with each frame update
     *
     * @param deltaTick percentage of time passed (0.0 - 1.0f)
     */
    public void lerpSize(float deltaTick) {
        visualSize = visualSize + deltaTick * (host.getBlastSize() - visualSize);
    }

    public float getVisualSize() {
        return visualSize;
    }
}
