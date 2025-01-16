package icbm.classic.lib.network.lambda.item;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;

@Value
@AllArgsConstructor
public class PlayerTargetSlot {
    PlayerEntity player;
    ResourceLocation itemKey;
    int slot;

    public PlayerTargetSlot(PlayerEntity player, int slot) {
        this.player = player;
        this.slot = slot;
        this.itemKey = player.inventory.getStackInSlot(slot).getItem().getRegistryName();
    }
}
