package icbm.classic.api.radio;

import javax.annotation.Nonnull;

/**
 * Applied to capabilities that have a single channel that can be set and accessed.
 * Some systems will have several channels either for sending or receiving. Thus,
 * can't be easily accessed.
 * <p>
 * This mostly exists for tools to get or set the value.
 */
public interface IRadioChannelAccess extends IRadio {

    /**
     * Unique ID of the channel. Takes place
     * of frequency normally found in radios.
     * Allowing for players to customize
     *
     * @return channel
     */
    @Nonnull
    String getChannel();

    /**
     * Sets channel id
     *
     * @param id to use
     */
    void setChannel(String id);
}
