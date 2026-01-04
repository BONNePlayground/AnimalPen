package lv.id.bonne.animalpen.data.provider.forge;


import org.jetbrains.annotations.NotNull;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;


public class ForgeModRecipeProvider extends RecipeProvider implements ModRecipeProvider
{
    public ForgeModRecipeProvider(PackOutput arg)
    {
        super(arg);
    }


    @Override
    protected void buildRecipes(@NotNull Consumer<FinishedRecipe> consumer)
    {
        this.buildModRecipes(consumer);
    }


    @Override
    @NotNull
    public CriterionTriggerInstance hasItem(ItemLike item)
    {
        return has(item);
    }
}