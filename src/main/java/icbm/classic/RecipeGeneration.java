package icbm.classic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import icbm.classic.api.ICBMClassicAPI;
import net.minecraftforge.oredict.OreDictionary;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class RecipeGeneration {

    public static void cartRecipes() {
        final File saveFolder = new File(".", "recipes/bombcarts");
        saveFolder.mkdirs();

        final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

        ICBMClassicAPI.EX_MINECART_REGISTRY.getExplosives().forEach(ex -> {

            final File file = new File(saveFolder, ex.getRegistryName().getResourcePath() + ".json");

            final JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "icbmclassic:explosive");

            // Result section
            final JsonObject resultJson = new JsonObject();
            resultJson.addProperty("explosive", ex.getRegistryName().toString());
            resultJson.addProperty("device", "icbmclassic:minecart");
            resultJson.addProperty("count", 1);
            recipeJson.add("result", resultJson);

            // pattern section
            final JsonArray patternJson = new JsonArray();
            patternJson.add("a");
            patternJson.add("c");
            recipeJson.add("pattern", patternJson);

            // pattern section
            final JsonObject keyJson = new JsonObject();
            recipeJson.add("key", keyJson);

            final JsonObject key1 = new JsonObject();
            key1.addProperty("item", "minecraft:minecart");
            keyJson.add("c", key1);

            final JsonObject key2 = new JsonObject();
            key2.addProperty("type", "icbmclassic:explosive");
            key2.addProperty("explosive", ex.getRegistryName().toString());
            keyJson.add("a", key2);

            try (Writer writer = new FileWriter(file)) {
                gson.toJson(recipeJson, writer);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void grenadeRecipes() {
        final File saveFolder = new File(".", "recipes/grenades");
        saveFolder.mkdirs();

        final Gson gson =  new GsonBuilder().setPrettyPrinting().create();

        ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosives().forEach(ex -> {

            final File file = new File(saveFolder, ex.getRegistryName().getResourcePath() + ".json");

            final JsonObject recipeJson = new JsonObject();
            recipeJson.addProperty("type", "icbmclassic:explosive");

            // Result section
            final JsonObject resultJson = new JsonObject();
            resultJson.addProperty("explosive", ex.getRegistryName().toString());
            resultJson.addProperty("device", "icbmclassic:grenade");
            resultJson.addProperty("count", 1);
            recipeJson.add("result", resultJson);

            // pattern section
            final JsonArray patternJson = new JsonArray();
            patternJson.add("s");
            patternJson.add("x");
            recipeJson.add("pattern", patternJson);

            // pattern section
            final JsonObject keyJson = new JsonObject();
            recipeJson.add("key", keyJson);

            final JsonObject key1 = new JsonObject();
            key1.addProperty("type","forge:ore_dict");
            key1.addProperty("ore", "string");
            keyJson.add("s", key1);

            final JsonObject key2 = new JsonObject();
            key2.addProperty("type", "icbmclassic:explosive");
            key2.addProperty("explosive", ex.getRegistryName().toString());
            keyJson.add("x", key2);

            try (Writer writer = new FileWriter(file)) {
                gson.toJson(recipeJson, writer);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
