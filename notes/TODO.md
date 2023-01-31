# Next release TODO:

- [ ] Unit Test Saving: Missiles Entity Ballistic
- [ ] Unit Test Saving: Missiles Entity Cruise
- [ ] Migrate BombCart to capability and have it store ItemStack used to place
- [X] Fix smoke localizations and textures
- [ ] Fix AB config translations
- [X] Improve Smoke usefulness
- [X] Nuke (alt for lacking uranium items)
- [X] AB missile scaling problems
- [X] AB missile model?
- [X] AB missile recipe
- [X] Radar redstone not updating correctly
- [X] RPG missiles colliding with player (creative collider???)
- [ ] Missile item frame doesn't render properly
- [ ] Entity riding stacks on  missiles containing a player, shouldn't be allowed for MissileTrackerWorld

# Recipe issues

- [ ] Emps explosive
- [ ] Code generator for carts
- [ ] Code generator for grenades
- [ ] debilitation explosive (alt for lacking sulfur)
- [ ] incendiary explosive (alt for lacking sulfur)

# After MC update (likely after radar and launcher rewrites)

- [ ] Missile module as it's own item (not using explosive ID, similar to what we did with AB missile)
- [ ] Item per explosive (missile, grenade, cart, block, etc)
- [ ] Drop crafting items
- [ ] Dart game using fake missile items
- [ ] Dispenser logic for missiles

# TODO future launcher rewrite

- [ ] Split multi-block
- [ ] Use static models for launcher (try to do this with missile as well of possible)
- [ ] decouple controller from base
- [ ] redstone doesn't connect to launcher
- [ ] UI customization of ballistic flight path (Arc min height, max height, fuel burn speed)
- [ ] Fuel for missiles (more visualized max range)
- [ ] Track owner of launcher
- [ ] Track launching method (redstone, remote, etc)
- [ ] Passed tracked data into launcher source for better event handling
- [ ] Have missile spawn from PRG visually, ensure we raytrace for collisions to perevent shooting through walls
- [ ] Scale down RPG missiles
- [ ] Add new missile item purely for RPG that are smaller in scale
- [ ] Add fuel item (coal + redstone), all missiles default with a little but range in silo will change fuel usage
- [ ] Add fuel tank to silos, will use fuel item to add to level but other mods could add fluid support for automation
- [ ] Make fuel config driven(on/off, fuel usage rate, electricity usage rates)

## TODO future radar rewrite

- [ ] Line of sight (Only show radar areas not blocked by large objects)
- [ ] Time in air before detection starts or height above ground?
- [ ] Custom radar AABB (min xyz, max xyz, with max distance from radar)
- [ ] Radar UI colorization for danager
- [ ] Change scale of radar grid to show AABB and range
- [ ] See if we can render an overlay of the area from a top down, can use pixel colors to save performance
- [ ] Better redstone control (side, output min, output max, missile detection equation (missiles - minCount > detectionCount))
- [ ] Way to see scan area outside of UI
- [ ] Trigger delays on redstone and radio signal for AB missiles
- [ ] radar classification system to give entities a shape, size, and threat level. Allowing an abstraction from just reporting explosive missiles
- [ ] cleanup radar map to use consumer pattern so it stops wasting as much RAM making `arraylists` all the time

# TODO future (Art update)

- [ ] Cart textures
- [ ] Grenade textures
- [ ] remove crafting items
- [ ] tool textures
- [ ] 3D model for laser
- [ ] Fake missiles for decoration

# TODO future +2

- [ ] Cluster missiles
- [ ] Homing missiles
- [ ] Holiday themed armies
- [ ] Missile Health Balance
- [ ] Missile Armor balance
- [ ] Battery Slots
- [X] Saltpeter crafting (likely had a furnace recipe)
- [ ] Alt path to get Sulfur drops (newer mc has rocks in caves that could work as an on-break loot table)
- [ ] saltpeter recipe supports sewage fluid instead of water if present
- [ ] change `IMissileFlightLogic#shouldRunEngineEffects` to return an object containing settings (effect type, size, color, etc)
- [ ] create interface to customize what data is saved when simulating missile to avoid wasted data
- [ ] Balance recipes for Mekanism https://github.com/mekanism/Mekanism/blob/1.12/src/main/java/mekanism/common/Mekanism.java
- [ ] Balance recipes for IC2