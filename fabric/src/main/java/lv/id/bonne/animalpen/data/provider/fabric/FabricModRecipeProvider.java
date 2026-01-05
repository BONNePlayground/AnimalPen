package lv.id.bonne.animalpen.data.provider.fabric;


import java.util.function.Consumer;

import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.level.ItemLike;


public class FabricModRecipeProvider extends FabricRecipeProvider implements ModRecipeProvider
{
    public FabricModRecipeProvider(FabricDataOutput output)
    {
        super(output);
    }


    @Override
    public void buildRecipes(Consumer<FinishedRecipe> consumer)
    {
        this.buildModRecipes(consumer);
    }


    @Override
    public CriterionTriggerInstance hasItem(ItemLike item)
    {
        return has(item);
    }
}