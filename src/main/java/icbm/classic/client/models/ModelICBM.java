package icbm.classic.client.models;

import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Function;

/**
 * Base for ICBM models.
 * <p>
 * PORT: 1.21 {@link Model} needs a render type factory and subclasses implement
 * {@code renderToBuffer(PoseStack, VertexConsumer, int light, int overlay, int color)}.
 */
public abstract class ModelICBM extends Model
{
    public ModelICBM()
    {
        this(RenderType::entitySolid);
    }

    public ModelICBM(Function<ResourceLocation, RenderType> renderType)
    {
        super(renderType);
    }

    /** 1.12 render hook, kept for subclasses. Rendering goes through renderToBuffer now. */
    public void render(float f5)
    {

    }
}
// PORTED
