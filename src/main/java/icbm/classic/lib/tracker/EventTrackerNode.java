package icbm.classic.lib.tracker;

import lombok.Data;

import java.util.LinkedList;

/**
 * Node in a chain of events. Useful for creating a debug flow through a method.
 */
@Data
public class EventTrackerNode {
    private EventTrackerEntry start;
    private LinkedList<EventTrackerEntry> entries;
    private EventTrackerEntry end;
}
