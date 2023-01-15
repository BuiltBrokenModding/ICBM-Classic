# Ready for merge

- [X] AB Kill logic (Not dying after killing missile)
- [X] AB tacking issue with base launcher
- [X] Cruise Missile AB as item (UI thinks it is invalid, likely server does as well)
- [X] Base Launcher Missile AB as item (right click of missile is not working as expected)
- [ ] Unit Test Saving: Explosive (TileEntity, Item)
- [ ] Unit Test Saving: Grenade (Entity, Item)
- [ ] Unit Test Saving: Carts (Entity, Item)
- [ ] Unit Test Saving: Missiles (Entity, Item)
- [ ] Unit Test Saving: Launchers (TileEntity, Inventory holding onto missile data)

# Next release TODO:

- [ ] Fix smoke localizations and textures
- [ ] Fix AB config translations
- [ ] Improve Smoke usefulness
- [ ] Fix recipes (nuke, ???)
- [ ] AB missile scaling problems
- [ ] AB missile model?
- [ ] AB missile recipe
- [ ] Radar redstone not updating correctly


# TODO future launcher rewrite

- [ ] Split multi-block
- [ ] Use static models for launcher (try to do this with missile as well of possible)
- [ ] decouple controller from base
- [ ] redstone doesn't connect to launcher
- [ ] UI customization of ballistic flight path (Arc min height, max height, fuel burn speed)
- [ ] Fuel for missiles (more visualized max range)

## TODO future radar rewrite

- [ ] Line of sight (Only show radar areas not blocked by large objects)
- [ ] Time in air before detection starts or height above ground?
- [ ] Custom radar AABB (min xyz, max xyz, with max distance from radar)
- [ ] Radar UI colorization for danager
- [ ] Change scale of radar grid to show AABB and range
- [ ] See if we can render an overlay of the area from a top down, can use pixel colors to save performance
- [ ] Better redstone control (side, output min, output max, missile detection equation (missiles - minCount > detectionCount))
- [ ] Way to see scan area outside of UI

# TODO future (Art update)

- [ ] Cart textures
- [ ] Grenade textures
- [ ] remove crafting items
- [ ] tool textures
- [ ] 3D model for laser

# TODO future +2

- [ ] Cluster missiles
- [ ] Homing missiles
- [ ] Holiday themed armies
- [ ] Missile Health Balance
- [ ] Missile Armor balance
- [ ] Battery Slots