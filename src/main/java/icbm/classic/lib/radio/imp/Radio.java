package icbm.classic.lib.radio.imp;

import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;


public abstract class Radio implements IRadio, INBTSerializable<NBTTagCompound>, IRadioChannelAccess {
    private String channel;

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        if(channel == null) {
            return RadioRegistry.EMPTY_HZ;
        }
        return channel;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound tag = new NBTTagCompound();
        if(channel != null) {
            tag.setString("channel", channel);
        }
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.channel = nbt.getString("channel");
    }
}
