package icbm.classic.mods.mekanism;

import icbm.classic.ICBMClassic;
import icbm.classic.lib.network.packet.PacketEntityPos;
import icbm.classic.lib.world.IProjectileBlockInteraction;
import icbm.classic.lib.world.ProjectileBlockInteraction;
import icbm.classic.mods.ModProxy;
import icbm.classic.lib.projectile.EntityProjectile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.lang.reflect.Method;

/**
 *
 * Created by Dark(DarkGuardsman, Robert) on 5/22/2018.
 */
public class MekProxy extends ModProxy
{
    public static final MekProxy INSTANCE = new MekProxy();

    private boolean isLoaded = false;

    @GameRegistry.ObjectHolder("mekanism:MachineBlock")
    public static Block machineBlock;

    @GameRegistry.ObjectHolder("mekanism:BasicBlock")
    public static Block basicBlock;

    @Override
    @Optional.Method(modid = "mekanism")
    public void init()
    {
        this.isLoaded = true;

        ICBMClassic.logger().info("Mekanism interaction: " + machineBlock + " " + basicBlock);

        if(machineBlock != null && basicBlock != null) {

            // Teleporter frame is basic block meta 7
            final IBlockState frameState = basicBlock.getStateFromMeta(7);

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

        final EnumFacing facingDirection = entity.getHorizontalFacing();

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
                if(entity.isEntityAlive() && entity.world == world) {
                    final BlockPos possiblePortal = entity.getPosition();
                    final BlockPos teleporter = findTeleporter(world, possiblePortal);
                    if(teleporter != null) {

                        // Update rotation to face exit
                        final EnumFacing openSide = getSide(world, possiblePortal);
                        if(openSide != null) {

                            // Move to outside portal
                            entity.setPosition(entity.posX + openSide.getFrontOffsetX(), entity.posY + yOffset, entity.posZ + openSide.getFrontOffsetZ());

                            // Update motion vector
                            double motionX = entity.motionX;
                            double motionZ = entity.motionZ;

                            if(facingDirection.getAxis() == EnumFacing.Axis.X && openSide.getAxis() != EnumFacing.Axis.X) {
                                entity.motionX = openSide.getFrontOffsetX() * motionZ;
                                entity.motionZ = motionX;
                            }
                            else if(facingDirection.getAxis() == EnumFacing.Axis.Z && openSide.getAxis() != EnumFacing.Axis.Z) {
                                entity.motionX = motionZ;
                                entity.motionZ = openSide.getFrontOffsetZ() * motionX;
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

                            ICBMClassic.packetHandler.sendToAllAround(new PacketEntityPos(entity), world, entity.posX, entity.posY, entity.posZ, 400);
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

    protected boolean isTeleporter(IBlockState state) {
        return state.getBlock() == machineBlock && state.getBlock().getMetaFromState(state) == 11;
    }

    protected EnumFacing getSide(World world, BlockPos pos) {
        for (EnumFacing side : EnumFacing.HORIZONTALS) {
            final BlockPos sidePos = pos.offset(side);
            if (world.isAirBlock(sidePos)) {
               return side;
            }
        }
        return null;


    }

    protected float getYaw(EnumFacing side) {
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
