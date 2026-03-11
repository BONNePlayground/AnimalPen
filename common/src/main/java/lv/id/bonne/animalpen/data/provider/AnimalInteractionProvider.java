package lv.id.bonne.animalpen.data.provider;


import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.data.listener.AnimalInteractionEntry;
import lv.id.bonne.animalpen.interaction.condition.ConditionEntry;
import lv.id.bonne.animalpen.interaction.condition.Operator;
import lv.id.bonne.animalpen.interaction.cooldown.CooldownEntry;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.ConsumerEntry;
import lv.id.bonne.animalpen.interaction.ingredient.CustomIngredient;
import lv.id.bonne.animalpen.interaction.loot.LootEntry;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.interaction.model.AnimalInteractionBuilder;
import lv.id.bonne.animalpen.interaction.textentry.TextEntry;
import lv.id.bonne.animalpen.interaction.textentry.TextEntryVisibility;
import lv.id.bonne.animalpen.interaction.value.BoolValue;
import lv.id.bonne.animalpen.interaction.value.IntValue;
import lv.id.bonne.animalpen.interaction.value.StringValue;
import lv.id.bonne.animalpen.registries.AnimalPenFunctionRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;


/**
 * Animal Interaction Data Provider.
 */
public class AnimalInteractionProvider implements DataProvider
{
    public AnimalInteractionProvider(PackOutput generator, CompletableFuture<HolderLookup.Provider> lookupProvider)
    {
        this.pathProvider = generator.createPathProvider(PackOutput.Target.DATA_PACK, "animal_interactions");
        this.registries = lookupProvider;
    }


