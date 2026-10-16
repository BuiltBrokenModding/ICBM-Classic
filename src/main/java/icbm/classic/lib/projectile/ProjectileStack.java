package icbm.classic.lib.projectile;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.content.cargo.parachute.EntityParachute;
import icbm.classic.lib.StackNbt;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nullable;

public class ProjectileStack implements IProjectileStack<EntityParachute>, INBTSerializable<CompoundTag> {

    /** Custom data key the 1.12 capability state is stored under, matches the 1.12 cap provider key */
    public static final String STACK_KEY = "projectile";

    @Getter
    private IProjectileData projectileData;

    /** PORT: item capabilities are rebuilt per lookup, state is read from and written to this stack */
    @Nullable
    private final ItemStack host;

    public ProjectileStack() {
        this.host = null;
    }

    public ProjectileStack(ItemStack host) {
        this.host = host;
        if (StackNbt.has(host, STACK_KEY)) {
            SAVE_LOGIC.load(this, StackNbt.getCompound(host, STACK_KEY));
        }
    }

    public ProjectileStack setProjectileData(IProjectileData projectileData) {
        this.projectileData = projectileData;
        saveToHost();
        return this;
    }

    /** Writes the current state back to the host stack, call after mutating {@link #getProjectileData()} */
    public void saveToHost() {
        if (host != null) {
            StackNbt.putCompound(host, STACK_KEY, SAVE_LOGIC.save(this));
        }
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag save) {
        SAVE_LOGIC.load(this, save);
    }

    private void loadData(IProjectileData data) {
        this.projectileData = data;
    }

    private static final NbtSaveHandler<ProjectileStack> SAVE_LOGIC = new NbtSaveHandler<ProjectileStack>()
        .mainRoot()
        .nodeBuildableObject("data", () -> ICBMClassicAPI.PROJECTILE_DATA_REGISTRY, ProjectileStack::getProjectileData, ProjectileStack::loadData)
        .base();
}
// PORTED
