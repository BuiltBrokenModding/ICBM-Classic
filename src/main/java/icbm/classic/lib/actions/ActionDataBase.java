package icbm.classic.lib.actions;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.actions.data.IActionFieldReceiver;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionDataBase implements IActionData, IActionFieldProvider, IActionFieldReceiver, INBTSerializable<CompoundNBT> {

    private final Map<ActionField, Object> fieldValueMap = new HashMap<>();

    @Override
    public <VALUE, TAG extends INBT> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
       if(this.getFields().contains(key)) {
           this.fieldValueMap.put(key, value);
       }
    }

    @Override
    public <VALUE, TAG extends INBT> VALUE getValue(ActionField<VALUE, TAG> key) {
        return key.cast(this.fieldValueMap.get(key));
    }

    @Override
    public CompoundNBT serializeNBT() {
        final CompoundNBT tag = new CompoundNBT();
        if(!fieldValueMap.isEmpty()) {
            final ListNBT list = new ListNBT();
            for(Map.Entry<ActionField, Object> entry : fieldValueMap.entrySet()) {
                final CompoundNBT entryTag = new CompoundNBT();
                entryTag.putString("key", entry.getKey().getKey()); //save key even if value is null
                //TODO see if we can save ActionField#type
                if(entry.getValue() != null) {
                    INBT valueSave = entry.getKey().save(entry.getValue());
                    if(valueSave != null) {
                        entryTag.put("value", valueSave);
                    }
                }
                list.add(entryTag);
            }
            tag.put("fields", list);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        if(nbt.contains("fields")) {
            this.fieldValueMap.clear();
            final ListNBT list = nbt.getList("fields", 10);
            for(int i = 0; i < list.size(); i++) {
                final CompoundNBT entryTag = list.getCompound(i);
                final String key = entryTag.getString("key");
                final ActionField actionField = ActionField.find(key, null);
                if(actionField != null && entryTag.contains("value")) {
                    this.fieldValueMap.put(actionField, actionField.load(entryTag.get("value")));
                }
            }
        }
    }
}
