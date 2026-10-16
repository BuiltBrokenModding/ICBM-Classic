#!/usr/bin/env python3
"""Writes 1.21 blockstates and models for the non-explosive ICBM blocks and items.

Re-runnable: overwrites only the files listed here. Explosive variant assets are owned elsewhere.
Usage: python3 scripts/gen_block_assets.py [resources_root]
"""
import json
import os
import sys

ROOT = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), '..', 'src', 'main', 'resources')
A = os.path.join(ROOT, 'assets', 'icbmclassic')
NS = 'icbmclassic'
DIRS6 = ['down', 'up', 'north', 'south', 'west', 'east']


def write(rel, data):
    path = os.path.join(A, rel)
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=2)
        f.write('\n')


def read(rel):
    with open(os.path.join(A, rel), encoding='utf-8') as f:
        return json.load(f)


def m(name):
    return f'{NS}:block/{name}'


def single(block, model):
    write(f'blockstates/{block}.json', {'variants': {'': {'model': m(model)}}})


def item_parent(item, model):
    write(f'models/item/{item}.json', {'parent': m(model)})


def item_generated(item, texture):
    write(f'models/item/{item}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:item/{texture}'}})


def set_render_type(model, rtype):
    data = read(f'models/block/{model}.json')
    data['render_type'] = f'minecraft:{rtype}'
    write(f'models/block/{model}.json', data)


def rot_variant(model, rot, horizontal):
    """1.12 BlockICBM rotation -> y rotation, `horizontal` maps direction -> y"""
    v = {'model': m(model)}
    y = horizontal.get(rot, 0)
    if y:
        v['y'] = y
    return v


# ---- simple cubes
for name, tex in [('concrete', 'concrete'), ('concrete_compact', 'concrete_compact'), ('concrete_reinforced', 'concrete_reinforced')]:
    write(f'models/block/{name}.json', {'parent': 'minecraft:block/cube_all', 'textures': {'all': f'{NS}:block/{tex}'}})
    single(name, name)
    item_parent(name, name)

single('reinforcedglass', 'glass_reinforced')
set_render_type('glass_reinforced', 'cutout')
item_parent('reinforcedglass', 'glass_reinforced')

for name in ['radioactive_dirt', 'radioactive_stone']:
    single(name, name)
    item_parent(name, name)

# ---- spikes (each flattened variant gets its own model)
for name in ['spikes', 'spikes_poison', 'spikes_fire']:
    write(f'models/block/{name}.json', {'parent': 'minecraft:block/tinted_cross', 'render_type': 'minecraft:cutout',
                                        'textures': {'cross': f'{NS}:block/{name}'}})
    single(name, name)
    write(f'models/item/{name}.json', {'parent': 'minecraft:item/generated', 'textures': {'layer0': f'{NS}:block/{name}'}})

# ---- glass pressure plate (vanilla PressurePlateBlock: powered)
write('models/block/glass_pressure_plate.json', {'parent': 'minecraft:block/pressure_plate_up', 'render_type': 'minecraft:cutout',
                                                  'textures': {'texture': f'{NS}:block/glass_pressure_plate'}})
write('models/block/glass_pressure_plate_down.json', {'parent': 'minecraft:block/pressure_plate_down', 'render_type': 'minecraft:cutout',
                                                       'textures': {'texture': f'{NS}:block/glass_pressure_plate'}})
# 1.12 inventory variant used the vanilla glass texture
write('models/block/glass_pressure_plate_inventory.json', {'parent': 'minecraft:block/pressure_plate_up', 'render_type': 'minecraft:cutout',
                                                            'textures': {'texture': 'minecraft:block/glass'}})
write('blockstates/glasspressureplate.json', {'variants': {
    'powered=false': {'model': m('glass_pressure_plate')},
    'powered=true': {'model': m('glass_pressure_plate_down')},
}})
item_parent('glasspressureplate', 'glass_pressure_plate_inventory')

# ---- glass button (vanilla ButtonBlock: face, facing, powered), same layout as minecraft:stone_button
for model in ['glass_button', 'glass_button_pressed', 'glass_button_inventory']:
    set_render_type(model, 'cutout')
button = {}
for powered in ['false', 'true']:
    model = m('glass_button_pressed' if powered == 'true' else 'glass_button')
    for face, x in [('floor', 0), ('wall', 90), ('ceiling', 180)]:
        for facing, y in [('north', 0), ('east', 90), ('south', 180), ('west', 270)]:
            yy = y if face != 'ceiling' else (y + 180) % 360
            v = {'model': model}
            if x:
                v['x'] = x
            if yy:
                v['y'] = yy
            if face == 'wall':
                v['uvlock'] = True
            button[f'face={face},facing={facing},powered={powered}'] = v
