# General changes

- Launcher -> Make det height it's own field that controls raytrace offset for triggering warhead
- Missile -> Have it track distance to ground
- Redmatter merge logic isn't working
- Redmatter move logic isn't working
- Audio needs updated on all files
- Remove fire effect from shrapnel or make it cause fire (if so make fire change random)
- break apart fragment entity into different unique entities that extend projectile
- break apart shrapnel blast into different types that take an entity contractor from a base class (for re-usability)
- Portal Handling: TF https://github.com/TeamTwilight/twilightforest/blob/1.12.x/src/main/java/twilightforest/block/BlockTFPortal.java
- Portal Handling: RF Tools - Dimensions

# EMP tower Visuals

Render lightning-bolts coming from EMP tower coil to side panels. Simulating the side panels exist to absorb gap voltage discharge.

Render lightning-bolts coming from EMP tower to random nearby blocks. Simulating energy energy storage.

Allow overriding max range of EMP tower at the risk of the tower exploding. If overloaded increase lightning-bolts and start harming nearby blocks. Simulating risk of overload.

Have emp tower have a spin up and downtime.

When EMP tower is disabled have it discharge any stored power.

Have emp tower submerge in water... mostly for visuals

Add visual emp effect to attacked tiles, entities, and items

# Unit tests:

- Unit Test Saving: Missiles Entity Ballistic
- Unit Test Saving: Missiles Entity Cruise

# Explosive rework

- Exothermic water handling -> should make steam instead of removing water

# Radio System Rework

- Antenna block to receive and send signals
- Remove radio message handling from screen
- Have radio message block to take signal from antenna and do action
- Antenna range and signal loss chance

Basically mimic ICBM 2's system

# Next release or so

- Add reaction (damage) handling for EMP effect on tiles
- Add fuse disable chance for missiles when EMP hits
- debilitation explosive (alt for lacking sulfur)
- incendiary explosive (alt for lacking sulfur)
- BlastConfusion keeps re-apply poison causing it to not take effect
- Config for projectile damage (Fragments, shrapnel, anvil)
- Min-Max damage limits for projectiles
- Damage types for projectiles

# Missile types

- Punching bag missile -> no damage, only pushes impacted entity very far
- Rope missile  -> when fired will leash anything nearby
- Cargo rocket -> Can do a 3x3 grid of items
- Creeper -> easter egg type that has a mini-creeper rendered on top

# Explosive types

- Torch -> lights up an area
- Glass dome -> for KiriCattus
- Marking Smoke (colored) -> improve marking smoke to customize color
- Smoke -> Actual smoke cloud, reduce user render distance
- Egg spawn -> Spawns mobs using existing spawn items
- Spawn eggs on missiles

## Visuals

- Could use something like https://www.youtube.com/watch?v=D4l-soncsGY for doing shockwave

# After MC update (likely after radar and launcher rewrites)

- Missile module as it's own item (not using explosive ID, similar to what we did with AB missile)
- Item per explosive (missile, grenade, cart, block, etc)
- Drop crafting items
- Dart game using fake missile items
- Dispenser logic for missiles
- remove crafting items
- missile module per tier

# TODO future launcher rewrite

- ~~Drop tiers, no reason to have tiers as a gating mechanic. Do 1 launcher set with attachments for different feature.~~
- ~~Split multi-block~~
- ~~Make launcher base a single block with optional visual rails~~
- ~~Use static models for launcher (try to do this with missile as well of possible)~~
- ~~decouple controller from base~~
- redstone doesn't connect to launcher
- UI customization of ballistic flight path (Arc min height, max height, fuel burn speed)
- Fuel for missiles (more visualized max range)
- Track owner of launcher
- Track launching method (redstone, remote, etc)
- ~~Passed tracked data into launcher source for better event handling~~
- Have missile spawn from PRG visually, ensure we raytrace for collisions to perevent shooting through walls
- Add fuel item (coal + redstone), all missiles default with a little but range in silo will change fuel usage
- Add fuel tank to silos, will use fuel item to add to level but other mods could add fluid support for automation
- Make fuel config driven(on/off, fuel usage rate, electricity usage rates)
- Simulated missiles are not taking with their entities attached
- Mechanic to add entities to missile (leash?)
- ~~Change accuracy to be based on range? Forcing players to move closer to have a better hit chance~~
- Implement IMissileLauncher capability
- Move lock height to each launcher using a UI for adjustments
- Wire block, 6 side handling with 4 connections per side... old RI wires basically.
- inaccuracy to cruise launchers (random angle that at short distance is unnoticeable)
- better status message handling
- add back-blast pressure (pushes entities behind the missile away from the missile, silo this will be horizontal)

## Custom missile paths

- UI to control how a missile will fly when fired
- Way to globally save these custom 'scripts'
- Way to register new script parts
- Script: Move(xyz) -> Allows moving exact or relative
- Script: Arc(xyz, ijk) -> Same as ballistic flight but only the arcing part
- Script: Speed -> Sets the movement speed
- Script: onDimChange -> jump to label
- Script: label -> mark a spot in code

