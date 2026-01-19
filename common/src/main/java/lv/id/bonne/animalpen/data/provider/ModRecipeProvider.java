//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.properties.WoodType;


public interface ModRecipeProvider
{
    default void buildModRecipes(HolderLookup.Provider provider, RecipeOutput consumer)
    {
        HolderGetter<Item> holder = provider.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPensItemRegistry.ANIMAL_CAGE.get()).
            define('B', ItemTags.BARS).
            define('G', Items.GLASS).
            pattern("BBB").
            pattern("BGB").
            pattern("BBB").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPensItemRegistry.ANIMAL_CONTAINER.get()).
            define('B', Items.GLASS_PANE).
            define('I', Items.IRON_INGOT).
            pattern("BIB").
            pattern("B B").
            pattern("BBB").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        animalPen(holder, WoodType.ACACIA, Items.ACACIA_FENCE, consumer);
        animalPen(holder, WoodType.BIRCH, Items.BIRCH_FENCE, consumer);
        animalPen(holder, WoodType.CRIMSON, Items.CRIMSON_FENCE, consumer);
        animalPen(holder, WoodType.DARK_OAK, Items.DARK_OAK_FENCE, consumer);
        animalPen(holder, WoodType.JUNGLE, Items.JUNGLE_FENCE, consumer);
        animalPen(holder, WoodType.OAK, Items.OAK_FENCE, consumer);
        animalPen(holder, WoodType.SPRUCE, Items.SPRUCE_FENCE, consumer);
        animalPen(holder, WoodType.WARPED, Items.WARPED_FENCE, consumer);
        animalPen(holder, WoodType.MANGROVE, Items.MANGROVE_FENCE, consumer);
        animalPen(holder, WoodType.BAMBOO, Items.BAMBOO_FENCE, consumer);
        animalPen(holder, WoodType.CHERRY, Items.CHERRY_FENCE, consumer);
        animalPen(holder, WoodType.PALE_OAK, Items.PALE_OAK_FENCE, consumer);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPenBlockRegistry.AQUARIUM.get()).
            define('G', Items.GLASS).
            define('F', Items.WATER_BUCKET).
            define('S', Items.STONE).
            pattern("G G").
            pattern("GFG").
            pattern(" S ").
            unlockedBy("has_water_animal_container",
                this.hasItem(AnimalPensItemRegistry.ANIMAL_CONTAINER.get())).
            save(consumer);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPensItemRegistry.BIRD_CATCHER.get()).
            define('B', Items.STRING).
            define('S', Items.STICK).
            pattern(" BB").
            pattern(" SB").
            pattern("S  ").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPenBlockRegistry.AVIARY.get()).
            define('G', Items.IRON_CHAIN).
            define('S', Items.SMOOTH_STONE_SLAB).
            pattern("GGG").
            pattern("G G").
            pattern("SSS").
            unlockedBy("has_bird_catcher",
                this.hasItem(AnimalPensItemRegistry.BIRD_CATCHER.get())).
            save(consumer);

        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPenBlockRegistry.GOLD_AVIARY.get()).
            define('G', Items.GOLD_INGOT).
            define('N', Items.GOLD_NUGGET).
            define('S', Items.SMOOTH_STONE_SLAB).
            pattern("NGN").
            pattern("G G").
            pattern("SSS").
            unlockedBy("has_bird_catcher",
                this.hasItem(AnimalPensItemRegistry.BIRD_CATCHER.get())).
            save(consumer);

        // Generate copper aviaries. Currently just base one
        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC,
                AnimalPenBlockRegistry.COPPER_AVIARIES.get(WeatheringCopper.WeatherState.UNAFFECTED).get()).
            group("animal_pens").
            define('F', Items.COPPER_INGOT).
            define('S', Items.SMOOTH_STONE_SLAB).
            pattern("FFF").
            pattern("F F").
            pattern("SSS").
            unlockedBy("has_bird_catcher",
                this.hasItem(AnimalPensItemRegistry.BIRD_CATCHER.get())).
            save(consumer);
    }


    private void animalPen(HolderGetter<Item> holder, WoodType woodType, Item fence, RecipeOutput consumer)
    {
        ShapedRecipeBuilder.shaped(holder, RecipeCategory.MISC, AnimalPenBlockRegistry.ANIMAL_PENS.get(woodType).get()).
            group("animal_pens").
            define('F', fence).
            define('S', Items.SMOOTH_STONE_SLAB).
            pattern("   ").
            pattern("F F").
            pattern("SSS").
            unlockedBy("has_animal_cage",
                this.hasItem(AnimalPensItemRegistry.ANIMAL_CAGE.get())).
            save(consumer);
    }


    Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item);
}
