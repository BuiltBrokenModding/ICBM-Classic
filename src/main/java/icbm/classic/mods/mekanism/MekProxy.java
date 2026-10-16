package icbm.classic.mods.mekanism;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.packet.PacketEntityPos;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.mods.ModProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Method;

/**
 * Mekanism interaction. Mekanism is not on the compile classpath, everything goes through registry names and
 * reflection, guarded by {@link #isModLoaded(String)}.
 * <p>
 * Created by Dark(DarkGuardsman, Robin) on 5/22/2018.
 */
public class MekProxy extends ModProxy
{
    public static final String MOD_ID = "mekanism";

    public static final MekProxy INSTANCE = new MekProxy();

    /** PORT: 1.12 machine block meta 11, flattened to its own block in Mekanism 1.21 */
    public static final ResourceLocation TELEPORTER = ResourceLocation.fromNamespaceAndPath(MOD_ID, "teleporter");
    /** PORT: 1.12 basic block meta 7, flattened to its own block in Mekanism 1.21 */
    public static final ResourceLocation TELEPORTER_FRAME = ResourceLocation.fromNamespaceAndPath(MOD_ID, "teleporter_frame");

    private boolean isLoaded = false;

    public static Block teleporterBlock;
    public static Block teleporterFrameBlock;

    /** Mekanism's public no-arg teleport method, null when this Mekanism version does not have it */
    private static Method teleportMethod;

    @Override
    public void preInit() {
        if (!isModLoaded(MOD_ID)) {
            return;
        }
        // Mekanism https://github.com/mekanism/Mekanism/blob/1.12/src/main/java/mekanism/common/MekanismBlocks.java
        //          https://github.com/mekanism/Mekanism/blob/1.12/src/main/java/mekanism/common/block/states/BlockStateMachine.java
        // PORT-TODO: 1.12 zeroed "electricityStored" through EMP block swaps for MachineBlock/MachineBlock2/MachineBlock3/EnergyCube.
        //  Mekanism 1.21 has one block per machine and saves energy in "EnergyContainers", so the NBT override no longer
        //  applies. Energy is drained through the generic FE capability EMP handling instead. Original setup kept for reference:
        // final CompoundTag energyOverride = new CompoundTag();
        // energyOverride.putDouble("electricityStored", 0);
        // EmpHandler.empBlockSwaps.setDefault(ResourceLocation.fromNamespaceAndPath("mekanism", "MachineBlock"), new BlockReplacementData().setBlockNBT(energyOverride), 0);
        // EmpHandler.empBlockSwaps.setDefault(ResourceLocation.fromNamespaceAndPath("mekanism", "MachineBlock2"), new BlockReplacementData().setBlockNBT(energyOverride), 0);
        // EmpHandler.empBlockSwaps.setDefault(ResourceLocation.fromNamespaceAndPath("mekanism", "MachineBlock3"), new BlockReplacementData().setBlockNBT(energyOverride), 0);
        // EmpHandler.empBlockSwaps.setDefault(ResourceLocation.fromNamespaceAndPath("mekanism", "EnergyCube"), new BlockReplacementData().setBlockNBT(energyOverride), 0);
    }

    @Override
    public void init()
    {
        if (!isModLoaded(MOD_ID)) {
            return;
        }
        this.isLoaded = true;

        teleporterBlock = BuiltInRegistries.BLOCK.getOptional(TELEPORTER).orElse(null);
        teleporterFrameBlock = BuiltInRegistries.BLOCK.getOptional(TELEPORTER_FRAME).orElse(null);

        ICBMClassic.logger().info("Mekanism interaction: " + teleporterBlock + " " + teleporterFrameBlock);

        if(teleporterBlock != null && teleporterBlock != Blocks.AIR && teleporterFrameBlock != null && teleporterFrameBlock != Blocks.AIR) {

            ICBMClassic.logger().info("Mekanism interaction: " + teleporterFrameBlock);

            // PORT: 1.12 Mekanism had a public no-arg TileEntityTeleporter#teleport(). Look it up once and only hook the frame
            //  when it exists, otherwise every hit logged a NoSuchMethodException and the projectile was pushed back without
            //  teleporting. Without it projectiles hit the frame like any other block.
            try {
                teleportMethod = Class.forName("mekanism.common.tile.TileEntityTeleporter").getMethod("teleport");
            }
            catch (ReflectiveOperationException | LinkageError e) {
                teleportMethod = null;
                ICBMClassic.logger().warn("Mekanism teleporter API changed, projectile teleport compat disabled");
            }
            if (teleportMethod == null) {
                return;
            }

            ProjectileBlockInteraction.addBlockInteraction(teleporterFrameBlock, (world, blockPos, hit, side, state, entity) -> {
                final BlockPos possiblePortal = blockPos.relative(side);
                final BlockPos teleporter = findTeleporter(world, possiblePortal);
                if(teleporter != null) {
                    teleport(world, teleporter, hit, entity);
                    return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
                }
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
            });
        }
    }

