package icbm.classic.client.render;

import com.builtbroken.mc.lib.render.RenderUtility;
import com.builtbroken.mc.lib.render.model.loader.EngineModelLoader;
import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.ExNightmare;
import icbm.classic.content.explosive.ex.Explosion;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.model.IModelCustom;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
/** Handles missile rendering for all object types. This
 * includes entity, item, inventory, and tiles.
 *
 * @author Calclavia, DarkCow
 * */
public class RenderMissile extends Render implements IItemRenderer
{
    private static IModelCustom TIER1_BASE;
    private static IModelCustom TIER2_BASE;
    private static IModelCustom TIER3_BASE;
    private static IModelCustom TIER4_BASE;

    public RenderMissile(float f)
    {
        this.shadowSize = f;
        TIER1_BASE = EngineModelLoader.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier1/missile_base_t1.obj"));
        TIER2_BASE = EngineModelLoader.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier2/missile_base_t2.obj"));
        TIER3_BASE = EngineModelLoader.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier3/missile_base_t3.obj"));
        TIER4_BASE = EngineModelLoader.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier4/missile_base_t4.obj"));
    }

    @Override
    public void doRender(Entity entity, double x, double y, double z, float f, float f1)
    {
        EntityMissile entityMissile = (EntityMissile) entity;
        Explosive e = entityMissile.getExplosiveType();
        Explosion missile = e == null ? (Explosion) Explosives.CONDENSED.handler : (Explosion) e;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glRotatef(entityMissile.prevRotationYaw + (entityMissile.rotationYaw - entityMissile.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
        float pitch = entityMissile.prevRotationPitch + (entityMissile.rotationPitch - entityMissile.prevRotationPitch) * f1 - 90;
        GL11.glRotatef(pitch, 0.0F, 0.0F, 1.0F);
        if (missile.missileModelPath != null && missile.missileModelPath.contains("missiles"))
        {
            GL11.glScalef(0.00625f, 0.00625f, 0.00625f);
        }
        else if (!(missile instanceof ExNightmare))
        {
            GL11.glScalef(0.07f, 0.07f, 0.07f);
        }
        renderMissile(missile);

        GL11.glPopMatrix();
    }

    public static void renderMissile(Explosion missile)
    {
        if (missile instanceof ExNightmare)
        {
            //Render body
            GL11.glPushMatrix();
            GL11.glScalef(0.00625f, 0.00625f, 0.00625f);
            FMLClientHandler.instance().getClient().renderEngine.bindTexture(((Explosion) Explosives.ANVIL.handler).getMissileResource());
            TIER1_BASE.renderAll();
            GL11.glPopMatrix();

            //Render head
            GL11.glPushMatrix();
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            GL11.glTranslated(-0.5, 3.8, -0.5);

            FMLClientHandler.instance().getClient().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            IIcon side = Blocks.lit_pumpkin.getIcon(1, 2);
            RenderUtility.renderCubeWithOverrides(0, 0, 0, 1, 1, 1, Blocks.lit_pumpkin, new IIcon[]{side, Blocks.lit_pumpkin.getIcon(2, 2), side, side, side, side}, 0);
            GL11.glPopMatrix();
        }
        else if (missile.getMissileModel() != null)
        {
            if (missile.missileModelPath.contains("missiles"))
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(missile.getMissileResource());
                if (missile.getTier() == 1)
                {
                    TIER1_BASE.renderAll();
                }
                else if (missile.getTier() == 2)
                {
                    TIER2_BASE.renderAll();
                }
                else if (missile.getTier() == 3)
                {
                    TIER3_BASE.renderAll();
                }
                else if (missile.getTier() == 4)
                {
                    TIER4_BASE.renderAll();
                }
                missile.getMissileModel().renderAll();
            }
            else
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(missile.getMissileResource());
                //FMLClientHandler.instance().getClient().renderEngine.bindTexture(SharedAssets.GREY_TEXTURE);
                missile.getMissileModel().renderAll();
            }
        }
        else
        {
            TIER1_BASE.renderAll();
        }
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity entity)
    {
        return null;
    }

    @Override
    public boolean handleRenderType(ItemStack item, IItemRenderer.ItemRenderType type)
    {
        return true;
    }

    @Override
    public boolean shouldUseRenderHelper(IItemRenderer.ItemRenderType type, ItemStack item, IItemRenderer.ItemRendererHelper helper)
    {
        return true;
    }

    @Override
    public void renderItem(IItemRenderer.ItemRenderType type, ItemStack item, Object... data)
    {
        Explosives ex = Explosives.get(item.getItemDamage());

        if (ex.handler instanceof Explosion)
        {
            Explosion missile = (Explosion) ex.handler;
            final boolean doMissileSizeFix = missile.missileModelPath != null && missile.missileModelPath.contains("missiles");

            float yaw = 0;
            float pitch = -90;
            float scale = doMissileSizeFix ? 0.00625f : 0.7f;


            switch (type)
            {
                case INVENTORY:

                    scale = doMissileSizeFix ? 0.0035f : 0.5f;

                    if (missile.getTier() == 2 || !missile.hasBlockForm())
                    {
                        scale = scale / 1.5f;
                        GL11.glTranslatef(-0.7f, 0f, 0f);
                    }
                    else if (missile.getTier() == 3)
                    {
                        scale = scale / 1.7f;
                        GL11.glTranslatef(-0.65f, 0f, 0f);
                    }
                    else if (missile.getTier() == 4)
                    {
                        scale = scale / 1.4f;
                        GL11.glTranslatef(-0.5f, 0f, 0f);
                    }
                    else
                    {
                        GL11.glTranslatef(-0.5f, 0f, 0f);
                    }
                    break;
                case EQUIPPED:
                    GL11.glTranslatef(1f, 0.3f, 0.5f);
                    break;
                case EQUIPPED_FIRST_PERSON:
                    GL11.glTranslatef(1.15f, -1f, 0.5f);
                    break;
                case ENTITY:
                    GL11.glTranslatef(-0.6f, 0f, 0f);
                    break;
                default:
                    break;
            }

            GL11.glRotatef(yaw, 0, 1f, 0f);
            GL11.glRotatef(pitch, 0, 0f, 1f);
            GL11.glScalef(scale, scale, scale);

            renderMissile(missile);
        }
    }
}