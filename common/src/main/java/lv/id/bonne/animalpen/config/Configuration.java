//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.config;


import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.HashSet;
import java.util.Set;

import lv.id.bonne.animalpen.config.annotations.JsonComment;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;


/**
 * The type Configuration.
 */
public class Configuration
{
    /**
     * Is invalid boolean.
     *
     * @return the boolean
     */
    public boolean isInvalid()
    {
        return
            this.blockedAnimals == null ||
            this.aquariumMobSize == null ||
            this.aquariumMobSize <= 0 ||
            this.animalPenMobSize == null ||
            this.animalPenMobSize <= 0 ||
            this.growthMultiplier == null ||
            this.growthMultiplier < 0 ||
            this.attackCooldown == null ||
            this.attackCooldown < 0 ||
            this.maxStoredAnimalVariants == null ||
            this.triggerAdvancements == null ||
            this.increaseStatistics == null ||
            this.maxStoredAnimalVariants < 0 ||
            this.debug == null ||
            this.showAllInteractions == null ||
            this.showCooldownsOnCrouch == null ||
            this.growAviaryMob == null ||
            this.aviaryMobSize == null ||
            this.aviaryMobSize <= 0;
    }


    /**
     * Sets defaults.
     */
    public void setDefaults(boolean init)
    {
        if (this.blockedAnimals == null || init)
        {
            this.blockedAnimals = new HashSet<>();
            this.blockedAnimals.add(Identifier.fromNamespaceAndPath("cobblemon", "pokemon"));
        }

        if (this.animalPenMobSize == null || this.animalPenMobSize <= 0 || init)
        {
            this.animalPenMobSize = 0.33f;
        }

        if (this.aquariumMobSize == null || this.aquariumMobSize <= 0 || init)
        {
            this.aquariumMobSize = 0.33f;
        }

        if (this.aviaryMobSize == null || this.aviaryMobSize <= 0 || init)
        {
            this.aviaryMobSize = 0.33f;
        }

        if (this.growAviaryMob == null || init)
        {
            this.growAviaryMob = false;
        }

        if (this.growthMultiplier == null || this.growthMultiplier < 0 || init)
        {
            this.growthMultiplier = 0.001f;
        }

        if (this.attackCooldown == null || this.attackCooldown < 0 || init)
        {
            this.attackCooldown = 5;
        }

        if (this.maxStoredAnimalVariants == null || this.maxStoredAnimalVariants < 0 || init)
        {
            this.maxStoredAnimalVariants = 16;
        }

        if (this.increaseStatistics == null || init)
        {
            this.increaseStatistics = false;
        }

        if (this.triggerAdvancements == null || init)
        {
            this.triggerAdvancements = false;
        }

        if (this.debug == null || init)
        {
            this.debug = false;
        }

        if (this.showAllInteractions == null || init)
        {
            this.showAllInteractions = false;
        }

        if (this.showCooldownsOnCrouch == null || init)
        {
            this.showCooldownsOnCrouch = false;
        }

        if (init)
        {
            this.maximalAnimalCount = Integer.MAX_VALUE;

            this.growAnimalPenMob = false;
            this.growAquariumMob = false;
        }
    }


// ---------------------------------------------------------------------
// Section: Getters
// ---------------------------------------------------------------------


    /**
     * The maximal amount of animals a pen can store.
     *
     * @return The maximal amount of animals.
     */
    public long getMaximalAnimalCount()
    {
        return this.maximalAnimalCount;
    }


    /**
     * Is grow animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowAnimalPenMob()
    {
        return this.growAnimalPenMob;
    }


    /**
     * Gets animal size.
     *
     * @return the animal size
     */
    public float getAnimalPenMobSize()
    {
        return this.animalPenMobSize;
    }


    /**
     * Is grow water animals boolean.
     *
     * @return the boolean
     */
    public boolean isGrowAquariumMob()
    {
        return this.growAquariumMob;
    }


    /**
     * Gets water animal size.
     *
     * @return the water animal size
     */
    public float getAquariumMobSize()
    {
        return this.aquariumMobSize;
    }


    /**
     * Is grow aviary mob boolean.
     *
     * @return the boolean
     */
    public boolean isGrowAviaryMob()
    {
        return this.growAviaryMob;
    }


    /**
     * Gets aviary mob size.
     *
     * @return the aviary mob size
     */
    public float getAviaryMobSize()
    {
        return this.aviaryMobSize;
    }


    /**
     * Gets growth multiplier.
     *
     * @return the growth multiplier
     */
    public Float getGrowthMultiplier()
    {
        return this.growthMultiplier;
    }


