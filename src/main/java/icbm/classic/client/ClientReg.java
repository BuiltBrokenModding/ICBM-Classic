package icbm.classic.client;

import icbm.classic.ICBMConstants;
import icbm.classic.client.render.entity.*;
import icbm.classic.client.render.entity.item.RenderAsItem;
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
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.lib.colors.ColorHelper;
import net.minecraft.block.BlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IEnviromentBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/7/19.
 */
@Mod.EventBusSubscriber(modid = ICBMConstants.DOMAIN, value = Dist.CLIENT)
public class ClientReg {
    @SubscribeEvent
    public static void registerBlockColor(ColorHandlerEvent.Block event) {
        event.getBlockColors().register(ClientReg::colorColoring, BlockReg.EMP_TOWER_BASE.get(), BlockReg.EMP_TOWER_COIL.get());
    }

    private static int colorColoring(BlockState state, @Nullable IEnviromentBlockReader worldIn, @Nullable BlockPos pos, int tintIndex) {
        if (worldIn != null && pos != null) {
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
    }

    @SubscribeEvent
    public static void registerAllModels(ModelRegistryEvent event) {
        //---------------------------------------
        //Entity renders
        //---------------------------------------
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, RenderExBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRedmatter.class, RenderRedmatter::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBlock.class, RenderEntityBlock::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosion.class, RenderExplosion::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class,
            (rm) -> new RenderAsItem<EntityGrenade>(rm, EntityGrenade::renderItemStack).setBillboard(true));
        RenderingRegistry.registerEntityRenderingHandler(EntityParachute.class, RenderParachute::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBalloon.class,
            (rm) -> new RenderAsItem<EntityBalloon>(rm, (e) -> e.getRenderStack().orElseThrow(IllegalStateException::new)));
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBeam.class, RenderLightBeam::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosiveFragment.class, RenderFragments::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityBombDroplet.class, RenderBombDroplet::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityPlayerSeat.class, RenderSeat::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySmoke.class, RenderSmoke::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, manager -> RenderMissile.INSTANCE = new RenderMissile(manager));


        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new TESRLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileCruiseLauncher.class, new TESRCruiseLauncher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new TESREmpTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEmpTowerFake.class, new TESREmpTower());
    }
}
