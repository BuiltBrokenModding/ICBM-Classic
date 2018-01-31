package icbm.classic.content.machines.launcher;

import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.world.map.radio.RadioRegistry;
import icbm.classic.prefab.TileFrequency;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileLauncherPrefab extends TileFrequency implements IRadioWaveReceiver
{
    /** Target position of the launcher */
    private Pos _targetPos = Pos.zero;

    @Override
    public void onLoad()
    {
        super.onLoad();
        if (isServer())
        {
            RadioRegistry.add(this);
        }
    }

    @Override
    public void invalidate()
    {
        RadioRegistry.remove(this);
        super.invalidate();
    }

    public Pos getTarget()
    {
        if (this._targetPos == null)
        {
            if (targetWithYValue())
            {
                this._targetPos = new Pos(getPos());
            }
            else
            {
                this._targetPos = new Pos(getPos().getX(), 0, getPos().getZ());
            }
        }

        return this._targetPos;
    }

    /**
     * Should we use the Y value when setting the target data
     * into the missile
     *
     * @return true if yes
     */
    public boolean targetWithYValue()
    {
        return false;
    }

    /**
     * Called to set the target
     *
     * @param target
     */
    public void setTarget(Pos target)
    {
        this._targetPos = target.floor();
        updateClient = true;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this._targetPos = new Pos(nbt.getCompoundTag("target"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt)
    {
        if (this._targetPos != null)
        {
            nbt.setTag("target", this._targetPos.toNBT());
        }

        return super.writeToNBT(nbt);
    }

    public String getStatus()
    {
        String color = "\u00a74";
        String status = LanguageUtility.getLocal("gui.misc.idle");
        return color + status;
    }

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data)
    {

    }

    @Override
    public Cube getRadioReceiverRange()
    {
        return RadioRegistry.INFINITE;
    }

    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        updateClient = true;
    }
}
