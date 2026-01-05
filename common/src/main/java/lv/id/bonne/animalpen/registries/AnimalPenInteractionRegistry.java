package lv.id.bonne.animalpen.registries;


import java.util.*;

import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;


/**
 * The type Animal Pen interaction registry.
 */
public class AnimalPenInteractionRegistry
{
    /**
     * Clears the data set.
     */
    public static void clear()
    {
        DATA.clear();
    }


    /**
     * Register the given animal resource id and its interaction data.
     *
     * @param id the animal resource id
     * @param data the interaction data
     */
    public static void register(ResourceKey<EntityType<?>> id, AnimalInteraction data)
    {
        DATA.computeIfAbsent(id, loc -> new ArrayList<>(3)).add(data);
    }


    /**
     * Register the given animal resource id and its interaction data.
     *
     * @param id the animal resource id
     * @param data the interaction data
     */
    public static void register(ResourceKey<EntityType<?>> id, List<AnimalInteraction> data)
    {
        DATA.put(id, data);
    }


    /**
     * Get animal interaction data.
     *
     * @param id the animal resource id
     * @return the animal interaction data
     */
    public static List<AnimalInteraction> get(ResourceKey<EntityType<?>> id)
    {
        return DATA.get(id);
    }


    /**
     * Gets all animal interaction data map.
     *
     * @return the all registry data.
     */
    public static Map<ResourceKey<EntityType<?>>, List<AnimalInteraction>> getAll()
    {
        return Collections.unmodifiableMap(DATA);
    }


    /**
     * This method returns first interaction that matches given mob with given item in hand.
     *
     * @param mob The mob that is interacted
     * @param mobNBT The item stack Compound Tag
     * @param itemInHand The interaction item
     * @return Optional of AnimalInteraction or Optional empty.
     */
    public static Optional<AnimalInteraction> matchInteraction(Mob mob, CompoundTag mobNBT, ItemStack itemInHand)
    {
        ResourceKey<EntityType<?>> key =
            BuiltInRegistries.ENTITY_TYPE.getResourceKey(mob.getType()).orElseThrow();
        
        if (!DATA.containsKey(key))
        {
            return Optional.empty();
        }

        CompoundTag cooldowns = mobNBT.
            getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
            getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);

        List<AnimalInteraction> interactions = DATA.get(key);

        return interactions.stream().
            filter(interaction -> !cooldowns.contains(interaction.id())).
            filter(interaction -> interaction.ingredient().test(itemInHand)).
            filter(interaction -> interaction.matchAllConditions(mobNBT)).
            findFirst();
    }


    /**
     * This method returns all interactions that can be done to given mob.
     *
     * @param mob The mob that which interactions are requested
     * @return Unmodifiable collection of given mob interactions.
     */
    public static Collection<AnimalInteraction> getInteractions(Mob mob)
    {
        ResourceKey<EntityType<?>> key =
            BuiltInRegistries.ENTITY_TYPE.getResourceKey(mob.getType()).orElseThrow();

        if (!DATA.containsKey(key))
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(DATA.get(key));
    }


    /**
     * Stores amount of entities that player should receive from server.
     */
    public static void setEntityCount(int entityCount)
    {
        AnimalPenInteractionRegistry.entityCount = entityCount;
    }


    /**
     * This method returns if registry contains all data about entities.
     */
    public static boolean containsAllEntities()
    {
        return DATA.size() == AnimalPenInteractionRegistry.entityCount;
    }

    /**
     * The amount of animals that should be received from server.
     */
    private static int entityCount;

    /**
     * The registry of animal interactions.
     */
    private static final Map<ResourceKey<EntityType<?>>, List<AnimalInteraction>> DATA = new HashMap<>();
}