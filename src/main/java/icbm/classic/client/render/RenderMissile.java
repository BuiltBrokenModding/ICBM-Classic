package icbm.classic.client.render;

import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityMissile;
import icbm.classic.content.explosive.Explosive;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.explosive.ex.Explosion;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
/** Handles missile rendering for all object types. This
 * includes entity, item, inventory, and tiles.
 *
 * @author Calclavia, DarkCow
 * */
public class RenderMissile extends Render<EntityMissile>
{
    private static IModel TIER1_BASE;
    private static IModel TIER2_BASE;
    private static IModel TIER3_BASE;
    private static IModel TIER4_BASE;

    public RenderMissile(RenderManager renderManager) throws Exception
    {
        super(renderManager);
        TIER1_BASE = OBJLoader.INSTANCE.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier1/missile_base_t1.obj"));
        TIER2_BASE = OBJLoader.INSTANCE.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier2/missile_base_t2.obj"));
        TIER3_BASE = OBJLoader.INSTANCE.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier3/missile_base_t3.obj"));
        TIER4_BASE = OBJLoader.INSTANCE.loadModel(new ResourceLocation(ICBMClassic.DOMAIN, "models/missiles/tier4/missile_base_t4.obj"));
    }

    @Override
    public void doRender(EntityMissile entityMissile, double x, double y, double z, float f, float f1)
    {
        Explosive explosive = entityMissile.getExplosiveType();
        Explosion explosion = explosive == null ? (Explosion) Explosives.CONDENSED.handler : (Explosion) explosive;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        GL11.glRotatef(entityMissile.prevRotationYaw + (entityMissile.rotationYaw - entityMissile.prevRotationYaw) * f1 - 90.0F, 0.0F, 1.0F, 0.0F);
        float pitch = entityMissile.prevRotationPitch + (entityMissile.rotationPitch - entityMissile.prevRotationPitch) * f1 - 90;
        GL11.glRotatef(pitch, 0.0F, 0.0F, 1.0F);

        GL11.glScalef(explosion.missileRenderScale, explosion.missileRenderScale, explosion.missileRenderScale);

        try
        {
            renderMissile(explosion);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        GL11.glPopMatrix();

        super.doRender(entityMissile, x, y, z, f, f1);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityMissile entity)
    {
        return null;
    }

    public static void renderMissile(Explosion missile) throws Exception
    {
        /*
        if (missile.missileModel != null)
        {
            if (missile.renderBodyForMissilTier)
            {
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(missile.missileTexture);
                if (missile.getTier() == BlockICBM.EnumTier.ONE)
                {
                    TIER1_BASE.renderAll();
                }
                else if (missile.getTier() == BlockICBM.EnumTier.TWO)
                {
                    TIER2_BASE.renderAll();
                }
                else if (missile.getTier() == BlockICBM.EnumTier.THREE)
                {
                    TIER3_BASE.renderAll();
                }
                else if (missile.getTier() == BlockICBM.EnumTier.FOUR)
                {
                    TIER4_BASE.renderAll();
                }
                missile.getMissileModel().renderAll();
            }
            else
            {
                IModel model = OBJLoader.INSTANCE.loadModel(missile.missileModel);
                FMLClientHandler.instance().getClient().renderEngine.bindTexture(missile.missileTexture);
                //FMLClientHandler.instance().getClient().renderEngine.bindTexture(SharedAssets.GREY_TEXTURE);
                model.renderAll();
            }
        }
        else
        {
            TIER1_BASE.renderAll();
        }
        */
    }
}