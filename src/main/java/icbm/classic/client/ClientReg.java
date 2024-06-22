package icbm.classic.client;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.mapper.BlockModelMapperExplosive;
import icbm.classic.client.mapper.ItemModelMapperExplosive;
import icbm.classic.client.render.entity.*;
import icbm.classic.client.render.entity.item.RenderAsItem;
import icbm.classic.config.ConfigItems;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.content.blocks.emptower.TESREmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.TileEmpTowerFake;
import icbm.classic.content.blocks.launcher.base.TESRLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.cargo.balloon.EntityBalloon;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.content.cargo.parachute.RenderParachute;
import icbm.classic.content.cluster.bomblet.EntityBombDroplet;
import icbm.classic.content.cluster.bomblet.RenderBombDroplet;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.flyingblock.EntityFlyingBlock;
import icbm.classic.content.entity.flyingblock.RenderEntityBlock;
import icbm.classic.content.items.ItemCrafting;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.colors.ColorHelper;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN, value=Side.CLIENT)
public class ClientReg
{
    private final static Map<IExplosiveData, ModelResourceLocation> grenadeModelMap = new HashMap();
    private final static Map<IExplosiveData, ModelResourceLocation> missileModelMap = new HashMap();
    private final static Map<IExplosiveData, Map<EnumFacing,ModelResourceLocation>> blockModelMap = new HashMap();
    private final static Map<IExplosiveData, ModelResourceLocation> itemBlockModelMap = new HashMap();
    private final static Map<IExplosiveData, ModelResourceLocation> cartModelMap = new HashMap();
    private final static Map<IExplosiveData, ModelResourceLocation> bombletModelMap = new HashMap();

    private static void clearModelCache()
    {
        grenadeModelMap.clear();
        missileModelMap.clear();
        blockModelMap.clear();
        itemBlockModelMap.clear();
        cartModelMap.clear();
    }

