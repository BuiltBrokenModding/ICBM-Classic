package icbm.classic.api.explosion.responses;

import icbm.classic.api.explosion.BlastState;

/**
 * Created by Robin Seifert on 5/21/2021.
 */
@Deprecated
public final class BlastResponse
{


    public final BlastState state;
    public final String errorMessage;
    public final Throwable error;

    public BlastResponse(BlastState state)
    {
        this(state, null);
    }

    public BlastResponse(BlastState state, String errorMessage)
    {
        this(state, errorMessage, null);
    }

    public BlastResponse(BlastState state, String errorMessage, Throwable error)
    {
        this.state = state;
        this.errorMessage = errorMessage;
        this.error = error;
    }
}
