# 🐾 Animal Pens Mod

## ✅ Optimize Your Farms & Reduce Lag!

Tired of lag caused by massive animal farms? **Animal Pens** lets you store animals in specialized **pens**, **aquariums** and **aviaries** while keeping full functionality!  
No more overcrowded barns - keep your animals neatly contained while still breeding, shearing, milking, and collecting drops like normal.

***

## 🎮 How It Works:

✔️ **Animal Cage & Container** – Capture land animals with the **Animal Cage** and water creatures with the **Animal Container**.  
✔️ **Feeding animals acts as breeding** – No need for free-roaming animals, just feed them inside their pen!  
✔️ **Most vanilla interactions**(1) are possible, including:  
    🗡️ _Animal farming_ for loot  
    ✂️ _Shearing sheep_  
    🥛 _Milking cows, mooshrooms, and goats_  
    🍄 _Collecting mushroom stew from mooshrooms_  
    🐝 _Bee pollen regeneration_  
    🐢 _Turtle scutes dropping based on settings_  
    🖌️ _Brush scutes from armadillo_  
    🧺 _Water bucket to pick up fishes or axolotls_

✔️ **Custom Interactions:**  
🔹 Chickens, Turtles and Sniffers – Use a bucket to collect eggs.  
🔹 Turtles – Drop **scutes** after feeding (configurable timing).  
🔹 Sniffers – Require a bowl to drop seeds.  
🔹 Fish Feeding – Fish eat **kelp & seagrass**, while other water creatures eat **fish**.  
🔹 Frogs – Drop **Frog Lights** when fed a Magma Cube.

✔️ **Variant Storage & Selection**  
    • Picking up an animal **saves its variant** inside the cage.  
    • Right-click an empty-handed pen to **view stored variants**.  
    • If _visual animal growing_ is enabled, you can adjust the display size of the animals.
    • You can adjust the minimal amount of animals that should be protected from killing.
    • You can toggle if animal pen/aquarium should be interactable only by you.

✔️ **Releasing animals** by crouch clicking on ground.  
✔️ Animal Pens, Aquariums and Aviaries can send **Redstone** signal with comparator.  
✔️ **Dispenser** can be used on Animal Pens, Aquariums and Aviaries with certain tools.

(1) Features not added:  
     Any animal transformation  
     Any animal interaction with other entities

***

## 🔧 Customizable Settings:

Easily adjust gameplay with the **mod’s configuration file** _\[config/animal\_pen\_config.json\]_, allowing you to tweak:

🗡️ **Attack cooldown** - Allows to change how fast players can kill animals in pen.

📏 **Animal Growth System** (Optional):  
    • If enabled, **more animals = larger pen display**.  
    • Adjustable growth multiplier.

🎨 **Stored Variants** – Allows to change how many variants can be stored per pen.

***

## 🔧 Data Driven Settings:

### Added Three New Item Tags:

These tags define which items can attack their respective blocks:

*   **`animal_pen:can_attack_aquarium`** – Items in this tag can attack the **Aquarium** block.
*   **`animal_pen:can_attack_pen`** – Items in this tag can attack the **Animal Pen** block.
*   **`animal_pen:can_attack_aviary`** – Items in this tag can attack the **Aviary** block.

### Example: Adding Custom Weapons

To allow **custom\_knife** and **modded\_dagger** to attack pens, create the following file in your **datapack**:

#### 📂 **For Minecraft 1.20.6 and Below:**

Path: `your_custom_datapack/data/animal_pen/tags/items/can_attack_pen.json`

#### 📂 **For Minecraft 1.21 and Above:**

Path: `your_custom_datapack/data/animal_pen/tags/item/can_attack_pen.json`

```
{
  "replace": false,
  "values": [
    "mod1_id:custom_knife",
    "mod2_id:modded_dagger"
  ]
}
```

This allows **custom weapons** to be used for attacking animal pens and aquariums.

### Added Three New Entity Tags:

These tags define which items can attack their respective blocks:

*   **`animal_pen:animal_cage_pickable`** – Mobs that can be picked up by Animal Cage.
*   **`animal_pen:bird_catcher_pickable`** – Mobs that can be picked up by Bird Catcher.
*   **`animal_pen:water_mob_container_pickable`** – Mobs that can be picked up by Water Animal Container.

#### 📂 **For Minecraft 1.20.6 and Below:**

Path: `<your_custom_datapack>/data/animal_pen/tags/entity_types/animal_cage_pickable.json`

#### 📂 **For Minecraft 1.21 and Above:**

Path: `<your_custom_datapack>/data/animal_pen/tags/entity_type/animal_cage_pickable.json`

### Customizable Interaction

You can also add or change interactions each animal. You can do it with data packs.
To add animal interaction you need to create a file: `<your_custom_datapack>/data/<your_data_pack_id>/animal_interactions/<your_interaction>.json`
To change animal interaction you need to change animal file: `<your_custom_datapack>/data/minecraft/animal_interactions/<entity_id>.json`

The interaction definition you can check at: [Wiki] (https://github.com/BONNePlayground/AnimalPen/wiki/Animal-Interaction)

***

## 📜 Commands:

Use these commands (server moderators) to manage the mod:

💾 `/animal_pen reset` – Resets the config file to default values.  
🔄 `/animal_pen reload` – Reloads the config file without restarting the game. 

***

## 🛠️ Requirements:

📌 **[Architectury API](https://modrinth.com/mod/architectury-api)** (Required).  
📖 **[Patchouli](https://modrinth.com/mod/patchouli)** (Optional, adds an in-game guidebook).  
📖 **[ClothConfigAPI](https://modrinth.com/mod/cloth-config)** (Optional, adds config editing GUI).

***

## 🎨 Mod Artwork:

Credit to **Breadcrumb5550** for the mod’s blocks and items!

***

With **Animal Pens**, you can **reduce lag, keep your farms organized, and maintain full interactivity!**