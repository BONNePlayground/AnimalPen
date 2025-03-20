# Animal Pens 1.4.0

- Implements food items registry. This registry allows to customize food items for animals using datapacks:
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
- Adds Alex's Mobs food items to be included by default 
- Implements ability for `fake` players to use `attack_items` as interaction.
- Adds sniffers ability to get eggs. (using bucket)
- Replace bucket with bowl for sniffers to get seeds.