package lv.id.bonne.animalpen.data.provider.fabric;


import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.level.ItemLike;


public class FabricModRecipeProvider extends FabricRecipeProvider implements ModRecipeProvider
{
    public FabricModRecipeProvider(FabricDataOutput output)
    {
        super(output);
    }


    @Override
    public void buildRecipes(RecipeOutput recipeOutput)
    {
        this.buildModRecipes(recipeOutput);
    }


    @Override
    public Criterion<InventoryChangeTrigger.TriggerInstance> hasItem(ItemLike item)
    {
        return has(item);
    }
}