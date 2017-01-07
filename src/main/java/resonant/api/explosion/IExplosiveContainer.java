package resonant.api.explosion;

import icbm.classic.content.explosive.Explosive;
import net.minecraft.nbt.NBTTagCompound;

/** An object that contains a reference to IExplosive. Carried by explosives, grenades and missile
 * entities etc.
 *
 * @author Calclavia */
public interface IExplosiveContainer
{
    public NBTTagCompound getTagCompound();

    public Explosive getExplosiveType();
}
