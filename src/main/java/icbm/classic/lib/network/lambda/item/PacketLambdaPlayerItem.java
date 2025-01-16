package icbm.classic.lib.network.lambda.item;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.lambda.PacketCodex;
import icbm.classic.lib.network.lambda.PacketCodexReg;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Data
@NoArgsConstructor
public class PacketLambdaPlayerItem {

    public static final String ERROR_HANDLING = "unexpected error writing to player inventory in Slot(%s, %s)";
    public static final String ERROR_NOT_SERVER = "Received packet server side but world(%s) is not WorldServer";
    public static final String ERROR_ITEM_MISMATCH = "Expected Item(%s) but found Item(%s)\nSlot(%s) ItemStack: %s";
    public static final String WARN_SLOT_BOUNDS = "Slot %s is out of bounds for player inventory size of %s";
    public static final String WARN_MISSING_ITEM_KEY = "Item key is not present on packet";

    private PacketCodexPlayerItem codex;
    private ResourceLocation itemKey;
    private int slot;

    private List<Consumer<PacketBuffer>> writers;
    private List<Consumer<ItemStack>> setters;

    public PacketLambdaPlayerItem(PacketCodexPlayerItem codex, PlayerTargetSlot slot, ItemStack target) {
        this.codex = codex;
        this.writers = codex.encodeAsWriters(target);
        this.itemKey = slot.getItemKey();
        this.slot = slot.getSlot();
    }

    public void encode(PacketBuffer buffer) {
        // Write general data
        buffer.writeInt(codex.getId());
        buffer.writeInt(slot);
        buffer.writeResourceLocation(itemKey);

        // Write data from builder
        writers.forEach(c -> c.accept(buffer));
    }

    public static PacketLambdaPlayerItem decode(PacketBuffer buffer) {
        final PacketLambdaPlayerItem packet = new PacketLambdaPlayerItem();

        // Read general data
        final int codexId = buffer.readInt();
        final PacketCodex codexFound = PacketCodexReg.get(codexId);
        if (!(codexFound instanceof PacketCodexPlayerItem)) {
            ICBMClassic.logger().error(String.format("PacketHeldItem: Failed to locate codex(%s)", codexId));
            return null;
        }
        packet.codex = (PacketCodexPlayerItem) codexFound;

        packet.slot = buffer.readInt();
        packet.itemKey = buffer.readResourceLocation();

        // Read data for builder
        packet.setters = packet.codex.decodeAsSetters(buffer);

        return packet;

    }

    public static void handle(PacketLambdaPlayerItem packet, Supplier<NetworkEvent.Context> contextSupplier) {
        final NetworkEvent.Context context = contextSupplier.get();
        switch (context.getDirection()) {
            case PLAY_TO_CLIENT:
                context.enqueueWork(packet::handleClientSide);
                context.setPacketHandled(true);
                break;
            case PLAY_TO_SERVER:
                if (context.getSender() != null) {
                    context.enqueueWork(() -> packet.handleServerSide(Objects.requireNonNull(contextSupplier.get().getSender())));
                    context.setPacketHandled(true);
                } else {
                    ICBMClassic.logger().error("Received packet with no sender.\n\tContext: {} \n\tPacket: {}", context, packet);
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + contextSupplier.get().getDirection());
        }
    }

    @OnlyIn(Dist.CLIENT)
    public void handleClientSide() {
        loadDataIntoItem(Minecraft.getInstance().player);
    }

    public void handleServerSide(PlayerEntity player) {
        loadDataIntoItem(player);
    }

    private void loadDataIntoItem(PlayerEntity player) {

        // Bad dev case, shouldn't happen -> ignore missing keys
        if (this.itemKey == null) {
            codex.logDebug(player.world, player.getPosition(), WARN_MISSING_ITEM_KEY);
            return;
        }

        // Bad dev case, shouldn't happen -> Ignore slots outside player range
        if (this.slot < 0 || this.slot >= player.inventory.getSizeInventory()) {
            codex.logDebug(player.world, player.getPosition(), String.format(WARN_MISSING_ITEM_KEY, slot, player.inventory.getSizeInventory()));
            return;
        }

        final PlayerTargetSlot targetSlot = new PlayerTargetSlot(player, this.itemKey, this.slot);

        // Get stack and copy to avoid mutations of original
        final ItemStack stack = player.inventory.getStackInSlot(this.slot).copy();

        // Verify we are the right target
        if(stack.isEmpty() || !this.itemKey.equals(stack.getItem().getRegistryName())) {
            codex.logDebug(player.world, player.getPosition(), String.format(ERROR_ITEM_MISMATCH, itemKey, stack.getItem(), targetSlot, stack));
            return;
        }

        try {
            setters.forEach(c -> c.accept(stack)); //TODO detect for issues and log so we know which setter failed

            if (codex.onFinished() != null) {
                codex.onFinished().accept(targetSlot, stack, player);
            }

            // Save changes
            player.inventory.setInventorySlotContents(this.slot, stack);
            if(!player.world.isRemote) {
                player.container.detectAndSendChanges();
            }

        } catch (Exception e) {
            codex.logError(player.world, player.getPosition(), String.format(ERROR_HANDLING, this.slot, stack), e);
        }
    }
}
