package icbm.classic.api.explosion.responses;

import icbm.classic.api.explosion.BlastState;

/**
 * Reference enum for error responses
 * <p>
 * Created by Robin Seifert on 5/22/2021.
 */
public enum BlastErrorResponses {
    MISSING_BLAST_REGISTRY(BlastState.ERROR, "ICBM:registry.blast.missing");

    private final BlastResponse response;

    BlastErrorResponses(final BlastState blastState, String errorMessage) {
        this.response = new BlastResponse(blastState, errorMessage);
    }

    public BlastResponse get() {
        return response;
    }
}
