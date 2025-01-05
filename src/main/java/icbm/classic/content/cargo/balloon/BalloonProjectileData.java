package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@Value
public class BalloonProjectileData extends CargoProjectileData<BalloonProjectileData, EntityBalloon> {

    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.balloon");

    NonNullSupplier<EntityType<EntityBalloon>> entityType;

    @Override
    public EntityBalloon newEntity(World world, boolean allowItemPicku) {
        return entityType.get().create(world); // TODO encode coloring
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
