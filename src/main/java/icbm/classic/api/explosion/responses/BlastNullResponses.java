package icbm.classic.api.explosion.responses;

import icbm.classic.api.explosion.BlastState;

/**
 * Reference enum for finding common blast responses for null data entries
 *
 * Created by Robin Seifert on 5/21/2021.
 */
public enum BlastNullResponses
{
    WORLD(BlastState.ERROR, "Minecraft:world.null"),
    BLAST_CREATION(BlastState.ERROR, "ICBM:blast.creation.null"),
    BLAST_FACTORY(BlastState.ERROR, "ICBM:blast.factory.null"),
    EXPLOSIVE_DATA(BlastState.ERROR, "ICBM:explosive.data.null"),
    EXPLOSIVE_CAPABILITY(BlastState.ERROR, "ICBM:explosive.capability.null");

    private final BlastResponse response;

    BlastNullResponses(final BlastState blastState, String errorMessage) {
        this.response = new BlastResponse(blastState, errorMessage);
    }

    public BlastResponse get() {
        return response;
    }
}
