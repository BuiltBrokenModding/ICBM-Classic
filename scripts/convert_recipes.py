#!/usr/bin/env python3
"""One-shot converter for the 1.12 `assets/icbmclassic/recipes/**` JSON into 1.21.1 `data/icbmclassic/recipe/*.json`.

Handles: metadata flattening, ore dictionary -> `c:` tags, `icbmclassic:explosive` recipes/ingredients
(-> `neoforge:components` ingredients and component results), forge/icbm conditions -> `neoforge:conditions`.
Every reference to a config-optional ICBM item adds a `neoforge:item_exists` condition so disabled
items (ConfigItems.ENABLE_*) disable the recipe instead of failing to load.

Usage: python3 scripts/convert_recipes.py <old_recipes_dir> <new_recipe_dir>
"""
import json
import os
import sys

NS = 'icbmclassic'

EX_IDS = {0: 'condensed', 1: 'shrapnel', 2: 'incendiary', 3: 'debilitation', 4: 'chemical', 5: 'anvil', 6: 'repulsive',
          7: 'attractive', 8: 'fragmentation', 9: 'contagious', 10: 'sonic', 11: 'breaching', 13: 'thermobaric',
          15: 'nuclear', 16: 'emp', 17: 'exothermic', 18: 'endothermic', 19: 'antigravitational', 20: 'ender',
          21: 'hypersonic', 22: 'antimatter', 23: 'redmatter', 24: 'missile'}

DEVICES = {f'{NS}:block': f'{NS}:explosives', f'{NS}:minecart': f'{NS}:bombcart', f'{NS}:grenade': f'{NS}:grenade',
           f'{NS}:missile': f'{NS}:explosive_missile', f'{NS}:explosive_bomblet': f'{NS}:explosive_bomblet'}

EXPLOSIVE_ITEMS = {f'{NS}:explosives', f'{NS}:bombcart', f'{NS}:grenade', f'{NS}:explosive_missile', f'{NS}:explosive_bomblet'}

ORE = {
    'circuitBasic': 'c:circuits/basic', 'circuitAdvanced': 'c:circuits/advanced', 'circuitElite': 'c:circuits/elite',
    'ingotSteel': 'c:ingots/steel', 'ingotIron': 'c:ingots/iron', 'ingotGold': 'c:ingots/gold', 'ingotCopper': 'c:ingots/copper',
    'ingotUranium': 'c:ingots/uranium', 'pelletUranium235': 'c:pellets/uranium_235',
    'plateSteel': 'c:plates/steel', 'plateIron': 'c:plates/iron', 'wireCopper': 'c:wires/copper', 'wireGold': 'c:wires/gold',
    'dustSulfur': 'c:dusts/sulfur', 'dustSaltpeter': 'c:dusts/saltpeter', 'dustCarbon': 'c:dusts/carbon',
    'dustCharcoal': 'c:dusts/charcoal', 'dustCoal': 'c:dusts/coal',
    'plankWood': 'minecraft:planks', 'stickWood': 'c:rods/wooden', 'dirt': 'minecraft:dirt', 'treeLeaves': 'minecraft:leaves',
    'treeSapling': 'minecraft:saplings', 'string': 'c:strings',
}
ORE_ITEMS = {'paper': 'minecraft:paper'}  # ore names without a sensible tag

