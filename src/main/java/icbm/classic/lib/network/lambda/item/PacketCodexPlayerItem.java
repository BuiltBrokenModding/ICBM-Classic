package icbm.classic.lib.network.lambda.item;

import icbm.classic.lib.network.lambda.PacketCodex;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class PacketCodexPlayerItem extends PacketCodex<PlayerTargetSlot, ItemStack> {

    public PacketCodexPlayerItem(ResourceLocation parent, ResourceLocation name) {
        super(parent, name, (target) -> target.getPlayer().inventory.getStackInSlot(target.getSlot()));
    }

    @Override
    public PacketLambdaPlayerItem build(PlayerTargetSlot target) {
        return new PacketLambdaPlayerItem(this, target, getConverter().apply(target));
    }

    @Override
    public boolean isValid(PlayerTargetSlot target) {
        // unused
        return true;
    }

}
