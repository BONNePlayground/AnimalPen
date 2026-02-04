//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.integrations.fabric;


import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;


public class JEIPlugin implements IModPlugin
{
    @Override
    @NotNull
    public ResourceLocation getPluginUid()
    {
        return AnimalPen.resourceOf("jei");
    }


    @Override
    public void registerCategories(IRecipeCategoryRegistration registration)
    {
        registration.addRecipeCategories(
            new ItemInfoRecipeCategory(registration.getJeiHelpers().getGuiHelper())
        );
    }


    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        List<ItemStack> animalList = new ArrayList<>();
        List<ItemStack> waterAnimalList = new ArrayList<>();
        List<ItemStack> flyingAnimalList = new ArrayList<>();

        for (EntityType<?> entityType : Registry.ENTITY_TYPE)
        {
            SpawnEggItem spawnEggItem = SpawnEggItem.byId(entityType);
            if (spawnEggItem == null) continue;

            if (entityType.is(AnimalPenTags.ANIMAL_CAGE_PICKABLE))
            {
                animalList.add(spawnEggItem.getDefaultInstance());
            }

            if (entityType.is(AnimalPenTags.WATER_MOB_CONTAINER_PICKABLE))
            {
                waterAnimalList.add(spawnEggItem.getDefaultInstance());
            }

            if (entityType.is(AnimalPenTags.BIRD_CATCHER_PICKABLE))
            {
                flyingAnimalList.add(spawnEggItem.getDefaultInstance());
            }
        }

        registration.addRecipes(ItemInfoRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
            new TranslatableComponent("jei.animal_pen.animal_cage"),
            animalList
        )));
        registration.addRecipes(ItemInfoRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()),
            new TranslatableComponent("jei.animal_pen.water_animal_container"),
            waterAnimalList
        )));
        registration.addRecipes(ItemInfoRecipeCategory.RECIPE_TYPE, List.of(new ItemInfoRecipe(
            new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()),
            new TranslatableComponent("jei.animal_pen.bird_catcher"),
            flyingAnimalList
        )));
    }


    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CAGE.get()),
            ItemInfoRecipeCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()),
            ItemInfoRecipeCategory.RECIPE_TYPE
        );
        registration.addRecipeCatalyst(
            new ItemStack(AnimalPensItemRegistry.ANIMAL_CONTAINER.get()),
            ItemInfoRecipeCategory.RECIPE_TYPE
        );
    }
}