package icbm.classic.client;

import icbm.classic.ICBMClassic;
import icbm.classic.api.EnumTier;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.render.entity.*;
import icbm.classic.content.blocks.emptower.TESREMPTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.blocks.launcher.base.TESRLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.blocks.launcher.frame.TESRLauncherFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.TESRLauncherScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TESRRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.entity.mobs.*;
import icbm.classic.content.items.ItemCrafting;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.prefab.tile.BlockICBM;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMClassic.DOMAIN)
public class ClientReg
{

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(ICBMClassic.DOMAIN);

        //Glass
        newBlockModel(BlockReg.blockReinforcedGlass, 0, "inventory", "");
        newBlockModel(BlockReg.blockGlassPlate, 0, "inventory", "");
        newBlockModel(BlockReg.blockGlassButton, 0, "inventory", "");

        //Spikes
        newBlockModel(BlockReg.blockSpikes, 0, "inventory", "");
        newBlockModel(BlockReg.blockSpikes, 1, "inventory", "_poison");
        newBlockModel(BlockReg.blockSpikes, 2, "inventory", "_fire");

        //Concrete
        newBlockModel(BlockReg.blockConcrete, 0, "inventory", "");
        newBlockModel(BlockReg.blockConcrete, 1, "inventory", "_compact");
        newBlockModel(BlockReg.blockConcrete, 2, "inventory", "_reinforced");

        //Battery
        newBlockModel(BlockReg.blockBattery, 0, "inventory", "");

        newBlockModel(BlockReg.blockCruiseLauncher, 0, "inventory", "");

        //Explosives
        registerExBlockRenders();
        registerGrenadeRenders();
        registerCartRenders();
        registerMissileRenders();

        //Machines
        newBlockModel(BlockReg.blockEmpTower, 0, "inventory", "");
        newBlockModel(BlockReg.blockRadarStation, 0, "inventory", "");
        newBlockModel(BlockReg.blockBattery, 0, "inventory", "");

        registerLauncherPart(BlockReg.blockLaunchBase);
        registerLauncherPart(BlockReg.blockLaunchSupport);
        registerLauncherPart(BlockReg.blockLaunchScreen);

        registerMultiBlockRenders();

        //items
        newItemModel(ItemReg.itemPoisonPowder, 0, "inventory", "");
        newItemModel(ItemReg.itemSulfurDust, 0, "inventory", "");
        newItemModel(ItemReg.itemSaltpeterDust, 0, "inventory", "");
        newItemModel(ItemReg.itemAntidote, 0, "inventory", "");
        newItemModel(ItemReg.itemSignalDisrupter, 0, "inventory", "");
        newItemModel(ItemReg.itemTracker, 0, "inventory", "");
        newItemModel(ItemReg.itemDefuser, 0, "inventory", "");
        newItemModel(ItemReg.itemRadarGun, 0, "inventory", "");
        newItemModel(ItemReg.itemRemoteDetonator, 0, "inventory", "");
        newItemModel(ItemReg.itemLaserDesignator, 0, "inventory", "");
        newItemModel(ItemReg.itemRocketLauncher, 0, "inventory", "");
        newItemModel(ItemReg.itemBattery, 0, "inventory", "");

