package icbm.classic.lib.radio.messages;

import icbm.classic.IcbmConstants;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class RadioTranslations {

    public static String RADIO_MESSAGE = "radio." + IcbmConstants.PREFIX + "message";
    public static String RADIO_TARGET_NULL = RADIO_MESSAGE + ".target.null";
    public static String RADIO_TARGET_SET = RADIO_MESSAGE + ".target.set";
    public static String RADIO_LAUNCH_SUCCESS = RADIO_MESSAGE + ".launch.success";
    public static String RADIO_LAUNCH_TRIGGERED = RADIO_MESSAGE + ".launch.triggered";
    public static String RADIO_LAUNCH_FAILED = RADIO_MESSAGE + ".launch.failed";
}
