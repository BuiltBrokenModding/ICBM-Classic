package icbm.classic.command.sub.blast;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.command.system.SubCommand;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastList extends SubCommand
{
    public CommandBlastList()
    {
        super("list");
    }

    @Override
    public void handleCommand(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException
    {
        //Convert list of explosives to string registry names
        String names = ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives().stream()
                .map(IExplosiveData::getRegistryName)
                .map(ResourceLocation::toString)
                .sorted()
                .collect(Collectors.joining(", "));

        //Output message TODO translate if possible?
        sender.sendMessage(new TextComponentString("Explosive Types: " + names));
    }
}
