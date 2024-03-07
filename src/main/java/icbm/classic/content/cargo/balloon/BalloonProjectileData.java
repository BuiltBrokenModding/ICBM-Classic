package icbm.classic.content.cargo.balloon;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import icbm.classic.content.cargo.parachute.EntityParachute;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@ToString
public class BalloonProjectileData extends CargoProjectileData<BalloonProjectileData, EntityBalloon> {

    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.balloon");

    @Override
    public EntityBalloon newEntity(World world, boolean allowItemPicku) {
        return new EntityBalloon(world); // TODO encode item used for coloring
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
