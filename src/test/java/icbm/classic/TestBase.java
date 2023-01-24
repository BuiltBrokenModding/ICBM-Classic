package icbm.classic;

import com.adelean.inject.resources.junit.jupiter.WithGson;
import com.builtbroken.mc.testing.junit.TestManager;
import com.google.common.base.Suppliers;
import com.google.gson.*;

import com.lunarshark.nbttool.utils.JsonUtils;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.Optional;
import java.util.function.Function;

public abstract class TestBase {

    @WithGson
    Gson gson = JsonUtils.gson;

    protected static TestManager testManager = new TestManager("general-tests", Assertions::fail);

    public TestBase() {}

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

    protected static <T> Capability<T> getCapOrCreate(Class<T> type, Runnable runnable) {
        return Optional.ofNullable(getCap(type)).orElseGet(() -> {
            runnable.run();
            return getCap(type);
        });
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

    protected static void assertExplosive(@Nonnull ItemStack stack, @Nonnull String registryName, @Nonnull NBTTagCompound customTag) {
        assertExplosive(stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), registryName, customTag);
    }

    protected static void assertExplosive(IExplosive explosive, @Nonnull String registryName, @Nonnull NBTTagCompound customTag) {

        // Check capability is returned with the correct type
        Assertions.assertNotNull(explosive, "Failed to get explosive capability");

        // Validate correct explosive
        final IExplosiveData explosiveData = explosive.getExplosiveData();
        Assertions.assertNotNull(explosiveData, "Explosive capability is lacking explosive data");
        Assertions.assertEquals(new ResourceLocation(registryName), explosiveData.getRegistryName());

        // Validate correct custom tag data
        Assertions.assertEquals(customTag, explosive.getCustomBlastData());
    }
}
