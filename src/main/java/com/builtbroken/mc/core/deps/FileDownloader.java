package com.builtbroken.mc.core.deps;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Handles downloading
 * Created by Dark on 7/29/2015.
 */
public class FileDownloader
{
    public static void downloadDeps(Dep... deps)
    {
        for (Dep dep : deps)
        {
            downloadDep(dep);
        }
    }

    public static void downloadDep(Dep dep)
    {
        URL url = dep.getURL();
        if (url != null)
        {
            downloadFromURL(url, dep.getOutputFolderPath(), dep.getFileName());
        }
    }

    public static void downloadFromURL(URL in, String out, String fileName)
    {
        try
        {
            Path outPath = Paths.get(out + "/" + fileName);
            if (!outPath.toFile().exists())
            {
                Files.copy(in.openStream(), outPath, StandardCopyOption.REPLACE_EXISTING);
            }

        } catch (IOException ex)
        {
            ex.printStackTrace();
        }
    }
}
