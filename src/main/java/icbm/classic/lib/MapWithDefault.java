package icbm.classic.lib;

import java.util.HashMap;


public class MapWithDefault<K, V> extends HashMap<K, V>
{

    private V defaultValue;

    public void setDefaultValue(V value)
    {
        this.defaultValue = value;
    }

    @Override
    public V get(Object key)
    {
        final V value = super.get(key);
        if (value == null)
        {
            return defaultValue;
        }
        return value;
    }
}
// PORTED
