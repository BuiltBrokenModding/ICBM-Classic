package icbm.classic.content.machines.launcher;

import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.lib.helper.LanguageUtility;
import com.builtbroken.mc.lib.transform.region.Cube;
import com.builtbroken.mc.lib.transform.vector.Pos;
import com.builtbroken.mc.lib.world.radio.RadioRegistry;
import icbm.classic.prefab.TileFrequency;
import net.minecraft.block.material.Material;
import net.minecraft.nbt.NBTTagCompound;

public abstract class TileLauncherPrefab extends TileFrequency implements IRadioWaveReceiver
{
    protected Pos targetPos = null;

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
        if (this.targetPos == null)
        {
            if (targetWithYValue())
            {
                this.targetPos = new Pos(this.xCoord, this.yCoord, this.zCoord);
            }
            else
            {
                this.targetPos = new Pos(this.xCoord, 0, this.zCoord);
            }
        }

        return this.targetPos;
    }

    public boolean targetWithYValue()
    {
        return false;
    }

    public void setTarget(Pos target)
    {
        this.targetPos = target.floor();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt)
    {
        super.readFromNBT(nbt);
        this.targetPos = new Pos(nbt.getCompoundTag("target"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt)
    {
        super.writeToNBT(nbt);

        if (this.targetPos != null)
        {
            nbt.setTag("target", this.targetPos.toNBT());
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
}
