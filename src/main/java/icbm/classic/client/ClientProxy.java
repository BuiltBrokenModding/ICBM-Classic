package icbm.classic.client;

import icbm.classic.CommonProxy;
import icbm.classic.ICBMClassic;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.client.fx.ParticleSmokeICBM;
import icbm.classic.client.render.entity.*;
import icbm.classic.content.entity.*;
import icbm.classic.content.entity.mobs.*;
import icbm.classic.content.blocks.launcher.cruise.TESRCruiseLauncher;
import icbm.classic.content.blocks.launcher.cruise.TileCruiseLauncher;
import icbm.classic.content.entity.missile.EntityMissile;
import icbm.classic.content.blocks.explosive.BlockExplosive;
import icbm.classic.content.items.ItemCrafting;
import icbm.classic.content.blocks.emptower.TESREMPTower;
import icbm.classic.content.blocks.emptower.TileEMPTower;
import icbm.classic.content.blocks.launcher.base.TESRLauncherBase;
import icbm.classic.content.blocks.launcher.base.TileLauncherBase;
import icbm.classic.content.blocks.launcher.frame.TESRLauncherFrame;
import icbm.classic.content.blocks.launcher.frame.TileLauncherFrame;
import icbm.classic.content.blocks.launcher.screen.TESRLauncherScreen;
import icbm.classic.content.blocks.launcher.screen.TileLauncherScreen;
import icbm.classic.content.blocks.radarstation.TESRRadarStation;
import icbm.classic.content.blocks.radarstation.TileRadarStation;
import icbm.classic.content.reg.BlockReg;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.transform.vector.Pos;
import icbm.classic.prefab.tile.BlockICBM;
import icbm.classic.api.EnumTier;
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
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy
{
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