        //crafting parts
        registerCraftingRender(ItemReg.itemIngot);
        registerCraftingRender(ItemReg.itemIngotClump);
        registerCraftingRender(ItemReg.itemPlate);
        registerCraftingRender(ItemReg.itemCircuit);
        registerCraftingRender(ItemReg.itemWire);

        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, manager -> new RenderExBlock(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBlock.class, manager -> new RenderEntityBlock(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosion.class, manager -> new RenderExplosion(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, manager -> new RenderGrenade(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBeam.class, manager -> new RenderLightBeam(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFragments.class, manager -> new RenderFragments(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerSeat.class, manager -> new RenderSeat(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, manager -> RenderMissile.INSTANCE = new RenderMissile(manager));

        RenderingRegistry.registerEntityRenderingHandler(EntityXmasSkeleton.class, manager -> new RenderSkeletonXmas(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityXmasSkeletonBoss.class, manager -> new RenderSkeletonXmas(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityXmasSnowman.class, manager -> new RenderSnowmanXmas(manager));

        RenderingRegistry.registerEntityRenderingHandler(EntityXmasZombie.class, manager -> new RenderZombieXmas(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityXmasZombieBoss.class, manager -> new RenderZombieXmas(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityXmasCreeper.class, manager -> new RenderCreeperXmas(manager));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new TESREMPTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRadarStation.class, new TESRRadarStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherFrame.class, new TESRLauncherFrame());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new TESRLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherScreen.class, new TESRLauncherScreen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCruiseLauncher.class, new TESRCruiseLauncher());
    }

    protected static void registerMultiBlockRenders()
    {
        //Disable rendering of the block, Fixes JSON errors as well
        ModelLoader.setCustomStateMapper(BlockReg.multiBlock, block -> Collections.emptyMap());
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.multiBlock));
    }

    protected static void registerExBlockRenders()
    {
        final String resourcePath = BlockReg.blockExplosive.getRegistryName().toString();

        ModelLoader.setCustomStateMapper(BlockReg.blockExplosive, new DefaultStateMapper()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
            }
        });
        for (IExplosiveData data : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosives())
        {
            IBlockState state = BlockReg.blockExplosive.getDefaultState().withProperty(BlockExplosive.EX_PROP, data).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
            String properties_string = getPropertyString(state.getProperties());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(BlockReg.blockExplosive), data.getRegistryID(), new ModelResourceLocation(resourcePath, properties_string));

        }
    }

    protected static void registerLauncherPart(Block block)
    {
        final String resourcePath = block.getRegistryName().toString();

        ModelLoader.setCustomStateMapper(block, new DefaultStateMapper()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
            }
        });
        for (EnumTier tier : new EnumTier[]{EnumTier.ONE, EnumTier.TWO, EnumTier.THREE})
        {
            IBlockState state = block.getDefaultState().withProperty(BlockICBM.TIER_PROP, tier).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
            String properties_string = getPropertyString(state.getProperties());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), tier.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
        }
    }

    protected static void registerGrenadeRenders()
    {
        final String resourcePath = ItemReg.itemGrenade.getRegistryName().toString();
        for (IExplosiveData data : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosives())
        {
            String properties_string = "explosive=" + data.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(ItemReg.itemGrenade, data.getRegistryID(), new ModelResourceLocation(resourcePath, properties_string));

        }
    }

    protected static void registerCartRenders()
    {
        final String resourcePath = ItemReg.itemBombCart.getRegistryName().toString();
        for (IExplosiveData data : ICBMClassicAPI.EX_MINECRT_REGISTRY.getExplosives())
        {
            String properties_string = "explosive=" + data.getRegistryName();
            ModelLoader.setCustomModelResourceLocation(ItemReg.itemBombCart, data.getRegistryID(), new ModelResourceLocation(resourcePath, properties_string));
        }
    }

    protected static void registerMissileRenders()
    {
        final String resourcePath = ItemReg.itemMissile.getRegistryName().toString();
        for (IExplosiveData data : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosives())
        {
            ModelLoader.setCustomModelResourceLocation(ItemReg.itemMissile, data.getRegistryID(), new ModelResourceLocation(resourcePath + "_" + (data.getTier().ordinal() + 1), "explosive=" + data.getRegistryName()));
        }

        //TODO register missile models that are not explosive based
        // ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemMissile, ex.ordinal(), new ModelResourceLocation(resourcePath, "explosive=" + ex.getName()));
        //
    }

    protected static void registerCraftingRender(ItemCrafting itemCrafting)
    {
        //Most crafting items can be disabled, so null check is needed
        if (itemCrafting != null)
        {
            final String resourcePath = itemCrafting.getRegistryName().toString();
            for (int i = 0; i < itemCrafting.subItems.length; i++)
            {
                String subItem = itemCrafting.subItems[i];
                ModelLoader.setCustomModelResourceLocation(itemCrafting, i, new ModelResourceLocation(resourcePath, "name=" + subItem));
            }
        }
    }

    protected static void newBlockModel(Block block, int meta, String varient, String sub)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }

    protected static void newItemModel(Item item, int meta, String varient, String sub)
    {
        ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName() + sub, varient));
    }

    public static String getPropertyString(Map<IProperty<?>, Comparable<?>> values, String... extrasArgs)
    {
        StringBuilder stringbuilder = new StringBuilder();

        for (Map.Entry<IProperty<?>, Comparable<?>> entry : values.entrySet())
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(",");
            }

            IProperty<?> iproperty = entry.getKey();
            stringbuilder.append(iproperty.getName());
            stringbuilder.append("=");
            stringbuilder.append(getPropertyName(iproperty, entry.getValue()));
        }


        if (stringbuilder.length() == 0)
        {
            stringbuilder.append("inventory");
        }

        for (String args : extrasArgs)
        {
            if (stringbuilder.length() != 0)
            {
                stringbuilder.append(",");
            }
            stringbuilder.append(args);
        }

        return stringbuilder.toString();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> String getPropertyName(IProperty<T> property, Comparable<?> comparable)
    {
        return property.getName((T) comparable);
    }
}