    /**
     * This indicates is given entity is blocked from being picked up.
     *
     * @param entityType Entity that need to be checked.
     * @return {@code true} if entity is blocked from being picked up, {@code false} otherwise.
     */
    public boolean isBlocked(EntityType<?> entityType)
    {
        return this.blockedAnimals.contains(entityType.arch$registryName());
    }


    /**
     * Gets attack cooldown.
     *
     * @return the attack cooldown
     */
    public int getAttackCooldown()
    {
        return this.attackCooldown;
    }


    /**
     * Gets max stored variants.
     *
     * @return the max stored variants
     */
    public int getMaxStoredVariants()
    {
        return this.maxStoredAnimalVariants;
    }


    /**
     * Gets trigger advancements.
     *
     * @return the trigger advancements
     */
    public boolean isTriggerAdvancements()
    {
        return this.triggerAdvancements;
    }


    /**
     * Gets increase statistics.
     *
     * @return the increase statistics
     */
    public boolean isIncreaseStatistics()
    {
        return this.increaseStatistics;
    }


    /**
     * Returns if debug is enabled.
     *
     * @return the debug value.
     */
    public boolean isDebug()
    {
        return this.debug;
    }


    /**
     * Is show cooldowns on crouch.
     *
     * @return the boolean
     */
    public boolean isShowCooldownsOnCrouch()
    {
        return this.showCooldownsOnCrouch;
    }


    /**
     * Is show all interactions.
     *
     * @return the boolean
     */
    public boolean isShowAllInteractions()
    {
        return this.showAllInteractions;
    }


    public static Configuration getDefaultConfig()
    {
        Configuration configuration = new Configuration();
        configuration.setDefaults(true);

        return configuration;
    }


// ---------------------------------------------------------------------
// Section: Setters
// ---------------------------------------------------------------------


    /**
     * Sets grow animal pen mob.
     *
     * @param growAnimalPenMob the grow animal pen mob
     */
    public void setGrowAnimalPenMob(boolean growAnimalPenMob)
    {
        this.growAnimalPenMob = growAnimalPenMob;
    }


    /**
     * Sets animal pen mob size.
     *
     * @param animalPenMobSize the animal pen mob size
     */
    public void setAnimalPenMobSize(float animalPenMobSize)
    {
        this.animalPenMobSize = animalPenMobSize;
    }


    /**
     * Sets grow aquarium mob.
     *
     * @param growAquariumMob the grow aquarium mob
     */
    public void setGrowAquariumMob(boolean growAquariumMob)
    {
        this.growAquariumMob = growAquariumMob;
    }


    /**
     * Sets aquarium mob size.
     *
     * @param aquariumMobSize the aquarium mob size
     */
    public void setAquariumMobSize(float aquariumMobSize)
    {
        this.aquariumMobSize = aquariumMobSize;
    }


    /**
     * Sets grow aviary mob.
     *
     * @param growAviaryMob the grow aviary mob
     */
    public void setGrowAviaryMob(boolean growAviaryMob)
    {
        this.growAviaryMob = growAviaryMob;
    }


    /**
     * Sets aviary mob size.
     *
     * @param aviaryMobSize the aviary mob size
     */
    public void setAviaryMobSize(float aviaryMobSize)
    {
        this.aviaryMobSize = aviaryMobSize;
    }


    /**
     * Sets growth multiplier.
     *
     * @param growthMultiplier the growth multiplier
     */
    public void setGrowthMultiplier(float growthMultiplier)
    {
        this.growthMultiplier = growthMultiplier;
    }


    /**
     * Sets show cooldowns on crouch.
     *
     * @param showCooldownsOnCrouch the show cooldowns on crouch
     */
    public void setShowCooldownsOnCrouch(boolean showCooldownsOnCrouch)
    {
        this.showCooldownsOnCrouch = showCooldownsOnCrouch;
    }


    /**
     * Sets show all interactions.
     *
     * @param showAllInteractions the show all interactions
     */
    public void setShowAllInteractions(boolean showAllInteractions)
    {
        this.showAllInteractions = showAllInteractions;
    }


    /**
     * Sets attack cooldown.
     *
     * @param attackCooldown the attack cooldown
     */
    public void setAttackCooldown(Integer attackCooldown)
    {
        this.attackCooldown = attackCooldown;
    }


    /**
     * Sets maximal animal count.
     *
     * @param maximalAnimalCount the maximal animal count
     */
    public void setMaximalAnimalCount(long maximalAnimalCount)
    {
        this.maximalAnimalCount = maximalAnimalCount;
    }


    /**
     * Sets max stored animal variants.
     *
     * @param maxStoredAnimalVariants the max stored animal variants
     */
    public void setMaxStoredAnimalVariants(Integer maxStoredAnimalVariants)
    {
        this.maxStoredAnimalVariants = maxStoredAnimalVariants;
    }


