package icbm.classic.client.fx;

import icbm.classic.lib.transform.vector.Pos;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.TextureSheetParticle;

/**
 * PORT: 1.12 Particle rendered particle texture index 0 by default, that is generic_0 in 1.21.
 */
public class FXAntimatterParticle extends TextureSheetParticle
{
    public FXAntimatterParticle(ClientLevel par1World, Pos position, double par8, double par10, double par12, double distance)
    {
        this(par1World, position, par8, par10, par12, 1.0F, distance);
    }

    public FXAntimatterParticle(ClientLevel par1World, Pos position, double par8, double par10, double par12, float par14, double distance)
    {
        super(par1World, position.x(), position.y(), position.z(), 0.0D, 0.0D, 0.0D);
        this.xd *= 0.10000000149011612D;
        this.yd *= 0.10000000149011612D;
        this.zd *= 0.10000000149011612D;
        this.xd += par8;
        this.yd += par10;
        this.zd += par12;
        this.rCol = this.gCol = this.bCol = (float) (Math.random() * 0.30000001192092896D);
        this.quadSize *= 0.75F;
        this.quadSize *= par14;
        this.lifetime = (int) (10D / (Math.random() * 0.8D + 0.2D));
        this.lifetime = (int) (this.lifetime * par14);
        this.setSprite(SmokeSprites.generic(0));
    }

    @Override
    protected int getLightColor(float partialTick)
    {
        return 240;
    }

    @Override
    public ParticleRenderType getRenderType()
    {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    /** Called to update the entity's position/logic. */
    @Override
    public void tick()
    {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime)
        {
            this.remove();
        }
    }
}
// PORTED
