package icbm.classic.command;

import icbm.classic.command.sub.CommandBlast;
import icbm.classic.command.sub.CommandLag;
import icbm.classic.command.sub.CommandRemove;
import icbm.classic.command.system.CommandGroup;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class ICBMCommands
{
    public static final CommandGroup ICBM_COMMAND = new CommandGroup("icbm");

    public static void init()
    {
        //Sub commands
        ICBMCommands.ICBM_COMMAND.registerCommand(new CommandBlast());
        ICBMCommands.ICBM_COMMAND.registerCommand(new CommandRemove());
        ICBMCommands.ICBM_COMMAND.registerCommand(new CommandLag());
    }
}
