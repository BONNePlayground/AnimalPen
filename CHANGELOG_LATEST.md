# Animal Pens 2.4.0

#### Changes:
- Changed Water Animal Container name to Aquatic Jar
- Changed Bird Catcher name to Avian Net
- Reworked block config screen.
- Reworked Variant Selection Screen
- Move stored variants to separate data-file to reduce ItemStack memory size
- Fixed incompatibility with mods that overwrite dispenser tool interactions
  Some mods are overwriting how dispenser interacts with items without keeping already existing behaviour, which broke interactions with animal pens.
- Implement server-side config option to block dispensers from interacting with animal pens (block_dispenser_interaction)

#### API:
- EntityFunction#interactDispenser method is changed and includes `index` of interaction slot. Old method is deprecated.