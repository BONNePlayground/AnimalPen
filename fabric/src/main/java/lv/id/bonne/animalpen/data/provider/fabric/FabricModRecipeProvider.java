package lv.id.bonne.animalpen.data.provider.fabric;


import org.jetbrains.annotations.NotNull;
import java.util.concurrent.CompletableFuture;

import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.triggers.Criterion;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;


public class FabricModRecipeProvider extends FabricRecipeProvider
{
    public FabricModRecipeProvider(FabricPackOutput output,
        CompletableFuture<HolderLookup.Provider> registriesFuture)
    {
        super(output, registriesFuture);
    }


    @Override
    protected RecipeProvider createRecipeProvider(HolderLookup.Provider provider,
        BootstrapContext<Recipe<?>> recipeOutput,
        BootstrapContext<Advancement> advancements)
    {
        return new CustomRecipeProvider(provider, recipeOutput, advancements);
    }



    @Override
    @NotNull
    public String getName()
    {
        return "Animal Pen Recipe Generator";
    }


    private static class CustomRecipeProvider extends RecipeProvider implements ModRecipeProvider
    {
        public CustomRecipeProvider(HolderLookup.Provider provider, BootstrapContext<Recipe<?>> recipes, BootstrapContext<Advancement> advancements)
        {
            super(recipes, advancements);
            this.provider = provider;
        }


        @Override
        public void buildRecipes()
        {
            this.buildModRecipes(this.provider, this.output);
        }


        @Override
        public Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item)
        {
            return has(item);
        }

        private final HolderLookup.Provider provider;
    }
}