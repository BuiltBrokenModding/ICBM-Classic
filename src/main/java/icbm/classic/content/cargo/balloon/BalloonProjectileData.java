package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@ToString
public class BalloonProjectileData extends CargoProjectileData<BalloonProjectileData, EntityBalloon> {

    public final static ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "holder.balloon");

    @Override
    public EntityBalloon newEntity(Level world, boolean allowItemPicku) {
        return new EntityBalloon(world); // TODO encode item used for coloring
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
// PORTED
