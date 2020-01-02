package icbm.classic.lib.explosive.reg;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.internal.Streams;
import com.google.gson.stream.JsonReader;
import icbm.classic.ICBMClassic;
import icbm.classic.api.EnumTier;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.IExplosiveRegistry;
import icbm.classic.api.reg.content.IExplosiveContentRegistry;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/4/19.
 */
public class ExplosiveRegistry implements IExplosiveRegistry
{

    //Map of explosion id to data
    public final HashMap<ResourceLocation, IExplosiveData> explosiveData = new HashMap();

    //Map of id to name, and its reverse
    public final Map<Integer, ResourceLocation> id_to_name = new HashMap();
    public final Map<ResourceLocation, Integer> name_to_id = new HashMap();

    //Content types to set of IDs enabled for content
    public final Map<ResourceLocation, IExplosiveContentRegistry> contentRegistry = new HashMap();


    //Ids
    private int nextID = 0;

    //Save file
    private File saveFile;

    private boolean locked = false;
    private boolean lockForce = false;
    private boolean allExplosivesLocked = false;
    private boolean lockNewContentTypes = false;

    private ImmutableSet<IExplosiveData> allExplosives;

    @Override
    public IExplosiveData register(ResourceLocation name, EnumTier tier, IBlastFactory blastFactory)
    {
        if (locked)
        {
            throw new RuntimeException("ExplosiveRegistry: new explosives can not be registered after registry phase");
        }

        if (name.toString().contains("_"))
        {
            throw new IllegalArgumentException("ExplosiveRegistry: '" + name + "' can not contain underscores");
        }

        int assignedID;

        if (name_to_id.containsKey(name))
        {
            assignedID = name_to_id.get(name);
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
        explosiveData.put(name, new ExplosiveData(name, assignedID, tier).blastFactory(blastFactory));

        //Return data
        return explosiveData.get(name);
    }

    public void lockNewExplosives()
    {
        if (!allExplosivesLocked)
        {
            allExplosivesLocked = true;
            allExplosives = explosiveData.values().stream().filter(e -> e != null).collect(ImmutableSet.toImmutableSet());
        }
        else
        {
            throw new RuntimeException(this + ": New explosives were locked twice!");
        }
    }

    public void completeLock()
    {
        if (!locked)
        {
            locked = true;
            getContentRegistries().forEach(reg -> reg.lockRegistry());
        }
        else
        {
            throw new RuntimeException(this + ": Registries were locked twice!");
        }
    }

    public void lockNewContentTypes()
    {
        if (!lockNewContentTypes)
        {
            lockNewContentTypes = true;
        }
        else
        {
            throw new RuntimeException(this + ": New content types were locked twice!");
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
    public Collection<IExplosiveContentRegistry> getContentRegistries()
    {
        return ImmutableList.copyOf(contentRegistry.values()); //TODO make immutable set
    }

    @Override
    public IExplosiveContentRegistry getContentRegistry(ResourceLocation contentID)
    {
        return contentRegistry.get(contentID);
    }

    @Override
    public void registerContentRegistry(ResourceLocation name, IExplosiveContentRegistry registry)
    {
        //Check lock
        if (locked || lockNewContentTypes)
        {
            throw new RuntimeException("ExplosiveRegistry: new explosive content types can not be registered after init phase");
        }

        //Check input data
        if (registry == null)
        {
            throw new IllegalArgumentException("ExplosiveRegistry: content type should not be null, Name: " + name);
        }

        if (name == null)
        {
            throw new IllegalArgumentException("ExplosiveRegistry: name is required for content registry type, " + registry);
        }

        //Check duplicate
        if (contentRegistry.containsKey(name) && contentRegistry.get(name) != null)
        {
            throw new RuntimeException("ExplosiveRegistry: duplicate content type detected for '" + name + "' Prev: " + contentRegistry.get(name) + "  New: " + registry);
        }

        //Insert
        contentRegistry.put(name, registry);
    }

    @Override
    public Set<IExplosiveData> getExplosives()
    {
        return allExplosives;
    }

    protected void setReg(ResourceLocation name, int id)
    {
        id_to_name.put(id, name);
        name_to_id.put(name, id);
    }

    public void forceID(ResourceLocation name, int id)
    {
        if (!lockForce)
        {
            setReg(name, id);
        }
    }

    public void lockForce()
    {
        lockForce = true;
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
            }
            catch (Exception e)
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
        }
        catch (Exception e)
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
