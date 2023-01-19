package icbm.classic;

import com.adelean.inject.resources.junit.jupiter.WithGson;
import com.builtbroken.mc.testing.junit.TestManager;
import com.google.gson.*;

import com.lunarshark.nbttool.utils.JsonUtils;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.IdentityHashMap;

public abstract class TestBase {

    @WithGson
    Gson gson = JsonUtils.gson;

    protected static TestManager testManager = new TestManager("general-tests", Assertions::fail);

    public TestBase(String type) {
        if("tile".equals(type)) {
            gson = JsonUtils.gsonTileEntityExcludeVanillaFields;
        }
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
    }

    protected static <T> Capability<T> getCap(Class<T> type) {
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
