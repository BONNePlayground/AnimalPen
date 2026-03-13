# Animal Pens 2.3.1

#### Fixes:
- Fixes sheep not growing back wool (visually)
- Replace spawn egg getter to platform independent method, so each mod does not need manual adding.

#### API Changes:
- Change `setSheared` to `setShearedState` in `ShearStateAccessor` interface doe to obfuscation access issues
- Add new function: `drop_loot` which drops one roll from loot table specified in `value` field.