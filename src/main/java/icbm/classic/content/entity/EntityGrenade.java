package icbm.classic.content.entity;

import icbm.classic.ICBMClassic;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.content.missile.logic.source.cause.EntityCause;
import icbm.classic.lib.actions.PotentialAction;
import icbm.classic.lib.projectile.EntityProjectile;
import lombok.experimental.Accessors;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullSupplier;

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

    @Override
    public void tick() {
        super.tick();

        // TODO move to conditional for fuse and remove() call inside potential action
        if (!this.world.isRemote && this.ticksExisted > FUSE_TIME) { //TODO decouple fuse from ticksExisted
            final IActionStatus status = this.explodeAction.doAction(world, this.posX, this.posY + 0.3f, this.posZ, new EntityCause(this));
            if (!status.isType(ActionStatusTypes.GREEN)) {
                ICBMClassic.logger().warn("Failed to trigger grenade due to status: {}\nEntity:{}", status, this);
                itemstack.ifPresent((stack) -> {
                    entityDropItem(stack, 0.0F);
                });
            }
            this.remove();
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        // Do nothing, as we are timer based
        //TODO consider impact trigger setting?
    }
}