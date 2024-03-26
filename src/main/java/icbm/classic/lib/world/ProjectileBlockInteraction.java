package icbm.classic.lib.world;

import icbm.classic.ICBMClassic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.material.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.BlockEntityEndGateway;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;

/**
 * For internal use or direct mod addons, use events for external interaction
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ProjectileBlockInteraction {

    private static final HashMap<BlockState, IProjectileBlockInteraction> stateToInteraction = new HashMap();
    private static final HashMap<Block, IProjectileBlockInteraction> blockToInteraction = new HashMap();
    private static final HashMap<Material, IProjectileBlockInteraction> materialToInteraction = new HashMap();

    // TODO once on 1.19 use TAG system to easily ID blocks "CollisionBasedPortal"

    public static void addBlockStateInteraction(BlockState state, IProjectileBlockInteraction function) {
        if (stateToInteraction.containsKey(state)) {
            ICBMClassic.logger().warn("interaction already exists for " + state + " replacing", new RuntimeException());
        }
        stateToInteraction.put(state, function);
    }

    public static void addBlockInteraction(Block block, IProjectileBlockInteraction function) {
        if (blockToInteraction.containsKey(block)) {
            ICBMClassic.logger().warn("interaction already exists for " + block + " replacing", new RuntimeException());
        }
        blockToInteraction.put(block, function);
    }

    public static void addMaterialInteraction(Material material, IProjectileBlockInteraction function) {
        if (materialToInteraction.containsKey(material)) {
            ICBMClassic.logger().warn("interaction already exists for " + material + " replacing", new RuntimeException());
        }
        materialToInteraction.put(material, function);
    }

    public static void addCollisionInteraction(Block block) {
        addBlockInteraction(block, (world, pos, hit, side, state, entity) -> IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT);
    }

    public static void breakBlockInteraction(Block block) {
        addBlockInteraction(block, (world, pos, hit, side, state, entity) -> {
            if (breakBlock(world, pos, state, entity)) {
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
        });
    }

    public static void breakMaterialInteraction(Material material) {
        addMaterialInteraction(material, (world, pos, hit, side, state, entity) -> {
            if (breakBlock(world, pos, state, entity)) {
                return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
        });
    }

    private static boolean breakBlock(Level level, BlockPos pos, BlockState state, Entity entity) {
        final Block block = state.getBlock();
        if (block.canEntityDestroy(state, world, pos, entity)) {
            block.dropBlockAsItem(world, pos, state, 0);
            world.setBlockToAir(pos);
            return true;
        }
        return false;
    }

    public static IProjectileBlockInteraction.EnumHitReactions handleSpecialInteraction(Level level, BlockPos pos, Vec3 hit, Direction side, BlockState state, Entity entity) {

        IProjectileBlockInteraction func = stateToInteraction.get(state);
        if (func != null) {
            final IProjectileBlockInteraction.EnumHitReactions result = func.apply(world, pos, hit, side, state, entity);
            if (result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        func = blockToInteraction.get(state.getBlock());
        if (func != null) {
            final IProjectileBlockInteraction.EnumHitReactions result = func.apply(world, pos, hit, side, state, entity);
            if (result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        func = materialToInteraction.get(state.getMaterial());
        if (func != null) {
            final IProjectileBlockInteraction.EnumHitReactions result = func.apply(world, pos, hit, side, state, entity);
            if (result != null && result != IProjectileBlockInteraction.EnumHitReactions.PASS) {
                return result;
            }
        }
        return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
    }

    public static void register() {

        // Portal handling
        addCollisionInteraction(Blocks.PORTAL);
        addCollisionInteraction(Blocks.END_PORTAL);
        addBlockInteraction(Blocks.END_GATEWAY, (world, pos, hit, side, state, entity) -> {
            final BlockEntity blockEntity = world.getBlockEntity(pos);
            if (tile instanceof BlockEntityEndGateway) {
                ((BlockEntityEndGateway) tile).teleportEntity(entity);
                return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
        });

        // Break as pass through handling
        breakMaterialInteraction(Material.LEAVES);
        breakMaterialInteraction(Material.SNOW);
        breakMaterialInteraction(Material.GLASS);
    }
}
