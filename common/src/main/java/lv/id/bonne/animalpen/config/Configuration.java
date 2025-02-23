//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.config;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.*;

import lv.id.bonne.animalpen.config.annotations.JsonComment;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;


public class Configuration
{
    public void init()
    {
        this.cooldownList.clear();

        // Food item
        this.cooldownList.computeIfAbsent(Items.APPLE.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(ANY,
                58 * 20,
                20,
                5 * 60 * 20));

        // Sheep and sharing
        this.cooldownList.computeIfAbsent(Items.SHEARS.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.SHEEP.arch$registryName(),
                59 * 20,
                20,
                5 * 60 * 20));

        // Chicken and bucket
        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.CHICKEN.arch$registryName(),
                60 * 5 * 20 + 20,
                -1 * 20,
                60 * 20));

        // Turtle and bucket
        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.TURTLE.arch$registryName(),
                60 * 5 * 20 + 20,
                -1 * 20,
                60 * 20));

        // Bee and pollen
        this.cooldownList.computeIfAbsent(Items.HONEY_BLOCK.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.BEE.arch$registryName(),
                60 * 20 + 20,
                -1 * 20,
                10 * 20));

        // Init non-used to show options
        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.COW.arch$registryName(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Items.BOWL.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.MOOSHROOM.arch$registryName(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.MOOSHROOM.arch$registryName(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.GOAT.arch$registryName(),
                0,
                0,
                0));

        this.cooldownList.computeIfAbsent(Items.MAGMA_BLOCK.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.FROG.arch$registryName(),
                60 * 5 * 20 + 20,
                -1 * 20,
                10 * 20));

        this.cooldownList.computeIfAbsent(Items.BUCKET.arch$registryName(), i -> new ArrayList<>()).
            add(new CooldownEntry(EntityType.SNIFFER.arch$registryName(),
                60 * 5 * 20 + 20,
                -1 * 20,
                10 * 20));

        // Init block drop limits at once.
        this.dropLimitList.clear();
        this.dropLimitList.put(Items.EGG.arch$registryName(), 16 * 5);
        this.dropLimitList.put(Items.TURTLE_EGG.arch$registryName(), 64 * 5);
        this.dropLimitList.put(Items.WHITE_WOOL.arch$registryName(), 64 * 5);
        this.dropLimitList.put(Items.PEARLESCENT_FROGLIGHT.arch$registryName(), 64 * 5);
        this.dropLimitList.put(Items.TORCHFLOWER_SEEDS.arch$registryName(), 64 * 5);

        this.animalSize = 0.33f;
        this.waterAnimalSize = 0.33f;

        this.growAnimals = false;
        this.growWaterAnimals = false;

        this.dropScuteAtStart = false;
    }


    public boolean isInvalid()
    {
        return this.dropLimitList == null ||
            this.cooldownList == null ||
            this.blockedAnimals == null ||
            this.waterAnimalSize <= 0 ||
            this.animalSize <= 0;
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * This method returns cooldowns for given item using on given entity.
     * @param entity Entity that is targeted.
     * @param usedItem Item that is used.
     * @param entityAmount Amount of entities.
     * @return Cooldown for next action.
     */
    public int getEntityCooldown(EntityType<?> entity, Item usedItem, long entityAmount)
    {
        if (!this.cooldownList.containsKey(usedItem.arch$registryName()))
        {
            return 0;
        }

        Collection<CooldownEntry> cooldownEntries = this.cooldownList.get(usedItem.arch$registryName());

        return cooldownEntries.stream().
            filter(cooldownEntry -> cooldownEntry.entity.equals(entity.arch$registryName())).
            findFirst().
            map(cooldownEntry -> cooldownEntry.getValue(entityAmount)).
            orElseGet(() -> cooldownEntries.stream().
                filter(cooldownEntry -> cooldownEntry.entity.equals(ANY)).
                findFirst().
                map(cooldownEntry -> cooldownEntry.getValue(entityAmount)).
                orElse(0));
    }


    /**
     * This method returns drop limit for given item.
     * @param item Drop limit for item.
     * @return Limit of items that can be dropped at once.
     */
    public int getDropLimits(Item item)
    {
        return this.dropLimitList.getOrDefault(item.arch$registryName(), 0);
    }


    /**
     * The maximal amount of animals a pen can store.
     * @return The maximal amount of animals.
     */
    public long getMaximalAnimalCount()
    {
        return this.maximalAnimalCount;
    }


    /**
     * Is drop scute at start boolean.
     *
     * @return the boolean
     */
    public boolean isDropScuteAtStart()
    {
        return this.dropScuteAtStart;
    }


    /**
     * Is grow animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowAnimals()
    {
        return this.growAnimals;
    }


    /**
     * Gets animal size.
     *
     * @return the animal size
     */
    public float getAnimalSize()
    {
        return this.animalSize;
    }


    /**
     * Is grow water animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowWaterAnimals()
    {
        return this.growWaterAnimals;
    }


    /**
     * Gets water animal size.
     *
     * @return the water animal size
     */
    public float getWaterAnimalSize()
    {
        return this.waterAnimalSize;
    }


    /**
     * This indicates is given entity is blocked from being picked up.
     * @param entityType Entity that need to be checked.
     * @return {@code true} if entity is blocked from being picked up, {@code false} otherwise.
     */
    public boolean isBlocked(EntityType<?> entityType)
    {
        return this.blockedAnimals.contains(entityType.arch$registryName());
    }


// ---------------------------------------------------------------------
// Section: variables
// ---------------------------------------------------------------------


    public static class CooldownEntry
    {
        public CooldownEntry(ResourceLocation entity, int base, int increment, int max)
        {
            this.entity = entity;
            this.baseCooldown = base;
            this.incrementPerAnimal = increment;
            this.cooldownLimit = max;
        }


        public int getValue(long entityAmount)
        {
            if (this.incrementPerAnimal > 0)
            {
                long endValue = this.baseCooldown + entityAmount * this.incrementPerAnimal;

                return (int) Math.min(this.cooldownLimit, endValue);
            }
            else if (this.incrementPerAnimal < 0)
            {
                long endValue = this.baseCooldown + entityAmount * this.incrementPerAnimal;

                return (int) Math.max(this.cooldownLimit, endValue);
            }
            else
            {
                return this.baseCooldown;
            }
        }


        @JsonComment("The entity ID on which cooldown is applied.")
        @Expose
        @SerializedName("entity")
        private ResourceLocation entity;

        @JsonComment("The base cooldown value for action.")
        @JsonComment("0 means that there is no cooldown.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("base_cooldown")
        private int baseCooldown;

        @JsonComment("The increment of cooldown per each animal.")
        @JsonComment("0 means that there is no cooldown increment.")
        @JsonComment("Negative value decreases base cooldown value.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("animal_increment")
        private int incrementPerAnimal;

        @JsonComment("The the maximal cooldown that can be applied.")
        @JsonComment("0 means that there is no cooldown limitation.")
        @JsonComment("Negative animal_increment makes this act as lowest limit.")
        @JsonComment("Values in game ticks.")
        @Expose
        @SerializedName("cooldown_limit")
        private int cooldownLimit;
    }


    @JsonComment("List of cooldowns that are applied when player performs action.")
    @JsonComment("Specifying: `animal_pen:any` will indicate that any entity using that item will have same cooldown.")
    @JsonComment("`minecraft:apple` is universal food item. It is used to indicate for feeding action.")
    @JsonComment("`minecraft:honey_bloc` is used to indicate pollen regeneration cooldown.")
    @JsonComment("<item> : <cooldown>.")
    @Expose
    @SerializedName("cooldowns")
    private Map<ResourceLocation, List<CooldownEntry>> cooldownList = new HashMap<>();

    @JsonComment("List of drop limits for items when player harvests items.")
    @JsonComment("<item> : <drop_limit>.")
    @Expose
    @SerializedName("drop_limits")
    private Map<ResourceLocation, Integer> dropLimitList = new HashMap<>();

    @JsonComment("Allows to set maximal amount of animals in the pen.")
    @JsonComment("Setting 0 will remove any limit.")
    @Expose
    @SerializedName("animal_limit_in_pen")
    private long maximalAnimalCount = Integer.MAX_VALUE;

    @JsonComment("Allows to enable animal growing in animal pen.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName("animals_can_grow")
    private boolean growAnimals = false;

    @JsonComment("Allows to change default animal size in pen.")
    @Expose
    @SerializedName("animal_size")
    private float animalSize = 0.33f;

    @JsonComment("Allows to enable water animal growing in aquarium.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName("water_animals_can_grow")
    private boolean growWaterAnimals = false;

    @JsonComment("Allows to change default water animal size in aquarium.")
    @Expose
    @SerializedName("water_animal_size")
    private float waterAnimalSize = 0.33f;

    @JsonComment("Allows to specify if turtle scute are dropped when player breeds animal (true).")
    @JsonComment("or when food cooldown timer is finished (false).")
    @Expose
    @SerializedName("turtle_scute_drop_time")
    private boolean dropScuteAtStart = false;

    @JsonComment("Set of animals that are blocked from picking up.")
    @Expose
    @SerializedName("blocked_animals")
    private Set<ResourceLocation> blockedAnimals = new HashSet<>();

    @JsonComment("")
    @Expose(serialize = false, deserialize = false)
    private final static ResourceLocation ANY = new ResourceLocation("animal_pen:any");
}
