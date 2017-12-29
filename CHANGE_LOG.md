# INFO
This log contains changes made to the project. Each entry contains changed made after the last version but before the number was changed. Any changes made after a number change are considered part of the next release. This is regardless if versions are still being released with that version number attached. 

If this is a problem, use exact build numbers to track changes. As each build logs the git-hash it was created from to better understand changes made.

# Versions
## 2.16.3 - 12/28/2017
### Runtime Changes
Added: translations to nightmare missile
Added: rematter ore (easter egg, not used for crafting)
Added: improved growth mechanics for redmatter
Fixed: redmatter animation
Fixed: redmatter graphic scale
Fixed: redmatter gaphics not being centered
Fixed: camo block inventory render (was a few pixels too high)
Fixed: launcher frame and base item renders
Fixed: Exothermic placing invalid fire blocks
        caused crashes due to MC attempting to clear invalid fire 
        blocks but triggering infinite loop in proces. Should not happen
        anymore. However, the issue is a vanilla bug that can happen with 
        any block.
Fixed: Settings not generating
Increased: redmatter gravity from 0.015 to 0.02
Increased: redmatter default size from 35 to 70
Implemented: redmatter disk render
Implemented: redmatter size scaling (will change size visually to match effect size)
Reworked: Configuration file

Removed: recipe disable configs (has been replaced with JSON recipe removal)

## 2.16.2 - 11/3/2017
### Runtime Changes
Added: Waila support for camo blocks
Added: Waila support for launcher parts
Added: Nightmare missile - Halloween themed entity spawn explosive
Fixed: Missile mounting on launchers (aka fixed missing riding)
Fixed: button hardness
Changed: cruise launcher energy cost from 100M UE to 10K UE (20k RF)

### Development Changes
Renamed: a few fields and methods (removing Chinese leftovers from calclavia's time)

//TODO update change log for older versions
