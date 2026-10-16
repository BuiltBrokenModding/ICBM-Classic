package icbm.classic;

import com.adelean.inject.resources.junit.jupiter.WithGson;
import com.builtbroken.mc.testing.junit.TestManager;
import com.google.gson.*;

import com.lunarshark.nbttool.utils.JsonUtils;
import com.lunarshark.nbttool.utils.SaveToJson;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.init.Bootstrap;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
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

    @WithGson
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
    }

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

    protected static void assertExplosive(@Nonnull ItemStack stack, @Nonnull String registryName, @Nonnull NBTTagCompound customTag) {
        assertExplosive(stack.getCapability(ICBMClassicAPI.EXPLOSIVE_CAPABILITY, null), registryName, customTag);
    }

    protected static void assertExplosive(IExplosive explosive, @Nonnull String registryName, @Nonnull NBTTagCompound customTag) {

        // Check capability is returned with the correct type
        Assertions.assertNotNull(explosive, "Failed to get explosive capability");

        // Validate correct explosive
        final IExplosiveData explosiveData = explosive.getExplosiveData();
        Assertions.assertNotNull(explosiveData, "Explosive capability is lacking explosive data");
        Assertions.assertEquals(new ResourceLocation(registryName), explosiveData.getRegistryKey());

        // Validate correct custom tag data
        //Assertions.assertEquals(customTag, explosive.getCustomBlastData());
    }

    protected void assertFloating(float expected, float actual, float precisionRange) {
        float delta = Math.abs(expected - actual);
        if(delta > precisionRange) {
            Assertions.fail(String.format("Actual: %s is off by %s from Expected: %s with range of %s", actual, delta, expected, precisionRange));
        }
    }

    protected void assertTags(NBTTagCompound expectedTag, NBTTagCompound actualTag) {
        if(!expectedTag.equals(actualTag)) {
            throw new AssertionFailedError("Compound tags do not match",  outputJson(expectedTag), outputJson(actualTag));
        }
    }

    protected String outputJson(NBTTagCompound tag) {
        JsonObject saveData = sortAndGet(SaveToJson.convertToGsonObjects(tag));
        return gson.toJson(saveData);
    }

    private static JsonObject sortAndGet(JsonObject jsonObject) {
        final List<String> keySet = jsonObject.entrySet().stream().map(Map.Entry::getKey).sorted().collect(Collectors.toList());
        final JsonObject temp = new JsonObject();
        for (String key : keySet) {
            JsonElement ele = jsonObject.get(key);
            if (ele.isJsonObject()) {
                ele = sortAndGet(ele.getAsJsonObject());
                temp.add(key, ele);
            } else if (ele.isJsonArray()) {
                temp.add(key, ele.getAsJsonArray());
            } else
                temp.add(key, ele.getAsJsonPrimitive());
        }
        return temp;
    }

    protected NBTTagCompound readSaveFile(File file) {
        try(FileInputStream fileinputstream = new FileInputStream(file)) {
            return CompressedStreamTools.readCompressed(fileinputstream);
        }
        catch (Exception e) {
            Assertions.fail("Failed to load save file: " + file);
        }
        return null;
    }
}
