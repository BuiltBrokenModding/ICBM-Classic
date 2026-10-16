package icbm.classic.client.fx;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

/**
 * PORT: 1.12 particles indexed particles.png directly. 1.21 particles need a {@link SpriteSet}, the vanilla ones
 * are private inside the particle engine, so this rebuilds the vanilla smoke set (generic_7 .. generic_0) from
 * the particle atlas. ICBM particles are spawned directly through the particle engine like 1.12 did, so no
 * ParticleType registration is needed.
 * <p>
 * The particle atlas belongs to the particle engine and is only registered with the texture manager.
 * {@link Minecraft#getTextureAtlas(ResourceLocation)} only knows the model atlases (blocks, banners, ...) and
 * crashes when asked for the particle atlas, so it must not be used here.
 */
public final class SmokeSprites implements SpriteSet
{
    public static final SmokeSprites INSTANCE = new SmokeSprites();

    /** Vanilla smoke order, same as assets/minecraft/particles/smoke.json */
    private static final ResourceLocation[] SMOKE = new ResourceLocation[8];

    static
    {
        for (int i = 0; i < SMOKE.length; i++)
        {
            SMOKE[i] = ResourceLocation.withDefaultNamespace("generic_" + (7 - i));
        }
    }

    private SmokeSprites()
    {
    }

    /** Looked up on each call as sprites are replaced when resources reload */
    private static TextureAtlasSprite sprite(ResourceLocation name)
    {
        final AbstractTexture texture = Minecraft.getInstance().getTextureManager().getTexture(TextureAtlas.LOCATION_PARTICLES, null);
        if (texture instanceof TextureAtlas atlas)
        {
            return atlas.getSprite(name);
        }
        return Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(MissingTextureAtlasSprite.getLocation());
    }

    /** @return single sprite, used by particles that only rendered the first particle texture in 1.12 */
    public static TextureAtlasSprite generic(int index)
    {
        return sprite(ResourceLocation.withDefaultNamespace("generic_" + index));
    }

    @Override
    public TextureAtlasSprite get(int age, int lifetime)
    {
        return sprite(SMOKE[lifetime <= 0 ? 0 : Math.min(SMOKE.length - 1, age * (SMOKE.length - 1) / lifetime)]);
    }

    @Override
    public TextureAtlasSprite get(RandomSource random)
    {
        return sprite(SMOKE[random.nextInt(SMOKE.length)]);
    }
}
// PORTED
