# 🐾 Animal Pens Mod

## ✅ Optimize Your Farms & Reduce Lag!

Tired of lag caused by massive animal farms? **Animal Pens** lets you store animals in specialized **pens** and **aquariums** while keeping full functionality!  
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

✔️ **Releasing animals** by crouch clicking on ground.

(1) Features not added:  
     Any animal transformation  
     Any animal interaction with other entites

***

## 🔧 Customizable Settings:

Easily adjust gameplay with the **mod’s configuration file** _\[config/animal\_pen\_config.json\]_, allowing you to tweak:

⚙️ **Cooldowns for actions** – Example:  
    • Feeding animals  
    • Shearing sheep  
    • Collecting eggs from chickens/turtles  
    • Milking cow

🗡️ **Attack cooldown** - Allows to change how fast players can kill animals in pen.

📦 **Drop Limits** – Prevent item overflow:  
    • 🐑 Wool drop limit  
    • 🥚 Egg drop limit

📏 **Animal Growth System** (Optional):  
    • If enabled, **more animals = larger pen display**.  
    • Adjustable growth multiplier.

🎨 **Stored Variants** – Allows to change how many variants can be stored per pen.

🚫 **Blocked Animals List** – Prevent specific mobs from being captured.

***

## 🔧 Data Driven Settings:

### Added Two New Item Tags:

These tags define which items can attack their respective blocks:

*   **`animal_pen:can_attack_aquarium`** – Items in this tag can attack the **Aquarium** block.
*   **`animal_pen:can_attack_pen`** – Items in this tag can attack the **Animal Pen** block.

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

### Customizable Food Items

You can also add or change food items for each animal. You can do it with data packs.
To add/change animal food you need to create a file: `<mod_that_adds_animal>/animal_foods/<animal_id>.json`

#### 📂 **For Minecraft 1.18.2-1.21.1**
```
{
  // optional value. Defines when the food items should be loaded
  "condition": "mod:<mod_id>",
  "food_items": [
    {
      "item": "<mod>:<item>"
    },
    {
      "tag": "<mod>:<tag>"
    }
  ]
}
```
#### 📂 **For Minecraft 1.21.2 and forward**

```
{
  // optional value. Defines when the food items should be loaded
  "condition": "mod:<mod_id>",
  "food_items": [
    "<mod>:<item>", // for items
    "#<mod>:<tag>"  // for tags (# at front)
  ]
}
```

***

## 📜 Commands:

Use these commands (server moderators) to manage the mod:

💾 `/animal_pen reset` – Resets the config file to default values. 
🔄 `/animal_pen reload` – Reloads the config file without restarting the game. 

***

## 🛠️ Requirements:

📌 **[Architectury API](https://modrinth.com/mod/architectury-api)** (Required).  
📖 **[Patchouli](https://modrinth.com/mod/patchouli)** (Optional, adds an in-game guidebook).

***

## 🎨 Mod Artwork:

Credit to **Breadcrumb5550** for the mod’s blocks and items!

***

With **Animal Pens**, you can **reduce lag, keep your farms organized, and maintain full interactivity!**