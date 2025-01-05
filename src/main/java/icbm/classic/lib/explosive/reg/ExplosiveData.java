package icbm.classic.lib.explosive.reg;

import icbm.classic.api.EnumTier;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.explosion.IBlastFactory;
import icbm.classic.api.explosion.IBlastInit;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.lib.actions.ActionDataBase;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Handles storing data about an explosive in the {@link ExplosiveRegistry}
 * @deprecated
 */
@ToString(of={"regName", "id"})
@RequiredArgsConstructor
@Data
public class ExplosiveData extends ActionDataBase implements IExplosiveData
{
    @Nonnull
    public final ResourceLocation regName;

    /** Will be removed in 1.13 */
    @Deprecated
    private final int id;

    @Nonnull
    private final IBlastFactory blastCreationFactory;

    @Override
    public ResourceLocation getRegistryKey()
    {
        return regName;
    }

    @Override
    public int getRegistryID()
    {
        return id;
    }

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
            return ((ExplosiveData) object).id == id;
        }
        return false;
    }

    @Override
    public int compareTo(IExplosiveData o)
    {
        return Integer.compare(getRegistryID(), o.getRegistryID());
    }
}
