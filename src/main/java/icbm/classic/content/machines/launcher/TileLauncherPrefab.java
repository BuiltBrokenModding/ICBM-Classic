package icbm.classic.content.machines.launcher;

import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.radio.RadioRegistry;
import icbm.classic.prefab.TileFrequency;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileLauncherPrefab extends TileFrequency implements IRadioWaveReceiver
{
    /** Target position of the launcher */
    private Pos _targetPos = null;

    /**
     * Creates a new TileMachine instance
     *
     * @param name     - name of the tile
     * @param material - material of the tile
     */
    public TileLauncherPrefab(String name, Material material)
    {
        super(name, material);
    }

    @Override
    public void firstTick()
    {
        super.firstTick();
        RadioRegistry.add(this);
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
                this._targetPos = new Pos(this.xCoord, this.yCoord, this.zCoord);
            }
            else
            {
                this._targetPos = new Pos(this.xCoord, 0, this.zCoord);
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
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (this._targetPos != null)
        {
            nbt.setTag("target", this._targetPos.toNBT());
        }
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
        return new Cube();
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        updateClient = true;
    }
}
