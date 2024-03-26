package icbm.classic.lib.capability.missile;

import icbm.classic.IcbmConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.ExplosiveType;
import icbm.classic.world.missile.entity.explosive.ExplosiveMissileEntity;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NBTBase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.common.capabilities.Capability;
import net.neoforged.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Applied to {@link ItemStack} that are missiles
 */
public class CapabilityMissileStack implements ICapabilityMissileStack {
    private final ItemStack stack; //TODO decouple from stack and directly save init data

    public CapabilityMissileStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public String getMissileId() {
        return IcbmConstants.PREFIX + "missile["
            + Optional.ofNullable(ICBMClassicHelpers.getExplosive(stack))
            .map(IExplosive::getExplosiveData)
            .map(ExplosiveType::getRegistryName)
            .map(Object::toString)
            .orElse("unknown")
            + "]";
    }

    @Override
    public IMissile newMissile(Level level) {
        final ExplosiveMissileEntity missile = new ExplosiveMissileEntity(world);
        missile.explosive.setStack(stack);
        return missile.getMissileCapability();
    }

    public static void register() {
        CapabilityManager.INSTANCE.register(ICapabilityMissileStack.class, new Capability.IStorage<ICapabilityMissileStack>() {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, Direction side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, Direction side, NBTBase nbt) {
                }
            },
            () -> new CapabilityMissileStack(ItemStack.EMPTY));
    }
}
