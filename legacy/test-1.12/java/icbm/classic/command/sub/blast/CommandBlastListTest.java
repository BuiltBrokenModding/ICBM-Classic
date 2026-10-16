package icbm.classic.command.sub.blast;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.lib.explosive.reg.ExplosiveRegistry;
import net.minecraft.command.CommandException;
import net.minecraft.util.ResourceLocation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/6/20.
 */
public class CommandBlastListTest
{
    private static TestManager testManager = new TestManager("CommandBlastListTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandBlastList commandBlastList = new CommandBlastList();

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
    void help()
    {
       commandBlastList.displayHelp(dummyCommandSender);
       Assertions.assertEquals(1, dummyCommandSender.messages.size());
       Assertions.assertEquals("/list", dummyCommandSender.messages.poll().getUnformattedText());
    }

    @Test
    void listBlasts() throws CommandException
    {
        //Setup blast registry
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = new ExplosiveRegistry();
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "bat"), EnumTier.FOUR, (w, x, y, z, s) -> null);
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("tree", "cat"), EnumTier.FOUR, (w, x, y, z, s) -> null);
        ICBMClassicAPI.EXPLOSIVE_REGISTRY.register(new ResourceLocation("bo", "fat"), EnumTier.FOUR, (w, x, y, z, s) -> null);
        ((ExplosiveRegistry)ICBMClassicAPI.EXPLOSIVE_REGISTRY).lockNewExplosives();

        final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

        //Trigger
        commandBlastList.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]);

        //Should only have 1 message to sender
        Assertions.assertEquals(1, dummyCommandSender.messages.size());
        Assertions.assertEquals("Explosive Types: bo:fat, tree:bat, tree:cat", dummyCommandSender.messages.poll().getUnformattedText());

        //Cleanup
        ICBMClassicAPI.EXPLOSIVE_REGISTRY = null;
    }
}
