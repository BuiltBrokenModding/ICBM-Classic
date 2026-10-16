package icbm.classic.lib.actions;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.actions.data.IActionFieldReceiver;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.HashMap;
import java.util.Map;

public abstract class ActionDataBase implements IActionData, IActionFieldProvider, IActionFieldReceiver, INBTSerializable<CompoundTag> {

    private final Map<ActionField, Object> fieldValueMap = new HashMap<>();

    @Override
    public <VALUE, TAG extends Tag> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
       if(this.getFields().contains(key)) {
           this.fieldValueMap.put(key, value);
       }
    }

    @Override
    public <VALUE, TAG extends Tag> VALUE getValue(ActionField<VALUE, TAG> key) {
        return key.cast(this.fieldValueMap.get(key));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag tag = new CompoundTag();
        if(!fieldValueMap.isEmpty()) {
            final ListTag list = new ListTag();
            for(Map.Entry<ActionField, Object> entry : fieldValueMap.entrySet()) {
                final CompoundTag entryTag = new CompoundTag();
                entryTag.putString("key", entry.getKey().getKey()); //save key even if value is null
                //TODO see if we can save ActionField#type
                if(entry.getValue() != null) {
                    Tag valueSave = entry.getKey().save(entry.getValue());
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
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if(nbt.contains("fields")) {
            this.fieldValueMap.clear();
            final ListTag list = nbt.getList("fields", 10);
            for(int i = 0; i < list.size(); i++) {
                final CompoundTag entryTag = list.getCompound(i);
                final String key = entryTag.getString("key");
                final ActionField actionField = ActionField.find(key, null);
                if(actionField != null && entryTag.contains("value")) {
                    this.fieldValueMap.put(actionField, actionField.load(entryTag.get("value")));
                }
            }
        }
    }
}
// PORTED
