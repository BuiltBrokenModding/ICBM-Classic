package icbm.classic.command;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandUtilsTest
{

    @Test
    void removeFront_zeroLength()
    {
        final String[] input = new String[0];
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[0], output);
    }

    @Test
    void removeFront_singleLength()
    {
        final String[] input = new String[] {"tree"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[0], output);
    }

    @Test
    void removeFront_normalArray()
    {
        final String[] input = new String[] {"tree", "remove", "last"};
        final String[] output = CommandUtils.removeFront(input);
        Assertions.assertArrayEquals(new String[] {"remove", "last"}, output);
    }
}
