package icbm.classic;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Optional;

public abstract class TestBase {

    /*@WithGson
    Gson gson = JsonUtils.gson;

    protected static TestManager testManager = new TestManager("general-tests", Assertions::fail);

    public TestBase() {}

    public TestBase(String type) {
        if("tile".equals(type)) {
            gson = JsonUtils.gsonTileEntityExcludeVanillaFields;
        }
    }

    @BeforeAll
    public static void loadGameAndMod()
    {
        Bootstrap.register();
        ICBMClassicMock.init();
    }

    @AfterAll
    public static void afterAllTests()
    {
        testManager.tearDownTest();
    }

    @AfterEach
    public void afterEachTest()
    {
        testManager.cleanupBetweenTests();
    }*/

    protected static <T> Capability<T> getCapOrCreate(Class<T> type, Runnable runnable) {
        return Optional.ofNullable(getCap(type)).orElseGet(() -> {
            runnable.run();
            return getCap(type);
        });
    }

    public static <T> Capability<T> getCap(Class<T> type) {
        try {
            final Field field = CapabilityManager.class.getDeclaredField("providers");
            field.setAccessible(true);
            final IdentityHashMap<String, Capability<?>> providers = (IdentityHashMap<String, Capability<?>>) field.get(CapabilityManager.INSTANCE);
            return (Capability<T>) providers.get(type.getName().intern());
        }
        catch (Exception e) {
            Assertions.fail("Failed to access capability", e);
            return null;
        }
    }
}
