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
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import org.apache.commons.lang3.NotImplementedException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ActionDataCluster implements IActionData, INBTSerializable<CompoundNBT> {
    public final static ResourceLocation REG_NAME = new ResourceLocation(ICBMConstants.DOMAIN, "entity.cluster.spawning");
    private final static ImmutableList<MetaTag> TAGS = ImmutableList.of(EntityActionTypes.ENTITY_CREATION);

    @Getter
    private final NonNullList<ItemStack> clusterSpawnEntries = NonNullList.create(); //TODO have cluster condense to stacks of 64 to save memory

    @Nonnull
    @Override
    public IAction create(World world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor) {
        final ActionCluster cluster = new ActionCluster(world, new Vec3d(x, y, z), source, this);
        cluster.setSpawnList(clusterSpawnEntries);
        if(fieldAccessor != null && fieldAccessor.hasField(ActionFields.HOST_ENTITY)) {
            final Entity host = fieldAccessor.getValue(ActionFields.HOST_ENTITY);
            if(host != null) {
                cluster.setSourcePitch(host.rotationPitch);
                cluster.setSourceYaw(host.rotationYaw);
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
    public CompoundNBT serializeNBT() {
        final CompoundNBT save = new CompoundNBT();
        final ListNBT spawnEntries = new ListNBT(); //TODO convert to node
        for (ItemStack stack : clusterSpawnEntries) {
            spawnEntries.add(stack.serializeNBT());
        }
        save.put("clusterSpawnEntries", spawnEntries);
        return save;
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        final ListNBT tagList = nbt.getList("clusterSpawnEntries", 10);
        clusterSpawnEntries.clear();
        for (int i = 0; i < tagList.size(); i++) {
            ItemStack stack = ItemStack.read(tagList.getCompound(i));
            clusterSpawnEntries.add(stack);
        }
    }

    /** @deprecated will be replaced by field provider to avoid mutable action instances */
    @Deprecated
    public ActionDataCluster copy() {
        final ActionDataCluster clone = new ActionDataCluster();
        clone.deserializeNBT(serializeNBT());
        return clone;
    }

    @Override
    public void register() {
        throw new NotImplementedException("Cluster is dynamic and doesn't register static");
    }
}
