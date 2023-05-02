package icbm.classic.lib.radio.imp;

import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import icbm.classic.lib.radio.RadioRegistry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.RandomStringUtils;


public abstract class Radio implements IRadio, INBTSerializable<NBTTagCompound>, IRadioChannelAccess {

    private String channel;

    @Getter @Setter
    private boolean isDisabled = false;

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        if(channel == null) {
            this.channel = RandomStringUtils.random(4, true, true);
        }
        return channel;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound tag = new NBTTagCompound();
        if(channel != null) {
            tag.setString("channel", channel);
        }
        tag.setBoolean("disabled", isDisabled);
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        this.channel = nbt.getString("channel");
        this.isDisabled = nbt.getBoolean("disabled");
    }
}
