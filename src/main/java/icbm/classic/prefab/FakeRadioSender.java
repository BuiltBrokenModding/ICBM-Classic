package icbm.classic.prefab;

import icbm.classic.api.data.IBoundBox;
import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.api.radio.messages.ITextMessage;
import icbm.classic.lib.data.BoundBlockPos;
import icbm.classic.lib.radio.RadioRegistry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.Optional;

/**
 * Wrapper used by items to act as a radio wave sender
 *
 *
 * Created by Dark(DarkGuardsman, Robert) on 4/24/2016.
 */
public class FakeRadioSender implements IRadioSender
{
    private static final Object[] empty = new Object[0];

    public final EntityPlayer player;
    public final ItemStack item;
    IBoundBox<BlockPos> bounds;

    public FakeRadioSender(EntityPlayer player, ItemStack item, Integer range)
    {
        this.player = player;
        this.item = item;
        if(range != null) {
            this.bounds = new BoundBlockPos(player.getPosition(), range);
        }
        else {
            this.bounds = RadioRegistry.INFINITE;
        }
    }

    @Override
    public BlockPos getBlockPos() {
        return player.getPosition();
    }

    @Override
    public World getWorld() {
        return player.getEntityWorld();
    }

    @Override
    public IBoundBox<BlockPos> getRange() {
        return bounds;
    }

    @Override
    public void onMessageCallback(IRadioReceiver receiver, IRadioMessage response) {
        if(response instanceof ITextMessage) {
            final ITextMessage textMessage = (ITextMessage) response;
            if(textMessage.shouldTranslate()) {
                final Object[] data = Optional.ofNullable(textMessage.getTranslationInputs()).orElse(empty);
                player.sendStatusMessage(new TextComponentTranslation(textMessage.getMessage(), data), true);
            }
            else {
                player.sendStatusMessage(new TextComponentString(((ITextMessage) response).getMessage()), true);
            }
        }
    }
}
