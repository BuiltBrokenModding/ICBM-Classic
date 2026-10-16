package icbm.classic.lib.capability.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Optional;

/**
 * Applied to {@link ItemStack} that are missiles
 */
public class CapabilityMissileStack implements ICapabilityMissileStack
{
    private final ItemStack stack; //TODO decouple from stack and directly save init data

    public CapabilityMissileStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile["
            + Optional.ofNullable(ICBMClassicHelpers.getExplosive(stack))
            .map(IExplosive::getExplosiveData)
            .map(IExplosiveData::getRegistryKey)
            .map(Object::toString)
            .orElse("unknown")
            + "]";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.explosive.setStack(stack);
        // PORT: health was set in the constructor before the explosive (and so its tier) was known
        missile.setHealth(missile.getMaxHealth());
        return missile.getMissileCapability();
    }

    // PORT: register() (CapabilityManager storage) removed, not needed in NeoForge
}
// PORTED
