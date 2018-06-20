package icbm.classic.content.machines.launcher;

import com.builtbroken.mc.api.map.radio.IRadioWaveReceiver;
import com.builtbroken.mc.api.map.radio.IRadioWaveSender;
import com.builtbroken.mc.imp.transform.region.Cube;
import com.builtbroken.mc.imp.transform.vector.Pos;
import com.builtbroken.mc.lib.world.map.radio.RadioRegistry;
import icbm.classic.content.explosive.Explosives;
import icbm.classic.content.items.ItemMissile;
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

    public abstract ItemStack getMissileStack();

    public String getMissileTypeName()
    {
        ItemStack stack = getMissileStack();
        if (stack != null)
        {
            if(stack.getItem() instanceof ItemMissile)
            {
                int meta = stack.getItemDamage();
                if (meta >= 0 && meta < Explosives.values().length)
                {
                    return Explosives.values()[meta].name().toLowerCase();
                }
                return "invalid type";
            }
            return "no missile";
        }
        return "empty";
    }

    public Explosives getMissileType()
    {
        ItemStack stack = getMissileStack();
        if (stack != null && stack.getItem() instanceof ItemMissile)
        {
            int meta = stack.getItemDamage();
            if (meta >= 0 && meta < Explosives.values().length)
            {
                return Explosives.values()[meta];
            }
        }
        return null;
    }

    public Pos getTarget()
    {
        if (this._targetPos == null)
        {
            setTarget(this.xCoord, this.yCoord, this.zCoord);
        }
        return this._targetPos;
    }

    public void setTarget(double x, double y, double z)
    {
        if (targetWithYValue())
        {
            this._targetPos = new Pos(x, y, z);
        }
        else
        {
            this._targetPos = new Pos(x, 0, z);
        }
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

    @Override
    public void receiveRadioWave(float hz, IRadioWaveSender sender, String messageHeader, Object[] data)
    {

    }

    @Override
    public Cube getRadioReceiverRange()
    {
        return RadioRegistry.INFINITE;
    }

    @Override
    public void onInventoryChanged(int slot, ItemStack prev, ItemStack item)
    {
        updateClient = true;
    }
}
