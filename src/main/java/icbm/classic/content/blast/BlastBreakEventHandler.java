package icbm.classic.content.blast;

import icbm.classic.api.events.BlastBreakEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.function.Predicate;

@Mod.EventBusSubscriber
public class BlastBreakEventHandler {
    // This predicate is used to determine if a block breakage should continue
    private static Predicate<Object> callback = null;

    // The public interface for setting the callback
    public static void setCallback(Predicate<Object> _callback) { callback = _callback; }

    @SubscribeEvent
    public static void onBlastBreak(BlastBreakEvent event) {
        // If we have a callback, and that callback says not to continue, then don't bother doing anything
        if(callback != null) {
            if(!callback.test(event)) {
                return;
            }
        }

        switch(event.getBreakageType()) {
            case SET_TO_AIR:
                // System.out.println("Set To Air");
                event.getWorld().setBlockToAir(event.getPosition());
                break;
            case SET_STATE:
                // System.out.println("Set State");
                event.getWorld().setBlockState(event.getPosition(), event.getNewState());
                break;
            case SET_STATE_WITH_FLAGS:
                // System.out.println("Set State with flags");
                event.getWorld().setBlockState(event.getPosition(), event.getNewState(), event.getFlags());
                break;
            case USE_CALLBACK:
                // System.out.println("With custom callback");
                event.getCallback().run();
                break;
        }
    }
}
