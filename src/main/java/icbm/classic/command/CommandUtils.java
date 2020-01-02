package icbm.classic.command;

import java.util.Arrays;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class CommandUtils
{
    public static String[] removeFront(String[] args)
    {
        if (args.length == 0 || args.length == 1)
        {
            return new String[0];
        }
        return Arrays.copyOfRange(args, 1, args.length);
    }
}
