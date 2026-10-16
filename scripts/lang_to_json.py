#!/usr/bin/env python3
"""Converts 1.12 `.lang` files into 1.21 `<locale>.json` files.

- keys are kept verbatim (comments and blank lines dropped, last duplicate wins like 1.12)
- for every key ending in `.name` the key without `.name` is emitted too (1.21 looks that up)
- java-style format arguments that 1.21's TranslatableContents can not handle are converted:
  `%1$d`/`%1$f`/`%1$i` -> `%1$s`, `%d`/`%f` -> `%s`, typo `%1%d` -> `%1$s`, bare `%1` -> `%1$s`.
  Custom placeholders replaced by code (`%x %y %z %p %j`) are left alone.
- `§` formatting and literal `\\n` markers are kept as-is (handled by the font renderer / LanguageUtility)
- an optional extras json `{"<locale>": {key: value}}` is merged on top (only adds missing keys unless
  the locale entry is under "override")

Usage: python3 scripts/lang_to_json.py <lang_dir> [extras.json] [--delete]
"""
import json
import os
import re
import sys
from collections import OrderedDict

INDEXED = re.compile(r'%(\d+)\$[dfi]')
TYPO = re.compile(r'%(\d+)%[dfis]')
BARE_INDEX = re.compile(r'%(\d+)(?![\d$%])')
PLAIN = re.compile(r'(?<!%)%[dfi]')


def convert_value(value):
    value = TYPO.sub(r'%\1$s', value)
    value = INDEXED.sub(r'%\1$s', value)
    value = BARE_INDEX.sub(r'%\1$s', value)
    value = PLAIN.sub('%s', value)
    return value


def parse_lang(path):
    entries = OrderedDict()
    with open(path, encoding='utf-8-sig') as f:
        for line in f:
            line = line.rstrip('\r\n')
            if not line.strip() or line.lstrip().startswith('#') or '=' not in line:
                continue
            key, value = line.split('=', 1)
            key = key.strip()
            entries[key] = convert_value(value)
    return entries


def to_json(entries):
    out = OrderedDict()
    for key, value in entries.items():
        out[key] = value
    for key, value in entries.items():
        if key.endswith('.name'):
            short = key[:-len('.name')]
            if short not in entries:
                out[short] = value
    return out


def main():
    args = [a for a in sys.argv[1:] if not a.startswith('--')]
    delete = '--delete' in sys.argv
    lang_dir = args[0]
    extras = {}
    if len(args) > 1:
        with open(args[1], encoding='utf-8') as f:
            extras = json.load(f)

    for name in sorted(os.listdir(lang_dir)):
        if not name.endswith('.lang'):
            continue
        locale = name[:-len('.lang')].lower()
        data = to_json(parse_lang(os.path.join(lang_dir, name)))
        for key, value in extras.get(locale, {}).items():
            if key not in data:
                data[key] = value
        for key, value in extras.get('override', {}).get(locale, {}).items():
            data[key] = value
        out_path = os.path.join(lang_dir, locale + '.json')
        with open(out_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write('\n')
        print(f'{name} -> {locale}.json ({len(data)} keys)')
        if delete:
            os.remove(os.path.join(lang_dir, name))


if __name__ == '__main__':
    main()
