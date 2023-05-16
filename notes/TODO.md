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
