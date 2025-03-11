# Animal Pens 1.3.1

- Adds ability to define which items can attack animal pens using `can_attack_pen` item tag.
- Adds ability to define which items can attack aquarium using `can_attack_aquarium` item tag.
- Implements ability to hold attack button to kill entities
  - Adds config option to define cooldown value
- Implements animal variant storage
  - Variants can be viewed and selected by clicking on animal pen/aquarium with empty hand
  - The menu allows to select/delete animal variant
  - Adds config option to limit amount of variants players can store
  - Supports buttons: arrow up, arrow down, enter and delete
- Implements display size changing using the new menu
  - Supports arrow left and arrow right to change size by 1 step
- Implements cooldown viewing in the new menu
- Implements new config value population on installing new version.