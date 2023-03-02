package icbm.classic.api.missiles.parts;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Implemented by objects used to create a missile instance object.
 *
 * Each part takes on a different aspect of the missile such as
 * flight logic, targeting data, source, and itemstack
 */
public interface IMissilePart extends INBTSerializable<NBTTagCompound> {

    /**
     * Name of the type of part. Used for save/load
     *
     * @return registry name
     */
    ResourceLocation getRegistryName();
}
