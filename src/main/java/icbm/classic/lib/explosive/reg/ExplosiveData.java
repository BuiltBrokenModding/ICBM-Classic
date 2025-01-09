package icbm.classic.lib.explosive.reg;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.actions.ActionDataBase;
import lombok.ToString;
import lombok.Value;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handles storing data about an explosive in the {@link ExplosiveRegistry}
 * @deprecated
 */
@ToString(of={"registryKey"})
@Value
public class ExplosiveData extends ActionDataBase implements IExplosiveData
{
    @Nonnull
    ResourceLocation registryKey;

    @Nonnull
    IBlastFactory blastCreationFactory;

    @Override
    @Nonnull
    public IAction create(World world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessors) {
        final IAction blast = blastCreationFactory.create(world, x, y, z, source);

        if(blast instanceof IBlastInit) {
            ((IBlastInit)blast).setExplosiveData(this);
            ((IBlastInit)blast).setActionSource(source);

            if (fieldAccessors != null && fieldAccessors.hasField(ActionFields.AREA_SIZE)) {
                ((IBlastInit)blast).setBlastSize(fieldAccessors.getValue(ActionFields.AREA_SIZE));
            }
        }

        blast.applyFields(fieldAccessors);
        blast.applyFields(this);

        return blast;
    }

    @Override
    public boolean equals(Object object)
    {
        if(object instanceof ExplosiveData)
        {
            return ((ExplosiveData) object).registryKey.equals(this.registryKey);
        }
        return false;
    }
}
