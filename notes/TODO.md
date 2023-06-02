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
- missile needs to fly up until it matches y level of target if under target

## Features

- add intentional spread to launchers, as accuracy is a bit too high at times
- Horizontal support rails to hold the new sideways missiles. Should be half slap in size
- Rotation for existing rails?
- Internal support rails to place above launcher?
- Redstone output from launcher... think I already documented this one but really want to add it
- have some missiles place a missile module placeholder sticking out of ground after use (chemical types, ender, any non-explosive)
- launcher screen show distance
- launcher screen show firing countdown
- launcher pad show effect when countdown has started... maybe flashing lights? Such as warning hazards
- launcher min distance check config
- launcher min distance check user setting in tile
- drill missile, basic version should work by breaking blocks slowly over time. Think matrix movies with the drones. It's an old idea I had for artillects to use... as it would be scary to hear an AI drill down. Never did work on it much but was reminded by drill charges in one of the COD games, warzone?, I saw on youtube shorts
- drill seeker, advanced version of drill missile... can no-clip through soft blocks to seek out targets. 
- drill seeker sensors, ways to customize how the drill missile navigates... such as detecting electronics via energy waves, vibrations via movement, heat spikes, radiation, etc.
- color coded remotes to let users easily ID which remote to use
- way to disable remote to prevent mis-clicks or require like alt + rightClick
- radio channel display on items

## Blast system rework

- Decouple explosive from blast
- Decouple builder from blast
- Decouple content system from blast, we need to start providing explosiveData + settings as a factory per content. This way we can customize by type and even introduce repeating explosiveData with different settings.
- Decouple item metadata from content index... required for 1.13+ (aka we have to split to 1 ItemStack per content entry) 
- Have BlastTNT invoke vanilla logic
- replace blast response with action system used by launchers
- fragment should carry motion and angle of source entity. can recycle cluster logic but without the discs?

## Chores

- update curse forge page description and images
- update wiki with different version information, show old vs new
- entity logs switched over to event system
- missile logs switched over to event systems
- remove old missile stack system
- finish implementing customizations on explosive capabilities, not all of them save/load
- add automation to test customizations
- add projectile spawning defaults for all vanilla content and some mods
- figure out a way to support ItemArrow having a entity spawning method... it requires EntityLivingBase but missiles/blocks are not living... will need fake source, or forge PR?

## Unit testing

- MinecraftJUnit - finish adding world id to test manager

## Cluster missiles (prototype)

reminded of this after talking to island (5/20/2023)

example: https://youtu.be/Es1A1XoM5ZE?t=10

https://en.wikipedia.org/wiki/Cluster_munition

https://www.warrelics.eu/forum/ordnance-ammo/bdu-28-b-dummy-bomb-754148/

https://www.ima-usa.com/products/original-u-s-vietnam-war-inert-bdu-28b-training-dummy-cluster-bomb-dated-1966?variant=40122823147589

https://www.bulletpicker.com/bomb_-dummy_-bdu-28_b.html

https://www.bulletpicker.com/pdf/TM%209-1385-51,%20Ammunition%20(Conventional)%20for%20Explosive%20Ordnance%20Disposal.pdf#page=453

### TODO

Strike-through means completed

- ~~this will eventually be moved to an addon to better control the feature set... maybe?~~
- create basic type in main mod
- create addon to expand basic type with customization
- ~~new entity type for missile extending base type, though we might be able to recycle existing explosive missile?~~
- ~~new entity type for droplets extending projectile class, this way it works the same as missiles without engines~~
- ~~new explosive type for cluster~
- improve settings pass through instead of using NBT compound
- ~~droplets should be an explosive container via capabilities just like missiles~~
- new FUSE component to use with missiles and bomblets
- ~~droplets should go off on impact~~ and a timer
- droplets should have a failure rate, by default turned off in config
- droplets should have a fall mode, ~~default should be gravity driven~~
- droplets should have a fall mode to simulate parachutes or fins
- droplets should spin and ~~easily spread out from host~~
- droplets with fins should spin less and guide on target better
- ~~droplets should track explosive type, scale, and settings~~
- ~~cluster missile should isolate this logic as an explosive type with settings~~
- ~~settings should include droplet count~~
- settings should allow for mixed droplet types, making it possible to have different explosives and settings
- limits on settings should be controlled via crafting, core explosive should just take input data

## Better understand Rotation math

https://stackoverflow.com/questions/31225062/rotating-a-vector-by-angle-and-axis-in-java

https://mathworld.wolfram.com/RotationMatrix.html

https://kennycason.com/posts/2008-12-25-graph3d-java-project-3d-points-to-2d.html

https://www.youtube.com/watch?v=gAbadNuQEjI

https://www.youtube.com/watch?v=4LKZw2_SUpA

https://medium.com/swlh/understanding-3d-matrix-transforms-with-pixijs-c76da3f8bd8

https://www.mathsisfun.com/algebra/matrix-multiplying.html

https://msl.cs.uiuc.edu/planning/node102.html

https://tutorial.math.lamar.edu/pdf/trig_cheat_sheet.pdf

https://www.youtube.com/watch?v=mhd9FXYdf4s