    @Override
    public CompletableFuture<?> run(CachedOutput cache)
    {
        return this.registries.thenCompose((provider) ->
        {
            // Default minecraft animals with custom implementations
            List<CompletableFuture<?>> featureList = new ArrayList<>();

            featureList.add(this.generateAxolotl(cache));
            featureList.add(this.generateBee(cache));
            featureList.add(this.generateChicken(cache));
            featureList.add(this.generateCow(cache));
            featureList.add(this.generateGoat(cache));
            featureList.add(this.generateMooshroom(cache));
            featureList.add(this.generateSheep(cache));
            featureList.add(this.generateTurtle(cache));
            featureList.add(this.generateFrog(cache));
            featureList.add(this.generateAllay(cache));
            featureList.add(this.generateSniffer(cache));
            featureList.add(this.generateArmadillo(cache));
            
            // Fishes
            featureList.add(this.generateFish(cache, EntityType.COD, Items.COD_BUCKET));
            featureList.add(this.generateFish(cache, EntityType.PUFFERFISH, Items.PUFFERFISH_BUCKET));
            featureList.add(this.generateFish(cache, EntityType.SALMON, Items.SALMON_BUCKET));
            featureList.add(this.generateFish(cache, EntityType.TROPICAL_FISH, Items.TROPICAL_FISH_BUCKET));
            featureList.add(this.generateFish(cache, EntityType.TADPOLE, Items.TADPOLE_BUCKET));

            // Only food animals
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.CAT,
                CustomIngredient.of(ItemTags.CAT_FOOD),
                SoundEvents.CAT_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.DOLPHIN,
                CustomIngredient.of(ItemTags.NAUTILUS_FOOD),
                false,
                SoundEvents.DOLPHIN_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.FOX,
                CustomIngredient.of(ItemTags.FOX_FOOD),
                SoundEvents.FOX_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.GLOW_SQUID,
                CustomIngredient.of(ItemTags.NAUTILUS_FOOD),
                false,
                SoundEvents.GLOW_SQUID_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.HOGLIN,
                CustomIngredient.of(ItemTags.HOGLIN_FOOD),
                SoundEvents.HOGLIN_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.LLAMA,
                CustomIngredient.of(ItemTags.LLAMA_FOOD),
                SoundEvents.LLAMA_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.OCELOT,
                CustomIngredient.of(ItemTags.OCELOT_FOOD),
                SoundEvents.OCELOT_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.PANDA,
                CustomIngredient.of(ItemTags.PANDA_FOOD),
                SoundEvents.PANDA_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.PIG,
                CustomIngredient.of(ItemTags.PIG_FOOD),
                SoundEvents.PIG_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.RABBIT,
                CustomIngredient.of(ItemTags.RABBIT_FOOD),
                SoundEvents.RABBIT_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.SQUID,
                CustomIngredient.of(ItemTags.NAUTILUS_FOOD),
                false,
                SoundEvents.SQUID_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.STRIDER,
                CustomIngredient.of(ItemTags.STRIDER_FOOD),
                SoundEvents.STRIDER_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.TRADER_LLAMA,
                CustomIngredient.of(ItemTags.LLAMA_FOOD),
                SoundEvents.LLAMA_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.PARROT,
                CustomIngredient.of(ItemTags.PARROT_FOOD),
                SoundEvents.PARROT_AMBIENT));

            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.WOLF,
                CustomIngredient.merge(CustomIngredient.of(ItemTags.WOLF_FOOD)),
                SoundEvents.WOLF_SOUNDS.values().iterator().next().ambientSound().value()));

            CustomIngredient horseFood = CustomIngredient.of(ItemTags.HORSE_FOOD);
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.DONKEY, horseFood,
                SoundEvents.DONKEY_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.HORSE, horseFood,
                SoundEvents.HORSE_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.MULE, horseFood,
                SoundEvents.MULE_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache, EntityType.SKELETON_HORSE, horseFood,
                SoundEvents.SKELETON_HORSE_AMBIENT));
            
            featureList.add(this.generateWithFoodAndAmbient(cache, 
                EntityType.ZOMBIE_HORSE,
                CustomIngredient.of(ItemTags.ZOMBIE_HORSE_FOOD),
                SoundEvents.ZOMBIE_HORSE_AMBIENT));

            featureList.add(this.generateWithFoodAndAmbient(cache,
                EntityType.CAMEL,
                CustomIngredient.of(ItemTags.CAMEL_FOOD),
                SoundEvents.CAMEL_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache,
                EntityType.CAMEL_HUSK,
                CustomIngredient.of(ItemTags.CAMEL_HUSK_FOOD),
                SoundEvents.CAMEL_HUSK_AMBIENT));

            featureList.add(this.generateWithFoodAndAmbient(cache,
                EntityType.NAUTILUS,
                CustomIngredient.of(ItemTags.NAUTILUS_FOOD),
                false,
                SoundEvents.NAUTILUS_AMBIENT));
            featureList.add(this.generateWithFoodAndAmbient(cache,
                EntityType.ZOMBIE_NAUTILUS,
                CustomIngredient.of(ItemTags.NAUTILUS_FOOD),
                false,
                SoundEvents.ZOMBIE_NAUTILUS_AMBIENT));

            // Entities with only ambient
            featureList.add(this.generateAmbient(cache, EntityType.BAT, SoundEvents.BAT_AMBIENT));
            featureList.add(this.generateAmbient(cache, EntityType.POLAR_BEAR, SoundEvents.POLAR_BEAR_AMBIENT));
            featureList.add(this.generateAmbient(cache, EntityType.HAPPY_GHAST, SoundEvents.HAPPY_GHAST_AMBIENT));

            return CompletableFuture.allOf(featureList.toArray(CompletableFuture[]::new));
        });
    }


    @Override
    @NotNull
    public String getName()
    {
        return "Animal Interactions";
    }


