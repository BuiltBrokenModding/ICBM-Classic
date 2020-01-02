package icbm.classic.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Robert Seifert on 1/2/20.
 */
public class DummyCommandSender implements ICommandSender //TODO add to test framework
{
    public World world;
    public MinecraftServer server;

    public final Queue<ITextComponent> messages = new LinkedList();

    @Override
    public String getName()
    {
        return "dummy";
    }

    @Override
    public boolean canUseCommand(int permLevel, String commandName)
    {
        return true;
    }

    @Override
    public World getEntityWorld()
    {
        return world;
    }

    @Nullable
    @Override
    public MinecraftServer getServer()
    {
        return server;
    }

    @Override
    public void sendMessage(ITextComponent component)
    {
        messages.offer(component);
    }
}
