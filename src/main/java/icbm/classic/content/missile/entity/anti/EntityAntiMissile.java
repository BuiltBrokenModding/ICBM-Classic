package icbm.classic.content.missile.entity.anti;

import icbm.classic.content.missile.entity.CapabilityMissile;
import icbm.classic.content.missile.entity.EntityMissile;
import icbm.classic.content.missile.logic.flight.FollowTargetLogic;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;

import javax.annotation.Nullable;

/**
 * Created by Robin Seifert on 11/30/2021.
 */
public class EntityAntiMissile extends EntityMissile<EntityAntiMissile> {
    protected final AntiMissileTarget scanLogic = new AntiMissileTarget(this);

    public EntityAntiMissile(World world) {
        super(world);
        this.getMissileCapability().setTargetData(scanLogic); //TODO create custom missileCap to force getTarget()
        this.getMissileCapability().setFlightLogic(new FollowTargetLogic());
    }

    @Override
    public void onUpdate() {
        scanLogic.tick();
        super.onUpdate();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.getCapability(capability, facing);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        //TODO add AB capability so radars can redirect targets
        return super.hasCapability(capability, facing);
    }
}
