package icbm.classic.content.blast.cluster.bomblet;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.projectile.IProjectileData;
import icbm.classic.api.missiles.projectile.ProjectileType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@NoArgsConstructor
public class BombletProjectileData implements IProjectileData<EntityBombDroplet> {

    public static final ResourceLocation NAME = new ResourceLocation(ICBMConstants.DOMAIN, "bomblet");
    public static final ProjectileType[] TYPES = new ProjectileType[]{ProjectileType.TYPE_BOMB};

    @Setter @Getter @Accessors(chain = true)
    private ItemStack explosiveStack = ItemStack.EMPTY;

    public ProjectileType[] getTypes() {
        return TYPES;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return NAME;
    }

    @Override
    public EntityBombDroplet newEntity(World world, boolean allowItemPickup) {
        final EntityBombDroplet bombDroplet = new EntityBombDroplet(world);
        bombDroplet.explosive.setStack(explosiveStack);
        return bombDroplet;
    }

    @Override
    public NBTTagCompound serializeNBT() {
        final NBTTagCompound save = new NBTTagCompound();
        save.setTag("explosive", explosiveStack.serializeNBT());
        return save;
    }

    @Override
    public void deserializeNBT(NBTTagCompound save) {
        explosiveStack = new ItemStack(save.getCompoundTag("explosive"));
    }
}
