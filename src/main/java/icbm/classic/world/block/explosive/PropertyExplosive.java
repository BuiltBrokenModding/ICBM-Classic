package icbm.classic.world.block.explosive;

import com.google.common.base.Optional;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.ExplosiveType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

/**
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2019.
 */
public class PropertyExplosive implements IProperty<ExplosiveType> {
    @Override
    public String getName() {
        return "explosive";
    }

    @Override
    public Collection<ExplosiveType> getAllowedValues() {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives();
    }

    @Override
    public Class<ExplosiveType> getValueClass() {
        return ExplosiveType.class;
    }

    @Override
    public Optional<ExplosiveType> parseValue(String value) {
        return Optional.fromNullable(ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(new ResourceLocation(value.replace("_", ":"))));
    }

    @Override
    public String getName(ExplosiveType value) {
        return value.getRegistryName().toString().replaceAll(":", "_");
    }
}
