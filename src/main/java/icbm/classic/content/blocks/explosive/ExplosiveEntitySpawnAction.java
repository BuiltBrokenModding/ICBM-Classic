package icbm.classic.content.blocks.explosive;

import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.content.entity.EntityExplosive;
import icbm.classic.lib.actions.ActionBase;
import icbm.classic.lib.actions.status.ActionResponses;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.INBT;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class ExplosiveEntitySpawnAction extends ActionBase
{
    private final NonNullSupplier<EntityType<EntityExplosive>> entityType;
    private Integer fuse;

    public ExplosiveEntitySpawnAction(World world, Vec3d position, IActionSource source, IActionData actionData, NonNullSupplier<EntityType<EntityExplosive>> entityType) {
        super(world, position, source, actionData);
        this.entityType = entityType;
    }

    @Override
    public <VALUE, TAG extends INBT> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
        if(key == ActionFields.DELAY_TICKS) {
            fuse = ActionFields.DELAY_TICKS.cast(value);
        }
    }

    @Nonnull
    @Override
    public IActionStatus doAction() {
        //Build entity
        final EntityExplosive entityExplosive = entityType.get().create(getWorld());
        entityExplosive.setPosition(getPosition().x, getPosition().y, getPosition().z);
        if(fuse != null) {
            entityExplosive.fuse = fuse;
        }

        //Attempt to spawn
        if (getWorld().addEntity(entityExplosive))
        {
            return ActionResponses.COMPLETED;
        }
        return ActionResponses.ENTITY_SPAWN_FAILED;
    }
}
