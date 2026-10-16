package icbm.classic.command.sub;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.TestUtils;
import icbm.classic.command.FakeBlast;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.lib.actions.WorkTickingActionHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.util.math.Vec3d;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Created by Robert Seifert on 1/14/20.
 */
public class CommandLagTest
{
    private static TestManager testManager = new TestManager("CommandLagTest", Assertions::fail);
    private final DummyCommandSender dummyCommandSender = new DummyCommandSender(testManager);

    private final CommandLag command = new CommandLag();

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
    void help_server()
    {
        command.displayHelp(dummyCommandSender);
        Assertions.assertEquals(0, dummyCommandSender.messages.size());
    }

    @Test
    void help_player()
    {
        command.displayHelp(testManager.getPlayer());
        Assertions.assertEquals(1, testManager.getPlayer().messages.size());
        Assertions.assertEquals("/lag [radius]", testManager.getPlayer().messages.poll().getUnformattedText());
    }

    @Test
    void command_removeNothing()
    {
        dummyCommandSender.position = new Vec3d(100, 200, 100);

        //Spawn some sheep to act as decoys
        TestUtils.sheep(testManager.getWorld(), 100, 20, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 30, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 40, 100);
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should start with 3 sheep");

        //Trigger command
        final String[] args = new String[]{"all"};
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, args));

        //Should still have 3 sheep
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should end with 3 sheep");
    }

    @Test
    void command_removeMissiles()
    {
        dummyCommandSender.position = new Vec3d(100, 20, 100);

        //Spawn some sheep to act as decoys
        TestUtils.sheep(testManager.getWorld(), 100, 20, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 30, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 40, 100);

        TestUtils.missile(testManager.getWorld(), 100, 10, 100);
        TestUtils.missile(testManager.getWorld(), -100, 10, -100);

        //Validate start condition
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntitySheep).count(), "Should start with 3 sheep");
        Assertions.assertEquals(2, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntityExplosiveMissile).count(), "Should start with 2 missiles");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]));

        //Validate output
        Assertions.assertEquals(1, dummyCommandSender.messages.size(), "Should have 1 chat message");
        Assertions.assertEquals(CommandLag.TRANSLATION_LAG_REMOVE, dummyCommandSender.pollLastMessage(), "Should get translation");

        //Should still have 3 sheep
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntitySheep).count(), "Should end with 3 sheep");
        Assertions.assertEquals(0, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntityExplosiveMissile).filter(Entity::isEntityAlive).count(), "Should end with 0 missiles");
    }

    @Test
    void command_removeBlasts()
    {
        dummyCommandSender.position = new Vec3d(100, 20, 100);

        //Spawn some sheep to act as decoys
        TestUtils.sheep(testManager.getWorld(), 100, 20, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 30, 100);
        TestUtils.sheep(testManager.getWorld(), 100, 40, 100);

        WorkTickingActionHandler.activeBlasts.add(new FakeBlast(null).setBlastPosition(100, 20, 100).setBlastWorld(testManager.getWorld()));
        WorkTickingActionHandler.activeBlasts.add(new FakeBlast(null).setBlastPosition(100, 20, 100).setBlastWorld(testManager.getWorld()));

        //Validate start condition
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should start with 3 entities");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]));

        //Validate output
        Assertions.assertEquals(1, dummyCommandSender.messages.size(), "Should have 1 chat message");
        Assertions.assertEquals(CommandLag.TRANSLATION_LAG_REMOVE, dummyCommandSender.pollLastMessage(), "Should get translation");

        //Should still have 3 sheep
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(), "Should end with 3 entities");
        Assertions.assertEquals(0, WorkTickingActionHandler.activeBlasts.size(), "Should end with 0 blasts");
    }
}
