package icbm.classic.lib.projectile;

import icbm.classic.api.missiles.projectile.IProjectileData;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import net.minecraft.item.ItemStack;

import java.util.function.Function;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class CachedProjectileData implements Function<ItemStack, IProjectileData> {

    private final Supplier<IProjectileData> builder;

    private IProjectileData instance;
    @Override
    public IProjectileData apply(ItemStack itemStack) {
        if(instance != null) {
            instance = builder.get();
        }
        return instance;
    }
}
