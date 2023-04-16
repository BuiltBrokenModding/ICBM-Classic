package icbm.classic.content.blast.redmatter.render;

import icbm.classic.ICBMConstants;
import icbm.classic.lib.colors.ColorB;
import icbm.classic.lib.colors.ColorHelper;
import icbm.classic.config.blast.ConfigBlast;
import icbm.classic.content.blast.redmatter.EntityRedmatter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@SideOnly(Side.CLIENT)
public class RenderRedmatter extends Render<EntityRedmatter>
{
    public static final ResourceLocation TEXTURE_FILE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "entity/redmatter/blackhole.png");
    public static ResourceLocation GREY_TEXTURE = new ResourceLocation(ICBMConstants.DOMAIN, ICBMConstants.TEXTURE_DIRECTORY + "models/grey.png");

    public static List<ColorB> randomColorsForBeams = new ArrayList();

    public ColorB colorIn = new ColorB(255, 255, 255); //TODO figure out how this works
    public ColorB colorOut = new ColorB(0, 0, 0);

    public RenderRedmatter(RenderManager renderManager)
    {
        super(renderManager);
    }

    @Override
    public void doRender(EntityRedmatter redmatter, double x, double y, double z, float entityYaw, float partialTicks)
    {
        final float visualSize = redmatter.clientLogic.getVisualSize();

        renderDisk(redmatter, x, y, z, visualSize);
        GlStateManager.color(1, 1, 1, 1);

        renderSphere(redmatter, x, y, z, visualSize);
        GlStateManager.color(1, 1, 1, 1);

        renderBeams(redmatter, x, y, z, visualSize);
        GlStateManager.color(1, 1, 1, 1);

        //Update size with a smooth transition
        redmatter.clientLogic.lerpSize(partialTicks);
    }

    public void renderSphere(EntityRedmatter redmatter, double x, double y, double z, float visualSize)
    {
        final float radius = Math.max(ConfigBlast.redmatter.RENDER_SCALE * visualSize, 0.1f);

        //--------------------------------------------------
        //Inside sphere
        //Setup
        GlStateManager.pushMatrix();
        //GlStateManager.enableBlend();
        //GlStateManager.disableLighting();

        //Translate
        GlStateManager.translate((float) x, (float) y, (float) z);

        //Assign texture
        bindTexture(GREY_TEXTURE);

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

        float ticks = redmatter.ticksExisted % 40;

        //Setup
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        //Translate
        GlStateManager.translate((float) x, (float) y, (float) z);

        //Assign texture
        bindTexture(GREY_TEXTURE);

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
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public void renderDisk(EntityRedmatter redmatter, double x, double y, double z, float visualSize)
    {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        float size = (float)Math.max(0.2, ConfigBlast.redmatter.RENDER_SCALE * visualSize * 3);

        //Setup
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableLighting();

        //Translate
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(-redmatter.ticksExisted, 0, 1, 0);

        //Assign texture
        this.bindTexture(TEXTURE_FILE);
        GlStateManager.color(1, 0, 0, 1); // TODO pick color randomly and pulse the color using sin(angle)

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
        GlStateManager.popMatrix();
        RenderHelper.enableStandardItemLighting();
    }

    public void renderBeams(EntityRedmatter redmatter, double x, double y, double z, float visualSize)
    {
        //This is basically a copy of the ender dragon Lighting effect with modifications to fit
        //Animation has been changed to remove size scale and have a ramp up then down time.
        //Additional to this animation has been customized for control and visuals

        final int totalAnimationTime = 4000;
        final int rotationAnimationTime = 200;
        final float animationPhaseTime = totalAnimationTime / 2f;

        //Get buffer
        final BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderHelper.disableStandardItemLighting();

        //Get tick value
        int ticks = redmatter.ticksExisted % totalAnimationTime;

        //Controls rotation of beams
        float rotationScale = (redmatter.ticksExisted % rotationAnimationTime) / (float) rotationAnimationTime;

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

        //Start
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) x, (float) y, (float) z);

        //Setup
        GlStateManager.disableTexture2D();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        GlStateManager.disableAlpha();
        GlStateManager.enableCull();

        Random redmatterBeamRandom = new Random(432L);

        //0 - 61 for scale of 1
        final int beamCount = (int) ((timeScale + timeScale * timeScale) / 2.0F * 30.0F);
        for (int beamIndex = 0; beamIndex < beamCount; ++beamIndex)
        {
            //Start
            GlStateManager.pushMatrix();

            //Calculate size
            float beamLength = (redmatterBeamRandom.nextFloat() + 1F) * visualSize;
            float beamWidth = (float)Math.max(0.1, redmatterBeamRandom.nextFloat()) * visualSize / 10;

            //Random rotations TODO see if we need to rotate so much
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(redmatterBeamRandom.nextFloat() * 360.0F + rotationScale * 360.0F, 0.0F, 0.0F, 1.0F);

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

            //Draw spike shape
            bufferbuilder.begin(6, DefaultVertexFormats.POSITION_COLOR);

            //center vertex
            bufferbuilder.pos(0.0D, 0.0D, 0.0D)
                    .color(colorIn.getRed(), colorIn.getGreen(), colorIn.getBlue(), colorIn.getAlpha())
                    .endVertex();

            //Outside vertex
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

            //draw
            Tessellator.getInstance().draw();

            //end
            GlStateManager.popMatrix();
        }

        //Cleanup
        GlStateManager.disableCull();
        GlStateManager.disableBlend();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.enableTexture2D();
        GlStateManager.enableAlpha();
        RenderHelper.enableStandardItemLighting();

        //End
        GlStateManager.popMatrix();
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRedmatter entity)
    {
        return GREY_TEXTURE;
    }
}
