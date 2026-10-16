package icbm.classic.client.render.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import icbm.classic.content.entity.EntityLightBeam;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * PORT: 1.12 drew the beam with the Tessellator and manual GL state like the 1.12 beacon. Now uses the vanilla
 * beacon beam render types: an opaque inner tube (1.12 drew it with blending off) and a translucent glow tube.
 */
public class RenderLightBeam extends EntityRenderer<EntityLightBeam>
{
    public static final ResourceLocation TEXTURE_BEACON_BEAM = ResourceLocation.parse("textures/entity/beacon_beam.png");

    public RenderLightBeam(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(EntityLightBeam beamEntity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        // PORT: 1.12 used 255 - posY, the world top is the max build height now
        int height = beamEntity.level().getMaxBuildHeight() - (int) beamEntity.getY();

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

        renderBeamSegment(poseStack, buffer, 0, -5, 0, partialTicks, 1, beamEntity.tickCount, //TODO instead of -5 raytrace to ground
                height,
                beamEntity.red, beamEntity.green, beamEntity.blue,
                beamRadius, beamGlowRadius);
    }

    public static void renderBeamSegment(PoseStack poseStack, MultiBufferSource buffer,
                                         double x, double y, double z,
                                         double partialTicks, double textureScale, double totalWorldTime, int height,
                                         float color_red, float color_green, float color_blue,
                                         double beamRadius, double glowRadius)
    {
        double time = totalWorldTime + partialTicks;
        double uv_animation = Mth.frac(time * 0.2D - (double) Mth.floor(time * 0.1D));

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

        // Inner beam, 1.12 had blending disabled so it is opaque
        renderBeamTube(poseStack, buffer.getBuffer(RenderType.beaconBeam(TEXTURE_BEACON_BEAM, false)),
                x, y, z, height, color_red, color_green, color_blue, 1.0F,
                nw_corner_x, nw_corner_z, ne_corner_x, ne_corner_z, sw_corner_x, sw_corner_z, se_corner_x, se_corner_z, uv_bottom, uv_top);

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

        // Glow, translucent
        renderBeamTube(poseStack, buffer.getBuffer(RenderType.beaconBeam(TEXTURE_BEACON_BEAM, true)),
                x, y, z, height, color_red, color_green, color_blue, 0.125F,
                nw_corner_x, nw_corner_z,
                ne_corner_x, ne_corner_z,
                sw_corner_x, sw_corner_z,
                se_corner_x, se_corner_z,
                uv_bottom, uv_top);
    }

    private static void renderBeamTube(PoseStack poseStack, VertexConsumer consumer,
                                       double x, double y, double z, int height,
                                       float color_red, float color_green, float color_blue, float alpha,
                                       double nw_corner_x, double nw_corner_z,
                                       double ne_corner_x, double ne_corner_z,
                                       double sw_corner_x, double sw_corner_z,
                                       double se_corner_x, double se_corner_z,
                                       double uv_bottom, double uv_top)
    {
        final PoseStack.Pose pose = poseStack.last();
        final float top = (float) (y + height);
        final float bottom = (float) y;
        final float vTop = (float) uv_top;
        final float vBottom = (float) uv_bottom;

        //Generate north face
        face(pose, consumer, x + nw_corner_x, z + nw_corner_z, x + ne_corner_x, z + ne_corner_z, top, bottom, vTop, vBottom, color_red, color_green, color_blue, alpha);

        //Generate south face
        face(pose, consumer, x + se_corner_x, z + se_corner_z, x + sw_corner_x, z + sw_corner_z, top, bottom, vTop, vBottom, color_red, color_green, color_blue, alpha);

        //Generate east face
        face(pose, consumer, x + ne_corner_x, z + ne_corner_z, x + se_corner_x, z + se_corner_z, top, bottom, vTop, vBottom, color_red, color_green, color_blue, alpha);

        //Generate west face
        face(pose, consumer, x + sw_corner_x, z + sw_corner_z, x + nw_corner_x, z + nw_corner_z, top, bottom, vTop, vBottom, color_red, color_green, color_blue, alpha);
    }

    /**
     * Emits one wall of the tube going from corner 1 (u = 1) to corner 2 (u = 0).
     * <p>
     * PORT: 1.12 called disableCull so each wall was visible from both sides. The beaconBeam render types cull
     * back faces, so the wall is emitted a second time in reverse winding with the same uv at each corner.
     * Culling keeps exactly one of the pair per view, so the glow layering and texture match 1.12.
     */
    private static void face(PoseStack.Pose pose, VertexConsumer consumer,
                             double x1, double z1, double x2, double z2,
                             float top, float bottom, float vTop, float vBottom,
                             float r, float g, float b, float a)
    {
        // 1.12 winding, front side faces into the tube
        vertex(pose, consumer, x1, top, z1, 1, vTop, r, g, b, a);
        vertex(pose, consumer, x1, bottom, z1, 1, vBottom, r, g, b, a);
        vertex(pose, consumer, x2, bottom, z2, 0, vBottom, r, g, b, a);
        vertex(pose, consumer, x2, top, z2, 0, vTop, r, g, b, a);

        // Reverse winding, front side faces out of the tube
        vertex(pose, consumer, x2, top, z2, 0, vTop, r, g, b, a);
        vertex(pose, consumer, x2, bottom, z2, 0, vBottom, r, g, b, a);
        vertex(pose, consumer, x1, bottom, z1, 1, vBottom, r, g, b, a);
        vertex(pose, consumer, x1, top, z1, 1, vTop, r, g, b, a);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, double x, float y, double z, float u, float v,
                               float r, float g, float b, float a)
    {
        consumer.addVertex(pose, (float) x, y, (float) z)
            .setColor(r, g, b, a)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityLightBeam entity)
    {
        return TEXTURE_BEACON_BEAM;
    }
}
// PORTED
