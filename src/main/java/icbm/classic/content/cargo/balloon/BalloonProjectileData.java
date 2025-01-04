package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Value;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@Value
public class BalloonProjectileData extends CargoProjectileData<BalloonProjectileData, EntityBalloon> {

    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.balloon");

    EntityType<EntityBalloon> entityType;

    @Override
    public EntityBalloon newEntity(World world, boolean allowItemPicku) {
        return entityType.create(world); // TODO encode coloring
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