# (item, meta) -> new item. None meta = any
ITEMS = {
    ('minecraft:carpet', 0): 'minecraft:white_carpet',
    ('minecraft:coal', 0): 'minecraft:coal', ('minecraft:coal', 1): 'minecraft:charcoal',
    ('minecraft:skull', 1): 'minecraft:wither_skeleton_skull',
    ('minecraft:noteblock', 0): 'minecraft:note_block',
    ('minecraft:snow', 0): 'minecraft:snow_block',
    ('minecraft:wooden_button', 0): '#minecraft:wooden_buttons',
    ('minecraft:dye', 32767): '#c:dyes',
    ('icbmclassic:concrete', 0): 'icbmclassic:concrete', ('icbmclassic:concrete', 1): 'icbmclassic:concrete_compact',
    ('icbmclassic:concrete', 2): 'icbmclassic:concrete_reinforced',
    ('icbmclassic:spikes', 0): 'icbmclassic:spikes', ('icbmclassic:spikes', 1): 'icbmclassic:spikes_poison',
    ('icbmclassic:spikes', 2): 'icbmclassic:spikes_fire',
    ('icbmclassic:emptower', 0): 'icbmclassic:emptower', ('icbmclassic:emptower', 1): 'icbmclassic:emptower_extender',
    ('icbmclassic:circuit', 0): 'icbmclassic:basic_circuit', ('icbmclassic:circuit', 1): 'icbmclassic:advanced_circuit',
    ('icbmclassic:circuit', 2): 'icbmclassic:elite_circuit',
    ('icbmclassic:wire', 0): 'icbmclassic:copper_wire', ('icbmclassic:wire', 1): 'icbmclassic:gold_wire',
    ('icbmclassic:plate', 0): 'icbmclassic:steel_plate', ('icbmclassic:plate', 1): 'icbmclassic:iron_plate',
    ('icbmclassic:clump', 0): 'icbmclassic:steel_clump',
    ('icbmclassic:ingot', 0): 'icbmclassic:steel_ingot',
}
RENAMES = {'icbmclassic:glassButton': 'icbmclassic:glassbutton', 'icbmclassic:poisonPowder': 'icbmclassic:poisonpowder',
           'icbmclassic:ballisticLauncher': 'icbmclassic:ballisticlauncher',
           'mekanism:atomicalloy': 'mekanism:alloy_atomic'}
# ICBM items that config can disable (ConfigItems.ENABLE_*)
OPTIONAL = {'icbmclassic:steel_ingot', 'icbmclassic:steel_clump', 'icbmclassic:steel_plate', 'icbmclassic:iron_plate',
            'icbmclassic:basic_circuit', 'icbmclassic:advanced_circuit', 'icbmclassic:elite_circuit',
            'icbmclassic:copper_wire', 'icbmclassic:gold_wire', 'icbmclassic:battery'}

# Ingredients of ICBM parts are referenced through their tags, which keeps working when the item is disabled
PREFER_TAG = {'icbmclassic:copper_wire': 'c:wires/copper', 'icbmclassic:gold_wire': 'c:wires/gold'}


class Ctx:
    def __init__(self):
        self.required_items = set()


def flatten_item(item, meta):
    item = RENAMES.get(item, item)
    if (item, meta) in ITEMS:
        return ITEMS[(item, meta)]
    if meta in (None, 0, 32767):
        return item
    raise ValueError(f'Unknown meta item {item}@{meta}')


def explosive_ingredient(item, explosive):
    return {'type': 'neoforge:components', 'items': item, 'components': {f'{NS}:explosive': f'{NS}:{explosive}'}}


def ingredient(ing, ctx):
    if isinstance(ing, list):
        out = []
        for x in ing:
            c = ingredient(x, ctx)
            out.extend(c if isinstance(c, list) else [c])
        return out
    t = ing.get('type')
    if t == 'forge:ore_dict':
        ore = ing['ore']
        if ore in ORE_ITEMS:
            return {'item': ORE_ITEMS[ore]}
        return {'tag': ORE[ore]}
    if t == f'{NS}:explosive':
        device = ing.get('device', f'{NS}:block')
        if device == f'{NS}:missile.module':
            return explosive_ingredient(f'{NS}:explosive_missile', 'missile')
        return explosive_ingredient(DEVICES[device], ing['explosive'].split(':')[1])
    item = RENAMES.get(ing['item'], ing['item'])
    meta = ing.get('data', ing.get('damage'))
    if item in EXPLOSIVE_ITEMS and meta is not None:
        return explosive_ingredient(item, EX_IDS[meta])
    new = flatten_item(item, meta)
    if new in PREFER_TAG:
        return {'tag': PREFER_TAG[new]}
    if new.startswith('#'):
        return {'tag': new[1:]}
    if new in OPTIONAL:
        ctx.required_items.add(new)
    return {'item': new}


