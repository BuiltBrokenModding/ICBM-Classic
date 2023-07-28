package icbm.classic.lib.projectile;

import icbm.classic.api.missiles.projectile.IProjectileData;
import lombok.Data;
import net.minecraft.item.ItemStack;

import java.util.function.Function;

@Data
public class ProjectileItemConversion {
    private final ItemStack sortStack;
    private final Function<ItemStack, IProjectileData> builder;
}
