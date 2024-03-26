package icbm.classic.mods.mekanism;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.packet.PacketEntityPos;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.mods.ModProxy;
import icbm.classic.prefab.entity.IcbmProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

/**
 * Created by Dark(DarkGuardsman, Robert) on 5/22/2018.
 */
public class MekProxy extends ModProxy {
    public static final MekProxy INSTANCE = new MekProxy();

    private boolean isLoaded = false;

    @GameRegistry.ObjectHolder("mekanism:MachineBlock")
    public static Block machineBlock;

    @GameRegistry.ObjectHolder("mekanism:BasicBlock")
    public static Block basicBlock;

    @Override
    @Optional.Method(modid = "mekanism")
    public void init() {
        this.isLoaded = true;

        ICBMClassic.logger().info("Mekanism interaction: " + machineBlock + " " + basicBlock);

        if (machineBlock != null && basicBlock != null) {

            // Teleporter frame is basic block meta 7
            final BlockState frameState = basicBlock.getStateFromMeta(7);

            ICBMClassic.logger().info("Mekanism interaction: " + frameState);

            ProjectileBlockInteraction.addBlockStateInteraction(frameState, (world, blockPos, hit, side, state, entity) -> {
                final BlockPos possiblePortal = blockPos.offset(side);
                final BlockPos teleporter = findTeleporter(world, possiblePortal);
                if (teleporter != null) {
                    teleport(world, teleporter, hit, entity);
                    return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
                }
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
            });
        }
    }

    protected BlockPos findTeleporter(Level level, BlockPos possiblePortal) {
        final BlockPos optionA = possiblePortal.below();

        if (isTeleporter(level.getBlockState(optionA))) {
            return optionA;
        }

        final BlockPos optionB = optionA.below();
        if (isTeleporter(level.getBlockState(optionB))) {
            return optionB;
        }

        final BlockPos optionC = optionB.below();
        if (isTeleporter(level.getBlockState(optionC))) {
            return optionC;
        }
        return null;
    }

    protected void teleport(Level level, BlockPos telePos, Vec3 hit, Entity entity) {
        if (entity instanceof IcbmProjectile) {
            ((IcbmProjectile<?>) entity).moveTowards(hit, -0.5);
        } else {
            entity.setPos(telePos.getX() + 0.5, telePos.getY() + 1.5, telePos.getZ() + 0.5);
        }

        final Direction facingDirection = entity.getHorizontalFacing();

        // Figure out how high above the top of the teleport block we are located
        // Once teleported mekanism will set the entity to +1 of the bottom of the teleporter block... which is not where we entered
        final double yOffset = entity.getY() - telePos.getY() - 1;

        final BlockEntity blockEntity = level.getBlockEntity(telePos);

        if (blockEntity != null && "mekanism.common.tile.BlockEntityTeleporter".equals(blockEntity.getClass().getTypeName())) {
            final Class cls = blockEntity.getClass();
            try {
                final Method method = cls.getMethod("teleport");
                method.invoke(blockEntity);

                // Cross dim sets it dead
                if (entity.isAlive() && entity.level() == level) {
                    final BlockPos possiblePortal = entity.getPosition();
                    final BlockPos teleporter = findTeleporter(level, possiblePortal);
                    if (teleporter != null) {

                        // Update rotation to face exit
                        final Direction openSide = getSide(level, possiblePortal);
                        if (openSide != null) {

                            // Move to outside portal
                            entity.setPos(entity.getX() + openSide.getFrontOffsetX(), entity.getY() + yOffset, entity.getZ() + openSide.getFrontOffsetZ());

                            // Update motion vector
                            double motionX = entity.motionX;
                            double motionZ = entity.motionZ;

                            if (facingDirection.getAxis() == Direction.Axis.X && openSide.getAxis() != Direction.Axis.X) {
                                entity.motionX = openSide.getFrontOffsetX() * motionZ;
                                entity.motionZ = motionX;
                            } else if (facingDirection.getAxis() == Direction.Axis.Z && openSide.getAxis() != Direction.Axis.Z) {
                                entity.motionX = motionZ;
                                entity.motionZ = openSide.getFrontOffsetZ() * motionX;
                            }

                            if (entity instanceof IcbmProjectile) {
                                ((IcbmProjectile) entity).rotateTowardsMotion();
                                //TODO manually calculate rotation based on directly
                                //  As some flight logic systems will not allow rotation towards motion
                            } else {
                                // Update facing
                                float yawAdjust = getYaw(openSide);
                                entity.getYRot() += yawAdjust;
                                while (entity.getYRot() > 360) {
                                    entity.getYRot() -= 360;
                                }
                                while (entity.getYRot() < -360) {
                                    entity.getYRot() += 360;
                                }
                            }

                            ICBMClassic.packetHandler.sendToAllAround(new PacketEntityPos(entity), level, entity.getX(), entity.getY(), entity.getZ(), 400);
                        } else {
                            // Update position to mimic enter point
                            entity.setPositionAndUpdate(entity.getX(), entity.getY() + yOffset, entity.getZ());
                        }


                    }
                }
            } catch (Exception e) {
                ICBMClassic.logger().error("Failed to teleport using mekanism portal", e);
            }
        }
    }

    protected boolean isTeleporter(BlockState state) {
        return state.getBlock() == machineBlock && state.getBlock().getMetaFromState(state) == 11;
    }

    protected Direction getSide(Level level, BlockPos pos) {
        for (Direction side : Direction.HORIZONTALS) {
            final BlockPos sidePos = pos.offset(side);
            if (world.isAirBlock(sidePos)) {
                return side;
            }
        }
        return null;


    }

    protected float getYaw(Direction side) {
        switch (side) {
            case NORTH:
                return 180;
            case SOUTH:
                return 0;
            case WEST:
                return 90;
            case EAST:
                return 270;
        }
        return 0;
    }

    public boolean isMekanismLoaded() {
        return isLoaded;
    }
}
