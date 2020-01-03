package icbm.classic.command.sub;

import com.builtbroken.mc.testing.junit.TestManager;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.IExplosiveRegistry;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import icbm.classic.command.CommandICBM;
import icbm.classic.command.DummyCommandSender;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Robert Seifert on 1/3/20.
 */
public class CommandBlastTest
{

    private static TestManager testManager = new TestManager("CommandUtils");
    private CommandICBM commandICBM = new CommandICBM("icbm").init();
    private CommandBlast commandBlast = (CommandBlast) commandICBM.subCommandMap.get("blast");

    @AfterEach
    public void cleanupBetweenTests()
    {
        testManager.cleanupBetweenTests();
    }

    @AfterAll
    public static void tearDown()
    {
        testManager.tearDownTest();
    }

    @Test
    void help_player()
    {
        final List<String> list = new ArrayList();
        commandBlast.collectHelpPlayer((str) -> list.add(str));
        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("<id> <scale>", list.get(0));
    }

    @Test
    void help_server()
    {
        final List<String> list = new ArrayList();
        commandBlast.collectHelpServer((str) -> list.add(str));
        Assertions.assertEquals(3, list.size());
        Assertions.assertEquals("list", list.get(0));
        Assertions.assertEquals("<id> <x> <y> <z> <scale>", list.get(1));
        Assertions.assertEquals("spread <amount> <id> <x> <y> <z> <scale>", list.get(2));
    }

    @Test
    void listBlasts() throws CommandException
    {
        //Setup blast registry
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "bat"), EnumTier.FOUR, () -> null);
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "cat"), EnumTier.FOUR, () -> null);
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("bo", "fat"), EnumTier.FOUR, () -> null);
        ((ExplosiveRegistry)ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockNewExplosives();

        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        //Trigger
        commandICBM.execute(testManager.getServer(), dummyCommandSender, new String[] {"blast", "list"});

        //Should only have 1 message to sender
        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("Explosive Types: bo:fat, tree:bat, tree:cat", dummyCommandSender.messages.poll().getUnformattedText());

        //Cleanup
        ICBMClassicAPI.EMP_CAPABILITY = null;
    }

    void spreadBlasts()
    {

    }

    void blastPlayer()
    {

    }

    void blastServer()
    {

    }
}
