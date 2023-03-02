# Graphics Overhaul

## Missiles Work Left

- Glass texture render
- Engine smoke position
- Engine smoke color
- UI scale
- Frame scale
- Silo rendering missiles as a block state

https://forums.minecraftforge.net/topic/95608-solved-1164-forgemulti-layer-block-rendering-issue/

Redmatter fluid in missile, have it move in circle for center tank and left-right inverse on lower tubes. 2 pixels left, 2 pixels right, repeat to give a centrifuge like visual.

Render cable coming from frame to missile. Recycle rope render from minecraft and increase in size

## Multi-Block Rework

- Multi-Block -> Place launcher frame
- EMP Tower -> Place multi-block self as a state
- Replace old back frame with frame decoration blocks
- tool interaction with multi-block (radar gun, laser det, tracker)

## Blocks Model

- ~~Silo (Pad)~~
- ~~Silo (Connector)~~ -> goes under frame as a full block to provide wire connections to silo pad
- ~~Silo Frame~~
- ~~Silo Screen~~
- ~~Cruise Launcher~~ -> static model, with upper animated model
- Radar Controller -> static model no animation
- EMP Tower -> static model with two state (charged, discharged) with charged having an animated electric creeper like texture

## Block Textures

- Reinforced Glass connected textures and new textures at 16x16
- Concrete textures
- Spikes
- connected textures for launcher connector

## Items

- RPG Model
- Render warhead from each model on RPG
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

# Next release

- Add handling for EMP effect on tiles
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
- Passed tracked data into launcher source for better event handling
- Have missile spawn from PRG visually, ensure we raytrace for collisions to perevent shooting through walls
- Add fuel item (coal + redstone), all missiles default with a little but range in silo will change fuel usage
- Add fuel tank to silos, will use fuel item to add to level but other mods could add fluid support for automation
- Make fuel config driven(on/off, fuel usage rate, electricity usage rates)
- Simulated missiles are not taking with their entities attached
- Spawn eggs on missiles
- Mechanic to add entities to missile (leash?)
- ~~Change accuracy to be based on range? Forcing players to move closer to have a better hit chance~~
- Implement IMissileLauncher capability
- Move lock height to each launcher using a UI for adjustments
- Wire block, 6 side handling with 4 connections per side... old RI wires basically.

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
- Scale power usage with range
- Add cooldown with animation and UI feedback
- Add focus area of effect allowing users to have EMP go towards sky only in a cone
- Rework UI to be closer to vanilla UIs
- Add visual feedback to show area of effect
- Add hz remote triggering

# TODO future radar rewrite

- Line of sight (Only show radar areas not blocked by large objects)
- Time in air before detection starts or height above ground?
- Custom radar AABB (min xyz, max xyz, with max distance from radar)
- Radar UI colorization for danager
- Change scale of radar grid to show AABB and range
- See if we can render an overlay of the area from a top down, can use pixel colors to save performance
- Better redstone control (side, output min, output max, missile detection equation (missiles - minCount > detectionCount))
- Way to see scan area outside of UI
- Trigger delays on redstone and radio signal for AB missiles
- radar classification system to give entities a shape, size, and threat level. Allowing an abstraction from just reporting explosive missiles
- cleanup radar map to use consumer pattern so it stops wasting as much RAM making `arraylists` all the time

# TODO future (Art update)

- Cart textures
- Grenade textures
- tool textures
- 3D model for laser
- Fake missiles for decoration
- Missiles point wrong direction in GUI, should match sword angle

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

# Random ideas

- Fishy Missile (Torpedo) -> 2 block long missile that moves through water like a fish
- Torp launchers -> for the above to deal better with water builds