# INFO

This log contains changes made to the project. Each entry contains changed made after the last version but before the number was changed. Any changes made after a number change are considered part of the next release. This is regardless if versions are still being released with that version number attached. 

# Versions

## 1.12.2-6.3.1 - June 20th, 2024

* Fixed: protective armor config not loading

## 1.12.2-6.3.0 - June 20th, 2024

### Runtime Changes

* Added: config to disable JEI support
* Added: config to disable payload recipes in JEI
* Changed: emp tower to ignore missile intercepts outside it's max range
* Reworked: missile emp effect
* Fixed: conditional action system not building meta types (broke emp and cluster)
* Fixed: emp tower not firing
* Fixed: emp tower animation not playing if power requirements were off
* Fixed: emp tower showing power requirements when power was disabled
* Fixed: possible issue with flying block duplication, isn't confirmed but changes setBlock from status 3 to 2

#### Missile EMP Rework

Previously missile would only disable it's engine on EMP. With 6.3.0 it will randomly switch between engine disable, engine fuel explosion, missile destruction, and payload triggering. All of this is configurable and can be disabled as desired.

Order of chance is destruction -> engine/guidance -> payload. Chance is less than equal to value set. Random is re-run per each section. With first to pass being picked.

* Destruction: 50%
    * Fuel: 100% -> Explosion: 1f
* Engine: 80%
    * guidance -> set to deadlogic zero fuel, falls out of sky but maintains existing velocity
* Payload: 10%
    * ExMissile -> explosive
    * ActionMissile(cluster) -> action
    * SAM -> Explosion: 1f

### Dev Changes

