package icbm.classic.content.blocks.explosive;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Optional;

/**
 * Created by Dark(DarkGuardsman, Robin) on 1/6/2019.
 * <p>
 * PORT: no longer a blockstate property (1.21 properties must have a fixed value set and can't read a registry
 * that is filled during common setup). The explosive lives in {@link TileEntityExplosive} and reaches the model
 * through {@link TileEntityExplosive#EXPLOSIVE_PROPERTY}. The naming helpers are kept for model lookups.
 */
public class PropertyExplosive
{
    public String getName()
    {
        return "explosive";
    }

    public Collection<IExplosiveData> getAllowedValues()
    {
        return ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosives();
    }

    public Class<IExplosiveData> getValueClass()
    {
        return IExplosiveData.class;
    }

    public Optional<IExplosiveData> parseValue(String value)
    {
        return Optional.ofNullable(ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(ResourceLocation.parse(value.replace("_", ":"))));
    }

    public String getName(IExplosiveData value)
    {
        return value.getRegistryKey().toString().replaceAll(":", "_");
    }
}
// PORTED
