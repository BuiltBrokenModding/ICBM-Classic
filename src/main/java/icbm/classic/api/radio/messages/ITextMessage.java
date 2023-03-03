package icbm.classic.api.radio.messages;

import icbm.classic.api.radio.IRadioMessage;
import net.minecraft.util.math.Vec3d;

import javax.annotation.Nullable;

/**
 * Packet containing general text data
 */
public interface ITextMessage extends IRadioMessage {

    /**
     * Contained message.
     *
     * @return message
     */
    String getMessage();

    /**
     * True if the message should be translated before
     * showing to a user.
     *
     * @return true if translate
     */
    boolean shouldTranslate();

    /**
     * Data to use for injecting into the translation
     * @return data or null if not used
     */
    @Nullable
    default Object[] getTranslationInputs() {
        return null;
    }
}
