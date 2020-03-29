# INFO
This log contains changes made to the project. Each entry contains changed made after the last version but before the number was changed. Any changes made after a number change are considered part of the next release. This is regardless if versions are still being released with that version number attached. 

If this is a problem, use exact build numbers to track changes. As each build logs the git-hash it was created from to better understand changes made.

# Versions

## 4.0.2

### Runtime Changes

### Developer Changes

## 4.0.1

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