Thinking something close to how AssemblyLine robotic arms worked. A quick UI that gave the user
a scroll list to add commands to run.

## Launcher controllers

- CC support as block with proxy
- OC support as block with proxy
- redstone controller, takes xyz target and responds to redstone firing all missiles on network
- silo wireless network controller (ICBM 2 radio system)

## Laser Det

- Show hit position on screen, recycle atomic science overlay
- Show laser going from tool to player

## Hand Launcher

- Scale down RPG missiles
- Add new missile item purely for RPG that are smaller in scale

## Cruise Launcher

- Angle aiming: yaw, pitch
- UI toggle to switch between pos, angle, relative(pos + self#blockPos)
- Angle limits
- Rotation speed adjustments
- Min range bypass and display using visuals (laser line with colors?)
- Colorblind friendly error indicators in UI
- Do a controlled translation line split for error text (can do this in general as well for better display of text)
- Improve energy bar render UI
- Improve energy bar tooltip format (add commas and metric increments to save space k FE, M FE)
- laser pointer on new model to show aim
- Sight package on side of launcher
- Ability to use like launcher pad to fire ballistic missiles

## Cruise Launcher Model

- Needs to match cold-war era tows and AT rockets
- 3 leg design, 2 in front and 1 in back
- Green and dark metal texture
- Show battery when installed... show on base of launcher
- Show cables when attached... show on base of launcher towards connection point
- Show missile on top of the launcher
- Handle rotation from ball joint
- Show launch button and little computer screen
- Show optional laser pointer aiming at target
- Optional, allow extending the height to mirror some AT rockets poking over cover

## Silo Model

- 1x1x1 block footprint
- Slightly indented on top to have missile set inside a cradle like shape
- Dark metal texture
- Grate on top with hole for smoke collection
- Grate on bottom for smoke exit
- Power/Fluid/Item connections on sides
- Few lights on top to show status

# TODO emp tower rewrite

- Remove modes
- Show power usage in UI
- ~~Scale power usage with range~~
- Add cooldown with animation and UI feedback
- Add focus area of effect allowing users to have EMP go towards sky only in a cone
- Rework UI to be closer to vanilla UIs
- Add visual feedback to show area of effect
- Add hz remote triggering

# TODO future radar rewrite

- Line of sight (Only show radar areas not blocked by large objects)
- Time in air before detection starts or height above ground?
- Custom radar AABB (min xyz, max xyz, with max distance from radar)
- See if we can render an overlay of the area from a top down, can use pixel colors to save performance
- Better redstone control (side, output min, output max, missile detection equation (missiles - minCount > detectionCount))
- Way to see scan area outside of UI
- Trigger delays on redstone and radio signal for AB missiles
- radar classification system to give entities a shape, size, and threat level. Allowing an abstraction from just reporting explosive missiles
- cleanup radar map to use consumer pattern so it stops wasting as much RAM making `arraylists` all the time
- create CC multi-block style monitor to show real time radar detection on face of blocks. Handle all 6 sides and do connected textures
- Allow screen to optionally render detected stuff on face
- Add animated texture to radar screen

# TODO future (Art update)

- Cart textures
- Grenade textures
- tool textures
- 3D model for laser
- Fake missiles for decoration

# TODO future +2

- Cluster missiles
- Homing missiles
- Holiday themed armies
- Missile Health Balance
- Missile Armor balance
- Battery Slots
- Alt path to get Sulfur drops (newer mc has rocks in caves that could work as an on-break loot table)
- saltpeter recipe supports sewage fluid instead of water if present
- change `IMissileFlightLogic#shouldRunEngineEffects` to return an object containing settings (effect type, size, color, etc)
- create interface to customize what data is saved when simulating missile to avoid wasted data
- Balance recipes for Mekanism https://github.com/mekanism/Mekanism/blob/1.12/src/main/java/mekanism/common/Mekanism.java
- Balance recipes for IC2
- Migrate BombCart to capability and have it store ItemStack used to place

# Packet system

- Use a lambda based function system to register packets per tile
- Remove logic from tile itself

Idea would be to clean up the code and make it as easy as possible to deal with packets. Can mimic how the SaveNode system works.

# Random ideas

- Fishy Missile (Torpedo) -> 2 block long missile that moves through water like a fish
- Torp launchers -> for the above to deal better with water builds
- Missile analyser -> Shows information about the missile and explosive. Such as max range, fuel consumption, energy consumption, area of effect, and damage types.
- Mechanical Deadman switch -> Hold to start, Release to setoff, alt-click to disarm
- Deadman switch -> Heart beat version of the above, right click to start, right click to stop... on death triggers
- Tracker item -> As an addon or standalone mod, place a physical entity on the player like an arrow. Then have it send a radio signal at short range that an item can listen for to find.
- Diffuser kit -> have it open a mini-game to defuse explosives, pull from a customizable set of puzzles that have to be solved in a limited time.