* Fixed: MetaTag generation using key as domain and other janky issues. This broke the MetaTag.isType check
* Changed: MetaTag key to be namespace (mod's domain) + path (previously key)
* Changed: MetaTag subtype creation to prefix the parent's path to better organize. Previously key was just `icbmclassic:error` now is `icbmclassic:action.status.red.error`. This will help with translation keys later and make it more clear what the tag is by name alone.

## 1.12.2-6.2.0 - June 14, 2024

* Added: config for missile visual arc limit
* Changed: missile arc default from 200 to 500 meters
* Fixed: blast not clearing if error is thrown
* Fixed: multi-threaded blast re-running after chunk reloads
* Fixed: blast not clearing if failed to load from save
* Fixed: blast saying failed to find explosive data when it was actually found during world load
* Fixed: explosive data returning null for a non-null api
* Fixed: blasts not removing from world tick handler
* Fixed: missiles entering simulation at world height due to vanilla chunk load detection

## 1.12.2-6.1.0 - June 12, 2024

* Added: configs for radar station
* Changed: simulation enter height to 500 from 25
* Changed: simulation exit height to 450 from 250
* Changed: simulation exit speed to 1m/t from 2m/t 
* Changed: sam firing delay from %tick to countdown reset
* Changed: sam firing delay to 40 ticks from 10 ticks
* Fixed: config gui crash due to parsing error
* Fixed: some console log spam left over from dev testing
* Fixed: typo in missile launcher power config

## 1.12.2-6.0.1 - May 26, 2024

TODO

## 1.12.2-6.0.0 - April 23rd, 2024

* Added: cluster missile - dynamic item support allowing anything to be added to payload. Some items have special projectile handling allowing spawning entity version instead of item.
* Added: projectile handling for arrows
* Added: projectile handling for potion arrows
* Added: projectile handling for spectral arrows
* Added: projectile handling for eggs
* Added: projectile handling for snowballs
* Added: projectile handling for spawn eggs
* Added: Parachute - allows attaching items/blocks for deployment
* Added: Balloon - similar to parachute but inverted gravity... mostly for lolz
* Added: ballistic arc version of rocket launcher. Allows firing a missile in same way as silo or basically a javelin launcher
* Reworked: FlyingBlocks to better handle placement and collect item version. Allows remembering machines and more complex blocks. Impacts redmatter, anti-gravity, endo, exo, sonic, and works with parachute system
* Reworked: Anti-gravity to remove 1 block per tick, apply outward motion, and extend flying block life to encourage return to ground
* Reworked large chunks of content to use action system, which will replace projectile and blast system as a generic conditional do-task system. For developers, this means large chunks of the API are deprecated and changed. Future plan is to feed everything through the action system and expose logic via listeners. Everything will have an action source, cause-by chain, and be abstracted to improve flexibility. Allowing for replacement of logic by other mods and interception of actions for better integrations. Such as providing improved world protection, handling admin logging, or even just showing events on a map.

TODO finish

## 1.12.2-5.4.0 - October 10th, 2023

### Runtime Changes

* Added: blockState ban/allow list for flying blocks
* Added: dynamictrees to flying block ban list due to block spawning issues
* Added: launcher rotation system
* Added: launcher firing solution system
* Added: uuid for launchers to track firing events
* Fixed(workaround): flying block crashing due to mods with broken block renders
* Fixed: redmatters not handling removal of explosive entities causing antimatter to fail to kill redmatter
* Fixed: some issues with redmatter movement
* Fixed: collision issues with redmatter causing issues with antimatter not killing redmatter
* Fixed: launcher min range check failing when only one axis was in use
* Fixed: issues with packet system
* Fixed: ore dictionary registering at wrong stage
* Fixed: missile platholder collision on seats
* Fixed: launcher rotation issues
* Fixed: launcher seat not saving player state properly
* Fixed: text inputs not resetting in some UIs
* Fixed: emp tower not respecting max range
* Fixed: accuracy being too high at shorter distances
* Reworked: launcher to use tick actions over ticks
* Removed: ic2 support, it didn't work and other mods provide FE to EU
* Note: add automated tests to confirm missile accuracy

### Dev Changes

* Added: blockState config list, can be used to detect any blockstate via config
* Added: entire mod selector for blockState
* Added: fuzzy start and end selector for blockState
* Added: block version of the above
* Added: common system for spawning flying blocks (used by anti-gravity, endothermic, exothermic, redmatter, etc)
* Added: new block, tool, and material icons by Morton00000 see credits file for which
* Added: event system for read-only information on common events and network calls
* Added: break line support to lang files
* Reworked: network system to better handle encoding/decoding... basically codex but with lambdas
* Reworked: ballistic flight logic to use sub-types for more customization options
* Removed: old gps system
* Changed: builds to use amazon JDK 8 for better consistency

## 5.3.0 - May 6th, 2023

Mostly cleanup and refactors for better addon support

### Runtime Changes

* Added: energy upkeep cost for EmpTower. firingCost = range^2 * configTickingCost
* Added: config for emp tower max range
* Added: config for emp tower bonus range
* Added: config for emp tower cost per area scaling. tickingCost = range^2 * configValue
* Added: config for emp tower cost ticking
* Added: config for emp tower cooldown
* Added: config for emp tower energy buffer size for cost ticking. energyBuffer = firingCost + (tickingCost * configValue)
* Added: config for emp tower energy received limit
* Added: disable radio interaction using `alt + click` over radio icon
* Added: energy cost and upkeep tooltips to energy bar hover tooltip
* Updated: gui tooltips to improve readability

### Dev Changes

* Added: isDisabled flag to radio capability instance, not exposed to API calls
* Added: IMachineInfo interface for use with CC addon to access configs and settings
* Added: machine info to empTower, radarStation, launcher, cruise launcher
* Added: disabled icon button, useful for disabling functionality and showing it as a 'canceled' style overlay
* Added: line split functionality to lang utility, allows tooltips to next line using ` \\n `
* Added: line indent functionality to lang utility, allows tooltips to prefix 2 spaces using ` \\t `
* Refactored: launcher network to store host tile, needed for CC support
* Refactored: energy storage usage, dropped IEnergyBuffer in favor of IEnergyStorage as primary
* Refactored: EnergyBuffer to be more lambda driven for easier reuse
* Deleted: TilePoweredMachine, moved helpers to EnergyBuffer
* Deleted: TileFrequency and TileLauncherPrefab, both were empty and only used by cruise launcher
* Deleted: EnergyBufferLimited and PowerBuffer subtypes of EnergyBuffer

## 5.2.3 - May 1st, 2023

* Fixed: Incorrect version definition, leading to the CC addon not working

## 5.2.2 - April 29th, 2023

Mostly dev changes for CC addon https://github.com/BuiltBrokenModding/ICBM-Classic-CC-Addon

* Added: way to get missile unique key from builder capability
* Added: getting for cruise launcher's capability, for use in CC addon

## 5.2.1 - April 25th, 2023

* Fixed: redmatter init not getting config scale due to unimplemented method


## 5.2.0 - April 25th, 2023

### Runtime Changes

* Added: User defined firing delay to launcher base. Allows users to define a delay in ticks in the UI. Useful for firing several missiles or handling redstone door interaction.
* Added: Firing data delay to launcher base. Allows controllers to supply additional delay length on top of user defined.
* Added: Better launcher screen feedback for when launchers are not ready to fire
* Added: Error feedback when status system breaks client side. Shouldn't happen but now has an error so not to confuse users.
* Added: blast scale config options
* Added: several configs for nukes to handle sub-blasts of rot and mutation. Including ability to customize damage multiplier.
* Fixed: Some issues of action status not syncing to clients causing launchers to act as not ready client side

### Dev Changes

* Added: Way to define delays in firing data
* Added: Warning spam to chat if status action is not registered for save/load
* Added: interface for fuse delay, used with firing data
* Added: interface for firing delay, used with firing data

## 5.1.2 - April 14th, 2023

* Fixed: inventory hardcoding drops to slot zero

## 5.1.1 - April 14th, 2023

* Fixed: inventory hardcoding issues
* Fixed: launcher network connector blocks dropping launcher's inventory
* Fixed: launcher network invalidation issues
* Fixed: nameplate showing for launcher

## 5.1.0

* Fixed: redstone interaction issues with explosive blocks

## 5.0.0

* Added: launcher connector to pass power, data, and inventory
* Added: launcher network system allowing any block to be part of the launcher setup and connect
* Added: support for several launchers connect to a single or multi-screen setup. All the rockets \0/
* Added: on/off redstone toggle to radar UI
* Added: auto generation of radio ID to help users pick a unique ID
* Added: UI to launcher base to handle lock height and later features such as grouping & delay fire
* Added: logic to have fragments break blocks (glass, leaves) and pass through on impact (leaves)
* Added: logic to have missiles break and pass through blocks as well
* Added: easer egg for flying chickens on missiles
* Added: ability to fire entities from offhand leash
* Added: vanilla portal interaction for missiles
* Added: collision override register for missiles. Allows other devs to add custom interaction, including portal support.
* Added: mekanism portal interaction for missiles, requires portal frame block as backstop for detection to work
* Added: missile subtitles, not all sounds are covered yet
* Added: direct redstone support to cruise launchers
* Added: xz motion feezing to missiles fixing over/under shooting targets are larger ranges
* Added: distance based inaccuracy
* Removed: tiers from launcher
* Removed: multi-block from launcher
* Reworked: Launcher heavily to be capability driven and decoupled from screen
* Reworked: launcher frame/support to be primary decorative
* Reworked: cruise launcher to work with launcher screen
* Reworked: block visuals and models
* Reworked: item visuals
* Reworked: launcher base to no longer be 3D, simplifying visuals. Later updates/addons will add more visuals to make up for loss of tier visuals.
* Reworked: launcher screen to no longer be 3D, simplifying visuals. Later updates will add more screens and controllers that may be 3D
* Reworked: launcher GUIs to visually look nicer and be easier to use. Including adding tooltips for icons to understand what each component does.
* Reworked: radar to no longer be 3D, simplifying visuals. Later radar rewrite will include 3D radar blocks that connect to this screen.
* Reworked: credit tracking to better match artist/license to each asset we use. Future no asset will be added without known credit/license to avoid killing the project.
* Reworked: emp tower to be optional multi-block. Each block added increases max range.
* Reworked: emp tower to have spin up/down, cooldown, and animations
* Reworked: radio system to use packets and provide two way feedback to users
* Removed: old xmas content, still planning on making an addon for this but am lazy
* Renamed: sulfur to organic sulfur to match that it is creeper dropped
* Updated: localization
* Updated: user feedback for tool usage and interaction
* Updated: radio system to handle strings instead of just numbers. Allowing more complex IDs to be used.
* Fixed: missile bound check for launcher base
* Fixed: proxy being called twice on load
* Fixed: missile flight path issues
* Fixed: fragments spawning inside group depending on missile angle
* Fixed: duplicate block information
* Fixed: cruise launcher not consistently hitting target
* Fixed: radar registry issues for missiles not showing on detectors
* Fixed: radar registry crashing servers when chunks unload
* Fixed: missiles not raytracing blocks correctly. Some cases missiles can still clip through at higher speeds due to MC bugs.
* Fixed: lock height being used as det height
* Fixed: laser det firing on network thread instead of main thread causing crashes at random

## 4.4.0

* Updated: all missile models and textures

## 4.3.0

* Removed: hypersonic missile, we already have sonic and don't need duplicate types with minimal size difference.

## 4.2.1

* Fixed: lwjgl being called server side
* Fixed: cruise launcher text box selection issue
* Fixed: antidote missing from creative tab

## 4.2.0

* Added: localizations for smoke ex types
* Added: creative mode way to add minecarts to missiles
* Added: inventory support to cruise launcher
* Added: battery slot to cruise launcher
* Added: color blast
* Added: smoke blast
* Added: recipe lookup for missile modules
* Added: SAM missile
* Reworked: missile logic and functionality
* Reworked: direct fired missiles to have fuel
* Reworked: missiles to be heavily capability driven for flight, targeting, and references
* Reworked: launchers to view missiles as capabilities to allow more types to be added outside existing items/entities
* Updated: recipe data and made improvements
* Fixed: recipe issues
* Fixed: item frame rendering for missiles
* Fixed: rider issues with missile seat
* Fixed: missiles simulating while being ridden by players, or by players riding another entity
* Fixed: cruise launcher not aiming correctly with laser det
* Fixed: cruise launcher min range detection
* Changed: nuke and large blast step calculation to remove atan - small performance improvement
* Fixed: Blocks with a hardness smaller than 0 (e.g. bedrock) are not included in thermobaric and nuclear blast power calculation

## 4.1.0 - November 3rd, 2021

Redmatter was completely overhauled to switch from a blast process to an entity process. This means the redmatter is no longer wired directly to the blast system. Along with this change the redmatter's logic was completely reworked to use a new raytrace system. This solves some performance issues and improves collecting blocks in a radius.

New ray traces system works by tracing towards the edge of the collection bound. This bound will scale up from the smallest size to largest over time. Once it hits the largest size it will not increase until the redmatter grows in size.

### Runtime Changes

* Rebuilt: redmatter to work as an entity with completely new logic
* Added: configuration options for redmatter scale visuals
* Added: configuration options for redmatter scale effects
* Added: configuration options for redmatter decay
* Changed: redmatter to use a edge boarder raytracer
* Changed: redmatter render scale
* Changed: redmatter beam color randomizer to be more consistent
* Changed: redmatter beam render width and length to improve visuals
* Changed: redmatter to turn water to ice when collecting to avoid reflows
* Fixed: duplicate raytraces for nuclear blast
* Fixed: fake blast not tracking correct entity source
* Fixed: gas particle client sync issues
* Fixed: issues with gas pathfinder
* Fixed: localization issues
* Removed: redmatter movement... will be restored later (aka broken)

### Developer Changes

* Added: IBlast capability
* Added: IBlastMovable capability
* Added: automated testing for basic blast logic
* Added: automated testing for math helpers
* Renamed: several methods
* Cleanedup: imports and minor code issues spotted by sonar

## 4.0.1 - March 28th, 2020

### Runtime Changes
* Added: dustSulfur to ore dictionary
* Added: dustSaltpeter to ore dictionary
* Fixed: Grenade infinite looping when accessing capability
* Fixed: redmatter size packet not using entity id
* Improved: EMP tower rotation
* Removed: random drop spread from tnt defuse

### Developer Changes
* Added: sonar code scanning
* Added: JaCoCo code coverage reporting
* Added: github action pipeline
* Changed: IExplosiveData#getTier() to be non-null

## 4.0.0

This is not a complete list as we got lazy tracking things in the change log. Cutting this version after commit 9f4470b63e5d535a74e72b5a5b39c3fa988c3988

### Runtime Changes
 * Added: lang support to most chat commands
 * Added: Launch button to launcher control panels
 * Added: Launch button to cruise launcher
 * Added: Tooltip to rocket launcher to indicate that it can fire any missile when the player is in creative mode
 * Added: Configuration option to dis-/allow antimatter blast destroying blocks (set to not destroy blocks by default)
 * Added: Configuration option to dis-/allow antimatter blast killing entities (set to not kill entities by default)
 * Added: Configuration options to set the amount of time bomb carts, explosives, and grenades should take to explode after being ignited (seperate config options per bomb cart/explosive/grenade type)

 * Changed: Explosion resistance of concrete blocks (reinforced concrete is now more resistant than vanilla obsidian)
 * Changed: Anti-Gravitational blast no longer affects creative players

 * Fixed: Gui background and tooltips not being drawn
 * Fixed: Several minor things in English and German localization
 * Fixed: Orientation of placed down anvil explosive
 * Fixed: Contagious explosive removing mobs instead of mutating them
 * Fixed: Contagious explosive making too much sound
 * Fixed: Russian, German and English language files
 * Fixed: Radar station not properly getting removed from the world causing other blocks to behave unexpectedly
 * Fixed: Disabling items in config causing a crash
 * Fixed: Warning with EntityDataManager
 * Fixed: Pick block for launcher base and launcher frame
 * Fixed: Launcher control panels facing away from the player when placing them down
 * Fixed: Configuration option to disallow antimatter blast from destroying unbreakable blocks not working
 * Fixed: Explosives sliding around after being ignited
 * Fixed: Anti-Gravitational blast picking up liquids as well as fire and more, causing lag

 * Improved: Item tooltips
 * Improved: Performance at several parts of the mod
 * Improved: Creative tab sorting
 * Improved: Render of explosives (now flash like vanilla TNT and expand shortly before exploding)
 * Improved: Chemical explosives (now only damages in air-range, meaning it wont pass through walls anymore
             Damage is only applied where particles are spawned. Damage has been tweaked and scales with the time you are
             inside the area of effect. Added kill messages, etc.)
 * Improved: Antimatter blast behaviour. This includes optimizations, including making it work better with water
 
 * Removed: S-Mine
 * Removed: Missile Module
 * Removed; Homing Missile
 * Removed: Anti-ballistic Missile
 * Removed: Cluster Missile
 * Removed: Nuclear Cluster Missile
 * Removed: Rejuvenation Explosives and Missile
 * Removed: Battery Box

### Development Changes
 * Added: Several events (BlastCancelEvent, ExplosiveDefuseEvent, LaserRemoteTriggerEvent, LauncherSetTargetEvent, MissileChunkEvent, MissileRideEvent, RadarGunTraceEvent, RemoteTriggerEvent)
 * Added: Data fixers for explosives and more to be compatible with releases below this version
 * Added: NBTConstants file for all nbt keys
 * Added: Enums for TNT blast's push type and EMP tower's EMP mode
 * Added: @ObjectHolder annotation for blocks and items
 * Added: Additional smoke variant that doesn't move upwards after time
 * Added: Fallback for loading a world with an explosive that no longer exists. (Otherwise the game would crash)
 * Added: Code that verifies that we are not using the same NBT save string twice for two different things
 * Added: Tier "NONE" which used for blasts that do not have an explosive/missile/bomb cart/grenade

 * Fixed: LanguageUtility#splitByLine not splitting correctly

 * Changed: Chinese class/method/field/variable names to be in English

 * Improved: Code
 * Improved: Language file handling
 * Improved: Some class names
 * Improved: Checks of equality with Blocks.AIR. They are now using Block#isAir for compatbility
 
 * Removed: Unused model files

## 3.3.1
### Runtime Changes
* Added: Support for Atomic Science uranium in nuke recipe

## 3.3.0
### Runtime Changes
* Added: Worker thread system (generates several threads to do blast calculations)
* Added: Block placement queue to reduce lag when runing several blasts at once
* Added: Redmatter death messages
* Added: Translation keys for radar guns
* Added: Missile insertion from screen
* Added: Interaction from launcher frame to base & screen
* Added: Energy bars to machine UIs

* Changed: Launcher to remove missile on sneak + right click
* Changed: Launcher to open screen UI on right click
* Changed: nuclear explosive to use new thread
* Changed: Antimatter explosive to use new thread

* Fixed: Iron Plate sill having an ore-dictionary name causing issues with Atomic Science and other mods
* Fixed: Missile not removing from inventory for RPG
* Fixed: Redmatter being seen through walls
* Fixed: air checks in several blasts
* Fixed: explosive blocks dropping when destroyed by explosives
* Fixed: formating and translation issues
* Fixed: spike recipe not using ore name for sulfur
* Fixed: some issues with redmatter movement

* Reworked: Fragment entity fixing several issues and improving functionality

* Implemented: creative tab sorting
* Implemented: Orginal UIs for machines
* Implemented: Cruise launcher

* Improved: UI handling
* Improved: redmatter fluid handling

### Development Changes
* Added: batch scripts to make building easier
* Added: run and output folder to .gitignore

* Updated: IC2 API
* Updated: To latest snapshot
* Updated: deps list

* Renamed: methods

## 3.2.0
### Runtime Changes
* Added: Chunloading for impact target
* Added: Missile simulation to allow missiles to move while world is unloaded
* Added: Delayed missile launch to act as a launching animation
* Added: Missile smoke

* Fixed: Missile riding
* Fixed: Missing battery recipes
* Fixed: Some recipes not using sulfurDust ore dictionary value


## 3.1.0
### Runtime Changes
* Added: IC2 support
* Added: Config for sulfur drop
* Added: Add blast command to spawn blasts
* Added: alias name of icbm to icbmc command (only works if ICBM 2 is not installed)
* Added: runtime check to redmatter to prevent it from using to much CPU time each tick

* Fixed: Thread not starting if entity tick was set high (sponge bug)
* Fixed: explosives not generating block breaks
* Fixed: redmatter stalling main thread
* Fixed: tiles not cleaning up correctly when broken
* Fixed: crash when placing a different tile in a previous location of a screen or frame
* Fixed: is air check in projectile
* Fixed: breaching blast not rotating in some cases (missile and commands)
* Fixed: '/icbm remove all' not clearing redmatter (or explosives in general)
* Fixed: debug spam in some cases0.
* Fixed: energy issues for explosive pathing
* Fixed: not being able placing presure plates on explosive blocks

* Improved: Thread performance
* Improved: explosive pathing in threads
* Improved: Missile position rendering
* Improved: redmatter animation and scaling

* Removed: redmatter beam scale that made it hard to look at

### Development Changes
* Added: Scale option to blast creation
* Added: sub command system to reduce work making commands

* Reworked: Thread handling to reduce issues
* Renamed: packet handlers and other classes

## 3.0.0- 3.0.1 [1.12 Beta]
Not full list of changes as I neglected to use version #s during the update

### Runtime Changes
* Added: handling for power pass through for full launcher frame & support
* Added: More explosive cart types

* Rewrote: mod for 1.12
* Rewrote: to not use VoltzEngine in order to be standalone
* Rewrote: usage of metadata in some case in prep for 1.13 update

* Temp Removed: Some graphics and audio until they can be ported
* Temp Removed: Camo block
* Temp Removed: Cruise Launcher

### Development Changes
* Rewrote: mod for 1.12
* Rewrote: to include parts of CodingLib
* Rewrote: to include parts of VoltzEngine
* Rewrote: Network handling
* Rewrote: Multi-Block handling

