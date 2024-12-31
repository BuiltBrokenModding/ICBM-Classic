package icbm.classic.content.cluster.missile;

import icbm.classic.ICBMConstants;
import icbm.classic.api.missiles.ICapabilityMissileStack;
import icbm.classic.api.missiles.IMissile;
import icbm.classic.content.actions.conditionals.ConditionAnd;
import icbm.classic.content.actions.conditionals.ConditionTargetDistance;
import icbm.classic.content.cluster.action.ActionDataCluster;
import icbm.classic.content.actions.conditionals.ConditionalImpact;
import icbm.classic.content.missile.entity.explosive.EntityMissileActionable;
import icbm.classic.lib.saving.NbtSaveHandler;
import lombok.Getter;
import lombok.Value;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.NonNullSupplier;

@Value
public class CapabilityClusterMissileStack implements ICapabilityMissileStack, INBTSerializable<CompoundNBT> {

    @Getter
    ActionDataCluster actionDataCluster = new ActionDataCluster(); // TODO abstract action and use field provider to pass in data from item

    NonNullSupplier<EntityType<EntityMissileActionable>> entityType;

    @Override
    public String getMissileId() {
        return ICBMConstants.PREFIX + "missile[cluster]";
    }

    @Override
    public IMissile newMissile(World world)
    {
        final EntityMissileActionable missile = entityType.get().create(world);
        missile.getMainAction().setActionData(actionDataCluster.copy());

        final ConditionAnd conditionAnd = new ConditionAnd();

        // Disable impact trigger
        conditionAnd.getConditions().add(new ConditionalImpact().setImpactDesired(false));

        // Trigger when near target
        conditionAnd.getConditions().add(new ConditionTargetDistance().setTriggerDistance(3)); //TODO pull from targetData?

        missile.getMainAction().withCondition(conditionAnd);

        return missile.getMissileCapability();
    }

    @Override
    public CompoundNBT serializeNBT() {
        return SAVE_LOGIC.save(this);
    }

    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        SAVE_LOGIC.load(this, nbt);
    }

    private static final NbtSaveHandler<CapabilityClusterMissileStack> SAVE_LOGIC = new NbtSaveHandler<CapabilityClusterMissileStack>()
        .mainRoot()
        /* */.nodeINBTSerializable("cluster_action", CapabilityClusterMissileStack::getActionDataCluster)
        .base();

}
