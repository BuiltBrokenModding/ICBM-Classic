package com.builtbroken.mc.core;

import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

/**
 * Loads a lot of content before all other mods have even started loading. Handles downloading
 * of missing files, and setup of file structures.
 * Created by Dark on 9/7/2015.
 */
public class EnginePreloader extends DummyModContainer
{
    public static final String version = "0.0.1";
    private static final ModMetadata md;

    static
    {
        md = new ModMetadata();
        md.modId = "voltzenginepreloader";
        md.name = "Voltz Engine Preloader";
        md.version = version;
    }

    public EnginePreloader()
    {
        super(md);
    }

    @Override
    public boolean registerBus(EventBus bus, LoadController controller)
    {
        return false;
    }
}
