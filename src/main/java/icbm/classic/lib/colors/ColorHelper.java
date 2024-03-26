package icbm.classic.lib.colors;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Created by Robin Seifert on 4/1/2022.
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ColorHelper {
    public static float[] argbToFloats(int argb) {
        float alpha = (float) (argb >> 24 & 255) / 255.0F;

        float red = (float) (argb >> 16 & 255) / 255.0F;
        float green = (float) (argb >> 8 & 255) / 255.0F;
        float blue = (float) (argb & 255) / 255.0F;

        return new float[]{red, green, blue, alpha};
    }

    public static int toARGB(int red, int green, int blue, int alpha) {
        return ((alpha & 255) << 24) + ((red & 255) << 16) + ((green & 255) << 8) + (blue & 255);
    }

    public static int toRGB(int red, int green, int blue) {
        return ((red & 255) << 16) + ((green & 255) << 8) + (blue & 255);
    }

    /**
     * From java.awt.Color... copied and modified to avoid loading awt module
     */
    public static ColorB HSBtoRGB(float hue, float saturation, float brightness) {
        int r = 0, g = 0, b = 0;
        if (saturation == 0) {
            r = g = b = (int) (brightness * 255.0f + 0.5f);
        } else {
            float h = (hue - (float) Math.floor(hue)) * 6.0f;
            float f = h - (float) java.lang.Math.floor(h);
            float p = brightness * (1.0f - saturation);
            float q = brightness * (1.0f - saturation * f);
            float t = brightness * (1.0f - (saturation * (1.0f - f)));
            switch ((int) h) {
                case 0:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (t * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 1:
                    r = (int) (q * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (p * 255.0f + 0.5f);
                    break;
                case 2:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (brightness * 255.0f + 0.5f);
                    b = (int) (t * 255.0f + 0.5f);
                    break;
                case 3:
                    r = (int) (p * 255.0f + 0.5f);
                    g = (int) (q * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 4:
                    r = (int) (t * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (brightness * 255.0f + 0.5f);
                    break;
                case 5:
                    r = (int) (brightness * 255.0f + 0.5f);
                    g = (int) (p * 255.0f + 0.5f);
                    b = (int) (q * 255.0f + 0.5f);
                    break;
            }
        }
        return new ColorB(r, g, b);
    }
}
