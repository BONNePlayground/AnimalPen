//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.integration.jei;


import org.jetbrains.annotations.NotNull;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.integration.jei.category.AnimalCageRecipeCategory;
import lv.id.bonne.animalpen.integration.jei.category.BirdCageRecipeCategory;
import lv.id.bonne.animalpen.integration.jei.category.WaterContainerRecipeCategory;
import lv.id.bonne.animalpen.integration.jei.recipe.ItemInfoRecipe;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;


@JeiPlugin
public class JEIPlugin implements IModPlugin
{
    @Override
    @NotNull
    public Identifier getPluginUid()
    {
        return AnimalPen.resourceOf("jei");
    }


    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(
            new AnimalCageRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new BirdCageRecipeCategory(registration.getJeiHelpers().getGuiHelper()),
            new WaterContainerRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }


    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        BuiltInRegistries.ENTITY_TYPE.stream().forEach(entityType ->
        {
            SpawnEggItem spawnEggItem = SpawnEggItem.byId(entityType);
            if (spawnEggItem == null)
            {
                return;
            }

            if (entityType.is(AnimalPenTags.ANIMAL_CAGE_PICKABLE))
            {
                registration.addRecipes(AnimalCageRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
                    new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
                    Component.translatable("jei.animal_pen.animal_cage"),
                    spawnEggItem.getDefaultInstance()
                )));
            }

            if (entityType.is(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE))
            {
                registration.addRecipes(WaterContainerRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
                    new ItemStack(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()),
                    Component.translatable("jei.animal_pen.water_animal_container"),
                    spawnEggItem.getDefaultInstance()
                )));
            }

            if (entityType.is(AnimalPenTags.BIRD_CATCHER_PICKABLE))
            {
                registration.addRecipes(BirdCageRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
                    new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()),
                    Component.translatable("jei.animal_pen.bird_catcher"),
                    spawnEggItem.getDefaultInstance()
                )));
            }
        });


        // Add description text that shows under the item in JEI
        registration.addIngredientInfo(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animal_pen.animal_cage.info")
        );

        registration.addIngredientInfo(
            new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animal_pen.bird_catcher.info")
        );

        registration.addIngredientInfo(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()),
            VanillaTypes.ITEM_STACK,
            Component.translatable("jei.animal_pen.water_animal_container.info")
        );
    }


    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
            AnimalCageRecipeCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()),
            BirdCageRecipeCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()),
            WaterContainerRecipeCategory.RECIPE_TYPE
        );
    }
}