package icbm.classic.content.cluster.bomblet;

import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import lombok.Value;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.NonNullSupplier;

import java.util.function.Supplier;

@Value
public class BombletProjectileStack implements IProjectileStack<EntityBombDroplet> {

    private final NonNullSupplier<EntityType<EntityBombDroplet>> entityType;

    @Override
    public IProjectileData<EntityBombDroplet> getProjectileData() {
        return new BombletProjectileData().setEntityType(entityType.get());
    }
}
