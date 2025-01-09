package icbm.classic.content.blocks.launcher.screen;

import icbm.classic.api.radio.IRadioMessage;
import icbm.classic.api.radio.IRadioReceiver;
import icbm.classic.api.radio.IRadioSender;
import icbm.classic.api.radio.messages.ITargetMessage;
import icbm.classic.api.radio.messages.ITriggerActionMessage;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.lib.radio.imp.RadioTile;
import icbm.classic.lib.radio.messages.RadioTranslations;
import icbm.classic.lib.radio.messages.TextMessage;
import net.minecraft.util.math.Vec3d;

public class RadioScreen extends RadioTile<TileLauncherScreen> implements IRadioReceiver {

    public RadioScreen(TileLauncherScreen host) {
        super(host);
    }

    @Override
    public void onMessage(IRadioSender sender, IRadioMessage packet) {
        if (canReceive(sender, packet)) {

            // Set target packet, run first as laser-det triggers both (set & fire) from the same packet
            if(packet instanceof ITargetMessage) {
                final double vel = host.getLaunchersInGroup()
                    .stream().mapToDouble(launcherEntry -> launcherEntry.getLauncher().getPayloadVelocity()).max()
                    .orElse(ConfigMissile.DIRECT_FLIGHT_SPEED);
                final Vec3d target = ((ITargetMessage) packet).getIntercept(host.getPos().getX() + 0.5, host.getPos().getY() + 0.5, host.getPos().getZ() + 0.5, vel);
                if(target != null) {
                    host.setTarget(target);

                    // Don't show set message if we are going to fire right away
                    if(!(packet instanceof ITriggerActionMessage)) {
                        sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_TARGET_SET, target.x, target.y, target.z));
                    }
                }
                else {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_TARGET_NULL));
                }
            }

            // Fire missile packet
            if(packet instanceof ITriggerActionMessage && ((ITriggerActionMessage) packet).shouldTrigger()) {
                if (host.fireAllLaunchers(false)) { // TODO collect all screens and provide a single feedback message
                    final Vec3d target = host.getTarget();
                    final double distance = Math.sqrt(target.squareDistanceTo(host.getPos().getX() + 0.5, host.getPos().getY() + 0.5, host.getPos().getZ() + 0.5)); // TODO base from launcher
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_SUCCESS, target.x, target.y, target.z, distance));

                    //((ILaunchMessage) packet).onLaunchCallback(); TODO implement
                }
                else {
                    sender.onMessageCallback(this, new TextMessage(getChannel(), RadioTranslations.RADIO_LAUNCH_FAILED));
                }
            }
        }
    }
}
