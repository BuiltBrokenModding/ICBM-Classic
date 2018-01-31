package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.ICBMClassic;
import icbm.classic.client.render.entity.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.tile.BlockExplosive;
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
import icbm.classic.prefab.BlockICBM;
import icbm.classic.prefab.EnumTier;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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

        //Explosives
        registerExBlockRenders();
        registerGrenadeRenders();
        registerCartRenders();
        registerMissileRenders();

        //Machines
        newBlockModel(ICBMClassic.blockEmpTower, 0, "inventory", "");
        newBlockModel(ICBMClassic.blockRadarStation, 0, "inventory", "");
        registerLauncherBase();

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

        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, manager -> new RenderEntityExplosive(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBlock.class, manager -> new RenderEntityBlock(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosion.class, manager -> new RenderExplosion(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, manager -> new RenderGrenade(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBeam.class, manager -> new RenderLightBeam(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityFragments.class, manager -> new RenderShrapnel(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerSeat.class, manager -> new RenderSeat(manager));
        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, manager -> RenderMissile.INSTANCE = new RenderMissile(manager));

        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new TESREMPTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRadarStation.class, new TESRRadarStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherFrame.class, new TESRLauncherFrame());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new TESRLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherScreen.class, new TESRLauncherScreen());
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

    protected void registerLauncherBase()
    {
        final String resourcePath = ICBMClassic.blockLaunchBase.getRegistryName().toString();

        ModelLoader.setCustomStateMapper(ICBMClassic.blockLaunchBase, new DefaultStateMapper()
        {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state)
            {
                return new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties()));
            }
        });
        for (EnumTier tier : new EnumTier[]{EnumTier.ONE, EnumTier.TWO, EnumTier.THREE})
        {
            IBlockState state = ICBMClassic.blockLaunchBase.getDefaultState().withProperty(BlockICBM.TIER_PROP, tier).withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
            String properties_string = getPropertyString(state.getProperties());
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(ICBMClassic.blockLaunchBase), tier.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
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
                String properties_string = "explosive=" + ex.getName();
                ModelLoader.setCustomModelResourceLocation(ICBMClassic.itemMissile, ex.ordinal(), new ModelResourceLocation(resourcePath, properties_string));
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

    public static void registerWithMapper(Block block)
    {
        if (block != null)
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

            NonNullList<ItemStack> subBlocks = NonNullList.create();
            block.getSubBlocks(null, subBlocks);

            for (ItemStack stack : subBlocks)
            {
                IBlockState state = block.getStateFromMeta(stack.getMetadata()); //TODO check if works correctly with the state system
                ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), stack.getMetadata(), new ModelResourceLocation(resourcePath, getPropertyString(state.getProperties())));
            }
        }
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