    /**
     * Sets trigger advancements.
     *
     * @param triggerAdvancements the trigger advancements
     */
    public void setTriggerAdvancements(Boolean triggerAdvancements)
    {
        this.triggerAdvancements = triggerAdvancements;
    }


    /**
     * Sets increase statistics.
     *
     * @param increaseStatistics the increase statistics
     */
    public void setIncreaseStatistics(Boolean increaseStatistics)
    {
        this.increaseStatistics = increaseStatistics;
    }


    /**
     * Sets debug.
     *
     * @param debug the debug
     */
    public void setDebug(Boolean debug)
    {
        this.debug = debug;
    }


// ---------------------------------------------------------------------
// Section: variables
// ---------------------------------------------------------------------


    @JsonComment("A cooldown value in game ticks between attacks that players can perform on animal pens.")
    @JsonComment("Default value: 5 game tick")
    @Expose
    @SerializedName("attack_cooldown")
    private Integer attackCooldown;

    @JsonComment("Allows to set maximal amount of animals in the pen.")
    @JsonComment("Setting 0 will remove any limit.")
    @Expose
    @SerializedName("animal_limit_in_pen")
    private long maximalAnimalCount = Integer.MAX_VALUE;

    @JsonComment("Allows to enable mob growing in animal pen.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName(value = "animal_pen_mob_can_grow", alternate = "animals_can_grow")
    private boolean growAnimalPenMob = false;

    @JsonComment("Allows to change default mob size in pen.")
    @Expose
    @SerializedName(value = "animal_pen_mob_size", alternate = "animal_size")
    private Float animalPenMobSize;

    @JsonComment("Allows to enable aquarium mob growing.")
    @JsonComment("The more animals are inside it, the larger it will be.")
    @Expose
    @SerializedName(value = "aquarium_mob_can_grow", alternate = "water_animals_can_grow")
    private boolean growAquariumMob = false;

    @JsonComment("Allows to change default mob size in aquarium.")
    @Expose
    @SerializedName(value = "aquarium_mob_size", alternate = "water_animal_size")
    private Float aquariumMobSize;

    @JsonComment("Allows to enable aviary mobs growing.")
    @JsonComment("The more mobs are inside it, the larger it will be.")
    @Expose
    @SerializedName("aviary_mob_can_grow")
    private Boolean growAviaryMob = false;

    @JsonComment("Allows to change default mob size in aviary.")
    @Expose
    @SerializedName("aviary_mob_size")
    private Float aviaryMobSize;

    @JsonComment("Allows to set how fast animals grows in pen and aquarium.")
    @JsonComment("Each animal is multiplied by given value to get end size.")
    @JsonComment("This option works only if animals_can_grow or water_animals_can_grow is enabled.")
    @JsonComment("Default value = 0.001")
    @Expose
    @SerializedName("growth_multiplier")
    private Float growthMultiplier;

    @JsonComment("Allows to set how many different animal variants can be stored per item.")
    @JsonComment("Players will not be able to store more different variants than this value.")
    @JsonComment("Be aware, this increases NBT data size, so not recommended to put infinite amount.")
    @JsonComment("Default value = 16")
    @Expose
    @SerializedName("max_stored_animal_variants")
    private Integer maxStoredAnimalVariants;

    @JsonComment("Allows to set if interactions with animal pens/aquariums should trigger advancements.")
    @JsonComment("Triggered Criteria: player_interacted_with_entity, bred_animals, filled_bucket, player_killed_entity")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("trigger_advancements")
    private Boolean triggerAdvancements;

    @JsonComment("Allows to set if interactions with animal pens/aquariums should increase statistics.")
    @JsonComment("Increased Statistics: Animals Bred, Mob Kills, Use Item <item>, Kill Entity <entity>")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("increase_statistics")
    private Boolean increaseStatistics;

    @JsonComment("Allows to toggle if cooldowns should be shown only while player is crouching (true).")
    @JsonComment("or be visible all the time (false).")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("show_cooldowns_while_crouching")
    private Boolean showCooldownsOnCrouch;

    @JsonComment("Allows to toggle if all interactions (evenNumber without cooldown) should be rendered")
    @JsonComment("above animal pen or aquarium")
    @JsonComment("Default value = false")
    @Expose
    @SerializedName("show_all_interactions_above")
    private Boolean showAllInteractions;

    @JsonComment("Set of animals that are blocked from picking up.")
    @Expose
    @SerializedName("blocked_animals")
    private Set<Identifier> blockedAnimals = new HashSet<>();

    @JsonComment("Debug code to indicate problems.")
    @Expose
    @SerializedName("debug")
    private Boolean debug;
}
