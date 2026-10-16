package icbm.classic.content.cargo.parachute;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@ToString
public class ParachuteProjectileData extends CargoProjectileData<ParachuteProjectileData, EntityParachute> {

    public final static ResourceLocation NAME = ResourceLocation.fromNamespaceAndPath(ICBMConstants.DOMAIN, "holder.parachute");

    @Override
    public EntityParachute newEntity(Level world, boolean allowItemPicku) {
        return new EntityParachute(world); //.setRenderItemStack(parachute);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
// PORTED
