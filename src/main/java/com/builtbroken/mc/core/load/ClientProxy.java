package com.builtbroken.mc.core.load;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * The Voltz Engine client proxy
 */
public class ClientProxy extends CommonProxy
{
    @SubscribeEvent
    public void registerAllModels(ModelRegistryEvent event)
    {
        //ModelLoader.setCustomModelResourceLocation(Engine.itemWrench, 0, new ModelResourceLocation(Engine.itemWrench.getRegistryName(), "inventory"));
    }

    @Override
    public void preInit()
    {
    }
}
