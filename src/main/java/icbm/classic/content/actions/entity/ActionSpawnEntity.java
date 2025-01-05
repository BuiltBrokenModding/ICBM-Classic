package icbm.classic.content.actions.entity;

import com.google.common.collect.ImmutableList;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.data.ActionField;
import icbm.classic.api.actions.data.ActionFields;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.lib.actions.ActionBase;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.actions.status.MissingFieldStatus;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class ActionSpawnEntity extends ActionBase {

    public static final List<ActionField> FIELDS = ImmutableList.of(
        ActionFields.YAW, ActionFields.PITCH, ActionFields.MOTION_VECTOR,
        ActionFields.ENTITY_REG_NAME, ActionFields.ENTITY_DATA);

    private ResourceLocation entityID;
    private CompoundNBT entityData;
    private Vec3d motion;
    private Float yaw;
    private Float pitch;

    public ActionSpawnEntity(World world, Vec3d position, IActionSource source, IActionData actionData) {
        super(world, position, source, actionData);
    }

    @Nonnull
    @Override
    public IActionStatus doAction() {
        if (entityID == null) {
            return new MissingFieldStatus().setSource("ActionSpawnEntity").setField("entityId");
        }
        final Entity entity = ForgeRegistries.ENTITIES.getValue(entityID).create(getWorld());
        if (entity == null) {
            return new MissingFieldStatus().setSource("ActionSpawnEntity#doAction()").setField("entityInstance");
        }

        entity.setPosition(getPosition().x, getPosition().y, getPosition().z);
        if (motion != null) {
            entity.setMotion(motion.x, motion.y, motion.z);
        }

        // Apply yaw
        if (yaw != null) {
            entity.rotationYaw = entity.prevRotationYaw = yaw;
        } else {
            entity.rotationYaw = entity.prevRotationYaw = MathHelper.wrapDegrees(getWorld().rand.nextFloat() * 360.0F);
        }
        if (entity instanceof LivingEntity) {
            ((LivingEntity) entity).rotationYawHead = entity.rotationYaw;
            ((LivingEntity) entity).renderYawOffset = entity.rotationYaw;
        }

        // Apply pitch
        if (pitch != null) {
            entity.rotationPitch = entity.prevRotationPitch = pitch;
        } else {
            entity.rotationYaw = entity.prevRotationYaw = MathHelper.wrapDegrees(getWorld().rand.nextFloat() * 360.0F);
        }

        // Handle data loading
        if (entityData != null) {
            final UUID uuid = entity.getUniqueID();

            CompoundNBT mergedData = entity.writeWithoutTypeId(new CompoundNBT());
            mergedData.merge(entityData);
            entity.read(mergedData);

            entity.setUniqueId(uuid);

        }

        if (!getWorld().addEntity(entity)) {
            return ActionResponses.ENTITY_SPAWN_FAILED;
        }
        return ActionResponses.COMPLETED;
    }

    @Override
    public <VALUE, TAG extends INBT> void setValue(ActionField<VALUE, TAG> key, VALUE value) {
        if (key == ActionFields.YAW) {
            this.yaw = ActionFields.YAW.cast(value);
        }
        if (key == ActionFields.PITCH) {
            this.pitch = ActionFields.PITCH.cast(value);
        }
        if (key == ActionFields.MOTION_VECTOR) {
            this.motion = (Vec3d) value;
        }
        if (key == ActionFields.ENTITY_REG_NAME) {
            this.entityID = (ResourceLocation) value;
        }
        if (key == ActionFields.ENTITY_DATA) {
            this.entityData = (CompoundNBT) value;
        }
        //TODO add function supplier to store then set on action to the entity BiConsumer<Entity, VALUE>
    }

    @Override
    public <VALUE, TAG extends INBT> VALUE getValue(ActionField<VALUE, TAG> key) {
        if (key == ActionFields.YAW) {
            return key.cast(yaw);
        }
        if (key == ActionFields.PITCH) {
            return key.cast(pitch);
        }
        if (key == ActionFields.MOTION_VECTOR) {
            return key.cast(motion);
        }
        if (key == ActionFields.ENTITY_REG_NAME) {
            return key.cast(entityID);
        }
        if (key == ActionFields.ENTITY_DATA) {
            return key.cast(entityData);
        }
        return null;
    }


    @Override
    public Collection<ActionField> getFields() {
        return FIELDS;
    }
}
