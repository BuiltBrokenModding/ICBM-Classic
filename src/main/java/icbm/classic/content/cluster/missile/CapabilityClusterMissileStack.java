package icbm.classic.content.cluster.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.config.missile.ConfigMissile;
import icbm.classic.content.actions.conditionals.ConditionAnd;
import icbm.classic.content.actions.conditionals.ConditionTargetDistance;
import icbm.classic.content.actions.conditionals.ConditionalImpact;
import icbm.classic.content.cluster.action.ActionDataCluster;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import icbm.classic.lib.StackNbt;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.INBTSerializable;

public class CapabilityClusterMissileStack implements ICapabilityMissileStack, INBTSerializable<CompoundTag> {

    /** Custom data key, matches the 1.12 capability provider key */
    public static final String STACK_KEY = "missile";

    private final ItemStack stack;

    @Getter
    private final ActionDataCluster actionDataCluster = new ActionDataCluster();

    /** PORT: item capabilities are rebuilt per lookup, state is loaded from the stack here and written back with {@link #saveToStack()} */
    public CapabilityClusterMissileStack(ItemStack stack) {
        this.stack = stack;
        if (StackNbt.has(stack, STACK_KEY)) {
            SAVE_LOGIC.load(this, StackNbt.getCompound(stack, STACK_KEY));
        }
    }

    /** Writes the cluster contents back to the stack, call after editing {@link #getActionDataCluster()} */
    public void saveToStack() {
        StackNbt.putCompound(stack, STACK_KEY, SAVE_LOGIC.save(this));
    }

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile[cluster]";
    }

    @Override
    public IMissile newMissile(Level world)
    {
        final EntityMissileActionable missile = new EntityMissileActionable(world)
            .setOriginalStack(stack)
            .setActionData(actionDataCluster)
            .initHealth(ConfigMissile.CLUSTER_MISSILE.MAX_HEALTH);

        final ConditionAnd conditionAnd = new ConditionAnd();

        // Disable impact trigger
        conditionAnd.getConditions().add(new ConditionalImpact().setImpactDesired(false));

        // Trigger when near target
        conditionAnd.getConditions().add(new ConditionTargetDistance().setTriggerDistance(3)); //TODO pull from targetData?

        missile.getMainAction().withCondition(conditionAnd);

        return missile.getMissileCapability();
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityClusterMissileStack> SAVE_LOGIC = new NbtSaveHandler<CapabilityClusterMissileStack>()
        .mainRoot()
        /* */.nodeINBTSerializable("cluster_action", CapabilityClusterMissileStack::getActionDataCluster)
        .base();

}
// PORTED
