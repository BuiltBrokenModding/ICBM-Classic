package icbm.classic.lib.capability.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import icbm.classic.content.reg.EntityReg;
import lombok.Value;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Applied to {@link ItemStack} that are missiles
 */
@Value
public class CapabilityMissileStack implements ICapabilityMissileStack
{
    NonNullSupplier<EntityType<EntityExplosiveMissile>> entityType;

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile[" + entityType.get().getRegistryName().toString() + "]";
    }

    @Override
    public IMissile newMissile(World world)
    {
        return entityType.get().create(world).getMissileCapability();
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ICapabilityMissileStack.class, new Capability.IStorage<ICapabilityMissileStack>()
            {
                @Nullable
                @Override
                public INBT writeNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, Direction side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, Direction side, INBT nbt)
                {
                }
            },
            () -> new CapabilityMissileStack(EntityReg.MISSILE_CONDENSED::get));
    }
}
