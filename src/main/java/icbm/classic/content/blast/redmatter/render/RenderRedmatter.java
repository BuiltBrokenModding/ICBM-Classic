package icbm.classic.content.blast.redmatter.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import icbm.classic.ICBMConstants;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
import icbm.classic.lib.colors.ColorB;
import icbm.classic.lib.colors.ColorHelper;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * PORT: 1.12 drew with immediate mode GL (GLU Sphere, Tessellator, GlStateManager). Rewritten with
 * PoseStack + RenderType. Spheres are generated here (32x32 like GLU), lighting was disabled so
 * everything renders full bright. Beams use the lightning render type which has the same
 * SRC_ALPHA/ONE additive blending the 1.12 code set up.
 */
public class RenderRedmatter extends EntityRenderer<EntityRedmatter>
{
    public static final ResourceLocation TEXTURE_FILE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "entity/redmatter/blackhole.png");
    public static ResourceLocation GREY_TEXTURE = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "models/grey.png");

    public static List<ColorB> randomColorsForBeams = new ArrayList();

    public ColorB colorIn = new ColorB(255, 255, 255); //TODO figure out how this works
    public ColorB colorOut = new ColorB(0, 0, 0);

    private static final int SPHERE_SLICES = 32;
    private static final int SPHERE_STACKS = 32;

    public RenderRedmatter(EntityRendererProvider.Context context)
    {
        super(context);
    }

    @Override
    public void render(EntityRedmatter redmatter, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight)
    {
        final float visualSize = redmatter.clientLogic.getVisualSize();

        renderDisk(redmatter, poseStack, buffer, visualSize);
        renderSphere(redmatter, poseStack, buffer, visualSize);
        renderBeams(redmatter, poseStack, buffer, visualSize);

        //Update size with a smooth transition
        redmatter.clientLogic.lerpSize(partialTicks);

        super.render(redmatter, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    public void renderSphere(EntityRedmatter redmatter, PoseStack poseStack, MultiBufferSource buffer, float visualSize)
    {
        final float radius = Math.max(ConfigBlast.redmatter.RENDER_SCALE * visualSize, 0.1f);

        //Inside sphere, black
        // 1.12 used the grey texture with lighting enabled, the sphere is pure black so light makes no difference
        final VertexConsumer inner = buffer.getBuffer(RenderType.entityTranslucent(GREY_TEXTURE));
        drawSphere(poseStack, inner, radius * 0.8f, 0, 0, 0, 1);

        //Outside sphere, pulsing dark blue
        float ticks = redmatter.tickCount % 40;

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
        final VertexConsumer outer = buffer.getBuffer(RenderType.entityTranslucent(GREY_TEXTURE));
        drawSphere(poseStack, outer, radius + scaleDelta, 0, 0, 0.2f, 0.8f);
    }

    /** Replacement for GLU Sphere#draw(radius, 32, 32) */
    private static void drawSphere(PoseStack poseStack, VertexConsumer consumer, float radius, float r, float g, float b, float a)
    {
        final PoseStack.Pose pose = poseStack.last();
        for (int stack = 0; stack < SPHERE_STACKS; stack++)
        {
            final float phi0 = (float) Math.PI * stack / SPHERE_STACKS;
            final float phi1 = (float) Math.PI * (stack + 1) / SPHERE_STACKS;
            for (int slice = 0; slice < SPHERE_SLICES; slice++)
            {
                final float theta0 = (float) (Math.PI * 2) * slice / SPHERE_SLICES;
                final float theta1 = (float) (Math.PI * 2) * (slice + 1) / SPHERE_SLICES;

                sphereVertex(pose, consumer, radius, phi0, theta0, r, g, b, a);
                sphereVertex(pose, consumer, radius, phi1, theta0, r, g, b, a);
                sphereVertex(pose, consumer, radius, phi1, theta1, r, g, b, a);
                sphereVertex(pose, consumer, radius, phi0, theta1, r, g, b, a);
            }
        }
    }

    private static void sphereVertex(PoseStack.Pose pose, VertexConsumer consumer, float radius, float phi, float theta, float r, float g, float b, float a)
    {
        final float nx = Mth.sin(phi) * Mth.cos(theta);
        final float ny = Mth.cos(phi);
        final float nz = Mth.sin(phi) * Mth.sin(theta);
        consumer.addVertex(pose, nx * radius, ny * radius, nz * radius)
            .setColor(r, g, b, a)
            .setUv(0, 0)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, nx, ny, nz);
    }

    public void renderDisk(EntityRedmatter redmatter, PoseStack poseStack, MultiBufferSource buffer, float visualSize)
    {
        float size = (float) Math.max(0.2, ConfigBlast.redmatter.RENDER_SCALE * visualSize * 3);

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-redmatter.tickCount));

        final VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE_FILE));
        // TODO pick color randomly and pulse the color using sin(angle)

        //top render
        PoseStack.Pose pose = poseStack.last();
        diskVertex(pose, consumer, -size, -size, 0, 0);
        diskVertex(pose, consumer, -size, +size, 0, 1);
        diskVertex(pose, consumer, +size, +size, 1, 1);
        diskVertex(pose, consumer, +size, -size, 1, 0);

        //bottom render
        poseStack.mulPose(Axis.XP.rotationDegrees(180));
        pose = poseStack.last();
        diskVertex(pose, consumer, -size, -size, 1, 1);
        diskVertex(pose, consumer, -size, +size, 1, 0);
        diskVertex(pose, consumer, +size, +size, 0, 0);
        diskVertex(pose, consumer, +size, -size, 0, 1);

        poseStack.popPose();
    }

    private static void diskVertex(PoseStack.Pose pose, VertexConsumer consumer, float x, float z, float u, float v)
    {
        consumer.addVertex(pose, x, 0, z)
            .setColor(1f, 0f, 0f, 1f)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(LightTexture.FULL_BRIGHT)
            .setNormal(pose, 0, 1, 0);
    }

    public void renderBeams(EntityRedmatter redmatter, PoseStack poseStack, MultiBufferSource buffer, float visualSize)
    {
        //This is basically a copy of the ender dragon Lighting effect with modifications to fit
        //Animation has been changed to remove size scale and have a ramp up then down time.
        //Additional to this animation has been customized for control and visuals

        final int totalAnimationTime = 4000;
        final int rotationAnimationTime = 200;
        final float animationPhaseTime = totalAnimationTime / 2f;

        //Get tick value
        int ticks = redmatter.tickCount % totalAnimationTime;

        //Controls rotation of beams
        float rotationScale = (redmatter.tickCount % rotationAnimationTime) / (float) rotationAnimationTime;

        //Controls beam count and other factors based on animation phase time
        float timeScale;
        if (ticks < animationPhaseTime)
        {
            timeScale = (ticks / animationPhaseTime);
        }
        else
        {
            timeScale = (2 - (ticks / animationPhaseTime));
        }

        final VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());

        Random redmatterBeamRandom = new Random(432L);

        //0 - 61 for scale of 1
        final int beamCount = (int) ((timeScale + timeScale * timeScale) / 2.0F * 30.0F);
        for (int beamIndex = 0; beamIndex < beamCount; ++beamIndex)
        {
            //Start
            poseStack.pushPose();

            //Calculate size
            float beamLength = (redmatterBeamRandom.nextFloat() + 1F) * visualSize;
            float beamWidth = (float) Math.max(0.1, redmatterBeamRandom.nextFloat()) * visualSize / 10;

            //Random rotations TODO see if we need to rotate so much
            poseStack.mulPose(Axis.XP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(redmatterBeamRandom.nextFloat() * 360.0F + rotationScale * 360.0F));

            //Get color based on state
            ColorB colorOut = this.colorOut;
            ColorB colorIn = this.colorIn;
            if (ConfigBlast.redmatter.RENDER_COLORED_BEAMS)
            {
                if (beamIndex < randomColorsForBeams.size())
                {
                    colorOut = randomColorsForBeams.get(beamIndex);
                }
                else
                {
                    //to get rainbow, pastel colors https://stackoverflow.com/questions/4246351/creating-random-colour-in-java
                    final float hue = redmatterBeamRandom.nextFloat();
                    final float saturation = 0.9f;//1.0 for brilliant, 0.0 for dull
                    final float luminance = 0.5f; //1.0 for brighter, 0.0 for black
                    colorOut = ColorHelper.HSBtoRGB(hue, saturation, luminance);

                    randomColorsForBeams.add(colorOut);
                }
            }

            //Draw spike shape, 1.12 used a triangle fan (center + 4 outer points), drawn as 3 degenerate quads
            final Matrix4f matrix = poseStack.last().pose();
            final float ax = -0.866F * beamWidth, az = -0.5F * beamWidth;
            final float bx = 0.866F * beamWidth, bz = -0.5F * beamWidth;
            final float cx = 0.0F, cz = 1.0F * beamWidth;
            beamTriangle(consumer, matrix, colorIn, colorOut, beamLength, ax, az, bx, bz);
            beamTriangle(consumer, matrix, colorIn, colorOut, beamLength, bx, bz, cx, cz);
            beamTriangle(consumer, matrix, colorIn, colorOut, beamLength, cx, cz, ax, az);

            //end
            poseStack.popPose();
        }
    }

    private static void beamTriangle(VertexConsumer consumer, Matrix4f matrix, ColorB colorIn, ColorB colorOut, float length, float x1, float z1, float x2, float z2)
    {
        consumer.addVertex(matrix, 0, 0, 0).setColor(colorIn.getRed(), colorIn.getGreen(), colorIn.getBlue(), colorIn.getAlpha());
        consumer.addVertex(matrix, x1, length, z1).setColor(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha());
        consumer.addVertex(matrix, x2, length, z2).setColor(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha());
        consumer.addVertex(matrix, x2, length, z2).setColor(colorOut.getRed(), colorOut.getGreen(), colorOut.getBlue(), colorOut.getAlpha());
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRedmatter entity)
    {
        return GREY_TEXTURE;
    }
}
// PORTED
