package lv.id.bonne.animalpen.data.provider.forge;


import lv.id.bonne.animalpen.data.provider.ModRecipeProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.world.level.ItemLike;


public class ForgeModRecipeProvider extends RecipeProvider implements ModRecipeProvider
{
    public ForgeModRecipeProvider(PackOutput arg)
    {
        super(arg);
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