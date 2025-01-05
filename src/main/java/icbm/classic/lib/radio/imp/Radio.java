package icbm.classic.lib.radio.imp;

import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.RandomStringUtils;


public abstract class Radio implements IRadio, INBTSerializable<CompoundNBT>, IRadioChannelAccess {

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
    public CompoundNBT serializeNBT() {
        final CompoundNBT tag = new CompoundNBT();
        if(channel != null) {
            tag.putString("channel", channel);
        }
        tag.putBoolean("disabled", isDisabled);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        this.channel = nbt.getString("channel");
        this.isDisabled = nbt.getBoolean("disabled");
    }
}
