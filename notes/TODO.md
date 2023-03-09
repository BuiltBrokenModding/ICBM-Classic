# TODO

Current objective for work on the project

Objective: Graphics Overhaul for `v5.x.x` release

## GUI

### Radar

- Convert radar dots to a object map
- Sync map to client instead of dot list
- Rescale map to match range
- Show detection range on map
- Show impact locations on map
- Mimic layout of cruise launcher minus button
- Add redstone toggle button
- Move trigger and detection fields in place where xyz is at in cruise launcher
- Move coordinate text to under radar map and include what is being hovered over (missile, ghast, etc)
- Update to use actual entity icons instead of just dots

### Silo Screen

- Migrate to xyz as a single text box
- Mimic layout of cruise launcher
- Add detonation height as it's own field
- Show inaccuracy better -  use real time data instead of fixed data
- Show status of all launchers instead of first
- sync status from server to client, don't calculate on client
- add energy slot

### Launch Pad

- Mimic layout of cruise launcher
- Add battery slot
- Add missile slot
- Add lock height
- Add launch index - will be unused for now. Meant for use with controllers to cycle between launcher sets as a delay
- Add launch delay

## Missiles/Launcher Work Left

- Glass texture render
- Engine smoke position
- Engine smoke color
- GUI item render scale
- Frame item render scale
- Silo rendering missiles as a block state
- Redmatter fluid in missile, have it move in circle for center tank and left-right inverse on lower tubes. 2 pixels left, 2 pixels right, repeat to give a centrifuge like visual.
- Render cable coming from frame to missile. Recycle rope render from minecraft and increase in size

## Block Textures

- Use new explosive textures
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