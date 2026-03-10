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
import net.minecraft.network.chat.TranslatableComponent;
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
        return new TranslatableComponent("jei.animal_pen.item_info_bird_catcher");
    }


    @Override
    @NotNull
    public RecipeType<ItemInfoRecipe> getRecipeType()
    {
        return RECIPE_TYPE;
    }


    @Override
    public ResourceLocation getUid()
    {
        return this.getRecipeType().getUid();
    }


    @Override
    public Class<? extends ItemInfoRecipe> getRecipeClass()
    {
        return getRecipeType().getRecipeClass();
    }


    public static final RecipeType<ItemInfoRecipe> RECIPE_TYPE =
        RecipeType.create(AnimalPen.MOD_ID, "item_info_bird_cage", ItemInfoRecipe.class);
}
