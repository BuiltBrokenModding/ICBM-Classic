package icbm.classic.lib.network.packet;

import icbm.classic.api.events.LaserRemoteTriggerEvent;
import icbm.classic.content.reg.ItemReg;
import icbm.classic.lib.radio.RadioRegistry;
import icbm.classic.lib.radio.messages.TriggerActionTargetMessage;
import icbm.classic.prefab.FakeRadioSender;
import icbm.classic.prefab.item.ItemRadio;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PacketLaserDetonator {
    private int dimId;
    private Vec3d target;
    private Hand hand;
    private String channel;

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeInt(dimId);
        packetBuffer.writeDouble(target.x);
        packetBuffer.writeDouble(target.y);
        packetBuffer.writeDouble(target.z);
        packetBuffer.writeEnumValue(hand);
        packetBuffer.writeString(channel);
    }

    public static PacketLaserDetonator decode(PacketBuffer packetBuffer) {
        final PacketLaserDetonator packet = new PacketLaserDetonator();
        packet.dimId = packetBuffer.readInt();
        packet.target = new Vec3d(packetBuffer.readDouble(), packetBuffer.readDouble(), packetBuffer.readDouble());
        packet.hand = packetBuffer.readEnumValue(Hand.class);
        packet.channel = packetBuffer.readString();
        return packet;
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        if(context.getDirection() == NetworkDirection.PLAY_TO_SERVER) {
            context.setPacketHandled(true);

            final PlayerEntity player = context.getSender();
            if(player == null) {
                return;
            }
            else if(player.world.getDimension().getType().getId() != dimId) {
                return;
            }

            final ItemStack stack = player.getHeldItem(hand);
            if(stack.getItem() != ItemReg.TOOL_DETONATOR_LASER.get()) {
                return;
            }

            ((ServerWorld) player.getEntityWorld()).getServer().execute(() -> {

                final LaserRemoteTriggerEvent event = new LaserRemoteTriggerEvent(player.world, target, player);
                if (!MinecraftForge.EVENT_BUS.post(event)) {
                    player.sendStatusMessage(new TranslationTextComponent(
                        stack.getTranslationKey() + ".target",
                        String.format("%.2f", event.getPos().x),
                        String.format("%.2f", event.getPos().y),
                        String.format("%.2f", event.getPos().z)
                    ), false);

                    RadioRegistry.popMessage(player.world, new FakeRadioSender(player, stack, null), new TriggerActionTargetMessage(channel, event.getPos()));
                }
                else if(event.cancelReason != null) {
                    player.sendStatusMessage(new TranslationTextComponent(event.cancelReason), true);
                }
                else {
                    player.sendStatusMessage(new TranslationTextComponent(stack.getTranslationKey() + ".laser.canceled"), false);
                }
            });
        }
    }
}
