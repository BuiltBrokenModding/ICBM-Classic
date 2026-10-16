package icbm.classic.api.actions.data;

import net.minecraft.nbt.Tag;

/**
 * Wrapper to provide fields
 *
 */
public interface IActionFieldReceiver {

    /**
     * Called to set a value
     *
     * @param key to set
     * @param value to set
     */
    default <VALUE, TAG extends Tag> void setValue(ActionField<VALUE, TAG> key, VALUE value) {

    }

    default <SELF extends IActionFieldReceiver> SELF applyFields(IActionFieldProvider fieldAccessor) {
        if(fieldAccessor != null && !fieldAccessor.getFields().isEmpty()) {
            for(ActionField field : fieldAccessor.getFields()) {
                this.setValue(field, fieldAccessor.getValue(field));
            }
        }
        return (SELF)this;
    }
}
// PORTED
