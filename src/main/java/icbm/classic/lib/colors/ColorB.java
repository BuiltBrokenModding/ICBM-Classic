package icbm.classic.lib.colors;

import lombok.Data;

@Data
public class ColorB {

    private byte red, green, blue, alpha;

    /**
     * Constructor for Color.
     */
    public ColorB() {
        this(0, 0, 0, 255);
    }

    /**
     * Constructor for Color. Alpha defaults to 255.
     */
    public ColorB(int r, int g, int b) {
        this(r, g, b, 255);
    }

    /**
     * Constructor for Color. Alpha defaults to 255.
     */
    public ColorB(byte r, byte g, byte b) {
        this(r, g, b, (byte) 255);
    }

    /**
     * Constructor for Color.
     */
    public ColorB(int r, int g, int b, int a) {
        set(r, g, b, a);
    }

    /**
     * Constructor for Color.
     */
    public ColorB(byte r, byte g, byte b, byte a) {
        set(r, g, b, a);
    }

    public void set(int r, int g, int b, int a) {
        red = (byte) r;
        green = (byte) g;
        blue = (byte) b;
        alpha = (byte) a;
    }

    /**
     * Set a color
     */
    public void set(byte r, byte g, byte b, byte a) {
        this.red = r;
        this.green = g;
        this.blue = b;
        this.alpha = a;
    }

    /**
     * Set a color
     */
    public void set(int r, int g, int b) {
        set(r, g, b, 255);
    }

    /**
     * Set a color
     */
    public void set(byte r, byte g, byte b) {
        set(r, g, b, (byte) 255);
    }
}
