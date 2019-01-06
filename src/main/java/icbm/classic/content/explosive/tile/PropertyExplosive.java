package icbm.classic.content.explosive.tile;

import com.google.common.base.Optional;
import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.reg.IExplosiveData;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.ResourceLocation;

import java.util.Collection;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 1/6/2019.
 */
public class PropertyExplosive implements IProperty<IExplosiveData>
{
    @Override
    public String getName()
    {
        return "explosive";
    }

    @Override
    public Collection<IExplosiveData> getAllowedValues()
    {
        return ICBMClassicAPI.EX_BLOCK_REGISTRY.getExplosives();
    }

    @Override
    public Class<IExplosiveData> getValueClass()
    {
        return IExplosiveData.class;
    }

    @Override
    public Optional<IExplosiveData> parseValue(String value)
    {
        return Optional.fromNullable(ICBMClassicAPI.EXPLOSIVE_REGISTRY.getExplosiveData(new ResourceLocation(value)));
    }

    @Override
    public String getName(IExplosiveData value)
    {
        return value.getRegistryName().toString();
    }
}
