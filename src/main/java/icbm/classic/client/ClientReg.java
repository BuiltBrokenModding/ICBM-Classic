package icbm.classic.client;

import icbm.classic.client.mapper.ExplosiveModels;
import icbm.classic.client.models.ModelChickenHelmet;
import icbm.classic.client.models.CruiseLauncherTopModel;
import icbm.classic.client.render.entity.RenderBombCart;
import icbm.classic.client.render.entity.RenderExBlock;
import icbm.classic.client.render.entity.RenderExplosion;
import icbm.classic.client.render.entity.RenderFragments;
import icbm.classic.client.render.entity.RenderLightBeam;
import icbm.classic.client.render.entity.RenderMissile;
import icbm.classic.client.render.entity.RenderSeat;
import icbm.classic.client.render.entity.RenderSmoke;
import icbm.classic.client.render.entity.item.RenderAsItem;
import icbm.classic.client.render.entity.layer.LayerChickenHelmet;
import icbm.classic.content.blast.redmatter.render.RenderRedmatter;
import icbm.classic.content.blocks.emptower.TESREmpTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.emptower.TileEmpTowerFake;
import icbm.classic.content.blocks.emptower.gui.GuiEMPTower;
import icbm.classic.content.blocks.launcher.base.TESRLauncherBase;
import icbm.classic.content.blocks.launcher.base.gui.GuiLauncherBase;
import icbm.classic.content.blocks.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.gui.GuiCruiseLauncher;
import icbm.classic.content.blocks.launcher.screen.gui.GuiLauncherScreen;
import icbm.classic.content.blocks.radarstation.gui.GuiRadarStation;
import icbm.classic.content.cargo.balloon.EntityBalloon;
import icbm.classic.content.cargo.parachute.RenderParachute;
import icbm.classic.content.cluster.bomblet.RenderBombDroplet;
import icbm.classic.content.entity.EntityGrenade;
import icbm.classic.content.entity.flyingblock.RenderEntityBlock;
import icbm.classic.content.items.ItemRocketLauncher;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.EntityReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.content.reg.MenuReg;
import icbm.classic.lib.colors.ColorHelper;
import net.minecraft.client.renderer.entity.ChickenRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Client registration, all listeners are on the mod bus and added by {@link ClientProxy#preInit(IEventBus)}.
 * <p>
 * PORT: 1.12 model registration (ModelLoader.setCustomModelResourceLocation, mesh definitions, state mappers)
 * is replaced by static JSON models named after the registry names, explosive variants are handled by
 * {@link ExplosiveModels}. Crafting items are flattened so they need no custom model mapping anymore.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
public final class ClientReg
{
    private ClientReg()
    {
    }

    public static void register(IEventBus modBus)
    {
        modBus.addListener(ClientReg::registerBlockColor);
        modBus.addListener(ClientReg::registerEntityRenders);
        modBus.addListener(ClientReg::registerLayerDefinitions);
        modBus.addListener(ClientReg::addLayers);
        modBus.addListener(ClientReg::registerScreens);
        modBus.addListener(ClientReg::clientSetup);
        modBus.addListener(ExplosiveModels::onRegisterAdditional);
        modBus.addListener(ExplosiveModels::onModifyBakingResult);
    }

    // ------------------------------------------------------------------
    // Colors
    // ------------------------------------------------------------------

    public static void registerBlockColor(RegisterColorHandlersEvent.Block event)
    {
        event.register((state, worldIn, pos, tintIndex) -> {
            if (worldIn != null && pos != null)
            {
                final BlockEntity tile = worldIn.getBlockEntity(pos);
                if (tile instanceof TileEMPTower tower)
                {
                    //TODO cache as chargePercent(0 to 100 int) -> value
                    int red = (int) Math.floor(Math.cos(tower.getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(tower.getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                }
                else if (tile instanceof TileEmpTowerFake fake && fake.getHost() != null)
                {
                    int red = (int) Math.floor(Math.cos(fake.getHost().getChargePercentage()) * 255);
                    int blue = (int) Math.floor(Math.sin(fake.getHost().getChargePercentage()) * 255);
                    return ColorHelper.toRGB(red, 0, blue);
                }
            }
            return 0;
        }, BlockReg.blockEmpTower.get());
    }

    // ------------------------------------------------------------------
    // Item properties
    // ------------------------------------------------------------------

    public static void clientSetup(FMLClientSetupEvent event)
    {
        // ItemProperties isn't thread safe
        event.enqueueWork(() -> {
            registerPulling(ItemReg.itemRocketLauncher.get());
            registerPulling(ItemReg.itemBallisticLauncher.get());
        });
    }

    /** 1.12 ItemRocketLauncher "pulling" property override */
    private static void registerPulling(ItemRocketLauncher item)
    {
        ItemProperties.register(item, ResourceLocation.withDefaultNamespace("pulling"),
            (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F);
    }

    // ------------------------------------------------------------------
    // Entity and block entity renders
    // ------------------------------------------------------------------

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityReg.BLOCK_EXPLOSIVE.get(), RenderExBlock::new);
        event.registerEntityRenderer(EntityReg.REDMATTER.get(), RenderRedmatter::new);
        event.registerEntityRenderer(EntityReg.BLOCK_GRAVITY.get(), RenderEntityBlock::new);
        event.registerEntityRenderer(EntityReg.EXPLOSION.get(), RenderExplosion::new);
        event.registerEntityRenderer(EntityReg.GRENADE.get(),
            (ctx) -> new RenderAsItem<EntityGrenade>(ctx, EntityGrenade::renderItemStack).setBillboard(true));
        event.registerEntityRenderer(EntityReg.PARACHUTE.get(), RenderParachute::new);
        // PORT: 1.12 used a fake balloon meta for the 3D model, now a standalone model
        event.registerEntityRenderer(EntityReg.BALLOON.get(),
            (ctx) -> new RenderAsItem<EntityBalloon>(ctx, EntityBalloon::getRenderItemStack).setModelOverride(ModelResourceLocation.standalone(ExplosiveModels.BALLOON_3D)));
        event.registerEntityRenderer(EntityReg.BEAM.get(), RenderLightBeam::new);
        event.registerEntityRenderer(EntityReg.BLOCK_FRAGMENT.get(), RenderFragments::new);
        event.registerEntityRenderer(EntityReg.BOMB_DROPLET.get(), RenderBombDroplet::new);
        event.registerEntityRenderer(EntityReg.MISSILE_SEAT.get(), RenderSeat::new);
        event.registerEntityRenderer(EntityReg.SMOKE.get(), RenderSmoke::new);
        // PORT: 1.12 used the vanilla TNT minecart render with the explosive as display block state
        event.registerEntityRenderer(EntityReg.BOMB_CART.get(), RenderBombCart::new);

        // Missiles, RenderMissile sets RenderMissile.INSTANCE in its constructor
        event.registerEntityRenderer(EntityReg.MISSILE_EXPLOSIVE.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_GENERIC.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_SAM.get(), RenderMissile::new);
        event.registerEntityRenderer(EntityReg.MISSILE_HELD_ITEM.get(), RenderMissile::new);

        // Block entities, raw types as the emp tower render is shared by the tower and its fake blocks
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_LAUNCHER_BASE.get(), ctx -> new TESRLauncherBase(ctx));
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_CRUISE_LAUNCHER.get(), ctx -> new TESRCruiseLauncher(ctx));
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_EMP_TOWER.get(), ctx -> new TESREmpTower(ctx));
        event.registerBlockEntityRenderer((BlockEntityType) BlockReg.TILE_EMP_TOWER_FAKE.get(), ctx -> new TESREmpTower(ctx));
    }

    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
        event.registerLayerDefinition(ModelChickenHelmet.LAYER, ModelChickenHelmet::createLayer);
        event.registerLayerDefinition(CruiseLauncherTopModel.LAYER, CruiseLauncherTopModel::createLayer);
    }

    /** 1.12 added the helmet layer to the chicken render in ClientProxy#init */
    public static void addLayers(EntityRenderersEvent.AddLayers event)
    {
        final EntityRenderer<?> renderer = event.getRenderer(EntityType.CHICKEN);
        if (renderer instanceof ChickenRenderer chickenRenderer)
        {
            chickenRenderer.addLayer(new LayerChickenHelmet(chickenRenderer, event.getEntityModels()));
        }
    }

    // ------------------------------------------------------------------
    // Screens
    // ------------------------------------------------------------------

    public static void registerScreens(RegisterMenuScreensEvent event)
    {
        event.register(MenuReg.EMP_TOWER.get(), GuiEMPTower::new);
        event.register(MenuReg.LAUNCHER_BASE.get(), GuiLauncherBase::new);
        event.register(MenuReg.CRUISE_LAUNCHER.get(), GuiCruiseLauncher::new);
        event.register(MenuReg.LAUNCHER_SCREEN.get(), GuiLauncherScreen::new);
        event.register(MenuReg.RADAR_STATION.get(), GuiRadarStation::new);
    }
}
// PORTED
