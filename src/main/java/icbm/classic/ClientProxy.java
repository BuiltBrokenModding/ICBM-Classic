package icbm.classic;

import com.builtbroken.jlib.data.vector.IPos3D;
import com.builtbroken.mc.lib.render.fx.*;
import com.builtbroken.mc.lib.transform.vector.Pos;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.client.fx.FXAntimatterPartical;
import icbm.classic.client.render.entity.*;
import icbm.classic.client.render.item.RenderItemMissile;
import icbm.classic.client.render.item.RenderRocketLauncher;
import icbm.classic.client.render.tile.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.explosive.ex.missiles.MissilePlayerHandler;
import icbm.classic.content.explosive.tile.TileExplosive;
import icbm.classic.content.machines.TileCruiseLauncher;
import icbm.classic.content.machines.TileEMPTower;
import icbm.classic.content.machines.TileMissileCoordinator;
import icbm.classic.content.machines.TileRadarStation;
import icbm.classic.content.machines.launcher.TileLauncherBase;
import icbm.classic.content.machines.launcher.TileLauncherFrame;
import icbm.classic.content.machines.launcher.TileLauncherScreen;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityDiggingFX;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.entity.Entity;
import net.minecraft.server.gui.IUpdatePlayerListBox;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;

import java.util.List;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
    private boolean disableReflectionFX = false;

    @Override
    public void preInit()
    {
        super.preInit();
    }

    @Override
    public void init()
    {
        super.init();
        RenderingRegistry.registerEntityRenderingHandler(EntityFlyingBlock.class, new RenderEntityBlock());
        RenderingRegistry.registerEntityRenderingHandler(EntityFragments.class, new RenderShrapnel());

        MinecraftForgeClient.registerItemRenderer(ICBMClassic.itemRocketLauncher, new RenderRocketLauncher());
        MinecraftForgeClient.registerItemRenderer(ICBMClassic.itemMissile, new RenderItemMissile());

        RenderingRegistry.registerBlockHandler(new RenderBombBlock());

        RenderingRegistry.registerEntityRenderingHandler(EntityExplosive.class, new RenderEntityExplosive());
        RenderingRegistry.registerEntityRenderingHandler(EntityMissile.class, new RenderMissile(0.5F));
        RenderingRegistry.registerEntityRenderingHandler(EntityExplosion.class, new RenderExplosion());
        RenderingRegistry.registerEntityRenderingHandler(EntityLightBeam.class, new RenderLightBeam());
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenade.class, new RenderGrenade());
        RenderingRegistry.registerEntityRenderingHandler(EntityBombCart.class, new RenderMinecart());

        ClientRegistry.bindTileEntitySpecialRenderer(TileCruiseLauncher.class, new RenderCruiseLauncher());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherBase.class, new RenderLauncherBase());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherScreen.class, new RenderLauncherScreen());
        ClientRegistry.bindTileEntitySpecialRenderer(TileLauncherFrame.class, new RenderLauncherFrame());
        ClientRegistry.bindTileEntitySpecialRenderer(TileRadarStation.class, new RenderRadarStation());
        ClientRegistry.bindTileEntitySpecialRenderer(TileEMPTower.class, new RenderEmpTower());
        ClientRegistry.bindTileEntitySpecialRenderer(TileMissileCoordinator.class, new RenderMissileCoordinator());
        ClientRegistry.bindTileEntitySpecialRenderer(TileExplosive.class, new RenderBombBlock());
    }

    @Override
    public boolean isGaoQing()
    {
        return Minecraft.getMinecraft().gameSettings.fancyGraphics;
    }

    @Override
    public int getParticleSetting()
    {
        return Minecraft.getMinecraft().gameSettings.particleSetting;
    }

    @Override
    public void spawnParticle(String name, World world, IPos3D position, double motionX, double motionY, double motionZ, float red, float green, float blue, float scale, double distance)
    {
        EntityFX fx = null;

        if (name.equals("smoke"))
        {
            fx = new FXSmoke(world, new Pos(position), red, green, blue, scale, distance);
        }
        else if (name.equals("missile_smoke"))
        {
            fx = (new FXSmoke(world, new Pos(position), red, green, blue, scale, distance)).setAge(100);
        }
        else if (name.equals("portal"))
        {
            fx = new FXEnderPortalPartical(world, new Pos(position), red, green, blue, scale, distance);
        }
        else if (name.equals("antimatter"))
        {
            fx = new FXAntimatterPartical(world, new Pos(position), red, green, blue, scale, distance);
        }
        else if (name.equals("digging"))
        {
            fx = new EntityDiggingFX(world, position.x(), position.y(), position.z(), motionX, motionY, motionZ, Block.getBlockById((int) red), 0, (int) green);
            fx.multipleParticleScaleBy(blue);
        }
        else if (name.equals("shockwave"))
        {
            fx = new FXShockWave(world, new Pos(position), red, green, blue, scale, distance);
        }

        if (fx != null)
        {
            fx.motionX = motionX;
            fx.motionY = motionY;
            fx.motionZ = motionZ;
            FMLClientHandler.instance().getClient().effectRenderer.addEffect(fx);
        }
    }

    @Override
    public void spawnShock(World world, Pos startVec, Pos targetVec)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBolt(world, startVec, targetVec, 0));
    }

    @Override
    public void spawnShock(World world, IPos3D startVec, IPos3D targetVec, int duration)
    {
        FMLClientHandler.instance().getClient().effectRenderer.addEffect(new FXElectricBoltSpawner(world, startVec, targetVec, 0, duration));
    }

    @Override
    public IUpdatePlayerListBox getDaoDanShengYin(EntityMissile eDaoDan)
    {
        return new MissilePlayerHandler(null, eDaoDan, Minecraft.getMinecraft().thePlayer);
    }

    @Override
    public List<Entity> getEntityFXs()
    {
        if (!this.disableReflectionFX)
        {
            try
            {
                EffectRenderer renderer = Minecraft.getMinecraft().effectRenderer;
                List[] fxLayers = (List[]) ReflectionHelper.getPrivateValue(EffectRenderer.class, renderer, 2);
                return fxLayers[0];
            }
            catch (Exception e)
            {
                ICBMClassic.INSTANCE.logger().error("Failed to use refection on entity effects.", e);
                this.disableReflectionFX = true;
            }
        }
        return null;
    }
}
