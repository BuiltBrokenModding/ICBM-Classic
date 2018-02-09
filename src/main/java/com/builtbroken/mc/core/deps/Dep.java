package com.builtbroken.mc.core.deps;

import net.minecraftforge.fml.relauncher.FMLInjectionData;

import java.io.File;
import java.net.URL;

/**
 * Prefab for new dependency downloads
 * Created by Dark on 7/29/2015.
 */
public abstract class Dep
{
    /** Download path for the file */
    public abstract URL getURL();

    /** Folder to download the file to inside the MC folder */
    public String getOutputFolderPath()
    {
        //MC_Folder/mods/MC_Version ex .minecraft/mods/1.7.10
        return ((File) FMLInjectionData.data()[6]).getAbsolutePath() + "/mods/" + FMLInjectionData.data()[4];
    }

    /** Main peace of the name of the file excluding the version # */
    public abstract String getGenericFileName();

    /** Gets exact file name for this version of the dep */
    public abstract String getFileName();

    public boolean isNewerVersion(String fileName)
    {
        return false;
    }
}
