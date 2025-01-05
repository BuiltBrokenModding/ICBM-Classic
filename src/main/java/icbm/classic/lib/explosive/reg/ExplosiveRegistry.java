package icbm.classic.lib.explosive.reg;

import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.api.reg.IExplosiveRegistry;
import net.minecraft.util.ResourceLocation;

import java.util.*;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/4/19.
 */
public class ExplosiveRegistry implements IExplosiveRegistry
{

    //Map of explosion id to data
    public final HashMap<ResourceLocation, IExplosiveData> explosiveData = new HashMap();

    //Map of id to name, and its reverse
    public final Map<Integer, ResourceLocation> id_to_name = new HashMap();
    public final Map<ResourceLocation, Integer> name_to_id = new HashMap();


    //Ids
    private int nextID = 0;

    private boolean locked = false;
    private boolean allExplosivesLocked = false;
    private boolean lockNewContentTypes = false;

    @Override
    public IExplosiveData register(ResourceLocation name, IBlastFactory blastFactory)
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
        explosiveData.put(name, new ExplosiveData(name, blastFactory));

        //Return data
        return explosiveData.get(name);
    }

    public void lockNewExplosives()
    {
        if (!allExplosivesLocked)
        {
            allExplosivesLocked = true;
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
    public IExplosiveData getExplosiveData(ResourceLocation name, boolean allowNull)
    {
        final IExplosiveData data = explosiveData.get(name);
        if(data == null && !allowNull) {
            return ICBMExplosives.CONDENSED;
        }
        return data;
    }

    protected void setReg(ResourceLocation name, int id)
    {
        id_to_name.put(id, name);
        name_to_id.put(name, id);
    }
}
