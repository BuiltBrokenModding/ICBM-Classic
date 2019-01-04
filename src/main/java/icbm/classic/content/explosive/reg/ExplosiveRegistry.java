package icbm.classic.content.explosive.reg;

import com.google.gson.*;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import icbm.classic.ICBMClassic;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.IExplosiveRegistry;
import icbm.classic.content.explosive.Explosives;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistry implements IExplosiveRegistry
{

    //Map of explosion id to data
    public final HashMap<ResourceLocation, ExplosiveData> explosiveData = new HashMap();

    //Map of id to name, and its reverse
    public final Map<Integer, ResourceLocation> id_to_name = new HashMap();
    public final Map<ResourceLocation, Integer> name_to_id = new HashMap();

    //Content types to set of IDs enabled for content
    public final Map<ResourceLocation, Set<Integer>> contentToIds = new HashMap();


    //Ids
    private int nextID = Explosives.values().length;

    //Save file
    private File saveFile;

    @Override
    public ExplosiveData register(ResourceLocation name, IBlastFactory blastFactory)
    {
        int assignedID;

        if (name_to_id.containsKey(name))
        {
            assignedID = name_to_id.get(nextID);
        }
        else
        {
            //Increase ID until we find a slot
            while (id_to_name.containsKey(nextID))
            {
                nextID++;
            }
            assignedID = nextID;
        }

        //Set into registry
        setReg(name, assignedID);

        //Store factory
        explosiveData.put(name, new ExplosiveData(name, assignedID).blastFactory(blastFactory));

        //Return data
        return explosiveData.get(name);
    }

    @Override
    public void enableContent(ResourceLocation contentID, ResourceLocation explosiveID)
    {
        final ExplosiveData data = explosiveData.get(explosiveID);
        if (data != null)
        {
            //Add to enable list on object itself
            data.enabledContent.add(contentID);

            //Add ID to set of ids
            if (contentToIds.get(contentID) == null)
            {
                contentToIds.put(contentID, new HashSet());
            }
            contentToIds.get(contentID).add(data.id);
        }
    }

    @Override
    public IExplosiveData getExplosiveData(ResourceLocation name)
    {
        return explosiveData.get(name);
    }

    @Override
    public IExplosiveData getExplosiveData(int id)
    {
        final ResourceLocation name = id_to_name.get(id);
        if (name != null)
        {
            return explosiveData.get(name);
        }
        return null;
    }

    @Override
    public Set<Integer> getExplosivesEnabledForContent(ResourceLocation contentID)
    {
        return contentToIds.get(contentID);
    }

    protected void setReg(ResourceLocation name, int id)
    {
        id_to_name.put(id, name);
        name_to_id.put(name, id);
    }

    public void loadReg(final File file)
    {
        saveFile = file;

        ICBMClassic.logger().info("ExplosiveRegistry: loading registry save, File: " + file);

        if (file.exists())
        {
            try (FileReader reader = new FileReader(file))
            {
                JsonReader jsonReader = new JsonReader(reader);
                JsonElement element = Streams.parse(jsonReader);
                if (element.isJsonObject())
                {
                    loadReg(element.getAsJsonObject());
                }
                else
                {
                    throw new RuntimeException("ExplosiveRegistry: Failed to load registry save file as JSON object, File: " + file);
                }
            } catch (Exception e)
            {
                throw new RuntimeException("ExplosiveRegistry: Unexpected error reading explosive registry save, File: " + file, e);
            }
        }
        else
        {
            ICBMClassic.logger().warn("ExplosiveRegistry: No save found for registry. This can be ignored if first run with the mod. File: " + file);
        }
    }


    protected void loadReg(JsonObject saveData)
    {
        for (Map.Entry<String, JsonElement> entry : saveData.entrySet())
        {
            if (entry.getValue().isJsonPrimitive())
            {
                //Get data
                String key = entry.getKey().trim().toLowerCase();
                int id = entry.getValue().getAsInt();

                //Duplicate check, values in map take priority over saved values
                checkDuplicate(id, key);

                //Store value
                setReg(new ResourceLocation(key), id);
            }
        }
    }

    protected void checkDuplicate(int id, String key)
    {
        if (id_to_name.containsKey(id) && id_to_name.get(id) != null && !key.equals(id_to_name.get(id).toString()))
        {
            throw new RuntimeException("ExplosiveRegistry: Duplicate registry detected with mismatching registry names for ID[" + id + "]. "
                    + "  Current: " + id_to_name.get(id)
                    + "  New: " + key);
        }
    }

    public void saveReg()
    {
        //Save data
        final JsonObject saveData = new JsonObject();
        saveReg(saveData);


        //Convert to JSON string
        final Gson gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();

        final String json = gson.toJson(saveData);

        //Ensure folders exist
        if (!saveFile.getParentFile().exists())
        {
            saveFile.getParentFile().mkdirs();
        }

        //Write string to file
        try (FileWriter fileWriter = new FileWriter(saveFile))
        {
            fileWriter.write(json);
        } catch (Exception e)
        {
            ICBMClassic.logger().error("ExplosiveRegistry: Failed to save registry to file, File: " + saveFile);
            e.printStackTrace();
        }
    }

    protected void saveReg(JsonObject saveData)
    {
        for (Map.Entry<ResourceLocation, Integer> entry : name_to_id.entrySet())
        {
            saveData.addProperty(entry.getKey().toString(), entry.getValue());
        }
    }
}
