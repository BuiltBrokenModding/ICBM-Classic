package icbm.classic.content.actions;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionProvider;
import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.data.meta.MetaTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

/**
 * General use action provider for dynamic action setups
 */
public final class ActionProvider implements IActionProvider, INBTSerializable<CompoundTag> {
    private final Map<MetaTag, IPotentialAction> actions = new HashMap();

    public ActionProvider withAction(MetaTag tag, IPotentialAction action) {
        actions.put(tag, action);
        return this;
    }

    @Nullable
    @Override
    public IPotentialAction getPotentialAction(MetaTag key) {
        return actions.get(key); //TODO do key#isSubType
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag compound = new CompoundTag();
        ListTag list = new ListTag();
        actions.forEach((tag, action) -> {
            final CompoundTag entry = new CompoundTag();
            entry.putString("key", tag.getKey());
            entry.put("value", ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY.save(action));
            list.add(entry);
        });
        compound.put("actions", list);
        return compound;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        if(nbt.contains("actions")) {
            ListTag list = nbt.getList("actions", 10);
            actions.clear();
            for (int i = 0; i < list.size(); i++) {
                CompoundTag entry = list.getCompound(i);
                String keyString = entry.getString("key");
                CompoundTag valueTag = entry.getCompound("value");
                MetaTag key = MetaTag.find(keyString);
                IPotentialAction value = ICBMClassicAPI.ACTION_POTENTIAL_REGISTRY.load(valueTag);
                actions.put(key, value);
            }
        }
    }

    /**
     * 1.12 registered a Forge capability storage + factory for {@link IActionProvider}. NeoForge 1.21 capabilities
     * need no registration of storage/factories and nothing exposes an IActionProvider capability, so this is a no-op.
     * Kept so callers ({@code ActionSystem#setup}) don't change.
     */
    public static void register()
    {
        // PORT: no capability registration needed in NeoForge 1.21
    }
}
// PORTED