// ---------------------------------------------------------------------
// Section: Generators
// ---------------------------------------------------------------------


    public AnimalInteraction generateFood(CustomIngredient food, FunctionKey... finishFunctions)
    {
        return this.generateFood(food, true, finishFunctions);
    }


    public AnimalInteraction generateFood(CustomIngredient food, boolean stackLimit, FunctionKey... finishFunctions)
    {
        return AnimalInteractionBuilder.create("feeding").
            ingredient(food).
            even(true).
            consume(new ConsumerEntry.Consume(stackLimit)).
            conditions(new ConditionEntry.AmountCondition(Operator.GTE, 2)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.FEEDING.get())).
            finishFunctions(finishFunctions).
            cooldown(new CooldownEntry.Linear(1160, 20, 6000)).
            textLines(TextEntry.ready("display.animal_pen.food_ready", food)).
            textLines(TextEntry.cooldown("display.animal_pen.food_cooldown", food)).
            textLines(new TextEntry("",
                "display.animal_pen.requires_food",
                CustomIngredient.EMPTY,
                food,
                TextEntryVisibility.NOT_MATCH,
                "2")).
            redstoneBit(1).
            build();
    }


    public AnimalInteraction generateAmbientSound(SoundEvent soundEvent)
    {
        return AnimalInteractionBuilder.create("ambient").
            sound(BuiltInRegistries.SOUND_EVENT.getKey(soundEvent)).
            cooldown(new CooldownEntry.Randomized(1200, 6000)).
            build();
    }



    public AnimalInteraction generateBucketable(Item bucketItem, Item resultItem, int redstoneSignal)
    {
        return AnimalInteractionBuilder.create("bucketable_pickup").
            ingredient(CustomIngredient.of(bucketItem)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.BUCKETABLE_PICKUP.get())).
            redstoneBit(redstoneSignal).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(resultItem))).
            build();
    }


