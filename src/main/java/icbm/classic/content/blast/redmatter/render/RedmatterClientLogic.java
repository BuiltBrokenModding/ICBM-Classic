package icbm.classic.content.blast.redmatter.render;

import icbm.classic.content.blast.redmatter.EntityRedmatter;

/**
 * Handles client size logic for the redmatter
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/23/2020.
 */
public class RedmatterClientLogic
{
    private final EntityRedmatter host;

    private float visualSize = 0.0F;

    public RedmatterClientLogic(EntityRedmatter host) {
        this.host = host;
    }

    /**
     * Triggered from the render to smoothly change size with each frame update
     *
     * @param deltaTick percentage of time passed (0.0 - 1.0f)
     */
    public void lerpSize(float deltaTick)
    {
        visualSize = visualSize + deltaTick * (host.getBlastSize() - visualSize);
    }

    public float getVisualSize()
    {
        return visualSize;
    }
}
