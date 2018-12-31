package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.ICBMClassic;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.client.render.entity.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.mobs.EntityXmasSkeleton;
import icbm.classic.content.entity.mobs.EntityXmasSkeletonBoss;
import icbm.classic.content.machines.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.content.machines.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.missile.EntityMissile;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.handlers.missiles.Missile;
import icbm.classic.content.explosive.tile.BlockExplosive;
import icbm.classic.content.items.ItemCrafting;
import icbm.classic.content.machines.emptower.TESREMPTower;
import icbm.classic.content.machines.emptower.TileEMPTower;
import icbm.classic.content.machines.launcher.base.TESRLauncherBase;
import icbm.classic.content.machines.launcher.base.TileLauncherBase;
import icbm.classic.content.machines.launcher.frame.TESRLauncherFrame;
import icbm.classic.content.machines.launcher.frame.TileLauncherFrame;
import icbm.classic.content.machines.launcher.screen.TESRLauncherScreen;
import icbm.classic.content.machines.launcher.screen.TileLauncherScreen;
import icbm.classic.content.machines.radarstation.TESRRadarStation;
import icbm.classic.content.machines.radarstation.TileRadarStation;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.BlockICBM;
import icbm.classic.prefab.tile.EnumTier;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    @Override
    public void doLoadModels()
    {
        OBJLoader.INSTANCE.addDomain(ICBMClassic.DOMAIN);

        //Glass
        newBlockModel(ICBMClassic.blockReinforcedGlass, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockGlassPlate, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockGlassButton, 0, "inventory", "");

        //Spikes
        newBlockModel(ICBMClassic.blockSpikes, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockSpikes, 1, "inventory", "_poison");
        newBlockModel(ICBMClassic.blockSpikes, 2, "inventory", "_fire");

        //Concrete
        newBlockModel(ICBMClassic.blockConcrete, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockConcrete, 1, "inventory", "_compact");
        newBlockModel(ICBMClassic.blockConcrete, 2, "inventory", "_reinforced");

        //Battery
        newBlockModel(ICBMClassic.blockBattery, 0, "inventory", "");

        newBlockModel(ICBMClassic.blockCruiseLauncher, 0, "inventory", "");

        //Explosives
        registerExBlockRenders();
        registerGrenadeRenders();
        registerCartRenders();
        registerMissileRenders();

        //Machines
        newBlockModel(ICBMClassic.blockEmpTower, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockRadarStation, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockBattery, 0, "inventory", "");

        registerLauncherPart(ICBMClassic.blockLaunchBase);
        registerLauncherPart(ICBMClassic.blockLaunchSupport);
        registerLauncherPart(ICBMClassic.blockLaunchScreen);

        registerMultiBlockRenders();

        //items
        newItemModel(ICBMClassic.itemPoisonPowder, 0, "inventory", "");
        newItemModel(ICBMClassic.itemSulfurDust, 0, "inventory", "");
        newItemModel(ICBMClassic.itemSaltpeterDust, 0, "inventory", "");
        newItemModel(ICBMClassic.itemAntidote, 0, "inventory", "");
        newItemModel(ICBMClassic.itemSignalDisrupter, 0, "inventory", "");
        newItemModel(ICBMClassic.itemTracker, 0, "inventory", "");
        newItemModel(ICBMClassic.itemDefuser, 0, "inventory", "");
        newItemModel(ICBMClassic.itemRadarGun, 0, "inventory", "");
        newItemModel(ICBMClassic.itemRemoteDetonator, 0, "inventory", "");
        newItemModel(ICBMClassic.itemLaserDesignator, 0, "inventory", "");
        newItemModel(ICBMClassic.itemRocketLauncher, 0, "inventory", "");
        newItemModel(ICBMClassic.itemBattery, 0, "inventory", "");

        //crafting parts
        registerCraftingRender(ICBMClassic.itemIngot);
        registerCraftingRender(ICBMClassic.itemIngotClump);
        registerCraftingRender(ICBMClassic.itemPlate);
        registerCraftingRender(ICBMClassic.itemCircuit);
        registerCraftingRender(ICBMClassic.itemWire);

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

        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new TESREMPTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRadarStation.class, new TESRRadarStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherFrame.class, new TESRLauncherFrame());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new TESRLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherScreen.class, new TESRLauncherScreen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCruiseLauncher.class, new TESRCruiseLauncher());
    }

    protected void registerMultiBlockRenders()
    {
        //Disable rendering of the block, Fixes JSON errors as well
        ModelLoader.setCustomStateMapper(ICBMClassic.multiBlock, block -> Collections.emptyMap());
        ModelBakery.registerItemVariants(Item.getItemFromBlock(ICBMClassic.multiBlock));
    }

    protected void registerExBlockRenders()
    {
        final String resourcePath = ICBMClassic.blockExplosive.getRegistryName().toString();

        ModelLoader.setCustomStateMapper(ICBMClassic.blockExplosive, new DefaultStateMapper()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
            }
        });
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasBlockForm())
            {
                IBlockState state = ICBMClassic.blockExplosive.getDefaultState().withProperty(BlockExplosive.EX_PROP, ex).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
                String properties_string = getPropertyString(state.getProperties());
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ICBMClassic.blockExplosive), ex.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
            }
        }
    }

    protected void registerLauncherPart(Block block)
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

    protected void registerGrenadeRenders()
    {
        final String resourcePath = ICBMClassic.itemGrenade.getRegistryName().toString();
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasGrenadeForm())
            {
                String properties_string = "explosive=" + ex.getName();
                ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemGrenade, ex.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
            }
        }
    }

    protected void registerCartRenders()
    {
        final String resourcePath = ICBMClassic.itemBombCart.getRegistryName().toString();
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasMinecartForm())
            {
                String properties_string = "explosive=" + ex.getName();
                ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemBombCart, ex.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
            }
        }
    }

    protected void registerMissileRenders()
    {
        final String resourcePath = ICBMClassic.itemMissile.getRegistryName().toString();
        for (Explosives ex : Explosives.values())
        {
            if (ex.handler.hasMissileForm())
            {
                if (ex.handler instanceof Missile)
                {
                    ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemMissile, ex.ordinal(), new ModelResourceLocation(resourcePath, "explosive=" + ex.getName()));
                }
                else
                {
                    ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemMissile, ex.ordinal(), new ModelResourceLocation(resourcePath + "_" + (ex.handler.getTier().ordinal() + 1), "explosive=" + ex.getName()));
                }
            }
        }
    }

    protected void registerCraftingRender(ItemCrafting itemCrafting)
    {
        //Most crafting items can be disabled, so null check is needed
        if(itemCrafting != null)
        {
            final String resourcePath = itemCrafting.getRegistryName().toString();
            for (int i = 0; i < itemCrafting.subItems.length; i++)
            {
                String subItem = itemCrafting.subItems[i];
                ModelLoader.setCustomModelResourceLocation(itemCrafting, i, new ModelResourceLocation(resourcePath, "name=" + subItem));
            }
        }
    }

    protected void newBlockModel(Block block, int meta, String varient, String sub)
    {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }

    protected void newItemModel(Item item, int meta, String varient, String sub)
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

    @Override
    public void spawnSmoke(World world, Pos position, double v, double v1, double v2, float red, float green, float blue, float scale, int ticksToLive)
    {
        if (world != null)
        {
            ParticleSmokeICBM particleSmokeICBM = new ParticleSmokeICBM(world, position, v, v1, v2, scale);
            particleSmokeICBM.setColor(red, green, blue, true);
            particleSmokeICBM.setAge(ticksToLive);
            Minecraft.getMinecraft().effectRenderer.addEffect(particleSmokeICBM);
        }
    }
}
