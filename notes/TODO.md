# Graphics Overhaul

## Missiles Tier 1

- ~~Condensed~~
- ~~Shrapnel~~
- ~~Incendiary~~
- ~~Debilitation~~
- ~~Chemical~~
- Anvil
- Repulsive
- Attractive
- ~~Colorful~~
- Smoke
- Missile Module

## Missiles Tier 2

- Fragmentation
- Contagious
- Sonic
- Breaching
- Thermometric

## Missiles Tier 3

- Nuclear
- EMP
- Exothermic
- Endothermic
- Anti-Gravitational
- Ender
- Hypersonic

## Missiles Special

- Antimatter
- Red Matter
- Surface to Air Missile

## Blocks Model

- Silo (Pad)
- Silo Frame Internal (Pad + Supports internal to block + extensions)
- Silo Frame External (Supports on outside, current rear support but as 1^3 blocks )
- Silo Support External (Side supports today, but as 1^3 blocks)
- Silo Screen
- Cruise Launcher
- Radar Controller
- EMP Tower

## Block Textures

- Reinforced Glass connected textures and new textures at 16x16
- Concrete textures
- Spikes

## Items

- RPG Model
- Radar Gun
- Remote Det
- Laser Det
- Tracker
- Signal Disrupter
- Defuser
- Battery
- Grenades
- Carts
- Wire Items
- Steel clump
- Bronze clump

## GUI

- EMP Tower
- Radar
- Silo Screen

## Audio

- Chemical should make a glass breaking sound on impact followed by gas leaking

# Unit tests:

- [ ] Unit Test Saving: Missiles Entity Ballistic
- [ ] Unit Test Saving: Missiles Entity Cruise

# Next release

- [ ] Add handling for EMP effect on tiles
- [ ] Add fuse disable chance for missiles when EMP hits 
- [ ] debilitation explosive (alt for lacking sulfur)
- [ ] incendiary explosive (alt for lacking sulfur)
- [ ] BlastConfusion keeps re-apply poison causing it to not take effect
- [ ] Config for projectile damage (Fragments, shrapnel, anvil)
- [ ] Min-Max damage limits for projectiles
- [ ] Damage types for projectiles

# Missile types

- [ ] Punching bag missile -> no damage, only pushes impacted entity very far
- [ ] Rope missile  -> when fired will leash anything nearby
- [ ] Cargo rocket -> Can do a 3x3 grid of items

# Explosive types

- [ ] Torch -> lights up an area
- [ ] Glass dome -> for KiriCattus
- [ ] Marking Smoke (colored) -> improve marking smoke to customize color
- [ ] Smoke missile -> Actual smoke cloud, reduce user render distance

# After MC update (likely after radar and launcher rewrites)

- [ ] Missile module as it's own item (not using explosive ID, similar to what we did with AB missile)
- [ ] Item per explosive (missile, grenade, cart, block, etc)
- [ ] Drop crafting items
- [ ] Dart game using fake missile items
- [ ] Dispenser logic for missiles
- [ ] remove crafting items

# TODO future launcher rewrite

- [ ] Drop tiers, no reason to have tiers as a gating mechanic. Do 1 launcher set with attachments for different feature.
- [ ] Split multi-block
- [ ] Make launcher base a single block with optional visual rails
- [ ] Use static models for launcher (try to do this with missile as well of possible)
- [ ] decouple controller from base
- [ ] redstone doesn't connect to launcher
- [ ] UI customization of ballistic flight path (Arc min height, max height, fuel burn speed)
- [ ] Fuel for missiles (more visualized max range)
- [ ] Track owner of launcher
- [ ] Track launching method (redstone, remote, etc)
- [ ] Passed tracked data into launcher source for better event handling
- [ ] Have missile spawn from PRG visually, ensure we raytrace for collisions to perevent shooting through walls
- [ ] Add fuel item (coal + redstone), all missiles default with a little but range in silo will change fuel usage
- [ ] Add fuel tank to silos, will use fuel item to add to level but other mods could add fluid support for automation
- [ ] Make fuel config driven(on/off, fuel usage rate, electricity usage rates)
- [ ] Simulated missiles are not taking with them entities attached
- [ ] Spawn eggs on missiles
- [ ] Mechanic to add entities to missile (leash?)

## Hand Launcher

- [ ] Scale down RPG missiles
- [ ] Add new missile item purely for RPG that are smaller in scale

## Cruise Launcher

- [ ] Angle aiming: yaw, pitch
- [ ] UI toggle to switch between pos, angle, relative(pos + self#blockPos)
- [ ] Angle limits
- [ ] Rotation speed adjustments
- [ ] Min range bypass and display using visuals (laser line with colors?)
- [ ] Colorblind friendly error indicators in UI
- [ ] Do a controlled translation line split for error text (can do this in general as well for better display of text)
- [ ] Improve energy bar render UI
- [ ] Improve energy bar tooltip format (add commas and metric increments to save space k FE, M FE)

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

- [ ] Remove modes
- [ ] Show power usage in UI
- [ ] Scale power usage with range
- [ ] Add cooldown with animation and UI feedback
- [ ] Add focus area of effect allowing users to have EMP go towards sky only in a cone
- [ ] Rework UI to be closer to vanilla UIs
- [ ] Add visual feedback to show area of effect
- [ ] Add hz remote triggering

# TODO future radar rewrite

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
- [ ] tool textures
- [ ] 3D model for laser
- [ ] Fake missiles for decoration
- [ ] Missiles point wrong direction in GUI, should match sword angle

# TODO future +2

- [ ] Cluster missiles
- [ ] Homing missiles
- [ ] Holiday themed armies
- [ ] Missile Health Balance
- [ ] Missile Armor balance
- [ ] Battery Slots
- [ ] Alt path to get Sulfur drops (newer mc has rocks in caves that could work as an on-break loot table)
- [ ] saltpeter recipe supports sewage fluid instead of water if present
- [ ] change `IMissileFlightLogic#shouldRunEngineEffects` to return an object containing settings (effect type, size, color, etc)
- [ ] create interface to customize what data is saved when simulating missile to avoid wasted data
- [ ] Balance recipes for Mekanism https://github.com/mekanism/Mekanism/blob/1.12/src/main/java/mekanism/common/Mekanism.java
- [ ] Balance recipes for IC2
- [ ] Migrate BombCart to capability and have it store ItemStack used to place