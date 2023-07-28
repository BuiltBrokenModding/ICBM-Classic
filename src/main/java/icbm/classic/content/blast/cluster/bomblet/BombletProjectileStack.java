package icbm.classic.content.blast.cluster.bomblet;

import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import net.minecraft.item.ItemStack;

import java.util.function.Supplier;

public class BombletProjectileStack implements IProjectileStack<EntityBombDroplet> {

    private final Supplier<ItemStack> stack;

    public BombletProjectileStack(Supplier<ItemStack> stack) {
        this.stack = stack;
    }

    @Override
    public IProjectileData<EntityBombDroplet> getProjectileData() {
        return new BombletProjectileData().setExplosiveStack(stack.get());
    }
}
