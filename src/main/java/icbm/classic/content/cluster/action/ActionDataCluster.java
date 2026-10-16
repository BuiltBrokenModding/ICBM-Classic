package icbm.classic.content.cluster.action;

import com.google.common.collect.ImmutableList;
import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.data.EntityActionTypes;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.data.meta.MetaTag;
import lombok.Getter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ActionDataCluster implements IActionData, INBTSerializable<CompoundTag> {
    public final static ResourceLocation REG_NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "entity.cluster.spawning");
    private final static ImmutableList<MetaTag> TAGS = ImmutableList.of(EntityActionTypes.ENTITY_CREATION);

    @Getter
    private final NonNullList<ItemStack> clusterSpawnEntries = NonNullList.create(); //TODO have cluster condense to stacks of 64 to save memory

    @Nonnull
    @Override
    public IAction create(Level world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor) {
        final ActionCluster cluster = new ActionCluster(world, new Vec3(x, y, z), source, this);
        cluster.setSpawnList(clusterSpawnEntries);
        if(fieldAccessor != null && fieldAccessor.hasField(ActionFields.HOST_ENTITY)) {
            final Entity host = fieldAccessor.getValue(ActionFields.HOST_ENTITY);
            if(host != null) {
                cluster.setSourcePitch(host.getXRot());
                cluster.setSourceYaw(host.getYRot());
            }
        }
        return cluster;
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return REG_NAME;
    }

    @Nonnull
    @Override
    public Collection<MetaTag> getTypeTags() {
        return TAGS;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        final CompoundTag save = new CompoundTag();
        final ListTag spawnEntries = new ListTag(); //TODO convert to node
        for (ItemStack stack : clusterSpawnEntries) {
            spawnEntries.add(stack.saveOptional(provider));
        }
        save.put("clusterSpawnEntries", spawnEntries);
        return save;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        final ListTag tagList = nbt.getList("clusterSpawnEntries", Tag.TAG_COMPOUND);
        clusterSpawnEntries.clear();
        for (int i = 0; i < tagList.size(); i++) {
            ItemStack stack = ItemStack.parseOptional(provider, tagList.getCompound(i));
            clusterSpawnEntries.add(stack);
        }
    }

    @Override
    public void register() {
        throw new NotImplementedException("Cluster is dynamic and doesn't register static");
    }
}
// PORTED
