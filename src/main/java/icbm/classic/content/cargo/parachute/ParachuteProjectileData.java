package icbm.classic.content.cargo.parachute;

import icbm.classic.ICBMConstants;
import icbm.classic.content.cargo.CargoProjectileData;
import lombok.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

@EqualsAndHashCode(callSuper = false)
@ToString
public class ParachuteProjectileData extends CargoProjectileData<ParachuteProjectileData, EntityParachute> {

    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.parachute");

    @Override
    public EntityParachute newEntity(World world, boolean allowItemPicku) {
        return new EntityParachute(world); //.setRenderItemStack(parachute);
    }

    @Nonnull
    @Override
    public ResourceLocation getRegistryKey() {
        return NAME;
    }
}
