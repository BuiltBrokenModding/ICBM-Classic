package com.builtbroken.mc.framework.mod;

import com.builtbroken.mc.framework.mod.loadable.LoadableHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * Default layout for a mod class to make it easier to keep mod.class
 * in the same general design and do the same general actions.
 * <p>
 * You will still need to place @Mod at the top of the class, create your own proxies,
 * and do other tasks that can't be abstracted out due to @Annotations
 *
 * @Mod
 * @Instance
 * @SidedProxy
 * @EventHandler
 * @Mod.Metadata
 * @ModstatInfo Created by robert on 12/7/2014.
 */
public abstract class AbstractMod implements IMod
{
    /** Loader handler for proxies and loadable objects */
    protected LoadableHandler loader;
    /** Info or error logger */
    protected Logger logger;
    /** Custom path to config file */
    protected String configPath;
    /** Configuration file */
    private Configuration config;
    /** Toggle to stop pre-init from firing in case extra handling needs to be done */
    protected boolean fireProxyPreInit = true;

    private final String domain;

    public String modIssueTracker;

    /**
     * @param domain - mod id uses to register textures with, etc
     */
    public AbstractMod(String domain)
    {
        this.domain = domain;
        loader = new LoadableHandler();
        logger = LogManager.getLogger(domain);
    }

    public AbstractMod(String domain, String configName)
    {
        this(domain);
        this.configPath = configName + ".cfg";
    }

    public void preInit(FMLPreInitializationEvent event)
    {
        NetworkRegistry.INSTANCE.registerGuiHandler(this, getProxy());

        //Handle configs
        if (this.getClass().toString().contains("com.builtbroken"))
        {
            if (configPath == null || configPath.isEmpty())
            {
                config = new Configuration(new File(event.getModConfigurationDirectory(), "bbm/" + event.getSuggestedConfigurationFile().getName()));
            }
            else
            {
                config = new Configuration(new File(event.getModConfigurationDirectory(), "bbm/" + configPath));
            }
        }
        else
        {
            if (configPath == null || configPath.isEmpty())
            {
                config = new Configuration(event.getSuggestedConfigurationFile());
            }
            else
            {
                config = new Configuration(new File(event.getModConfigurationDirectory(), configPath));
            }
        }
        getConfig().load();

        //Load default handlers
        loader.applyModule(getProxy());

        //Call nub friendly loader methods
        loadHandlers(loader);

        //Fire post load methods
        if (fireProxyPreInit)
        {
            loader.preInit();
        }
    }

    public void init(FMLInitializationEvent event)
    {

        //Fire post load methods
        loader.init();
    }

    public void postInit(FMLPostInitializationEvent event)
    {
        //Fire post load methods
        loader.postInit();

        //Close save file
        getConfig().save();
    }

    public void loadComplete(FMLLoadCompleteEvent event)
    {
        loader.loadComplete();
    }

    /**
     * Load event handlers, recipe handlers, etc
     */
    public void loadHandlers(LoadableHandler loader)
    {

    }

    public Configuration getConfig()
    {
        return config;
    }

    public Logger logger()
    {
        return this.logger;
    }

    public abstract AbstractProxy getProxy();


    @Override
    public final String getPrefix()
    {
        return domain + ":";
    }

    @Override
    public final String getDomain()
    {
        return domain;
    }
}
