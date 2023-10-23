package icbm.classic.lib.capability.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.ICBMClassicHelpers;
import icbm.classic.api.caps.IExplosive;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.api.reg.IExplosiveData;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;
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
            .map(IExplosiveData::getRegistryName)
            .map(Object::toString)
            .orElse("unknown")
            + "]";
    }

    @Override
    public IMissile newMissile(World world)
    {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.explosive.setStack(stack);
        return missile.getMissileCapability();
    }

    public static void register()
    {
        CapabilityManager.INSTANCE.register(ICapabilityMissileStack.class, new Capability.IStorage<ICapabilityMissileStack>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, EnumFacing side)
                {
                    return null;
                }

                @Override
                public void readNBT(Capability<ICapabilityMissileStack> capability, ICapabilityMissileStack instance, EnumFacing side, NBTBase nbt)
                {
                }
            },
            () -> new CapabilityMissileStack(ItemStack.EMPTY));
    }
}
