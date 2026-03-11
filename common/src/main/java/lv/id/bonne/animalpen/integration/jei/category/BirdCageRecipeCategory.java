//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.integration.jei.category;


import org.jetbrains.annotations.NotNull;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.integration.jei.recipe.ItemInfoRecipe;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


public class BirdCageRecipeCategory extends AbstractRecipeCategory
{
    public BirdCageRecipeCategory(IGuiHelper guiHelper)
    {
        super(guiHelper, new ItemStack(AnimalPensItemRegistry.BIRD_CATCHER.get()));
    }


    @Override
    @NotNull
    public Component getTitle()
    {
        return Component.translatable("jei.animal_pen.item_info_bird_catcher");
    }


    @Override
    @NotNull
    public RecipeType<ItemInfoRecipe> getRecipeType()
    {
        return RECIPE_TYPE;
    }


    public static final RecipeType<ItemInfoRecipe> RECIPE_TYPE =
        RecipeType.create(AnimalPen.MOD_ID, "item_info_bird_cage", ItemInfoRecipe.class);
}
