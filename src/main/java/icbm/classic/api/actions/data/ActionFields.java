package icbm.classic.api.actions.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.world.entity.Entity;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ActionFields {
    /** Size of area from center of action. For a box with size 3, this means it will generate 7x7 area. As size will be 3 up + 1 center + 3 down */
    public static ActionField<Float, FloatTag> AREA_SIZE;

    /** RUNTIME_ONLY: Way to access host entity, usually provided by the entity itself */
    public static ActionField<Entity, Tag> HOST_ENTITY;

    /** RUNTIME_ONLY: Position of the host */
    public static ActionField<Vec3, CompoundTag> HOST_POSITION;

    /** RUNTIME_ONLY: Rough direction the host is facing */
    public static ActionField<Direction, ByteTag> HOST_DIRECTION;

    /** True if system has impacted something. Usually being entity has impacted ground or another entity. */
    public static ActionField<Boolean, ByteTag> IMPACTED;

    /** Position of the current target or aim, or desired target position of an action */
    public static ActionField<Vec3, CompoundTag> TARGET_POSITION;

    /** Motion vector to set */
    public static ActionField<Vec3, CompoundTag> MOTION_VECTOR;

    /** Rotation yaw to set */
    public static ActionField<Float, FloatTag> YAW;

    /** Rotation pitch to set */
    public static ActionField<Float, FloatTag> PITCH;

    /** Entity registry name, for use with {@link net.minecraft.world.entity.EntityType} */
    public static ActionField<ResourceLocation, StringTag> ENTITY_REG_NAME;

    /** Entity save data */
    public static ActionField<CompoundTag, CompoundTag> ENTITY_DATA;
}
// PORTED
