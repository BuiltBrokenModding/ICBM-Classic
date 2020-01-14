package icbm.classic.command.sub;

import com.builtbroken.mc.testing.junit.TestManager;
import com.builtbroken.mc.testing.junit.testers.DummyCommandSender;
import icbm.classic.content.entity.missile.EntityMissile;
import net.minecraft.entity.passive.EntitySheep;
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

    private void sheep(int x, int y, int z) {
        final EntitySheep sheep = new EntitySheep(testManager.getWorld());
        sheep.forceSpawn = true;
        sheep.setPosition(x, y, z);
        testManager.getWorld().spawnEntity(sheep);
    }

    private void missile(int x, int y, int z) {
        final EntityMissile missile = new EntityMissile(testManager.getWorld());
        missile.forceSpawn = true;
        missile.setPosition(x, y, z);
        testManager.getWorld().spawnEntity(missile);
    }

    @Test
    void command_removeNothing() {

        //Spawn some sheep to act as decoys
        sheep(100, 20, 100);
        sheep(100, 30, 100);
        sheep(100, 40, 100);
        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(),"Should start with 3 sheep");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]));

        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.size(),"Should end with 3 sheep");
    }

    @Test
    void command_removeSomething() {

        //Spawn some sheep to act as decoys
        sheep(100, 20, 100);
        sheep(100, 30, 100);
        sheep(100, 40, 100);
        missile(100, 10, 100);
        missile(-100, 10, -100);
        Assertions.assertEquals(5, testManager.getWorld().loadedEntityList.size(),"Should start with 5 entities");

        //Trigger command
        Assertions.assertDoesNotThrow(() -> command.handleCommand(testManager.getServer(), dummyCommandSender, new String[0]));

        Assertions.assertEquals(3, testManager.getWorld().loadedEntityList.stream().filter(e -> e instanceof EntitySheep).count(),"Should end with 3 sheep");
    }
}
