package icbm.classic;

import com.google.gson.*;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.junit.jupiter.api.Assertions;
import org.opentest4j.AssertionFailedError;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
