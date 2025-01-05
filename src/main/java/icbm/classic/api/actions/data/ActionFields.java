package icbm.classic.api.actions.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.*;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

@NoArgsConstructor(access = AccessLevel.NONE)
public final class ActionFields {
    /** Size of area from center of action. For a box with size 3, this means it will generate 7x7 area. As size will be 3 up + 1 center + 3 down */
    public static ActionField<Float, FloatNBT> AREA_SIZE;

    /** RUNTIME_ONLY: Way to access host entity, usually provided by the entity itself */
    public static ActionField<Entity, INBT> HOST_ENTITY;

    /** RUNTIME_ONLY: Position of the host */
    public static ActionField<Vec3d, CompoundNBT> HOST_POSITION;

    /** RUNTIME_ONLY: Rough direction the host is facing */
    public static ActionField<Direction, ByteNBT> HOST_DIRECTION;

    /** True if system has impacted something. Usually being entity has impacted ground or another entity. */
    public static ActionField<Boolean, ByteNBT> IMPACTED;

    /** Position of the current target or aim, or desired target position of an action */
    public static ActionField<Vec3d, CompoundNBT> TARGET_POSITION;

    /** Motion vector to set */
    public static ActionField<Vec3d, CompoundNBT> MOTION_VECTOR;

    /** Rotation yaw to set */
    public static ActionField<Float, FloatNBT> YAW;

    /** Rotation pitch to set */
    public static ActionField<Float, FloatNBT> PITCH;

    /** Entity registry name, for use with {@link net.minecraft.entity.EntityList} */
    public static ActionField<ResourceLocation, StringNBT> ENTITY_REG_NAME;

    /** Entity save data */
    public static ActionField<CompoundNBT, CompoundNBT> ENTITY_DATA;

    /** Delay in ticks before triggering action */
    public static ActionField<Integer, IntNBT> DELAY_TICKS;
}
