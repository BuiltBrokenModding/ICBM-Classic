package icbm.classic.content.blocks.launcher.base;

import icbm.classic.api.launcher.IMissileLauncher;
import icbm.classic.api.missiles.cause.IMissileCause;
import icbm.classic.api.missiles.parts.IMissileTarget;
import icbm.classic.api.missiles.parts.IMissileTargetDelayed;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Used to store firing information when working with countdowns/delays
 */
@Data
@AllArgsConstructor
public class FiringPackage {

    /** Input: Target data */
    private IMissileTarget targetData;

    /** Input: Cause of firing the missile */
    private IMissileCause cause;

    /** Counter: Time to tick down before firing */
    private int countDown;

    public void launch(IMissileLauncher missileLauncher) {
        if(targetData instanceof IMissileTargetDelayed) {
            targetData = ((IMissileTargetDelayed) targetData).cloneWithoutDelay();
        }

        // Invoke normal launch so we fire events and handle logic consistently
        missileLauncher.launch(targetData, cause, false); //TODO add callback to firing source
    }
}
