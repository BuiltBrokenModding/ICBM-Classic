# INFO
This log contains changes made to the project. Each entry contains changed made after the last version but before the number was changed. Any changes made after a number change are considered part of the next release. This is regardless if versions are still being released with that version number attached. 

If this is a problem, use exact build numbers to track changes. As each build logs the git-hash it was created from to better understand changes made.

# Versions
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

