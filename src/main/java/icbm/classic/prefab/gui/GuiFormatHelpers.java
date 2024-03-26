package icbm.classic.prefab.gui;

import icbm.classic.IcbmConstants;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

public class GuiFormatHelpers {

    // Localizations
    private static final String LANG_KEY = "gui." + IcbmConstants.PREFIX;
    private static final String LANG_ERROR = LANG_KEY + "error";
    private static final String ERROR_NULL = LANG_ERROR + ".null";
    private static final String ERROR_FORMAT_INT = LANG_ERROR + ".format.number.int";
    private static final String ERROR_FORMAT_FLOAT = LANG_ERROR + ".format.number.float";
    private static final String ERROR_FORMAT_V3D = LANG_ERROR + ".format.vector.3d";
    private static final String ERROR_FORMAT_V2D = LANG_ERROR + ".format.vector.2d";

    /**
     * Processes a vector from a string
     *
     * @param inputText matching the format "x,y,z" with x/y/z being a numeric value
     * @param setter    to pass back the vector
     * @return error feedback, null for no errors
     */
    public static String parseVec3(String inputText, Consumer<Vec3> setter) {
        if (inputText != null) {
            final String[] split = inputText.split(",");
            if (split.length == 3) {
                try {
                    final double x = Double.parseDouble(split[0].trim());
                    final double y = Double.parseDouble(split[1].trim());
                    final double z = Double.parseDouble(split[2].trim());
                    setter.accept(new Vec3(x, y, z)); //TODO use a builder
                    return null;
                } catch (NumberFormatException e) {
                    return ERROR_FORMAT_V3D;
                }
            } else {
                return ERROR_FORMAT_V3D;
            }
        } else {
            return ERROR_NULL;
        }
    }

    /**
     * Processes a integer from a string
     *
     * @param inputText containing a whole number
     * @param setter    to pass back the int
     * @return error feedback, null for no errors
     */
    public static String parseInt(String inputText, Consumer<Integer> setter) {
        if (inputText != null) {
            try {
                setter.accept(Integer.parseInt(inputText.trim()));
                return null;
            } catch (NumberFormatException e) {
                return ERROR_FORMAT_INT;
            }
        } else {
            return ERROR_NULL;
        }
    }
}
