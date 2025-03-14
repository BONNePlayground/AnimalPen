package lv.id.bonne.animalpen.registries;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.architectury.registry.registries.Registries;
import lv.id.bonne.animalpen.AnimalPen;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;


/**
 * The type Animal pen food registry.
 */
public class AnimalPenFoodRegistry
{
    /**
     * Clear the registry
     */
    public static void clear()
    {
        DATA.clear();
    }


    /**
     * Register the given animal resource id and its food data.
     *
     * @param id the animal resource id
     * @param data the food data
     */
    public static void register(ResourceLocation id, AnimalFoodData data)
    {
        DATA.put(id, data);
    }


    /**
     * Get animal food data.
     *
     * @param id the animal resource id
     * @return the animal food data
     */
    public static AnimalFoodData get(ResourceLocation id)
    {
        return DATA.get(id);
    }


    /**
     * Gets all animal food data map.
     *
     * @return the all registry data.
     */
    public static Map<ResourceLocation, AnimalFoodData> getAll()
    {
        return Collections.unmodifiableMap(DATA);
    }


    /**
     * This method returns if given item stack is a food item for given animal.
     *
     * @param entity The entity resource location.
     * @param stack The item stack that need to be checked.
     * @return {@code true} if given stack is food item for given entity, {@code false} otherwise.
     */
    public static boolean isFood(ResourceLocation entity, ItemStack stack)
    {
        return DATA.getOrDefault(entity, EMPTY).matches(stack);
    }


    /**
     * This method returns the food items for given animal.
     *
     * @param entity The entity resource location.
     * @return List of ItemStacks that are food for given entity.
     */
    public static List<ItemStack> getFood(ResourceLocation entity)
    {
        if (!DATA.containsKey(entity))
        {
            // Returns empty list as food is not defined.
            return Collections.emptyList();
        }

        if (DATA.get(entity).ingredients().isEmpty())
        {
            // Empty ingredients.
            return Collections.emptyList();
        }

        // Search through all items and matches them as food items.
        return Registries.get(AnimalPen.MOD_ID).
            get(Registry.ITEM_REGISTRY).entrySet().stream().
            map(Map.Entry::getValue).
            map(Item::getDefaultInstance).
            filter(stack -> !stack.isEmpty()).
            filter(stack -> DATA.get(entity).matches(stack)).
            toList();
    }


    /**
     * The type Animal food data.
     */
    public record AnimalFoodData(List<Ingredient> ingredients)
    {
        /**
         * Returns true if the provided stack matches any of the defined ingredients.
         *
         * @param stack the stack
         * @return the boolean
         */
        public boolean matches(ItemStack stack)
        {
            return this.ingredients.stream().anyMatch(ing -> ing.test(stack));
        }
    }

    /**
     * The registry of animal foods.
     */
    private static final Map<ResourceLocation, AnimalFoodData> DATA = new HashMap<>();

    /**
     * The empty data object to not initialize it all time.
     */
    private static final AnimalFoodData EMPTY = new AnimalFoodData(Collections.emptyList());
}