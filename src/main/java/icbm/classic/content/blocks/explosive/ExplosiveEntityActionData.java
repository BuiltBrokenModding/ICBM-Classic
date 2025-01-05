package icbm.classic.content.blocks.explosive;

import com.google.common.collect.ImmutableList;
import icbm.classic.ICBMConstants;
import icbm.classic.api.actions.IAction;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.EntityActionTypes;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.data.meta.MetaTag;
import icbm.classic.content.entity.EntityExplosive;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;

public class ExplosiveEntityActionData implements IActionData {
    private final static ImmutableList<MetaTag> TAGS = ImmutableList.of(EntityActionTypes.ENTITY_CREATION);

    private final ResourceLocation name;
    private final NonNullSupplier<EntityType<EntityExplosive>> entityType;

    public ExplosiveEntityActionData(String name, NonNullSupplier<EntityType<EntityExplosive>> entityType) {
        this.name = new ResourceLocation(ICBMConstants.DOMAIN, "entity_explosive_" + name + "_spawning");
        this.entityType = entityType;
    }

    @Nonnull
    @Override
    public IAction create(World world, double x, double y, double z, @Nonnull IActionSource source, @Nullable IActionFieldProvider fieldAccessor) {
        //TODO if fuse is zero or less spawn non-entity action
        return new ExplosiveEntitySpawnAction(world, source.getPosition(), source, this, entityType).applyFields(fieldAccessor);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return name;
    }

    @Nonnull
    @Override
    public Collection<MetaTag> getTypeTags() {
        return TAGS;
    }
}
