package com.builtbroken.mc.prefab.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

import java.util.ArrayList;
import java.util.List;

public class NumericField extends GuiTextField
{
    NumericType type = NumericType.INT;
    static List charsA = new ArrayList();
    static List charsB = new ArrayList();

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

    public NumericField(int id, FontRenderer fontRender, int xPos, int yPos, int width, int height)
    {
        this(id, fontRender, NumericType.DOUBLE, xPos, yPos, width, height);
    }

    public NumericField(int id, FontRenderer fontRender, NumericType type, int xPos, int yPos, int width, int height)
    {
        super(id, fontRender, xPos, yPos, width, height);
        this.type = type;
    }

    public void writeText(String str)
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
            super.writeText(str);
        }
        catch (Exception e)
        {

        }
    }

    public int getTextAsInt()
    {
        String text = this.getText();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Integer.parseInt(text);
            }
            catch (Exception e)
            {
                this.setText("" + 0);
            }
        }
        return 0;
    }

    public double getTextAsDouble()
    {
        String text = this.getText();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Double.parseDouble(text);
            }
            catch (Exception e)
            {
                this.setText("" + 0);
            }
        }
        return 0;
    }

    public float getTextAsFloat()
    {
        String text = this.getText();
        if (text != null && !text.isEmpty())
        {
            try
            {
                return Float.parseFloat(text);
            }
            catch (Exception e)
            {
                this.setText("" + 0);
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
