package icbm.classic.client.render.entity;

import icbm.classic.content.entity.EntityLightBeam;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
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

        final float beamGrowthRate = beamEntity.beamGrowthRate * partialTicks;
        if (beamEntity.clientBeamProgress < beamEntity.getBeamProgress())
        {
            beamEntity.clientBeamProgress = Math.min(beamEntity.getBeamProgress(), beamEntity.clientBeamProgress + beamGrowthRate);
        }
        //Decrease size slowly
        else if (beamEntity.clientBeamProgress > beamEntity.getBeamProgress())
        {
            beamEntity.clientBeamProgress = Math.max(beamEntity.getBeamProgress(), beamEntity.clientBeamProgress - beamGrowthRate);
        }

        float beamRadius = Math.max(0, beamEntity.clientBeamProgress * beamEntity.beamSize);
        float beamGlowRadius = Math.max(0, beamEntity.clientBeamProgress * beamEntity.beamGlowSize);

        renderBeamSegment(x, y - 5, z, partialTicks, 1, beamEntity.ticksExisted, //TODO instead of -5 raytrace to ground
                height,
                beamEntity.red, beamEntity.green, beamEntity.blue,
                beamRadius, beamGlowRadius);

        GlStateManager.enableFog();
    }


    public static void renderBeamSegment(double x, double y, double z,
                                         double partialTicks, double textureScale, double totalWorldTime, int height,
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

        double time = totalWorldTime + partialTicks;
        double uv_animation = MathHelper.frac(time * 0.2D - (double) MathHelper.floor(time * 0.1D));

        double nw_corner_x = 0.5D - beamRadius;
        double nw_corner_z = 0.5D - beamRadius;
        double ne_corner_x = 0.5D + beamRadius;
        double ne_corner_z = 0.5D - beamRadius;
        double sw_corner_x = 0.5D - beamRadius;
        double sw_corner_z = 0.5D + beamRadius;
        double se_corner_x = 0.5D + beamRadius;
        double se_corner_z = 0.5D + beamRadius;
        double uv_bottom = -1.0D + uv_animation;
        double uv_top = (double) height * textureScale + uv_bottom;

        renderBeamTube(x, y, z, height, color_red, color_green, color_blue, tessellator, bufferbuilder, nw_corner_x, nw_corner_z, ne_corner_x, ne_corner_z, sw_corner_x, sw_corner_z, se_corner_x, se_corner_z, uv_bottom, uv_top);


        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.depthMask(false);

        nw_corner_x = 0.5D - glowRadius;
        nw_corner_z = 0.5D - glowRadius;
        ne_corner_x = 0.5D + glowRadius;
        ne_corner_z = 0.5D - glowRadius;

        sw_corner_x = 0.5D - glowRadius;
        sw_corner_z = 0.5D + glowRadius;
        se_corner_x = 0.5D + glowRadius;
        se_corner_z = 0.5D + glowRadius;

        uv_bottom = -1.0D;
        uv_top = (double) height * textureScale + uv_bottom;

        renderBeamTube(x, y, z, height, color_red, color_green, color_blue, tessellator, bufferbuilder,
                nw_corner_x, nw_corner_z,
                ne_corner_x, ne_corner_z,
                sw_corner_x, sw_corner_z,
                se_corner_x, se_corner_z,
                uv_bottom, uv_top);

        //Reset state
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    private static void renderBeamTube(double x, double y, double z, int height,
                                       float color_red, float color_green, float color_blue,
                                       Tessellator tessellator, BufferBuilder bufferbuilder,
                                       double nw_corner_x, double nw_corner_z,
                                       double ne_corner_x, double ne_corner_z,
                                       double sw_corner_x, double sw_corner_z,
                                       double se_corner_x, double se_corner_z,
                                       double uv_bottom, double uv_top)
    {
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX_COLOR);

        //Generate north face
        //-------------------------------------

        //NW corner top
        bufferbuilder.pos(x + nw_corner_x, y + height, z + nw_corner_z).tex(1.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NE corner bottom
        bufferbuilder.pos(x + nw_corner_x, y, z + nw_corner_z).tex(1.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NE corner bottom
        bufferbuilder.pos(x + ne_corner_x, y, z + ne_corner_z).tex(0.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NE corner top
        bufferbuilder.pos(x + ne_corner_x, y + height, z + ne_corner_z).tex(0.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //Generate south face
        //-------------------------------------

        //SE corner top
        bufferbuilder.pos(x + se_corner_x, y + height, z + se_corner_z).tex(1.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SE corner bottom
        bufferbuilder.pos(x + se_corner_x, y, z + se_corner_z).tex(1.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SW corner bottom
        bufferbuilder.pos(x + sw_corner_x, y, z + sw_corner_z).tex(0.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SW corner top
        bufferbuilder.pos(x + sw_corner_x, y + height, z + sw_corner_z).tex(0.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //Generate east face
        //-------------------------------------

        //NE corner top
        bufferbuilder.pos(x + ne_corner_x, y + height, z + ne_corner_z).tex(1.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NE corner bottom
        bufferbuilder.pos(x + ne_corner_x, y, z + ne_corner_z).tex(1.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SE corner bottom
        bufferbuilder.pos(x + se_corner_x, y, z + se_corner_z).tex(0.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SE corner top
        bufferbuilder.pos(x + se_corner_x, y + height, z + se_corner_z).tex(0.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //Generate west face
        //-------------------------------------

        //SW corner top
        bufferbuilder.pos(x + sw_corner_x, y + height, z + sw_corner_z).tex(1.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //SW corner bottom
        bufferbuilder.pos(x + sw_corner_x, y, z + sw_corner_z).tex(1.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NW corner bottom
        bufferbuilder.pos(x + nw_corner_x, y, z + nw_corner_z).tex(0.0D, uv_bottom)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();

        //NW corner top
        bufferbuilder.pos(x + nw_corner_x, y + height, z + nw_corner_z).tex(0.0D, uv_top)
                .color(color_red, color_green, color_blue, 0.125F).endVertex();
        tessellator.draw();
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityLightBeam entity)
    {
        return TEXTURE_BEACON_BEAM;
    }

}