package icbm.classic.api.caps;

import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.nbt.NBTTagCompound;

import javax.annotation.Nullable;

/**
 * Used in capabilities to provide an explosive for usage
 * Created by Dark(DarkGuardsman, Robert) on 1/7/19.
 */
public interface IExplosiveProvider
{

    /**
     * Gets the explosive provided
     *
     * @return
     */
    @Nullable
    IExplosiveData getExplosiveData();

    /**
     * Gets the custom NBT data to encode into
     * any blast created from the explosive
     *
     * @return nbt
     */
    @Nullable
    NBTTagCompound getCustomBlastData();
}
