package lv.id.bonne.animalpen.registries;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import net.minecraft.nbt.CompoundTag;
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
    public static void register(EntityType<?> id, AnimalInteraction data)
    {
        DATA.computeIfAbsent(id, loc -> new ArrayList<>(3)).add(data);
    }


    /**
     * Register the given animal resource id and its interaction data.
     *
     * @param id the animal resource id
     * @param data the interaction data
     */
    public static void register(EntityType<?> id, List<AnimalInteraction> data)
    {
        DATA.put(id, data);
    }


    /**
     * Get animal interaction data.
     *
     * @param id the animal resource id
     * @return the animal interaction data
     */
    public static List<AnimalInteraction> get(EntityType<?> id)
    {
        return DATA.get(id);
    }


    /**
     * Gets all animal interaction data map.
     *
     * @return the all registry data.
     */
    public static Map<EntityType<?>, List<AnimalInteraction>> getAll()
    {
        Map<EntityType<?>, List<AnimalInteraction>> snapshot = new HashMap<>();

        for (var entry : DATA.entrySet())
        {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }

        return Collections.unmodifiableMap(snapshot);
    }


    public static int getAmount()
    {
        return DATA.size();
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
        if (!DATA.containsKey(mob.getType()))
        {
            return Optional.empty();
        }

        CompoundTag cooldowns = mobNBT.
            getCompound(AnimalPenCompoundTags.TAG_ANIMAL_DATA).
            getCompound(AnimalPenCompoundTags.TAG_COOLDOWN);

        List<AnimalInteraction> interactions = DATA.get(mob.getType());

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
        if (!DATA.containsKey(mob.getType()))
        {
            return Collections.emptyList();
        }

        return Collections.unmodifiableCollection(DATA.get(mob.getType()));
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
    private static final Map<EntityType<?>, List<AnimalInteraction>> DATA = new ConcurrentHashMap<>();
}