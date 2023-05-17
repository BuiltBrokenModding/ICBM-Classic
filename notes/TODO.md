# TODO

## Missiles/Launcher Work Left

- Engine smoke position
- Engine smoke color
- Redmatter fluid in missile, have it move in circle for center tank and left-right inverse on lower tubes. 2 pixels left, 2 pixels right, repeat to give a centrifuge like visual.
- Render cable coming from frame to missile. Recycle rope render from minecraft and increase in size

## Block Textures

- connected textures for launcher connector
- glass connected textures

## Entity Textures

- Redmatter Spiral
- Fragment 

## Issues

- Disabled icon for radio hover is too hard to tell apart from normal, consider color shift or hard boarder
- emp hitbox
- radar gui is missing disable radio feature
- emp tower structure doesn't disconnect on block break
- screen doesn't connect to power on it's own
- launcher base doesn't store rotation but uses it for missile seat & rendering
- missile seat offset is aligned to booster and not center of rocket... may need to implement a custom renderer to show player on missile and not seated next to missile
- launchers don't pass inventory to other launchers
- launcher collision check is not working
- missile is rendering based on launcher's light level. Should take into account open space's light level to avoid a dark/shadowy render

## Features

- add intentional spread to launchers, as accuracy is a bit too high at times
- Horizontal support rails to hold the new sideways missiles. Should be half slap in size
- Rotation for existing rails?
- Internal support rails to place above launcher?
- Redstone output from launcher... think I already documented this one but really want to add it
- have some missiles place a missile module placeholder sticking out of ground after use (chemical types, ender, any non-explosive)

## Admin tools

### UserTracker

event system for actions a player does with the mod. For use with admin tools either to log in a database or write logs. 

We could even provide a general purpose addon to handle logging out to a database. With a main mod to handle common connection pool and api access.

Ex: `UserTracker.event(player, action, data[]); UserTracker.event(player, RADAR_GUN_TRACE, hand, stack, hit)`

In the code we would want to set listeners for the events. Think of this as a mini-event system that doesn't use the forge/fml event bus. Instead, it would be a dedicated bus that runs only server side.

If no listeners are registered then events do not fire. When events do fire they are always read-only. Meaning any invoke should provide final-immutable data. As this isn't a replacement for actual events.

Dev side we can register a listener to act as a debugger. Might even be able to recycle this as a performance tool. Though that would require some thought. Specifically a `start` and `end` event. Maybe implement an event nesting mechanic?

Example:

```java
final EventNode start = UserTracker.playerEvent(RADAR_GUN_TRACE_START, player, hand, stack);
start.add(UserTracker.playerEvent(RADAR_GUN_TRACE_HIT, player, hand, stack, hit))
start.end(UserTracker.playerEvent(RADAR_GUN_TRACE_END, player, hand, stack));
```

Also should consider different event builders

```java

public static void event(EventType type, Object[] data);

public static void playerEvent(EventType type, EntityPlayer player, Object[] data);

```

Then again no real reason, as we could just setup encoders/decoders for all data inputs. Having the player just be an encoder type. Only real advantage would be easy access to instance data.... but since this is read-only that might be a bad idea.

As for encoders, we can make this a `column -> fieldGetter` like system. Then have some mechanic to go from EventType to column easily.

```java
public class EventColumn<DATA, RAW> {
    int id; //set by eventType
    String name; //mod+unique
    Type type; //class type, Integer
    Supplier<DATA> getter;
    Supplier<RAW> dataAccessor; // would either be a findFirst(type) or findAt(index)
    boolean required; // for validator to know if the column should have data when created, not all columns would always be needed
    
    public E get(EventEntry entry) {
        final RAW dataAtIndex = dataAccessor.accept(entry); 
        //TODO validate type
        if(dataAtIndex != null) {
            return getter.accept(dataAtIndex);
        }
        return null;
    }
}

public class EventType {
    int id; //set by registry
    String name; //mod+unique
    EventColumn[] columns;
    
    public <DATA> getColumn(int id, EventEntry entry) {
        //TODO validate type
        return (DATA)columns[id].get(entry);
    }
    
    //TODO validator function, would want to check the data[] input to ensure we match our columns
    
    //TODO logger function, would want to have a default to spit out to a log file
}

public class EntryEvent {
    EventType type;
    Object[] data;
}
```