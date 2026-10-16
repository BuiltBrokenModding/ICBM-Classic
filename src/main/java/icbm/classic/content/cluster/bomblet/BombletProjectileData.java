package icbm.classic.content.cluster.bomblet;

import com.google.common.collect.ImmutableList;
import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.data.meta.MetaTag;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.IProjectileDataRegistry;
import icbm.classic.api.missiles.projectile.ProjectileTypes;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import java.util.Collection;

@NoArgsConstructor
public class BombletProjectileData implements IProjectileData<EntityBombDroplet>, INBTSerializable<CompoundTag> {

    public static final ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "bomblet");
    public static final ImmutableList<MetaTag> TYPES = ImmutableList.of(ProjectileTypes.TYPE_EXPLOSIVE);

    @Setter @Getter @Accessors(chain = true)
    private ItemStack explosiveStack = ItemStack.EMPTY;

    @Nonnull
    public Collection<MetaTag> getTypeTags() {
        return TYPES;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }

    @Nonnull
    @Override
    public IProjectileDataRegistry getRegistry() {
        return ICBMClassicAPI.PROJECTILE_DATA_REGISTRY;
    }

    @Override
    public EntityBombDroplet newEntity(Level world, boolean allowItemPickup) {
        final EntityBombDroplet bombDroplet = new EntityBombDroplet(world);
        bombDroplet.explosive.setStack(explosiveStack);
        return bombDroplet;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag save = new CompoundTag();
        save.put("explosive", explosiveStack.saveOptional(provider));
        return save;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag save) {
        explosiveStack = ItemStack.parseOptional(provider, save.getCompound("explosive"));
    }
}
// PORTED
