package lv.id.bonne.animalpen.data.provider.neoforge;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;


public class NeoForgeModRecipeProvider extends RecipeProvider implements ModRecipeProvider
{
    public NeoForgeModRecipeProvider(HolderLookup.Provider provider, RecipeOutput output)
    {
        super(provider, output);
        this.provider = provider;
    }


    @Override
    protected void buildRecipes()
    {
        this.buildModRecipes(this.provider, this.output);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item)
    {
        return has(item);
    }


    // The runner to add to the data generator
    public static class Runner extends RecipeProvider.Runner
    {
        // Get the parameters from GatherDataEvent.
        public Runner(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider)
        {
            super(output, lookupProvider);
        }


        @Override
        @NotNull
        protected RecipeProvider createRecipeProvider(HolderLookup.@NotNull Provider provider,
            @NotNull RecipeOutput output)
        {
            return new NeoForgeModRecipeProvider(provider, output);
        }


        @Override
        @NotNull
        public String getName()
        {
            return "Animal Pen Recipe Generator";
        }
    }


    private final HolderLookup.Provider provider;
}