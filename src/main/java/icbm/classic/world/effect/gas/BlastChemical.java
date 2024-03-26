package icbm.classic.world.effect.gas;

import icbm.classic.ICBMClassic;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.entity.EntityLivingBase;

public class BlastChemical extends BlastGasBase {

    public static final int DURATION = 20 * 30; //TODO move to config

    public static final float red = 0.8f, green = 0.8f, blue = 0;


    public BlastChemical() {
        super(DURATION, false);
    }

    @Override
    protected boolean canEffectEntities() {
        return true;
    }

    @Override
    protected boolean canGasEffect(EntityLivingBase entity) {
        return super.canGasEffect(entity) && !entity.isEntityInvulnerable(CHEMICAL_DAMAGE);
    }

    @Override
    protected void applyEffect(final EntityLivingBase entity, final int hitCount) {
        ICBMClassic.chemicalPotion.poisonEntity(location.toPos(), entity);
        if (hitCount > 20) {
            entity.aattackEntityFrom(CHEMICAL_DAMAGE, (hitCount - 10f) / 10);
        }
    }

    @Override
    protected float getParticleColorRed(final Vec3i pos) {
        return red;
    }

    @Override
    protected float getParticleColorGreen(final Vec3i pos) {
        return green;
    }

    @Override
    protected float getParticleColorBlue(final Vec3i pos) {
        return blue;
    }
}