write('blockstates/glassbutton.json', {'variants': button})
item_parent('glassbutton', 'glass_button_inventory')

# ---- launcher base, property `facing` (6 dirs). 1.12 x:-90 == 270
base = {'up': {}, 'down': {'x': 180}, 'north': {'x': 270, 'y': 180}, 'south': {'x': 270},
        'east': {'x': 270, 'y': 270}, 'west': {'x': 270, 'y': 90}}
write('blockstates/launcherbase.json', {'variants': {f'facing={k}': dict({'model': m('silo_pad')}, **v) for k, v in base.items()}})
item_parent('launcherbase', 'silo_pad')

# ---- BlockICBM `rotation` blocks
SCREEN_Y = {'north': 180, 'south': 0, 'east': 270, 'west': 90}
CRUISE_Y = {'north': 0, 'south': 180, 'east': 90, 'west': 270}
write('blockstates/launcherscreen.json', {'variants': {f'rotation={d}': rot_variant('silo_screen', d, SCREEN_Y) for d in DIRS6}})
item_parent('launcherscreen', 'silo_screen')
write('blockstates/cruiselauncher.json', {'variants': {f'rotation={d}': rot_variant('cruise_launcher', d, CRUISE_Y) for d in DIRS6}})
item_parent('cruiselauncher', 'cruise_launcher')

frame = {}
for t in ['top', 'middle', 'bottom']:
    set_render_type(f'frame_{t}', 'cutout')
    for d in DIRS6:
        frame[f'rotation={d},type={t}'] = rot_variant(f'frame_{t}', d, CRUISE_Y)
write('blockstates/launcherframe.json', {'variants': frame})
item_parent('launcherframe', 'frame_middle')

# ---- radar station: rotation, redstone, type
radar_tex = {'off': 'radar_off', 'on': 'radar', 'warning': 'radar_warning', 'danger': 'radar_danger'}
for t, tex in radar_tex.items():
    write(f'models/block/radar_screen_{t}.json', {'parent': m('radar_screen'), 'textures': {'north': f'{NS}:block/screen/{tex}'}})
radar = {}
for d in DIRS6:
    for red in ['false', 'true']:
        for t in radar_tex:
            radar[f'redstone={red},rotation={d},type={t}'] = rot_variant(f'radar_screen_{t}', d, SCREEN_Y)
write('blockstates/radarstation.json', {'variants': radar})
item_parent('radarstation', 'radar_screen')

# ---- launcher connector: one face model per side and state, combined by multipart
FACE_UV = [0, 0, 16, 16]
for side in DIRS6:
    for state in ['connected', 'disconnected']:
        write(f'models/block/launcher_connector_{side}_{state}.json', {
            'parent': 'minecraft:block/block',
            'render_type': 'minecraft:cutout',
            'textures': {'face': f'{NS}:block/silo_connector/{state}', 'particle': f'{NS}:block/silo_connector/disconnected'},
            'elements': [{'from': [0, 0, 0], 'to': [16, 16, 16],
                          'faces': {side: {'uv': FACE_UV, 'texture': '#face', 'cullface': side}}}]
        })
multipart = []
for side in DIRS6:
    for val, state in [('true', 'connected'), ('false', 'disconnected')]:
        multipart.append({'when': {side: val}, 'apply': {'model': m(f'launcher_connector_{side}_{state}')}})
write('blockstates/launcher_connector.json', {'multipart': multipart})
item_parent('launcher_connector', 'launcher_connector')

# ---- emp tower: type
set_render_type('emp_tower_electric', 'translucent')
write('blockstates/emptower.json', {'variants': {
    'type=base': {'model': m('emp_tower_base')},
    'type=coil': {'model': m('emp_tower_coil')},
    'type=electric': {'model': m('emp_tower_electric')},
    'type=spin': {'model': m('emp_tower_spin')},
}})
item_parent('emptower', 'emp_tower')
item_parent('emptower_extender', 'emp_tower_1')

# ---- flattened crafting items
for item, tex in [('steel_ingot', 'ingot_steel'), ('steel_clump', 'clump_steel'), ('steel_plate', 'plate_steel'),
                  ('iron_plate', 'plate_iron'), ('basic_circuit', 'circuit_basic'), ('advanced_circuit', 'circuit_advanced'),
                  ('elite_circuit', 'circuit_elite'), ('copper_wire', 'wire_copper'), ('gold_wire', 'wire_gold')]:
    item_generated(item, tex)

# ---- balloon: 2d item model, 3d model for the entity renderer (1.12 used fake meta 1 / render=3d)
balloon = read('models/item/balloon.json')
if 'elements' in balloon:
    write('models/item/balloon_3d.json', balloon)
item_generated('balloon', 'balloon_craft')

print('done')
