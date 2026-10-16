package icbm.classic.command;

import com.mojang.brigadier.CommandDispatcher;
import icbm.classic.command.sub.CommandLag;
import icbm.classic.command.sub.CommandRemove;
import icbm.classic.command.sub.blast.CommandBlastList;
import icbm.classic.command.sub.blast.CommandBlastSpread;
import icbm.classic.command.sub.blast.CommandBlastTrigger;
import icbm.classic.command.system.CommandEntryPoint;
import icbm.classic.command.system.CommandGroup;
import net.minecraft.commands.CommandSourceStack;

/**
 * Created by Robin Seifert on 1/6/20.
 */
public class ICBMCommands
{
    public static final CommandGroup ICBM_COMMAND = new CommandGroup("icbm");
    public static final CommandGroup BLAST_COMMAND = new CommandGroup("blast");

    public static final String TRANSLATION_UNKNOWN_COMMAND = "command.icbmclassic:icbm.error.unknown.command";

    private static boolean initialized = false;

    /**
     * Registers {@code /icbm} with Brigadier. Called from {@code RegisterCommandsEvent}, which fires
     * again on every {@code /reload}, so the command tree is only built once.
     *
     * @param dispatcher - server command dispatcher
     */
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher)
    {
        init();
        dispatcher.register(CommandEntryPoint.build("icbm", ICBM_COMMAND));
    }

    public static synchronized void init()
    {
        if (initialized)
        {
            return;
        }
        initialized = true;

        //Sub commands
        initBlastCommand();
        ICBMCommands.ICBM_COMMAND.registerCommand(new CommandRemove());
        ICBMCommands.ICBM_COMMAND.registerCommand(new CommandLag());
    }

    private static void initBlastCommand()
    {
        ICBMCommands.ICBM_COMMAND.registerCommand(BLAST_COMMAND);
        BLAST_COMMAND.registerCommand(new CommandBlastList());
        BLAST_COMMAND.registerCommand(new CommandBlastTrigger());
        BLAST_COMMAND.registerCommand(new CommandBlastSpread());
    }
}
// PORTED
