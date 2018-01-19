package icbm.classic.client.render.entity;

import com.builtbroken.mc.client.SharedAssets;
import com.builtbroken.mc.core.References;
import icbm.classic.ICBMClassic;
import icbm.classic.content.entity.EntityExplosion;
import icbm.classic.content.explosive.blast.BlastRedmatter;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderExplosion extends Render<EntityExplosion>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMClassic.DOMAIN, References.TEXTURE_DIRECTORY + "blackhole.png");
    public Color colorIn = new Color(16777215);
    public Color colorOut = new Color(0);

    public Random random = new Random();

    public RenderExplosion(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityExplosion entityExplosion, double x, double y, double z, float par8, float par9)
    {
        if (entityExplosion.getBlast() != null)
        {
            // RedM atter Render
            if (entityExplosion.getBlast() instanceof BlastRedmatter)
            {
                final BlastRedmatter redmatter = (BlastRedmatter) entityExplosion.getBlast();
                final float scale = redmatter.getScaleFactor();

                renderDisk(entityExplosion, redmatter, x, y, z, scale, par8, par9);
                GlStateManager.color(1, 1, 1, 1);

                renderSphere(entityExplosion, redmatter, x, y, z, scale, par8, par9);
                GlStateManager.color(1, 1, 1, 1);


                renderBeams(entityExplosion, redmatter, x, y, z, scale, par8, par9);
                GlStateManager.color(1, 1, 1, 1);
            }
            else
            {
                if (entityExplosion.getBlast().getRenderModel() != null && entityExplosion.getBlast().getRenderResource() != null)
                {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate((float) x, (float) y + 1F, (float) z);
                    GlStateManager.rotate(entityExplosion.rotationPitch, 0.0F, 0.0F, 1.0F);
                    this.bindTexture(entityExplosion.getBlast().getRenderResource());
                    entityExplosion.getBlast().getRenderModel().render(entityExplosion, (float) x, (float) y, (float) z, par8, par9, 0.0625F);
                    GlStateManager.popMatrix();
                }
            }
        }
    }

    public void renderSphere(EntityExplosion entityExplosion, BlastRedmatter redmatter, double x, double y, double z, float scale, float par8, float par9)
    {
        final float radius = Math.max(BlastRedmatter.ENTITY_DESTROY_RADIUS * scale, 0.1f);

        //--------------------------------------------------
        //Inside sphere
        //Setup
        GlStateManager.pushMatrix();
        //GlStateManager.enableBlend();
        //GlStateManager.disableLighting();

        //Translate
        GlStateManager.translate((float) x, (float) y, (float) z);

        //Assign texture
        bindTexture(SharedAssets.GREY_TEXTURE);

        //Assign color
        GlStateManager.color(0.0F, 0.0F, 0.0F, 1);

        //Render outer sphere
        new Sphere().draw(radius * 0.8f, 32, 32);

        //Reset
        //GlStateManager.enableLighting();
        //GlStateManager.disableBlend();
        GlStateManager.popMatrix();


        //--------------------------------------------------
        //Outside sphere

        float ticks = entityExplosion.ticksExisted % 40;

        //Setup
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();
        GlStateManager.disableDepth();

        //Translate
        GlStateManager.translate((float) x, (float) y, (float) z);

        //Assign texture
        bindTexture(SharedAssets.GREY_TEXTURE);

        //Assign color
        GlStateManager.color(0.0F, 0.0F, 0.2F, 0.8f);

        //Render outer sphere
        final float scaleSize = 0.0005f;
        final float fullSize = radius * scaleSize * 20;
        float scaleDelta;
        if (ticks > 20)
        {
            scaleDelta = fullSize - (radius * scaleSize * (ticks - 20));
        }
        else
        {
            scaleDelta = radius * scaleSize * ticks;
        }
        new Sphere().draw(radius + scaleDelta, 32, 32);

        //Reset
        GlStateManager.enableDepth();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public void renderDisk(EntityExplosion entityExplosion, BlastRedmatter redmatter, double x, double y, double z, float scale, float par8, float par9)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        float size = BlastRedmatter.ENTITY_DESTROY_RADIUS * scale * 3;

        //Setup
        GlStateManager.pushMatrix();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        //Translate
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-entityExplosion.ticksExisted, 0, 1, 0);

        //Assign texture
        this.bindTexture(TEXTURE_FILE);

        //top render
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-size, 0, -size).tex(0, 0).endVertex();
        bufferbuilder.pos(-size, 0, +size).tex(0, 1).endVertex();
        bufferbuilder.pos(+size, 0, +size).tex(1, 1).endVertex();
        bufferbuilder.pos(+size, 0, -size).tex(1, 0).endVertex();
        Tessellator.getInstance().draw();

        //bottom render
        GlStateManager.rotate(180, 1, 0, 0);
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(-size, 0, -size).tex(1, 1).endVertex();
        bufferbuilder.pos(-size, 0, +size).tex(1, 0).endVertex();
        bufferbuilder.pos(+size, 0, +size).tex(0, 0).endVertex();
        bufferbuilder.pos(+size, 0, -size).tex(0, 1).endVertex();
        Tessellator.getInstance().draw();

        //Reset
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public void renderBeams(EntityExplosion entityExplosion, BlastRedmatter redmatter, double x, double y, double z, float scale, float par8, float par9)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();

        /** Enderdragon Light */
        float ticks = entityExplosion.ticksExisted;

        while (ticks > 200)
        {
            ticks -= 100;
        }

        RenderHelper.disableStandardItemLighting();
        float var41 = (5 + ticks) / 200.0F;
        float var51 = 0.0F;

        if (var41 > 0.8F)
        {
            var51 = (var41 - 0.8F) / 0.2F;
        }


        Random rand = new Random(432L);

        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();
        GlStateManager.disableDepth();


        int beamCount = (int) ((var41 + var41 * var41) / 2.0F * 60.0F);
        for (int beamIndex = 0; beamIndex < beamCount; ++beamIndex)
        {
            GlStateManager.pushMatrix();
            float beamLength = (rand.nextFloat() * 20.0F + 5.0F + var51 * 10.0F) * scale;
            float beamWidth = (rand.nextFloat() * 2.0F + 1.0F + var51 * 2.0F) * scale;

            //Random rotations TODO see if we need to rotate so much
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(rand.nextFloat() * 360.0F + var41 * 90.0F, 0.0F, 0.0F, 1.0F);

            //Draw spike shape
            bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);

            //center
            bufferbuilder.pos(0.0D, 0.0D, 0.0D)
                    .color(colorIn.getRed(), colorIn.getGreen(), colorIn.getBlue(), colorIn.getAlpha())
                    .endVertex();

            //Outside
            bufferbuilder.pos(-0.866D * beamWidth, beamLength, -0.5F * beamWidth)
                    .color(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha())
                    .endVertex();
            bufferbuilder.pos(0.866D * beamWidth, beamLength, -0.5F * beamWidth)
                    .color(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha())
                    .endVertex();
            bufferbuilder.pos(0.0D, beamLength, 1.0F * beamWidth)
                    .color(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha())
                    .endVertex();
            bufferbuilder.pos(-0.866D * beamWidth, beamLength, -0.5F * beamWidth)
                    .color(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha())
                    .endVertex();

            Tessellator.getInstance().draw();

            GlStateManager.popMatrix();
        }

        GlStateManager.enableDepth();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityExplosion entity)
    {
        return entity.getBlast().getRenderResource();
    }
}
