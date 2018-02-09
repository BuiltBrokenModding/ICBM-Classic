package com.builtbroken.mc.framework.mod.loadable;

import net.minecraftforge.fml.common.Loader;

import java.util.*;

/**
 * The Object that handles the load calls or submods of the mod
 * <p>
 * to have the submodules work, You must register them in this class, Adding support for a submodule
 * includes only acquiring its class and throwing it in the registerModules method, this is handled
 * as such to allow turning these modules off by configuration, and disable them if the parent mod
 * is not loaded (Integration modules with other mods)
 * <p>
 * Replace @Mod annotation with this system and it allows better handling in the end of it
 *
 * @author tgame14, Calclavia
 * @since 23/02/14
 */
public final class LoadableHandler
{
    /** Map of loadable to pair(HasLoadedPreInit, HasLoadedInit) */
    private HashMap<ILoadable, List<LoadPhase>> loadables = new HashMap();
    /** Current phase of the loader. Doesn't always match current phase of MC. */
    private LoadPhase phase = LoadPhase.PRELAUNCH;

    private List<ILoadable> waitingToBeAdded = new ArrayList();

    private boolean running = false;

    public void applyModule(Class<?> clazz)
    {
        applyModule(clazz, true);
    }

    /**
     * Applies a specific ILoadable module to be loaded.
     */
    public void applyModule(Class<?> clazz, boolean load)
    {
        if (load)
        {
            if (clazz.getAnnotation(LoadWithMod.class) != null)
            {
                String id = clazz.getAnnotation(LoadWithMod.class).mod_id();
                if (!Loader.isModLoaded(id))
                {
                    return;
                }
            }
            try
            {
                Object module = clazz.newInstance();

                if (module instanceof ILoadableProxy)
                {
                    ILoadableProxy subProxy = (ILoadableProxy) module;

                    if (subProxy.shouldLoad())
                    {
                        loadables.put(subProxy, new ArrayList());
                    }
                }
                else if (module instanceof ILoadable)
                {
                    loadables.put((ILoadable) module, new ArrayList());
                }
            }
            catch (InstantiationException | IllegalAccessException e)
            {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Call for modules late or as already existing modules, DO NOT CALL FOR REGISTERED Proxies!
     */
    public void applyModule(ILoadable module)
    {
        if (phase == LoadPhase.DONE || phase == LoadPhase.LOAD_COMPLETE && running)
        {
            throw new RuntimeException("Module '" + module + "' was added to late into loading phase to be added!!! To prevent damage to the game and saves the game will be closed.");
        }
        synchronized (waitingToBeAdded)
        {
            if (running)
            {
                waitingToBeAdded.add(module);
            }
            else
            {
                loadables.put(module, new ArrayList());
            }
        }
    }

    public void preInit()
    {
        load(LoadPhase.PREINIT);
    }

    public void init()
    {
        load(LoadPhase.INIT);
    }

    public void postInit()
    {
        load(LoadPhase.POSTINIT);
    }

    public void loadComplete()
    {
        load(LoadPhase.LOAD_COMPLETE);
        phase = LoadPhase.DONE;
    }

    protected void load(LoadPhase untilPhase)
    {
        running = true;
        phase = untilPhase;

        if (!waitingToBeAdded.isEmpty())
        {
            for (ILoadable l : waitingToBeAdded)
            {
                loadables.put(l, new ArrayList());
            }
            waitingToBeAdded.clear();
        }

        Iterator<Map.Entry<ILoadable, List<LoadPhase>>> it = loadables.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<ILoadable, List<LoadPhase>> proxy = it.next();

            //Pre init and up
            if (untilPhase.ordinal() >= LoadPhase.PREINIT.ordinal())
            {
                if (!proxy.getValue().contains(LoadPhase.PREINIT))
                {
                    proxy.getValue().add(LoadPhase.PREINIT);
                    proxy.getKey().preInit();
                }

                //Init and up
                if (untilPhase.ordinal() >= LoadPhase.INIT.ordinal())
                {
                    if (!proxy.getValue().contains(LoadPhase.INIT))
                    {
                        proxy.getValue().add(LoadPhase.INIT);
                        proxy.getKey().init();
                    }

                    //Post init and up
                    if (untilPhase.ordinal() >= LoadPhase.POSTINIT.ordinal())
                    {
                        if (!proxy.getValue().contains(LoadPhase.POSTINIT))
                        {
                            proxy.getValue().add(LoadPhase.POSTINIT);
                            proxy.getKey().postInit();
                        }

                        //Last phase
                        if (untilPhase.ordinal() >= LoadPhase.LOAD_COMPLETE.ordinal())
                        {
                            if (!proxy.getValue().contains(LoadPhase.LOAD_COMPLETE))
                            {
                                proxy.getKey().loadComplete();
                                proxy.getValue().add(LoadPhase.LOAD_COMPLETE);
                            }
                        }
                    }
                }
            }
        }

        //If ILoadable(s) were added from inside a loader recall load
        if (!waitingToBeAdded.isEmpty())
        {
            load(untilPhase);
        }

        running = false;
    }

    public enum LoadPhase
    {
        PRELAUNCH,
        PREINIT,
        INIT,
        POSTINIT,
        LOAD_COMPLETE,
        DONE
    }
}
