package icbm.classic.lib.radio.imp;

import icbm.classic.api.radio.IRadio;
import icbm.classic.api.radio.IRadioChannelAccess;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.RandomStringUtils;


public abstract class Radio implements IRadio, INBTSerializable<CompoundTag>, IRadioChannelAccess {

    private String channel;

    @Getter
    @Setter
    private boolean isDisabled = false;

    @Override
    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannel() {
        if (channel == null) {
            this.channel = RandomStringUtils.random(4, true, true);
        }
        return channel;
    }

    @Override
    public CompoundTag serializeNBT() {
        final CompoundTag tag = new CompoundTag();
        if (channel != null) {
            tag.putString("channel", channel);
        }
        tag.setBoolean("disabled", isDisabled);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.channel = nbt.getString("channel");
        this.isDisabled = nbt.getBoolean("disabled");
    }
}
