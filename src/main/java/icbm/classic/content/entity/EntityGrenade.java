package icbm.classic.content.entity;

import icbm.classic.api.actions.IActionData;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.projectile.EntityProjectile;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;
import net.minecraftforge.fml.network.NetworkHooks;

@Accessors(chain = true)
public class EntityGrenade extends EntityProjectile<EntityGrenade> {
    private static final int FUSE_TIME = 100; //TODO config

    /**
     * Explosive capability
     */
    // TODO public final CapabilityExplosiveEntity explosive = new CapabilityExplosiveEntity(this);

    private final PotentialAction explodeAction = new PotentialAction();
    private final LazyOptional<ItemStack> itemstack;


    public EntityGrenade(EntityType<EntityGrenade> type, World par1World, IActionData actionData, NonNullSupplier<ItemStack> itemstack) {
        super(type, par1World);
        this.explodeAction.setActionData(actionData);
        this.itemstack = LazyOptional.of(itemstack);
    }

    public ItemStack renderItemStack() {
        return itemstack.orElse(ItemStack.EMPTY);
    }

    /**
     * Called to update the entity's position/logic.
     */
    @Override
    public void tick() {
        super.tick();

        // TODO move to conditional for fuse and remove() call inside potential action
        if (this.ticksExisted > FUSE_TIME) { //TODO decouple fuse from ticksExisted
            this.explodeAction.doAction(world, this.posX, this.posY + 0.3f, this.posZ, new EntityCause(this));
            this.remove();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        // Do nothing, as we are timer based
        //TODO consider impact trigger setting?
    }
}