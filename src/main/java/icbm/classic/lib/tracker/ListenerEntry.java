package icbm.classic.lib.tracker;

import icbm.classic.ICBMClassic;
import lombok.Data;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;

@Data
public class ListenerEntry implements IEventTrackerListener {
    public final ResourceLocation name;
    public final IEventTrackerListener listener;
    public final Supplier<Boolean> conditional;

    public ListenerEntry(ResourceLocation name, IEventTrackerListener listener) {
        this.name = name;
        this.listener = listener;
        this.conditional = () -> true;
    }

    public ListenerEntry(ResourceLocation name, IEventTrackerListener listener, Supplier<Boolean> conditional) {
        this.name = name;
        this.listener = listener;
        this.conditional = conditional;
    }

    @Override
    public void accept(EventTrackerEntry entry) {
        try {
            listener.accept(entry);
        } catch (Exception e) {
            ICBMClassic.logger().error(String.format("Listener(%s): error while handling entry(%s)", name, entry.getType()));
        }
    }

    @Override
    public boolean consumes(EventTrackerType type) {
        return conditional.get() && listener.consumes(type);
    }
}
