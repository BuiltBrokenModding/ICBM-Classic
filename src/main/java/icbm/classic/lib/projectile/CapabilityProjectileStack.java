package icbm.classic.lib.projectile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;

/**
 * Applied to {@link ItemStack} that are projectiles
 */
public class CapabilityProjectileStack implements IProjectileStack<Entity>
{
    private final ResourceLocation projectileDataKey;

    public CapabilityProjectileStack(ResourceLocation projectileDataKey) {
        this.projectileDataKey = projectileDataKey;
    }

    @Override
    public IProjectileData<Entity> getProjectileData() {
        return ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.getOrBuild(projectileDataKey);
    }

    // PORT: 1.12 register() (CapabilityManager) removed, NeoForge capabilities need no storage registration
}
// PORTED
