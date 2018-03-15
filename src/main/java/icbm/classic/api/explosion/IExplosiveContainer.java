package icbm.classic.api.explosion;

import net.minecraft.nbt.NBTTagCompound;

/** An object that contains a reference to IExplosive. Carried by explosives, grenades and missile
 * entities etc.
 *
 * @author Calclavia */
@Deprecated //Will be replaced with capabilities
public interface IExplosiveContainer
{
    NBTTagCompound getExplosiveData();

    IExplosive getExplosiveType();
}
