package icbm.classic.content.parachute;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.ProjectileType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ParachuteProjectileData implements IProjectileData<EntityParachute> {

    private final static ProjectileType[] TYPE = new ProjectileType[] {ProjectileType.TYPE_ENTITY, ProjectileType.TYPE_HOLDER};
    public final static ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "holder.parachute");

    @Getter
    @Setter
    @Accessors(chain = true)
    private ItemStack passengerItemStack = ItemStack.EMPTY;

    @Override
    public ProjectileType[] getTypes() {
        return TYPE;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityParachute newEntity(World world, boolean allowItemPicku) {
        return new EntityParachute(world); //.setRenderItemStack(parachute);
    }

    @Override
    public void onEntitySpawned(@Nonnull EntityParachute entity, @Nullable Entity source) {
        if(!passengerItemStack.isEmpty()) {

        }
    }

    @Override
    public NBTTagCompound serializeNBT() {
        return null;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {

    }
}
