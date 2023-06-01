package icbm.classic.lib.capability.missile;

import icbm.classic.api.missiles.projectile.IProjectileStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

/**
 * Applied to {@link ItemStack} that are projectiles
 */
public class CapabilityProjectileStack implements IProjectileStack
{
    private final ItemStack stack; //TODO decouple from stack and directly save init data

    public CapabilityProjectileStack(ItemStack stack) {
        this.stack = stack;
    }

    @Override
    public ResourceLocation getName() {
        return stack.getItem().getRegistryName();
    }

    @Override
    public Entity newEntity(World world) {
        return new EntityCreeper(world);
    }


    public static void register()
    {
        CapabilityManager.INSTANCE.register(IProjectileStack.class, new Capability.IStorage<IProjectileStack>()
            {
                @Nullable
                @Override
                public NBTBase writeNBT(Capability<IProjectileStack> capability, IProjectileStack instance, EnumFacing side) {
                    return null;
                }

                @Override
                public void readNBT(Capability<IProjectileStack> capability, IProjectileStack instance, EnumFacing side, NBTBase nbt) {

                }
            },
            () -> new CapabilityProjectileStack(ItemStack.EMPTY));
    }
}