def result(res, recipe_type, ctx):
    count = res.get('count', 1)
    if recipe_type == f'{NS}:explosive':
        device = res.get('device', f'{NS}:block')
        out = {'id': DEVICES[device], 'count': count,
               'components': {f'{NS}:explosive': f'{NS}:{res["explosive"].split(":")[1]}'}}
        return out
    item = RENAMES.get(res['item'], res['item'])
    meta = res.get('data')
    if item in EXPLOSIVE_ITEMS and meta is not None:
        return {'id': item, 'count': count, 'components': {f'{NS}:explosive': f'{NS}:{EX_IDS[meta]}'}}
    new = flatten_item(item, meta)
    if new in OPTIONAL:
        ctx.required_items.add(new)
    return {'id': new, 'count': count}


def condition(c):
    t = c['type']
    if t in ('forge:and', 'forge:or'):
        return {'type': 'neoforge:' + t.split(':')[1], 'values': [condition(v) for v in c['values']]}
    if t == f'{NS}:ore_value':
        tag_empty = {'type': 'neoforge:tag_empty', 'tag': ORE[c['value']]}
        return tag_empty if c['condition'] == 'false' else {'type': 'neoforge:not', 'value': tag_empty}
    if t == 'minecraft:item_exists':
        exists = {'type': 'neoforge:item_exists', 'item': RENAMES.get(c['item'], c['item'])}
        return exists if c.get('condition', 'true') == 'true' else {'type': 'neoforge:not', 'value': exists}
    raise ValueError(f'Unknown condition {c}')


def convert(old):
    ctx = Ctx()
    t = old['type']
    new = {}
    conditions = [condition(c) for c in old.get('conditions', [])]
    if t in ('minecraft:crafting_shaped', f'{NS}:explosive'):
        new['type'] = 'minecraft:crafting_shaped'
        new['category'] = 'misc'
        if 'group' in old:
            new['group'] = old['group']
        new['pattern'] = old['pattern']
        new['key'] = {k: ingredient(v, ctx) for k, v in old['key'].items()}
    elif t == 'minecraft:crafting_shapeless':
        new['type'] = 'minecraft:crafting_shapeless'
        new['category'] = 'misc'
        new['ingredients'] = [ingredient(v, ctx) for v in old['ingredients']]
    else:
        raise ValueError(f'Unknown recipe type {t}')
    new['result'] = result(old['result'], t, ctx)
    conditions += [{'type': 'neoforge:item_exists', 'item': i} for i in sorted(ctx.required_items)]
    if conditions:
        new = dict([('neoforge:conditions', conditions)] + list(new.items()))
    return new


def name_for(rel):
    """Keeps the 1.12 recipe id (sub folders allowed in 1.21), lower cased: tools/rpgBallistic -> tools/rpgballistic"""
    return rel[:-len('.json')].lower()


def main():
    src, dst = sys.argv[1], sys.argv[2]
    os.makedirs(dst, exist_ok=True)
    count = 0
    for root, _, files in os.walk(src):
        for f in sorted(files):
            if not f.endswith('.json') or f.startswith('_'):
                continue
            path = os.path.join(root, f)
            rel = os.path.relpath(path, src).replace(os.sep, '/')
            with open(path) as fh:
                old = json.load(fh)
            new = convert(old)
            out = os.path.join(dst, name_for(rel) + '.json')
            os.makedirs(os.path.dirname(out), exist_ok=True)
            with open(out, 'w') as fh:
                json.dump(new, fh, indent=2)
                fh.write('\n')
            count += 1
    print(f'converted {count} recipes')


if __name__ == '__main__':
    main()
