package icbm.classic.content.blast.cluster.bomblet;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.projectile.IProjectileStack;
import icbm.classic.api.missiles.projectile.ProjectileType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class CapBombletProjectile implements IProjectileStack {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "bomblet");
    public static final ProjectileType[] TYPES = new ProjectileType[]{TYPE_BOMB};

    private final Supplier<ItemStack> stack;

    public CapBombletProjectile(Supplier<ItemStack> stack) {
        this.stack = stack;
    }

    @Override
    public ProjectileType[] getTypes() {
        return TYPES;
    }

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public Entity newEntity(World world) {
        final EntityBombDroplet bombDroplet = new EntityBombDroplet(world);
        bombDroplet.explosive.setStack(stack.get());
        return bombDroplet;
    }
}
