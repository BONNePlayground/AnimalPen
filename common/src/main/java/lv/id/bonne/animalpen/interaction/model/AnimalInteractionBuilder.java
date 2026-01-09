package lv.id.bonne.animalpen.interaction.model;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.condition.ConditionEntry;
import lv.id.bonne.animalpen.interaction.cooldown.CooldownEntry;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.ConsumerEntry;
import lv.id.bonne.animalpen.interaction.ingredient.CustomIngredient;
import lv.id.bonne.animalpen.interaction.loot.LootEntry;
import lv.id.bonne.animalpen.interaction.textentry.TextEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;


/**
 * This builder allows easier AnimalInteraction construction.
 */
public class AnimalInteractionBuilder
{
    private AnimalInteractionBuilder()
    {
    }


    /**
     * Ingredient animal interaction builder.
     *
     * @param ingredient the ingredient
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder ingredient(CustomIngredient ingredient)
    {
        this.ingredient = ingredient;
        return this;
    }


    /**
     * Consume animal interaction builder.
     *
     * @param consume the consume
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder consume(ConsumerEntry consume)
    {
        this.consume = consume;
        return this;
    }


    /**
     * Conditions animal interaction builder.
     *
     * @param conditions the conditions
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder conditions(List<ConditionEntry> conditions)
    {
        this.conditions.addAll(conditions);
        return this;
    }


    /**
     * Conditions animal interaction builder.
     *
     * @param conditions the conditions
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder conditions(ConditionEntry conditions)
    {
        this.conditions.add(conditions);
        return this;
    }


    /**
     * Loot entry for animal interaction builder.
     *
     * @param lootEntry the loot entry
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder lootEntry(LootEntry lootEntry)
    {
        this.lootEntry = lootEntry;
        return this;
    }


    /**
     * Even animal interaction builder.
     *
     * @param even the even
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder even(boolean even)
    {
        this.even = even;
        return this;
    }


    /**
     * Cooldown animal interaction builder.
     *
     * @param cooldown the cooldown
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder cooldown(CooldownEntry cooldown)
    {
        this.cooldown = cooldown;
        return this;
    }


    /**
     * Text lines animal interaction builder.
     *
     * @param textLines the text lines
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder textLines(TextEntry textLines)
    {
        this.textLines.add(textLines);
        return this;
    }


    /**
     * Start functions animal interaction builder.
     *
     * @param startFunctions the start functions
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder runFunctions(FunctionKey... startFunctions)
    {
        this.runFunctions.addAll(Arrays.asList(startFunctions));
        return this;
    }


    /**
     * End functions animal interaction builder.
     *
     * @param endFunctions the end functions
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder finishFunctions(FunctionKey... endFunctions)
    {
        this.finishFunctions.addAll(Arrays.asList(endFunctions));
        return this;
    }


    /**
     * Sound animal interaction builder.
     *
     * @param sound the sound
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder sound(Identifier sound)
    {
        this.sound = sound;
        return this;
    }


    /**
     * Redstone signal animal interaction builder.
     *
     * @param redstoneBit the redstone signal
     * @return the animal interaction builder
     */
    public AnimalInteractionBuilder redstoneBit(int redstoneBit)
    {
        if (redstoneBit > 4 || redstoneBit < 0)
        {
            AnimalPen.LOGGER.error("REDSTONE BIT value is out of bounds.");
        }

        this.redstoneBit = Mth.clamp(redstoneBit, 0, 4);
        return this;
    }


    /**
     * Builds the AnimalInteraction, returning null if required mod is not loaded.
     *
     * @return the animal interaction
     */
    public AnimalInteraction build()
    {
        return new AnimalInteraction(
            id, ingredient,
            conditions, even, consume, lootEntry,
            cooldown, textLines,
            runFunctions, finishFunctions,
            sound, redstoneBit
        );
    }


    /**
     * Create animal interaction builder.
     *
     * @param id the id
     * @return the animal interaction builder
     */
    public static AnimalInteractionBuilder create(String id)
    {
        AnimalInteractionBuilder builder = new AnimalInteractionBuilder();
        builder.id = id;
        return builder;
    }


    /**
     * The list of conditions that are required for interaction to operate
     */
    private final List<ConditionEntry> conditions = new ArrayList<>();

    /**
     * The list if text lines displayed for current interaction
     */
    private final List<TextEntry> textLines = new ArrayList<>();

    /**
     * The run functions for current interaction that are triggered when interaction happens
     */
    private final List<FunctionKey> runFunctions = new ArrayList<>();

    /**
     * The finish functions for current interaction that are triggered when cooldown runs out
     */
    private final List<FunctionKey> finishFunctions = new ArrayList<>();

    /**
     * The ID of interaction
     */
    private String id;

    /**
     * The ingredient for main item used.
     */
    private CustomIngredient ingredient = CustomIngredient.EMPTY;

    /**
     * The indication if item should be consumed
     */
    private ConsumerEntry consume = new ConsumerEntry.Interact();

    /**
     * The loot table id for dropping loot
     */
    private LootEntry lootEntry = null;

    /**
     * The indication if interaction should affect only even number of entities
     */
    private boolean even = false;

    /**
     * The cooldown object that defines how cooldown will be applied to interaction
     */
    private CooldownEntry cooldown = null;

    /**
     * The sound that is played when interaction happens
     */
    private Identifier sound = null;

    /**
     * The redstone signal bit value for current interaction 0-4
     */
    private int redstoneBit = 0;
}