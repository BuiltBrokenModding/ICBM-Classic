package icbm.classic.client.render.entity;

import icbm.classic.content.entity.EntityLightBeam;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class RenderLightBeam extends Render<EntityLightBeam>
{
    public static final ResourceLocation TEXTURE_BEACON_BEAM = new ResourceLocation("textures/entity/beacon_beam.png");

    public RenderLightBeam(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityLightBeam beamEntity, double x, double y, double z, float entityYaw, float partialTicks)
    {
        GlStateManager.alphaFunc(516, 0.1F);
        this.bindTexture(TEXTURE_BEACON_BEAM);

        GlStateManager.disableFog();

        int height = 255 - (int) beamEntity.posY;

        renderBeamSegment(x, y - 5, z, partialTicks, 1, 0, //TODO instead of -5 raytrace to ground
                height,
                1, 0, 0,
                0.5D, 1D);

        GlStateManager.enableFog();
    }


    public static void renderBeamSegment(double x, double y, double z, double partialTicks,
                                         double textureScale, double totalWorldTime,
                                         int height,
                                         float color_red, float color_green, float color_blue,
                                         double beamRadius, double glowRadius)
    {
        //Setup render state
        GlStateManager.glTexParameteri(3553, 10242, 10497);
        GlStateManager.glTexParameteri(3553, 10243, 10497);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);


        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        double d3 = 0.5D - beamRadius;
        double d4 = 0.5D - beamRadius;
        double d5 = 0.5D + beamRadius;
        double d6 = 0.5D - beamRadius;
        double d7 = 0.5D - beamRadius;
        double d8 = 0.5D + beamRadius;
        double d9 = 0.5D + beamRadius;
        double d10 = 0.5D + beamRadius;
        double d13 = -1.0D;
        double d14 = (double) height * textureScale + d13;
        //(double) height * textureScale + d13;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d3, y + height, z + d4).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y, z + d4).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y, z + d6).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + height, z + d6).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d9, y + height, z + d10).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y, z + d10).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y, z + d8).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + height, z + d8).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d5, y + height, z + d6).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y, z + d6).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y, z + d10).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + height, z + d10).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d7, y + height, z + d8).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y, z + d8).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y, z + d4).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + height, z + d4).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        tessellator.draw();


        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);
        d3 = 0.5D - glowRadius;
        d4 = 0.5D - glowRadius;
        d5 = 0.5D + glowRadius;
        d6 = 0.5D - glowRadius;
        d7 = 0.5D - glowRadius;
        d8 = 0.5D + glowRadius;
        d9 = 0.5D + glowRadius;
        d10 = 0.5D + glowRadius;
        d13 = -1.0D;
        d14 = (double) height * textureScale + d13;

        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);
        bufferbuilder.pos(x + d3, y + height, z + d4).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y, z + d4).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y, z + d6).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y + height, z + d6).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d9, y + height, z + d10).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y, z + d10).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y, z + d8).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y + height, z + d8).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d5, y + height, z + d6).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d5, y, z + d6).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y, z + d10).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d9, y + height, z + d10).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        bufferbuilder.pos(x + d7, y + height, z + d8).tex(1.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d7, y, z + d8).tex(1.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y, z + d4).tex(0.0D, d13)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        bufferbuilder.pos(x + d3, y + height, z + d4).tex(0.0D, d14)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        tessellator.draw();

        //Reset state
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLightBeam entity)
    {
        return TEXTURE_BEACON_BEAM;
    }

}