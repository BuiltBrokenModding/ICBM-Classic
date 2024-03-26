package icbm.classic.client;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.refs.ICBMExplosives;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.client.mapper.BlockModelMapperExplosive;
import icbm.classic.client.mapper.ItemModelMapperExplosive;
import icbm.classic.client.render.entity.*;
import icbm.classic.config.ConfigItems;
import icbm.classic.lib.colors.ColorHelper;
import icbm.classic.world.IcbmItems;
import icbm.classic.world.blast.redmatter.RedmatterEntity;
import icbm.classic.world.blast.redmatter.render.RenderRedmatter;
import icbm.classic.world.block.emptower.EmpTowerBlockEntity;
import icbm.classic.world.block.emptower.TESREmpTower;
import icbm.classic.world.block.emptower.TileEmpTowerFake;
import icbm.classic.world.block.launcher.base.LauncherBaseBlockEntity;
import icbm.classic.world.block.launcher.base.TESRLauncherBase;
import icbm.classic.world.block.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.world.block.launcher.cruise.TileCruiseLauncher;
import icbm.classic.world.entity.*;
import icbm.classic.world.entity.flyingblock.FlyingBlockEntity;
import icbm.classic.world.entity.flyingblock.RenderEntityBlock;
import icbm.classic.world.item.CraftingItem;
import icbm.classic.world.missile.entity.EntityMissile;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = IcbmConstants.MOD_ID, value = Dist.CLIENT)
public class ClientReg {
    private static final Map<ExplosiveType, ModelResourceLocation> GRENADE_MODEL_MAP = new HashMap<>();
    private static final Map<ExplosiveType, ModelResourceLocation> MISSILE_MODEL_MAP = new HashMap<>();
    private static final Map<ExplosiveType, Map<Direction, ModelResourceLocation>> BLOCK_MODEL_MAP = new HashMap<>();
    private static final Map<ExplosiveType, ModelResourceLocation> ITEM_BLOCK_MODEL_MAP = new HashMap<>();
    private static final Map<ExplosiveType, ModelResourceLocation> CART_MODEL_MAP = new HashMap<>();

    private static void clearModelCache() {
        GRENADE_MODEL_MAP.clear();
        MISSILE_MODEL_MAP.clear();
        BLOCK_MODEL_MAP.clear();
        ITEM_BLOCK_MODEL_MAP.clear();
        CART_MODEL_MAP.clear();
    }

