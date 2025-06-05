package lv.id.bonne.animalpen.registries;


import org.jetbrains.annotations.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.network.RegistryFriendlyByteBuf;
import lv.id.bonne.animalpen.listeners.TaggableIngredient;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;


/**
 * The type Animal pen food registry.
 */
public class AnimalPenFoodRegistry
{
    public static void setSyncedData(Map<ResourceLocation, AnimalFoodData> newData)
    {
        DATA.clear();
        DATA.putAll(newData);
    }

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
        return DATA.containsKey(entity) && DATA.get(entity).matches(stack);
    }


    /**
     * This method returns the food items for given animal.
     *
     * @param entity The entity resource location.
     * @return Array of ItemStacks that are food for given entity.
     */
    @Nullable
    public static ItemStack[] getFood(ResourceLocation entity)
    {
        if (!DATA.containsKey(entity))
        {
            // Returns empty list as food is not defined.
            return null;
        }

        return DATA.get(entity).ingredient().getItemStacks();
    }


    /**
     * The type Animal food data.
     */
    public record AnimalFoodData(TaggableIngredient ingredient)
    {
        /**
         * Returns true if the provided stack matches any of the defined ingredients.
         *
         * @param stack the stack
         * @return the boolean
         */
        public boolean matches(ItemStack stack)
        {
            return this.ingredient.test(stack);
        }


        /**
         * Codec for animal food data.
         */
        public static final StreamCodec<RegistryFriendlyByteBuf, AnimalFoodData> STREAM_CODEC =
            TaggableIngredient.CONTENTS_STREAM_CODEC.map(AnimalFoodData::new, AnimalFoodData::ingredient);
    }

    /**
     * The registry of animal foods.
     */
    private static final Map<ResourceLocation, AnimalFoodData> DATA = new HashMap<>();
}