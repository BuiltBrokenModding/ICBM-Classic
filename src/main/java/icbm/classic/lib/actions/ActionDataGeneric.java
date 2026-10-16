package icbm.classic.lib.actions;

import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.IActionFieldProvider;
import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;


public final class ActionDataGeneric extends ActionDataBase {

    private final CreateAction createAction;
    @Getter
    private final ResourceLocation registryKey;
    private final Collection<ActionField> supportedFields;

    public ActionDataGeneric(ResourceLocation registryKey, CreateAction createAction, Collection<ActionField> supportedFields) {
        this.createAction = createAction;
        this.registryKey = registryKey;
        this.supportedFields = supportedFields;
    }

    @Override
    public Collection<ActionField> getFields() {
        if(supportedFields != null) {
            return supportedFields;
        }
        return Collections.EMPTY_LIST;
    }

    @Nonnull
    @Override
    public IAction create(Level world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor) {
        return createAction.create(world, x, y, z, source, this)
            .applyFields(this)
            .applyFields(fieldAccessor);
    }

    public interface CreateAction {
        IAction create(Level world, double x, double y, double z, @Nonnull IActionSource source, IActionData data);
    }
}
// PORTED
