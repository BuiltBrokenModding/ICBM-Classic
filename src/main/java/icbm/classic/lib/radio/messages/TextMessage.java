package icbm.classic.lib.radio.messages;

import icbm.classic.api.radio.messages.ITextMessage;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
public class TextMessage implements ITextMessage {
    private final String channel;
    private final String message;
    @Accessors(fluent = true)
    private final boolean shouldTranslate;
    private final Object[] translationInputs;

    public TextMessage(String channel, String message) {
        this.channel = channel;
        this.message = message;
        this.shouldTranslate = true;
        this.translationInputs = null;
    }

    public TextMessage(String channel, String message, Object... data) {
        this.channel = channel;
        this.message = message;
        this.shouldTranslate = true;
        this.translationInputs = data;
    }
}
