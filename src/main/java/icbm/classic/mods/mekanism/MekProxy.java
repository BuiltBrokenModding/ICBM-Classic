package icbm.classic.mods.mekanism;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.projectile.EntityProjectile;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.mods.ModProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Method;

/**
 *
 * Created by Dark(DarkGuardsman, Robin) on 5/22/2018.
 */
public class MekProxy extends ModProxy
{
    private boolean isLoaded = false;

    public static Block machineBlock;

    public static Block basicBlock;

    @Override
    public void init()
    {
        machineBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("mekanism:MachineBlock"));
        basicBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("mekanism:BasicBlock"));
        this.isLoaded = true;

        ICBMClassic.logger().info("Mekanism interaction: " + machineBlock + " " + basicBlock);

        if(machineBlock != null && basicBlock != null) {

            // Teleporter frame is basic block meta 7
            final BlockState frameState = basicBlock.getDefaultState(); //.getStateFromMeta(7);

            ICBMClassic.logger().info("Mekanism interaction: " + frameState);

            ProjectileBlockInteraction.addBlockStateInteraction(frameState, (world, blockPos, hit, side, state, entity) -> {
                final BlockPos possiblePortal = blockPos.offset(side);
                final BlockPos teleporter = findTeleporter(world, possiblePortal);
                if(teleporter != null) {
                    teleport(world, teleporter, hit, entity);
                    return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
                }
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
            });
        }
    }

    protected BlockPos findTeleporter(World world, BlockPos possiblePortal) {
        final BlockPos optionA = possiblePortal.down();

        if(isTeleporter(world.getBlockState(optionA))) {
            return optionA;
        }

        final BlockPos optionB = optionA.down();
        if(isTeleporter(world.getBlockState(optionB))) {
           return optionB;
        }

        final BlockPos optionC = optionB.down();
        if(isTeleporter(world.getBlockState(optionC))) {
            return optionC;
        }
        return null;
    }

    protected void teleport(World world, BlockPos telePos, Vec3d hit, Entity entity) {
        if(entity instanceof EntityProjectile) {
            ((EntityProjectile<?>) entity).moveTowards(hit, -0.5);
        }
        else {
            entity.setPosition(telePos.getX() + 0.5, telePos.getY() + 1.5, telePos.getZ() + 0.5);
        }

        final Direction facingDirection = entity.getHorizontalFacing();

        // Figure out how high above the top of the teleport block we are located
        // Once teleported mekanism will set the entity to +1 of the bottom of the teleporter block... which is not where we entered
        final double yOffset = entity.posY - telePos.getY() - 1;

        final TileEntity tile = world.getTileEntity(telePos);

        if(tile != null && "mekanism.common.tile.TileEntityTeleporter".equals(tile.getClass().getTypeName())) {
            final Class cls = tile.getClass();
            try {
                final Method method = cls.getMethod("teleport");
                method.invoke(tile);

                // Cross dim sets it dead
                if(entity.isAlive() && entity.world == world) {
                    final BlockPos possiblePortal = entity.getPosition();
                    final BlockPos teleporter = findTeleporter(world, possiblePortal);
                    if(teleporter != null) {

                        // Update rotation to face exit
                        final Direction openSide = getSide(world, possiblePortal);
                        if(openSide != null) {

                            // Move to outside portal
                            entity.setPosition(entity.posX + openSide.getXOffset(), entity.posY + yOffset, entity.posZ + openSide.getZOffset());

                            // Update motion vector
                            double motionX = entity.getMotion().x;
                            double motionZ = entity.getMotion().z;

                            if(facingDirection.getAxis() == Direction.Axis.X && openSide.getAxis() != Direction.Axis.X) {
                                entity.setMotion(openSide.getXOffset() * motionZ, entity.getMotion().y, motionX);
                            }
                            else if(facingDirection.getAxis() == Direction.Axis.Z && openSide.getAxis() != Direction.Axis.Z) {
                                entity.setMotion(motionZ, entity.getMotion().y, openSide.getZOffset() * motionX);
                            }

                            if(entity instanceof EntityProjectile) {
                                ((EntityProjectile)entity).rotateTowardsMotion(1);
                                //TODO manually calculate rotation based on directly
                                //  As some flight logic systems will not allow rotation towards motion
                            }
                            else {
                                // Update facing
                                float yawAdjust = getYaw(openSide);
                                entity.rotationYaw += yawAdjust;
                                while(entity.rotationYaw > 360) {
                                    entity.rotationYaw -= 360;
                                }
                                while(entity.rotationYaw < -360) {
                                    entity.rotationYaw += 360;
                                }
                            }

                            // TODO ICBMClassic.packetHandler.sendToAllAround(new PacketEntityPos(entity), world, entity.posX, entity.posY, entity.posZ, 400);
                        }
                        else {
                            // Update position to mimic enter point
                            entity.setPositionAndUpdate(entity.posX, entity.posY + yOffset, entity.posZ);
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
        return state.getBlock() == machineBlock; // && state.getBlock().getMetaFromState(state) == 11;
    }

    protected Direction getSide(World world, BlockPos pos) {
        for (Direction side : Direction.Plane.HORIZONTAL) {
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
