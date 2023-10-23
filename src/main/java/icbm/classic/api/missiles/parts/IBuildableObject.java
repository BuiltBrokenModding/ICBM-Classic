package icbm.classic.api.missiles.parts;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;

/**
 * Implemented by objects that can be recreated from save or packet data
 */
public interface IBuildableObject extends INBTSerializable<NBTTagCompound> {

    /**
     * Name of the type of part. Used for save/load
     *
     * @return registry name
     */
    ResourceLocation getRegistryName();
}
