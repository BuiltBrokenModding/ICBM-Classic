package icbm.classic.prefab.entity;

import icbm.classic.ICBMClassic;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEndGateway;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Optional;

/**
 * For internal use or direct mod addons, use events for external interaction
 */
@NoArgsConstructor(access = AccessLevel.NONE)
public final class ProjectileBlockInteraction {

    private static final HashMap<IBlockState, IProjectileBlockInteraction> stateToPortalInteraction = new HashMap();
    private static final HashMap<Block, IProjectileBlockInteraction> blockToPortalInteraction = new HashMap();

    // TODO once on 1.19 use TAG system to easily ID blocks "CollisionBasedPortal"

    public static void addBlockStateInteraction(IBlockState state, IProjectileBlockInteraction function) {
        if(stateToPortalInteraction.containsKey(state)) {
            ICBMClassic.logger().warn("interaction already exists for " + state + " replacing", new RuntimeException());
        }
        stateToPortalInteraction.put(state, function);
    }

    public static void addBlockInteraction(Block block, IProjectileBlockInteraction function) {
        if(blockToPortalInteraction.containsKey(block)) {
            ICBMClassic.logger().warn("interaction already exists for " + block + " replacing", new RuntimeException());
        }
        blockToPortalInteraction.put(block, function);
    }



    public static void addCollisionInteraction(Block block) {
        addBlockInteraction(block, (world, pos, hit, side, state, entity) -> IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT);
    }

    public static IProjectileBlockInteraction.EnumHitReactions handleSpecialInteraction(World world, BlockPos pos, Vec3d hit, EnumFacing side, IBlockState state, Entity entity) {
        final IProjectileBlockInteraction func = Optional.ofNullable(stateToPortalInteraction.get(state))
            .orElseGet(() -> blockToPortalInteraction.get(state.getBlock()));
        if(func != null) {
            return func.apply(world, pos, hit, side, state, entity);
        }
        return IProjectileBlockInteraction.EnumHitReactions.CONTINUE;
    }

    public static void register() {
        addCollisionInteraction(Blocks.PORTAL);
        addCollisionInteraction(Blocks.END_PORTAL);
        addBlockInteraction(Blocks.END_GATEWAY, (world, pos, hit, side, state, entity) -> {
            final TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityEndGateway) {
                ((TileEntityEndGateway) tile).teleportEntity(entity);
                return IProjectileBlockInteraction.EnumHitReactions.TELEPORTED;
            }
            return IProjectileBlockInteraction.EnumHitReactions.CONTINUE_NO_IMPACT;
        });
    }
}
