package com.builtbroken.mc.core;

import com.builtbroken.mc.core.deps.DepDownloader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * Core mod for Voltz Engine handing anything that needs to be done before mods load.
 * Created by Dark on 9/7/2015.
 * -Dfml.coreMods.load=com.builtbroken.mc.core.EngineCoreMod
 */
@IFMLLoadingPlugin.MCVersion()
public class EngineCoreMod implements IFMLLoadingPlugin
{
    /** Grab the mod's main logger, in theory should be the same logger */
    public static final Logger logger = LogManager.getLogger("VoltzEngine");
    public static boolean devMode = false;

    public EngineCoreMod()
    {
        //TODO see if there is a better place to load this as a construct is not designed for downloading
        final boolean notDevMode = System.getProperty("development") == null || !System.getProperty("development").equalsIgnoreCase("true");
        final boolean doDownloads = System.getProperty("disableDepDownloader") == null || !System.getProperty("disableDepDownloader").equalsIgnoreCase("true");
        final boolean enableASM = System.getProperty("enableAsmTemplates") == null || System.getProperty("enableAsmTemplates").equalsIgnoreCase("true");

        devMode = !notDevMode;

        if (notDevMode && doDownloads)
        {
            DepDownloader.load();
        }
        if (enableASM)
        {
            //TemplateManager.load();
        }
    }

    @Override
    public String[] getASMTransformerClass()
    {
        return new String[0];//new String[]{"com.builtbroken.mc.core.asm.ChunkTransformer", "com.builtbroken.mc.core.asm.template.ClassTransformer"};
    }

    @Override
    public String getModContainerClass()
    {
        return "com.builtbroken.mc.core.EnginePreloader";
    }

    @Override
    public String getSetupClass()
    {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data)
    {

    }

    @Override
    public String getAccessTransformerClass()
    {
        return null;
    }
}
