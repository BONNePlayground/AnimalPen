//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import java.util.function.Consumer;

import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.properties.WoodType;


public interface ModRecipeProvider
{
    default void buildModRecipes(Consumer<FinishedRecipe> consumer)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPensItemRegistry.ANIMAL_CAGE.get()).
            define('B', Items.IRON_BARS).
            define('G', Items.GLASS).
            pattern("BBB").
            pattern("BGB").
            pattern("BBB").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPensItemRegistry.ANIMAL_CONTAINER.get()).
            define('B', Items.GLASS_PANE).
            define('I', Items.IRON_INGOT).
            pattern("BIB").
            pattern("B B").
            pattern("BBB").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        animalPen(WoodType.ACACIA, Items.ACACIA_FENCE, consumer);
        animalPen(WoodType.BIRCH, Items.BIRCH_FENCE, consumer);
        animalPen(WoodType.CRIMSON, Items.CRIMSON_FENCE, consumer);
        animalPen(WoodType.DARK_OAK, Items.DARK_OAK_FENCE, consumer);
        animalPen(WoodType.JUNGLE, Items.JUNGLE_FENCE, consumer);
        animalPen(WoodType.OAK, Items.OAK_FENCE, consumer);
        animalPen(WoodType.SPRUCE, Items.SPRUCE_FENCE, consumer);
        animalPen(WoodType.WARPED, Items.WARPED_FENCE, consumer);
        animalPen(WoodType.MANGROVE, Items.MANGROVE_FENCE, consumer);
        animalPen(WoodType.BAMBOO, Items.BAMBOO_FENCE, consumer);
        animalPen(WoodType.CHERRY, Items.CHERRY_FENCE_GATE, consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPenBlockRegistry.AQUARIUM.get()).
            define('G', Items.GLASS).
            define('F', Items.WATER_BUCKET).
            define('S', Items.STONE).
            pattern("G G").
            pattern("GFG").
            pattern(" S ").
            unlockedBy("has_water_animal_container",
                this.hasItem(AnimalPensItemRegistry.ANIMAL_CONTAINER.get())).
            save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPensItemRegistry.BIRD_CATCHER.get()).
            define('B', Items.STRING).
            define('S', Items.STICK).
            pattern(" BB").
            pattern(" SB").
            pattern("S  ").
            unlockedBy("has_crafting_table",
                this.hasItem(Items.CRAFTING_TABLE)).
            save(consumer);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPenBlockRegistry.AVIARY.get()).
            define('G', Items.CHAIN).
            define('S', Items.SMOOTH_STONE_SLAB).
            pattern("GGG").
            pattern("G G").
            pattern("SSS").
            unlockedBy("has_bird_catcher",
                this.hasItem(AnimalPensItemRegistry.BIRD_CATCHER.get())).
            save(consumer);
    }


    private void animalPen(WoodType woodType, Item fence, Consumer<FinishedRecipe> consumer)
    {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AnimalPenBlockRegistry.ANIMAL_PENS.get(woodType).get()).
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


    CriterionTriggerInstance hasItem(ItemLike item);
}
