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
- launcher screen show distance
- launcher screen show firing countdown
- launcher pad show effect when countdown has started... maybe flashing lights? Such as warning hazards
- launcher min distance check config
- launcher min distance check user setting in tile

## Chores

- entity logs switched over to event system
- missile logs switched over to event systems

## Unit testing

- MinecraftJUnit - add world id to test manager

## Cluster missiles (prototype)

- this will eventually be moved to an addon to better control the feature set... maybe?
- reminded of this after talking to island (5/20/2023)
- example: https://youtu.be/Es1A1XoM5ZE?t=10
- new entity type for missile extending base type, though we might be able to recycle existing explosive missile?
- new entity type for droplets extending projectile class, this way it works the same as missiles without engines
- new explosive type for cluster, this might take some rework of how explosive settings work. Currently, they are NBT driven but this might not be realistic
- droplets should be an explosive container via capabilities just like missiles
- droplets should go off on impact and a timer
- droplets should have a failure rate, by default turned off in config
- droplets should have a fall mode, default should be gravity driven
- droplets should have a fall mode to simulate parachutes or fins
- droplets should spin and easily spread out from host
- droplets with fins should spin less and guide on target better
- droplets should track explosive type, scale, and settings
- cluster missile should isolate this logic as an explosive type with settings
- settings should include droplet count
- settings should allow for mixed droplet types, making it possible to have different explosives and settings
- limits on settings should be controlled via crafting, core explosive should just take input data