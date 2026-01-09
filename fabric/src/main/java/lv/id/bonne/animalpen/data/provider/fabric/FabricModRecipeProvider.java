package lv.id.bonne.animalpen.data.provider.fabric;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;


public class FabricModRecipeProvider extends FabricRecipeProvider
{
    public FabricModRecipeProvider(FabricDataOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }


    @Override
    @NotNull
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput)
    {
        return new CustomRecipeProvider(provider, recipeOutput);
    }



    @Override
    @NotNull
    public String getName()
    {
        return "Animal Pen Recipe Generator";
    }


    private static class CustomRecipeProvider extends RecipeProvider implements ModRecipeProvider
    {

        protected CustomRecipeProvider(HolderLookup.Provider provider, RecipeOutput recipeOutput)
        {
            super(provider, recipeOutput);
            this.provider = provider;
            this.recipeOutput = recipeOutput;
        }


        @Override
        public void buildRecipes()
        {
            this.buildModRecipes(this.provider, this.recipeOutput);
        }


        @Override
        public Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item)
        {
            return has(item);
        }


        private final RecipeOutput recipeOutput;

        private final HolderLookup.Provider provider;
    }
}