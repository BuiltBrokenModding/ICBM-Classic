package icbm.classic.api.missiles.parts;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.common.util.INBTSerializable;

/**
 * Implemented by objects that can be recreated from save or packet data
 */
public interface IBuildableObject extends INBTSerializable<CompoundTag> {

    /**
     * Name of the type of part. Used for save/load
     *
     * @return registry name
     */
    ResourceLocation getRegistryName();
}