    @SubscribeEvent
    public static void registerBlockColor(ColorHandlerEvent.Block event) {
        event.getBlockColors().registerBlockColorHandler((state, worldIn, pos, tintIndex) -> {
            if (worldIn != null && pos != null) {
                final BlockEntity blockEntity = worldIn.getBlockEntity(pos);
                if (tile instanceof EmpTowerBlockEntity) {
                    //TODO cache as chargePercent(0 to 100 int) -> value
                    int red = (int) Math.floor(Math.cos(((EmpTowerBlockEntity) tile).getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(((EmpTowerBlockEntity) tile).getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                } else if (tile instanceof TileEmpTowerFake && ((TileEmpTowerFake) tile).getHost() != null) {
                    int red = (int) Math.floor(Math.cos(((TileEmpTowerFake) tile).getHost().getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(((TileEmpTowerFake) tile).getHost().getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                }
            }
            return 0;
        }, BlockReg.blockEmpTower);
    }

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event) {
        OBJLoader.INSTANCE.addDomain(IcbmConstants.MOD_ID);

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

        //Explosives
        registerExBlockRenders();
        registerGrenadeRenders();
        registerCartRenders();
        registerMissileRenders();

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
        newItemModel(IcbmItems.itemPoisonPowder, 0, "inventory", "");
        newItemModel(IcbmItems.itemSulfurDust, 0, "inventory", "");
        newItemModel(IcbmItems.itemSaltpeterDust, 0, "inventory", "");
        newItemModel(IcbmItems.itemSaltpeterBall, 0, "inventory", "");
        newItemModel(IcbmItems.itemAntidote, 0, "inventory", "");
        newItemModel(IcbmItems.itemSignalDisrupter, 0, "inventory", "");
        newItemModel(IcbmItems.itemTracker, 0, "inventory", "");
        newItemModel(IcbmItems.itemDefuser, 0, "inventory", "");
        newItemModel(IcbmItems.itemRadarGun, 0, "inventory", "");
        newItemModel(IcbmItems.itemRemoteDetonator, 0, "inventory", "");
        newItemModel(IcbmItems.itemLaserDetonator, 0, "inventory", "");
        newItemModel(IcbmItems.itemRocketLauncher, 0, "inventory", "");
        newItemModel(IcbmItems.itemBattery, 0, "inventory", "");
        ModelLoader.setCustomModelResourceLocation(IcbmItems.itemSAM, 0, new ModelResourceLocation(IcbmConstants.MOD_ID + ":missiles/surface_to_air", "inventory"));

        //crafting parts
        if (ConfigItems.ENABLE_CRAFTING_ITEMS) {
            if (ConfigItems.ENABLE_INGOTS_ITEMS) {
                registerCraftingRender(IcbmItems.itemIngot);
                registerCraftingRender(IcbmItems.itemIngotClump);
            }

            if (ConfigItems.ENABLE_PLATES_ITEMS)
                registerCraftingRender(IcbmItems.itemPlate);

            if (ConfigItems.ENABLE_CIRCUIT_ITEMS)
                registerCraftingRender(IcbmItems.itemCircuit);

            if (ConfigItems.ENABLE_WIRES_ITEMS)
                registerCraftingRender(IcbmItems.itemWire);
        }

        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(ExplosiveEntity.class, RenderExBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(RedmatterEntity.class, RenderRedmatter::new);
        RenderingRegistry.registerEntityRenderingHandler(FlyingBlockEntity.class, RenderEntityBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(ExplosionEntity.class, RenderExplosion::new);
        RenderingRegistry.registerEntityRenderingHandler(GrenadeEntity.class, RenderGrenade::new);
        RenderingRegistry.registerEntityRenderingHandler(LightBeamEntity.class, RenderLightBeam::new);
        RenderingRegistry.registerEntityRenderingHandler(FragmentsEntity.class, RenderFragments::new);
        RenderingRegistry.registerEntityRenderingHandler(PlayerSeatEntity.class, RenderSeat::new);
        RenderingRegistry.registerEntityRenderingHandler(SmokeEntity.class, RenderSmoke::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, manager -> RenderMissile.INSTANCE = new RenderMissile(manager));

        ClientRegistry.bindBlockEntitySpecialRenderer(LauncherBaseBlockEntity.class, new TESRLauncherBase());
        ClientRegistry.bindBlockEntitySpecialRenderer(TileCruiseLauncher.class, new TESRCruiseLauncher());
        ClientRegistry.bindBlockEntitySpecialRenderer(EmpTowerBlockEntity.class, new TESREmpTower());
        ClientRegistry.bindBlockEntitySpecialRenderer(TileEmpTowerFake.class, new TESREmpTower());
    }

    protected static void registerExBlockRenders() {
        for (ExplosiveType data : ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            //Add block state
            final HashMap<Direction, ModelResourceLocation> facingModelMap = new HashMap<>();
            final String resourcePath = data.getRegistryName().getResourceDomain() + ":explosives/" + data.getRegistryName().getResourcePath();

            for (Direction facing : Direction.VALUES) {
                facingModelMap.put(facing, new ModelResourceLocation(resourcePath, "explosive=" + data.getRegistryName().toString().replace(":", "_") + ",rotation=" + facing));
            }

            BLOCK_MODEL_MAP.put(data, facingModelMap);

            //Add item state
            //BlockState state = BlockReg.blockExplosive.getDefaultState().withProperty(BlockICBM.ROTATION_PROP, Direction.UP);
            // String properties_string = getPropertyString(state.getProperties());
            ITEM_BLOCK_MODEL_MAP.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }
        //Block state mapper
        ModelLoader.setCustomStateMapper(BlockReg.blockExplosive, new BlockModelMapperExplosive(BLOCK_MODEL_MAP, BLOCK_MODEL_MAP.get(ICBMExplosives.CONDENSED).get(Direction.UP)));
        //Item state mapper
        ModelLoader.setCustomMeshDefinition(Item.getItemFromBlock(BlockReg.blockExplosive), new ItemModelMapperExplosive(ITEM_BLOCK_MODEL_MAP, ITEM_BLOCK_MODEL_MAP.get(ICBMExplosives.CONDENSED)));
        ModelBakery.registerItemVariants(Item.getItemFromBlock(BlockReg.blockExplosive), ITEM_BLOCK_MODEL_MAP.values()
            .stream()
            .map(mrl -> new ResourceLocation(mrl.getResourceDomain(), mrl.getResourcePath()))
            .collect(Collectors.toList())
            .toArray(new ResourceLocation[ITEM_BLOCK_MODEL_MAP.values().size()]));
    }

    protected static void registerGrenadeRenders() {
        for (ExplosiveType data : ICBMClassicAPI.EX_GRENADE_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryName().getResourceDomain() + ":grenades/" + data.getRegistryName().getResourcePath();
            GRENADE_MODEL_MAP.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }

        ModelLoader.registerItemVariants(IcbmItems.itemGrenade, GRENADE_MODEL_MAP.values()
            .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(IcbmItems.itemGrenade, new ItemModelMapperExplosive(GRENADE_MODEL_MAP, GRENADE_MODEL_MAP.get(ICBMExplosives.CONDENSED)));
    }

    protected static void registerCartRenders() {
        for (ExplosiveType data : ICBMClassicAPI.EX_MINECART_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryName().getResourceDomain() + ":bombcarts/" + data.getRegistryName().getResourcePath();
            CART_MODEL_MAP.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }
        ModelLoader.registerItemVariants(IcbmItems.itemBombCart, CART_MODEL_MAP.values()
            .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));
        ModelLoader.setCustomMeshDefinition(IcbmItems.itemBombCart, new ItemModelMapperExplosive(CART_MODEL_MAP, CART_MODEL_MAP.get(ICBMExplosives.CONDENSED)));
    }

    protected static void registerMissileRenders() {
        for (ExplosiveType data : ICBMClassicAPI.EX_MISSILE_REGISTRY.getExplosives()) //TODO run loop once for all 4 content types
        {
            final String resourcePath = data.getRegistryName().getResourceDomain() + ":missiles/" + data.getRegistryName().getResourcePath();
            MISSILE_MODEL_MAP.put(data, new ModelResourceLocation(resourcePath, "inventory"));
        }

        // Missile module, also used as fallback for all renders
        final ModelResourceLocation fallback = new ModelResourceLocation(IcbmConstants.MOD_ID + ":missiles/missile", "inventory");
        MISSILE_MODEL_MAP.put(ICBMExplosives.MISSILEMODULE, fallback);

        // Register variants to item so model files load
        ModelLoader.registerItemVariants(IcbmItems.itemExplosiveMissile, MISSILE_MODEL_MAP.values()
            .stream().map(model -> new ResourceLocation(model.getResourceDomain() + ":" + model.getResourcePath())).toArray(ResourceLocation[]::new));

        // Custom def to handle fallback for missing models
        ModelLoader.setCustomMeshDefinition(IcbmItems.itemExplosiveMissile, new ItemModelMapperExplosive(MISSILE_MODEL_MAP, fallback));
    }

    protected static void registerCraftingRender(CraftingItem craftingItem) {
        //Most crafting items can be disabled, so null check is needed
        if (craftingItem != null) {
            final String resourcePath = craftingItem.getRegistryName().toString();
            for (int i = 0; i < craftingItem.subItems.length; i++) {
                String subItem = craftingItem.subItems[i];
                ModelLoader.setCustomModelResourceLocation(craftingItem, i, new ModelResourceLocation(resourcePath, "name=" + subItem));
            }
        }
    }

    protected static void newBlockModel(Block block, int meta, String varient, String sub) {
        if (block != null) //incase the block was disabled via config or doesn't exist due to something else
            ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(block), meta, new ModelResourceLocation(block.getRegistryName() + sub, varient));
    }

    protected static void newItemModel(Item item, int meta, String varient, String sub) {
        if (item != null) //incase the item was disabled via config or doesn't exist due to something else
            ModelLoader.setCustomModelResourceLocation(item, meta, new ModelResourceLocation(item.getRegistryName() + sub, varient));
    }
}
