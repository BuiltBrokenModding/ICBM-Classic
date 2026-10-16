#!/usr/bin/env python3
"""Checks that blockstates/models only reference models and textures that exist, and that every
registered block/item has a blockstate/item model. Usage: python3 scripts/check_assets.py"""
import json, os, re, glob, sys
BASE = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..')
A = os.path.join(BASE, 'src/main/resources/assets/icbmclassic')
JAVA = os.path.join(BASE, 'src/main/java/icbm/classic/content/reg')
VANILLA_OK = ('minecraft:', 'builtin/')
problems = []

def norm(ref, default_folder=None):
    if ':' not in ref:
        ref = 'minecraft:' + ref
    return ref

def model_exists(ref):
    ref = norm(ref)
    ns, path = ref.split(':', 1)
    if ns != 'icbmclassic':
        return True
    return os.path.exists(os.path.join(A, 'models', path + '.json'))

def tex_exists(ref):
    ref = norm(ref)
    ns, path = ref.split(':', 1)
    if ns != 'icbmclassic':
        return True
    return os.path.exists(os.path.join(A, 'textures', path + '.png'))

for f in glob.glob(os.path.join(A, 'models/**/*.json'), recursive=True):
    d = json.load(open(f))
    if 'parent' in d and not model_exists(d['parent']):
        problems.append(f'{f}: missing parent {d["parent"]}')
    for k, t in d.get('textures', {}).items():
        if not t.startswith('#') and not tex_exists(t):
            problems.append(f'{f}: missing texture {t}')
    for o in d.get('overrides', []):
        if not model_exists(o['model']):
            problems.append(f'{f}: missing override model {o["model"]}')

def walk(v, f):
    if isinstance(v, dict):
        if 'model' in v and isinstance(v['model'], str):
            if not model_exists(v['model']) or ':' not in v['model'] or '/' not in v['model']:
                problems.append(f'{f}: bad model {v["model"]}')
        for x in v.values():
            walk(x, f)
    elif isinstance(v, list):
        for x in v:
            walk(x, f)

for f in glob.glob(os.path.join(A, 'blockstates/**/*.json'), recursive=True):
    d = json.load(open(f))
    if 'forge_marker' in d:
        problems.append(f'{f}: forge_marker blockstate')
    walk(d, f)

blocks = re.findall(r'BLOCKS\.register\("([a-z_0-9]+)"', open(os.path.join(JAVA, 'BlockReg.java')).read())
items_src = open(os.path.join(JAVA, 'ItemReg.java')).read()
items = re.findall(r'ITEMS\.register\("([a-z_0-9]+)"', items_src) + re.findall(r'crafting\([^,]+, "([a-z_0-9]+)"', items_src)
items += [b for b in blocks if re.search(r'blockItem\(BlockReg\.\w+\)', items_src)]
for b in blocks:
    if not os.path.exists(os.path.join(A, 'blockstates', b + '.json')):
        problems.append(f'no blockstate for block {b}')
for i in set(items):
    if not os.path.exists(os.path.join(A, 'models/item', i + '.json')):
        problems.append(f'no item model for {i}')
print('\n'.join(problems) or 'OK')
