package icbm.classic.lib.actions;

import icbm.classic.api.ICBMClassicAPI;
import icbm.classic.api.actions.IActionData;
import icbm.classic.api.actions.IPotentialAction;
import icbm.classic.api.actions.cause.IActionCause;
import icbm.classic.api.actions.cause.IActionSource;
import icbm.classic.api.actions.conditions.ICondition;
import icbm.classic.api.actions.conditions.IConditionCause;
import icbm.classic.api.actions.data.IActionFieldProvider;
import icbm.classic.api.actions.status.ActionStatusTypes;
import icbm.classic.api.actions.status.IActionStatus;
import icbm.classic.content.missile.logic.source.ActionSource;
import icbm.classic.content.missile.logic.source.cause.CausedByBlock;
import icbm.classic.lib.actions.fields.ActionFieldProvider;
import icbm.classic.lib.actions.status.ActionResponses;
import icbm.classic.lib.saving.NbtSaveHandler;
import icbm.classic.lib.tile.ITick;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraftforge.common.util.INBTSerializable;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Pre-built action for general purpose implementation where all components are known.
 */
public abstract class PotentialActionImp<SELF extends PotentialActionImp<SELF>> implements IPotentialAction, ITick, INBTSerializable<CompoundNBT> {

    @Getter @Setter(AccessLevel.PRIVATE)
    private ICondition preCheck;

    @Getter @Setter
    private IActionFieldProvider fieldProvider;

    public SELF withCondition(ICondition check) {
        this.preCheck = check;
        if(check != null) {
            check.init(fieldProvider);
        }
        return (SELF) this;
    }

    public SELF withProvider(IActionFieldProvider provider) {
        this.fieldProvider = provider;
        if(preCheck != null) {
            preCheck.init(fieldProvider);
        }
        return (SELF) this;
    }

    @Nonnull
    @Override
    public IActionStatus checkAction(World world, double x, double y, double z, @Nullable IActionCause cause) {
        // Edge case: lazy init failed to get action data
        if(this.getActionData() == null) {
            return ActionResponses.MISSING_ACTION_DATA;
        }
        return preCheck == null ? ActionResponses.READY : preCheck.getCondition();
    }

    public IActionStatus doAction(TileEntity tile, @Nullable IActionCause cause) {
        final IActionCause selfCause = new CausedByBlock(tile.getWorld(), tile.getPos(), tile.getWorld().getBlockState(tile.getPos())).setPreviousCause(cause);
        return doAction(tile.getWorld(), tile.getPos(), selfCause);
    }

    public IActionStatus doAction(World world, BlockPos pos, @Nullable IActionCause cause) {
        return doAction(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, cause);
    }

    @Nonnull
    @Override
    public IActionStatus doAction(World world, double x, double y, double z, @Nullable IActionCause cause) {
        final IActionData actionData = this.getActionData();

        // Edge case: lazy init failed to get action data
        if(actionData == null) {
            return ActionResponses.MISSING_ACTION_DATA;
        }

        final IActionStatus preCheckStatus = checkAction(world, x, y, z, cause); //TODO if preCheck is a special type use in cause-by chain. Example: timer
        if(preCheckStatus.isType(ActionStatusTypes.BLOCKING)) {
            return preCheckStatus;
        }

        IActionCause causeToUse = cause;
        if(preCheck != null) {
            if(preCheck instanceof IConditionCause) {
                final IActionCause preCheckCause = ((IConditionCause) preCheck).getCause();
                if(preCheckCause != null) {
                    causeToUse = preCheckCause.setPreviousCause(cause);
                }
            }
            preCheck.reset();
        }

        final IActionSource actionSource = new ActionSource(DimensionType.getKey(world.getDimension().getType()), new Vec3d(x, y, z), causeToUse);
        return ICBMClassicAPI.ACTION_LISTENER.runAction(actionData.create(world, x, y, z, actionSource, Optional.ofNullable(this.fieldProvider).map(ActionFieldProvider::new).get()));
    }

    @Override
    public void update(int tick, boolean isServer) {
        if(isServer && preCheck != null) {
            preCheck.onTick();
        }
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<PotentialActionImp> SAVE_LOGIC = new NbtSaveHandler<PotentialActionImp>()
        .mainRoot()
        /* */.nodeBuildableObject("pre_check", () -> ICBMClassicAPI.CONDITION_REGISTRY, PotentialActionImp::getPreCheck, PotentialActionImp::setPreCheck)
        .base();
}