    protected BlockPos findTeleporter(Level world, BlockPos possiblePortal) {
        final BlockPos optionA = possiblePortal.below();

        if(isTeleporter(world.getBlockState(optionA))) {
            return optionA;
        }

        final BlockPos optionB = optionA.below();
        if(isTeleporter(world.getBlockState(optionB))) {
           return optionB;
        }

        final BlockPos optionC = optionB.below();
        if(isTeleporter(world.getBlockState(optionC))) {
            return optionC;
        }
        return null;
    }

    protected void teleport(Level world, BlockPos telePos, Vec3 hit, Entity entity) {
        if(entity instanceof EntityProjectile) {
            ((EntityProjectile<?>) entity).moveTowards(hit, -0.5);
        }
        else {
            entity.setPos(telePos.getX() + 0.5, telePos.getY() + 1.5, telePos.getZ() + 0.5);
        }

        final Direction facingDirection = entity.getDirection();

        // Figure out how high above the top of the teleport block we are located
        // Once teleported mekanism will set the entity to +1 of the bottom of the teleporter block... which is not where we entered
        final double yOffset = entity.getY() - telePos.getY() - 1;

        final BlockEntity tile = world.getBlockEntity(telePos);

        if(tile != null && "mekanism.common.tile.TileEntityTeleporter".equals(tile.getClass().getTypeName())) {
            try {
                teleportMethod.invoke(tile);

                // Cross dim sets it dead
                if(entity.isAlive() && entity.level() == world) {
                    final BlockPos possiblePortal = entity.blockPosition();
                    final BlockPos teleporter = findTeleporter(world, possiblePortal);
                    if(teleporter != null) {

                        // Update rotation to face exit
                        final Direction openSide = getSide(world, possiblePortal);
                        if(openSide != null) {

                            // Move to outside portal
                            entity.setPos(entity.getX() + openSide.getStepX(), entity.getY() + yOffset, entity.getZ() + openSide.getStepZ());

                            // Update motion vector
                            final Vec3 motion = entity.getDeltaMovement();
                            double motionX = motion.x;
                            double motionZ = motion.z;

                            if(facingDirection.getAxis() == Direction.Axis.X && openSide.getAxis() != Direction.Axis.X) {
                                entity.setDeltaMovement(openSide.getStepX() * motionZ, motion.y, motionX);
                            }
                            else if(facingDirection.getAxis() == Direction.Axis.Z && openSide.getAxis() != Direction.Axis.Z) {
                                entity.setDeltaMovement(motionZ, motion.y, openSide.getStepZ() * motionX);
                            }

                            if(entity instanceof EntityProjectile) {
                                ((EntityProjectile<?>)entity).rotateTowardsMotion(1);
                                //TODO manually calculate rotation based on directly
                                //  As some flight logic systems will not allow rotation towards motion
                            }
                            else {
                                // Update facing
                                float yawAdjust = getYaw(openSide);
                                float yaw = entity.getYRot() + yawAdjust;
                                while(yaw > 360) {
                                    yaw -= 360;
                                }
                                while(yaw < -360) {
                                    yaw += 360;
                                }
                                entity.setYRot(yaw);
                            }

                            ICBMClassic.packetHandler.sendToAllAround(new PacketEntityPos(entity), world, entity.getX(), entity.getY(), entity.getZ(), 400);
                        }
                        else {
                            // Update position to mimic enter point
                            entity.teleportTo(entity.getX(), entity.getY() + yOffset, entity.getZ());
                        }
                    }
                }
            }
            catch (Exception e) {
                ICBMClassic.logger().error("Failed to teleport using mekanism portal", e);
            }
        }
    }

    protected boolean isTeleporter(BlockState state) {
        return teleporterBlock != null && state.getBlock() == teleporterBlock;
    }

    protected Direction getSide(Level world, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
            final BlockPos sidePos = pos.relative(side);
            if (world.isEmptyBlock(sidePos)) {
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
            default:
                return 0;
        }
    }

    public boolean isMekanismLoaded() {
        return isLoaded;
    }
}
// PORTED
