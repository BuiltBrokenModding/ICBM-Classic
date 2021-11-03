package icbm.classic.api.explosion.responses;

import icbm.classic.api.explosion.BlastState;

/**
 * Reference enum for finding common blast responses for forge events
 *
 * Created by Robin Seifert on 5/21/2021.
 */
public enum BlastForgeResponses
{
    EXPLOSION_EVENT(BlastState.CANCLED, "Forge:event.explosion"),
    ENTITY_SPAWNING(BlastState.CANCLED, "Forge:event.entity.spawning");

    private final BlastResponse response;

    BlastForgeResponses(final BlastState blastState, String errorMessage) {
        this.response = new BlastResponse(blastState, errorMessage);
    }

    public BlastResponse get() {
        return response;
    }
}
