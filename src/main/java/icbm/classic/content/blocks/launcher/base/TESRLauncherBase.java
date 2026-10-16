package icbm.classic.content.blocks.launcher.base;

import com.mojang.blaze3d.vertex.PoseStack;
import icbm.classic.client.render.entity.RenderMissile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Renders the missile held by the launcher base. Client only, register for BlockReg.TILE_LAUNCHER_BASE.
 */
public class TESRLauncherBase implements BlockEntityRenderer<TileLauncherBase>
{
    private static final float missileOffset = 2f; //TODO add custom render type for missiles as static blocks so offset can be driven by model
    private static final List<Vec3> FACE_OFFSETS = Arrays.stream(Direction.values())
        .map(direction -> new Vec3(
            0.5 + direction.getStepX() * missileOffset,
            0.5 + direction.getStepY() * missileOffset,
            0.5 + direction.getStepZ() * missileOffset
        )).collect(Collectors.toList());

    public TESRLauncherBase(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public void render(TileLauncherBase launcher, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, int packedOverlay)
    {
        //Render missile
        if (!launcher.getMissileStack().isEmpty() && RenderMissile.INSTANCE != null)
        {
            final Vec3 offset = FACE_OFFSETS.get(launcher.getLaunchDirection().ordinal());
            // PORT-CHECK: agreed RenderMissile#renderItem signature with port-missiles (pose stack relative to block origin)
            RenderMissile.INSTANCE.renderItem(launcher.getMissileStack(), launcher.getLevel(), poseStack, buffer, packedLight,
                offset.x, offset.y, offset.z,
                launcher.getMissileYaw(true), launcher.getMissilePitch(true), partialTicks);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileLauncherBase tile)
    {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(TileLauncherBase tile)
    {
        // 1.12 TileLauncherBase used INFINITE_EXTENT_AABB
        return AABB.INFINITE;
    }
}
// PORTED