    @SubscribeEvent
    public static void registerBlockColor(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if(worldIn != null && pos != null) {
                final TileEntity tile = worldIn.getTileEntity(pos);
                if (tile instanceof TileEMPTower) {
                    //TODO cache as chargePercent(0 to 100 int) -> value
                    int red = (int) Math.floor(Math.cos(((TileEMPTower) tile).getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(((TileEMPTower) tile).getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                } else if (tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null) {
                    int red = (int) Math.floor(Math.cos(((TileEmpTowerFake) tile).getHost().getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(((TileEmpTowerFake) tile).getHost().getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                }
            }
            return 0;
        },  BlockReg.blockEmpTower);
    }

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event)
    {
        OBJLoader.INSTANCE.addDomain(ICBMConstants.DOMAIN);

        //reset
        clearModelCache();

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

        //Radioactive
        newBlockModel(BlockReg.blockRadioactive, 0, "inventory", "_dirt");
        newBlockModel(BlockReg.blockRadioactive, 1, "inventory", "_stone");

        //Explosives
        registerExBlockRenders();
        registerGrenadeRenders();
        registerCartRenders();
        registerMissileRenders();
        registerBombletRenders();

        //Machines
        newBlockModel(BlockReg.blockEmpTower, 0, "inventory_0", "");
        newBlockModel(BlockReg.blockEmpTower, 1, "inventory_1", "");
        newBlockModel(BlockReg.blockRadarStation, 0, "inventory", "");
        newBlockModel(BlockReg.blockLaunchBase, 0, "inventory", "");
        newBlockModel(BlockReg.blockLaunchScreen, 0, "inventory", "");
        newBlockModel(BlockReg.blockLaunchSupport, 0, "inventory", "");
        newBlockModel(BlockReg.blockLaunchConnector, 0, "inventory", "");
        newBlockModel(BlockReg.blockCruiseLauncher, 0, "inventory", "");

        //items
        newItemModel(ItemReg.itemPoisonPowder, 0, "inventory", "");
        newItemModel(ItemReg.itemSulfurDust, 0, "inventory", "");
        newItemModel(ItemReg.itemSaltpeterDust, 0, "inventory", "");
        newItemModel(ItemReg.itemSaltpeterBall, 0, "inventory", "");
        newItemModel(ItemReg.itemAntidote, 0, "inventory", "");
        newItemModel(ItemReg.itemDefuser, 0, "inventory", "");
        newItemModel(ItemReg.itemRadarGun, 0, "inventory", "");
        newItemModel(ItemReg.itemRemoteDetonator, 0, "inventory", "");
        newItemModel(ItemReg.itemLaserDetonator, 0, "inventory", "");
        newItemModel(ItemReg.itemRocketLauncher, 0, "inventory", "");
        newItemModel(ItemReg.itemBallisticLauncher, 0, "inventory", "");
        newItemModel(ItemReg.itemBattery, 0, "inventory", "");

        newItemModel(ItemReg.itemBombletEmpty, 0, "inventory", "");

        newItemModel(ItemReg.itemParachute, 0, "inventory", "");
        newItemModel(ItemReg.itemBalloon, 0, "render=2d", "");
        newItemModel(ItemReg.itemBalloon, 1, "render=3d", ""); // Fake meta version purely for entity renderer
        ModelLoader.setCustomModelResourceLocation(ItemReg.itemSAM, 0, new ModelResourceLocation(ICBMConstants.DOMAIN + ":missiles/surface_to_air", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ItemReg.itemClusterMissile, 0, new ModelResourceLocation(ICBMConstants.DOMAIN + ":missiles/cluster", "inventory"));

        //crafting parts
        if(ConfigItems.ENABLE_CRAFTING_ITEMS)
        {
            if(ConfigItems.ENABLE_INGOTS_ITEMS)
            {
                registerCraftingRender(ItemReg.itemIngot);
                registerCraftingRender(ItemReg.itemIngotClump);
            }

            if(ConfigItems.ENABLE_PLATES_ITEMS)
                registerCraftingRender(ItemReg.itemPlate);

            if(ConfigItems.ENABLE_CIRCUIT_ITEMS)
                registerCraftingRender(ItemReg.itemCircuit);

            if(ConfigItems.ENABLE_WIRES_ITEMS)
                registerCraftingRender(ItemReg.itemWire);
        }

        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, RenderExBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRedmatter.class, RenderRedmatter::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBlock.class, RenderEntityBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosion.class, RenderExplosion::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class,
            (rm) -> new RenderAsItem<EntityGrenade>(rm, EntityGrenade::renderItemStack).setBillboard(true));
        RenderingRegistry.registerEntityRenderingHandler(EntityParachute.class,
                RenderParachute::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class,
            (rm) -> new RenderAsItem<EntityBalloon>(rm, EntityBalloon::getRenderItemStack));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBeam.class, RenderLightBeam::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFragments.class, RenderFragments::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBombDroplet.class, RenderBombDroplet::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerSeat.class, RenderSeat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySmoke.class, RenderSmoke::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, manager -> RenderMissile.INSTANCE = new RenderMissile(manager));


        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new TESRLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCruiseLauncher.class, new TESRCruiseLauncher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new TESREmpTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEmpTowerFake.class, new TESREmpTower());
    }

    protected static void registerExBlockRenders()
    {
        for (IExplosiveData data : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            //Add block state
            final HashMap<EnumFacing,ModelResourceLocation> facingModelMap = new HashMap<>();
            final String resourcePath = data.getRegistryKey().getResourceDomain() + ":explosives/" + data.getRegistryKey().getResourcePath();

            for(EnumFacing facing : EnumFacing.VALUES)
            {
                facingModelMap.put(facing, new ModelResourceLocation(resourcePath, "explosive=" + data.getRegistryKey().toString().replace(":", "_") + ",rotation=" + facing));
            }

            blockModelMap.put(data, facingModelMap);

            //Add item state
            //IBlockState state = BlockReg.blockExplosive.getDefaultState().withProperty(BlockICBM.ROTATION_PROP, EnumFacing.UP);
            // String properties_string = getPropertyString(state.getProperties());
            itemBlockModelMap.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }
        //Block state mapper
        ModelLoader.setCustomStateMapper(BlockReg.blockExplosive, new BlockModelMapperExplosive(blockModelMap, blockModelMap.get(ICBMExplosives.CONDENSED).get(EnumFacing.UP)));
        //Item state mapper
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockReg.blockExplosive), new ItemModelMapperExplosive(itemBlockModelMap, itemBlockModelMap.get(ICBMExplosives.CONDENSED)));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockExplosive), itemBlockModelMap.values()
                .stream()
                .map(mrl -> new ResourceLocation(mrl.getResourceDomain(), mrl.getResourcePath()))
                .collect(Collectors.toList())
                .toArray(new ResourceLocation[itemBlockModelMap.values().size()]));
    }

    protected static void registerGrenadeRenders()
    {
        for (IExplosiveData data : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryKey().getResourceDomain() + ":grenades/" + data.getRegistryKey().getResourcePath();
            grenadeModelMap.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }

        ModelLoader.registerItemVariants(ItemReg.itemGrenade, grenadeModelMap.values()
                .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(ItemReg.itemGrenade, new ItemModelMapperExplosive(grenadeModelMap, grenadeModelMap.get(ICBMExplosives.CONDENSED)));
    }

    protected static void registerBombletRenders()
    {
        for (IExplosiveData data : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = "icbmclassic:explosive_bomblet"; //TODO add per version models
            bombletModelMap.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }

        ModelLoader.registerItemVariants(ItemReg.itemBombletExplosive, bombletModelMap.values()
            .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(ItemReg.itemBombletExplosive, new ItemModelMapperExplosive(bombletModelMap, bombletModelMap.get(ICBMExplosives.CONDENSED)));
    }

    protected static void registerCartRenders()
    {
        for (IExplosiveData data : ICBMClassicAPI.EX_MINECART_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryKey().getResourceDomain() + ":bombcarts/" + data.getRegistryKey().getResourcePath();
            cartModelMap.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }
        ModelLoader.registerItemVariants(ItemReg.itemBombCart, cartModelMap.values()
                .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(ItemReg.itemBombCart, new ItemModelMapperExplosive(cartModelMap, cartModelMap.get(ICBMExplosives.CONDENSED)));
    }

    protected static void registerMissileRenders()
    {
        for (IExplosiveData data : ICBMClassicAPI.EX_MISSILE_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryKey().getResourceDomain() + ":missiles/" + data.getRegistryKey().getResourcePath();
            missileModelMap.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }

        // Missile module, also used as fallback for all renders
        final ModelResourceLocation fallback = new ModelResourceLocation(ICBMConstants.DOMAIN + ":missiles/missile", "inventory");
        missileModelMap.put(ICBMExplosives.MISSILEMODULE, fallback);

        // Register variants to item so model files load
        ModelLoader.registerItemVariants(ItemReg.itemExplosiveMissile, missileModelMap.values()
                .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));

        // Custom def to handle fallback for missing models
        ModelLoader.setCustomMeshDefinition(ItemReg.itemExplosiveMissile, new ItemModelMapperExplosive(missileModelMap, fallback));
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
        if(block != null) //incase the block was disabled via config or doesn't exist due to something else
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }

    protected static void newItemModel(Item item, int meta, String varient, String sub)
    {
        if(item != null) //incase the item was disabled via config or doesn't exist due to something else
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName() + sub, varient));
    }
}
