package icbm.classic.prefab.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Text box limited to numeric input. Client only.
 */
public class NumericField extends EditBox
{
    NumericType type = NumericType.INT;
    static List<Character> charsA = new ArrayList<>();
    static List<Character> charsB = new ArrayList<>();

    static
    {

        char[] s = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '+', '-' };
        for (char c : s)
        {
            charsA.add(c);
            charsB.add(c);
        }
        charsB.add('.');
    }

    public NumericField(int id, Font fontRender, int xPos, int yPos, int width, int height)
    {
        this(id, fontRender, NumericType.DOUBLE, xPos, yPos, width, height);
    }

    public NumericField(int id, Font fontRender, NumericType type, int xPos, int yPos, int width, int height)
    {
        super(fontRender, xPos, yPos, width, height, Component.empty());
        this.type = type;
    }

    @Override
    public void insertText(String str)
    {
        try
        {
            char[] chars = str.toCharArray();
            for (char c : chars)
            {
                if (type == NumericType.INT)
                {
                    if (!charsA.contains(c))
                    {
                        return;
                    }
                }
                else if (!charsB.contains(c))
                {
                    return;
                }
            }
            super.insertText(str);
        }
        catch (Exception e)
        {

        }
    }

    /** 1.12 name */
    public void writeText(String str)
    {
        insertText(str);
    }

    public int getTextAsInt()
    {
        String text = this.getValue();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Integer.parseInt(text);
            }
            catch (Exception e)
            {
                this.setValue("" + 0);
            }
        }
        return 0;
    }

    public double getTextAsDouble()
    {
        String text = this.getValue();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Double.parseDouble(text);
            }
            catch (Exception e)
            {
                this.setValue("" + 0);
            }
        }
        return 0;
    }

    public float getTextAsFloat()
    {
        String text = this.getValue();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Float.parseFloat(text);
            }
            catch (Exception e)
            {
                this.setValue("" + 0);
            }
        }
        return 0;
    }

    public enum NumericType
    {
        INT,
        DOUBLE,
        FLOAT
    }

}
// PORTED
