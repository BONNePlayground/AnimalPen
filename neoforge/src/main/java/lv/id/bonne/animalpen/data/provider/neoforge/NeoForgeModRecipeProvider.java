package lv.id.bonne.animalpen.data.provider.neoforge;


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
    public NeoForgeModRecipeProvider(PackOutput arg, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        super(arg, lookupProvider);
    }


    @Override
    protected void buildRecipes(RecipeOutput consumer)
    {
        this.buildModRecipes(consumer);
    }

    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item)
    {
        return has(item);
    }
}