// ---------------------------------------------------------------------
// Section: Modded generators
// ---------------------------------------------------------------------


    public CompletableFuture<?> generateWithInteractions(CachedOutput cache,
        EntityType<?> entityType,
        List<AnimalInteraction> interactions,
        String... mods)
    {
        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE, new AnimalInteractionEntry(Optional.of(entityType.builtInRegistryHolder().key()),
                Arrays.stream(mods).toList(),
                interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(entityType.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


// ---------------------------------------------------------------------
// Section: Private constructors
// ---------------------------------------------------------------------


    private CompletableFuture<?> generateAmbient(CachedOutput cache,
        EntityType<?> entityType,
        SoundEvent soundEvent)

    {
        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE, AnimalInteractionEntry.of(entityType.builtInRegistryHolder().key(),
                List.of(this.generateAmbientSound(soundEvent)))).
            getOrThrow();

        Path file = this.pathProvider.json(entityType.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateWithFoodAndAmbient(CachedOutput cache,
        EntityType<?> entityType,
        CustomIngredient foodItem,
        SoundEvent soundEvent)
    {
        return this.generateWithFoodAndAmbient(cache, entityType, foodItem, true, soundEvent);
    }


    public CompletableFuture<?> generateWithFoodAndAmbient(CachedOutput cache,
        EntityType<?> entityType,
        CustomIngredient foodItem,
        boolean withStackLimit,
        SoundEvent soundEvent)
    {
        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE, AnimalInteractionEntry.of(entityType.builtInRegistryHolder().key(),
                List.of(this.generateFood(foodItem, withStackLimit), this.generateAmbientSound(soundEvent)))).
            getOrThrow();

        Path file = this.pathProvider.json(entityType.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateAxolotl(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(3);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.AXOLOTL_FOOD),
            false));
        // Water Pickup
        interactions.add(this.generateBucketable(Items.WATER_BUCKET, Items.AXOLOTL_BUCKET, 2));
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.AXOLOTL_IDLE_WATER));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.AXOLOTL.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.AXOLOTL.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateChicken(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.CHICKEN_FOOD)));

        // Egg Dropping
        interactions.add(AnimalInteractionBuilder.create("eggs").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(BuiltInLootTables.CHICKEN_LAY.identifier(), 80, true)).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.CHICKEN_EGG)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.EGG))).
            textLines(TextEntry.cooldown("display.animal_pen.egg_cooldown", CustomIngredient.of(Items.EGG))).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.CHICKEN_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.CHICKEN.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.CHICKEN.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateBee(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.BEE_FOOD)));

        // Shears
        interactions.add(AnimalInteractionBuilder.create("shearing").
            ingredient(CustomIngredient.merge(CustomIngredient.of(Items.SHEARS),
                CustomIngredient.of(AnimalPenTags.COMMON_SHEARS))).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/shear/honeycomb"))).
            consume(new ConsumerEntry.Damage(1)).
            conditions(new ConditionEntry.PropertiesCondition(AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                Operator.GTE,
                new IntValue(5))).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                -5)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.BEEHIVE_SHEAR)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.HONEYCOMB))).
            textLines(new TextEntry("",
                "display.animal_pen.requires_pollen",
                CustomIngredient.EMPTY,
                CustomIngredient.of(Items.HONEYCOMB),
                TextEntryVisibility.NOT_MATCH,
                "5")).
            build());
        // glass bottle
        interactions.add(AnimalInteractionBuilder.create("glass_bottle").
            ingredient(CustomIngredient.of(Items.GLASS_BOTTLE)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/glass_bottle/honey_bottle"))).
            consume(new ConsumerEntry.Replace()).
            conditions(new ConditionEntry.PropertiesCondition(AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                Operator.GTE,
                new IntValue(5))).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                -5)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.BOTTLE_FILL)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.HONEY_BOTTLE))).
            textLines(new TextEntry("",
                "display.animal_pen.requires_pollen",
                CustomIngredient.EMPTY,
                CustomIngredient.of(Items.HONEYCOMB),
                TextEntryVisibility.NOT_MATCH,
                "5")).
            build());
        // Pollen collector
        interactions.add(AnimalInteractionBuilder.create("pollen").
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.BEEHIVE_WORK)).
            conditions(new ConditionEntry.PropertiesCondition(AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                Operator.LT,
                new IntValue(10))).
            finishFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL, 0)).
            cooldown(new CooldownEntry.Linear(1200, -4, 4)).
            textLines(TextEntry.cooldown("display.animal_pen.pollen_cooldown",
                CustomIngredient.of(Items.HONEY_BLOCK),
                CustomIngredient.of(Items.HONEY_BLOCK))).
            textLines(new TextEntry("display.animal_pen.pollen_level",
                "display.animal_pen.pollen_level",
                CustomIngredient.of(Items.HONEY_BLOCK),
                CustomIngredient.of(Items.HONEY_BLOCK),
                TextEntryVisibility.COOLDOWN,
                "[" + AnimalPenCompoundTags.TAG_POLLEN_LEVEL + "]", "10")).
            textLines(new TextEntry("display.animal_pen.pollen_level_max",
                "display.animal_pen.pollen_level_max",
                CustomIngredient.of(Items.HONEY_BLOCK),
                CustomIngredient.of(Items.HONEY_BLOCK),
                TextEntryVisibility.NOT_MATCH,
                "10")).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.BEE_POLLINATE));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.BEE.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.BEE.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateCow(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.COW_FOOD)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket"))).
            consume(new ConsumerEntry.Replace()).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.COW_MILK)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            textLines(TextEntry.cooldown("display.animal_pen.milk_cooldown", CustomIngredient.of(Items.MILK_BUCKET))).
            build());
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.COW_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.COW.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.COW.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateMooshroom(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.COW_FOOD)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket"))).
            consume(new ConsumerEntry.Replace()).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.COW_MILK)).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            build());
        // Soup pickup
        interactions.add(AnimalInteractionBuilder.create("stew").
            ingredient(CustomIngredient.of(Items.BOWL)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bowl/mushroom_stew"))).
            consume(new ConsumerEntry.Replace()).
            conditions(new ConditionEntry.MobCondition("stew_effects", Operator.HAS, new BoolValue(false))).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.MOOSHROOM_MILK)).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MUSHROOM_STEW))).
            build());
        interactions.add(AnimalInteractionBuilder.create("stew").
            ingredient(CustomIngredient.of(Items.BOWL)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bowl/suspicious_stew"))).
            consume(new ConsumerEntry.Replace()).
            conditions(new ConditionEntry.MobCondition("stew_effects", Operator.HAS, new BoolValue(true))).
            conditions(new ConditionEntry.MobCondition("Type", Operator.MATCH, new StringValue("brown"))).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_REMOVE_EFFECT.get())).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.MOOSHROOM_MILK_SUSPICIOUSLY)).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.SUSPICIOUS_STEW))).
            build());
        // Flowers
        interactions.add(AnimalInteractionBuilder.create("flower").
            ingredient(CustomIngredient.of(ItemTags.SMALL_FLOWERS)).
            consume(new ConsumerEntry.Replace()).
            conditions(new ConditionEntry.MobCondition("stew_effects", Operator.HAS, new BoolValue(false))).
            conditions(new ConditionEntry.MobCondition("Type", Operator.MATCH, new StringValue("brown"))).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_SET_EFFECT.get())).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.MOOSHROOM_EAT)).
            textLines(TextEntry.ready("display.animal_pen.apply_ready", CustomIngredient.of(Items.SUSPICIOUS_STEW))).
            build());
        interactions.add(AnimalInteractionBuilder.create("flower").
            ingredient(CustomIngredient.of(ItemTags.SMALL_FLOWERS)).
            conditions(new ConditionEntry.MobCondition("stew_effects", Operator.HAS, new BoolValue(true))).
            conditions(new ConditionEntry.MobCondition("Type", Operator.MATCH, new StringValue("brown"))).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_FAILED_EFFECT.get())).
            build());
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.COW_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.MOOSHROOM.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.MOOSHROOM.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateGoat(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.GOAT_FOOD)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket"))).
            consume(new ConsumerEntry.Replace()).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.GOAT_MILK)).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            build());
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.GOAT_AMBIENT));
        interactions.add(AnimalInteractionBuilder.create("ambient_screaming").
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.GOAT_SCREAMING_AMBIENT)).
            cooldown(new CooldownEntry.Randomized(12000, 36000)).
            build());

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.GOAT.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.GOAT.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateSheep(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(18);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.SHEEP_FOOD)));

        // Shearing
        for (DyeColor value : DyeColor.values())
        {
            interactions.add(AnimalInteractionBuilder.create("shearing").
                ingredient(CustomIngredient.merge(CustomIngredient.of(Items.SHEARS),
                    CustomIngredient.of(AnimalPenTags.COMMON_SHEARS))).
                lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/shear/wool"), 320, true)).
                conditions(new ConditionEntry.MobCondition("Color", Operator.EQ, new IntValue(value.getId()))).
                consume(new ConsumerEntry.Damage(1)).
                cooldown(new CooldownEntry.Static(1200)).
                sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.SHEEP_SHEAR)).
                redstoneBit(2).
                runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOB_SET_SHEARED.get(), null, true)).
                finishFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOB_SET_SHEARED.get(), null, false)).
                textLines(TextEntry.ready("display.animal_pen.full_ready",
                    CustomIngredient.of(AnimalPenItemHelper.ITEM_BY_DYE.get(value)))).
                textLines(TextEntry.cooldown("display.animal_pen.wool_cooldown",
                    CustomIngredient.of(AnimalPenItemHelper.ITEM_BY_DYE.get(value)))).
                build());
        }

        // Dye
        interactions.add(AnimalInteractionBuilder.create("dye").
            ingredient(CustomIngredient.of(Items.WHITE_DYE,
                Items.ORANGE_DYE,
                Items.MAGENTA_DYE,
                Items.LIGHT_BLUE_DYE,
                Items.YELLOW_DYE,
                Items.LIME_DYE,
                Items.PINK_DYE,
                Items.GRAY_DYE,
                Items.LIGHT_GRAY_DYE,
                Items.CYAN_DYE,
                Items.PURPLE_DYE,
                Items.BLUE_DYE,
                Items.BROWN_DYE,
                Items.GREEN_DYE,
                Items.RED_DYE,
                Items.BLACK_DYE)).
            consume(new ConsumerEntry.Replace()).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.DYE_USE)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.SHEEP_CHANGE_COLOR.get())).
            textLines(TextEntry.ready("display.animal_pen.color_ready",
                CustomIngredient.of(AnimalPenItemHelper.ITEM_BY_DYE.values().stream().map(ItemStack::new)))).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.SHEEP_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.SHEEP.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.SHEEP.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateTurtle(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.TURTLE_FOOD),
            FunctionKey.of(AnimalPenFunctionRegistry.TURTLE_DROP_SCUTE.get())));

        // Egg Dropping
        interactions.add(AnimalInteractionBuilder.create("eggs").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bucket/turtle_egg"), 320, true)).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.TURTLE_LAY_EGG)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.TURTLE_EGG))).
            textLines(TextEntry.cooldown("display.animal_pen.egg_cooldown", CustomIngredient.of(Items.TURTLE_EGG))).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.TURTLE_SWIM));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.TURTLE.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.TURTLE.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateFish(CachedOutput cache, EntityType<?> entityType, Item resultItem)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        if (entityType != EntityType.TADPOLE)
        {
            interactions.add(this.generateFood(CustomIngredient.of(Items.KELP, Items.SEAGRASS)));
        }

        // Water Pickup
        interactions.add(this.generateBucketable(Items.WATER_BUCKET, resultItem, 2));

        // ambient
        if (entityType == EntityType.COD)
        {
            interactions.add(this.generateAmbientSound(SoundEvents.COD_AMBIENT));
        }
        else if (entityType == EntityType.PUFFERFISH)
        {
            interactions.add(this.generateAmbientSound(SoundEvents.PUFFER_FISH_FLOP));
        }
        else if (entityType == EntityType.SALMON)
        {
            interactions.add(this.generateAmbientSound(SoundEvents.SALMON_AMBIENT));
        }
        else if (entityType == EntityType.TROPICAL_FISH)
        {
            interactions.add(this.generateAmbientSound(SoundEvents.TROPICAL_FISH_AMBIENT));
        }
        else if (entityType == EntityType.TADPOLE)
        {
            interactions.add(this.generateAmbientSound(SoundEvents.TADPOLE_FLOP));
        }

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(entityType.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(entityType.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateFrog(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(5);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(ItemTags.FROG_FOOD)));

        // Froglight
        interactions.add(AnimalInteractionBuilder.create("froglight").
            ingredient(CustomIngredient.of(Items.MAGMA_BLOCK)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/magma_cube/froglight"))).
            consume(new ConsumerEntry.Consume(true)).
            conditions(new ConditionEntry.MobCondition("variant",
                Operator.MATCH,
                new StringValue(
                "minecraft:temperate"))).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.FROG_EAT)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready",
                CustomIngredient.of(Items.OCHRE_FROGLIGHT))).
            textLines(TextEntry.cooldown("display.animal_pen.frog_light_cooldown",
                CustomIngredient.of(Items.OCHRE_FROGLIGHT))).
            build());

        interactions.add(AnimalInteractionBuilder.create("froglight").
            ingredient(CustomIngredient.of(Items.MAGMA_BLOCK)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/magma_cube/froglight"))).
            consume(new ConsumerEntry.Consume(true)).
            conditions(new ConditionEntry.MobCondition("variant",
                Operator.MATCH,
                new StringValue(
                "minecraft:warm"))).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.FROG_EAT)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready",
                CustomIngredient.of(Items.PEARLESCENT_FROGLIGHT))).
            textLines(TextEntry.cooldown("display.animal_pen.frog_light_cooldown",
                CustomIngredient.of(Items.PEARLESCENT_FROGLIGHT))).
            build());

        interactions.add(AnimalInteractionBuilder.create("froglight").
            ingredient(CustomIngredient.of(Items.MAGMA_BLOCK)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/magma_cube/froglight"))).
            consume(new ConsumerEntry.Consume(true)).
            conditions(new ConditionEntry.MobCondition("variant",
                Operator.MATCH,
                new StringValue(
                "minecraft:cold"))).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.FROG_EAT)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready",
                CustomIngredient.of(Items.VERDANT_FROGLIGHT))).
            textLines(TextEntry.cooldown("display.animal_pen.frog_light_cooldown",
                CustomIngredient.of(Items.VERDANT_FROGLIGHT))).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.FROG_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.FROG.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.FROG.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateAllay(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(5);

        // Food
        CustomIngredient food = CustomIngredient.of(Items.AMETHYST_SHARD);

        interactions.add(AnimalInteractionBuilder.create("feeding").
            ingredient(food).
            consume(new ConsumerEntry.Consume(true)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.DUPLICATE.get())).
            cooldown(new CooldownEntry.Linear(1160, 20, 6000)).
            textLines(TextEntry.ready("display.animal_pen.food_ready", food)).
            textLines(TextEntry.cooldown("display.animal_pen.food_cooldown", food)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.AMETHYST_BLOCK_CHIME)).
            redstoneBit(1).
            build());

        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.ALLAY_AMBIENT_WITHOUT_ITEM));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.ALLAY.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.ALLAY.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateSniffer(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        CustomIngredient food = CustomIngredient.of(ItemTags.SNIFFER_FOOD);
        interactions.add(this.generateFood(food));
        // Egg Dropping
        interactions.add(AnimalInteractionBuilder.create("eggs").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/bucket/sniffer_egg"), 320, true)).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.SNIFFER_EGG_PLOP)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.SNIFFER_EGG))).
            textLines(TextEntry.cooldown("display.animal_pen.egg_cooldown", CustomIngredient.of(Items.SNIFFER_EGG))).
            build());
        // Seed Pickup
        interactions.add(AnimalInteractionBuilder.create("sniff").
            ingredient(CustomIngredient.of(Items.BOWL)).
            lootEntry(LootEntry.of(BuiltInLootTables.SNIFFER_DIGGING.identifier(), 320, true)).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            consume(new ConsumerEntry.Interact()).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.SNIFFER_DIGGING)).
            redstoneBit(3).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD))).
            textLines(TextEntry.cooldown("display.animal_pen.sniff_cooldown", CustomIngredient.of(Items.TORCHFLOWER_SEEDS, Items.PITCHER_POD))).
            build());
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.SNIFFER_IDLE));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.SNIFFER.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.SNIFFER.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }


    private CompletableFuture<?> generateArmadillo(CachedOutput cache)
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        CustomIngredient food = CustomIngredient.of(ItemTags.ARMADILLO_FOOD);
        interactions.add(this.generateFood(food));
        // Brushing
        interactions.add(AnimalInteractionBuilder.create("brush").
            ingredient(CustomIngredient.merge(CustomIngredient.of(Items.BRUSH),
                CustomIngredient.of(AnimalPenTags.COMMON_BRUSHES))).
            lootEntry(LootEntry.of(AnimalPen.resourceOf("animal_interactions/brush/armadillo_scute"))).
            consume(new ConsumerEntry.Damage(16)).
            sound(BuiltInRegistries.SOUND_EVENT.getKey(SoundEvents.ARMADILLO_BRUSH)).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.ARMADILLO_SCUTE))).
            textLines(TextEntry.cooldown("display.animal_pen.brush_cooldown", CustomIngredient.of(Items.ARMADILLO_SCUTE))).
            build());
        // ambient
        interactions.add(this.generateAmbientSound(SoundEvents.ARMADILLO_AMBIENT));

        JsonElement json = AnimalInteractionEntry.CODEC.
            encodeStart(JsonOps.INSTANCE,
                AnimalInteractionEntry.of(EntityType.ARMADILLO.builtInRegistryHolder().key(), interactions)).
            getOrThrow();

        Path file = this.pathProvider.json(EntityType.ARMADILLO.arch$registryName());

        return DataProvider.saveStable(cache, json, file);
    }
    
    
// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    private final PackOutput.PathProvider pathProvider;

    private final CompletableFuture<HolderLookup.Provider> registries;
}