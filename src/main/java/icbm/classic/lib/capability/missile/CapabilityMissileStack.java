package icbm.classic.lib.capability.missile;

import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.missile.entity.explosive.EntityExplosiveMissile;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

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
    public IMissile newMissile(World world)
    {
        final EntityExplosiveMissile missile = new EntityExplosiveMissile(world);
        missile.explosive.setStack(stack);
        return missile.missileCapability;
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
