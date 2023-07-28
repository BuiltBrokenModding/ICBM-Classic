package icbm.classic.lib.projectile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.lib.projectile.vanilla.ArrowProjectileData;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

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
        return ICBMClassicAPI.PROJECTILE_DATA_REGISTRY.build(projectileDataKey);
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IProjectileStack.class, new Capability.IStorage<IProjectileStack>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IProjectileStack> capability, IProjectileStack instance, EnumFacing side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IProjectileStack> capability, IProjectileStack instance, EnumFacing side, NBTBase nbt) {

                }
            },
            () -> new CapabilityProjectileStack(ArrowProjectileData.NAME));
    }
}
