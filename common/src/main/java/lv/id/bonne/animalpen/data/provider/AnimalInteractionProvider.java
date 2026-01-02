package lv.id.bonne.animalpen.data.provider;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import org.jetbrains.annotations.NotNull;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.interaction.condition.ConditionEntry;
import lv.id.bonne.animalpen.interaction.condition.Operator;
import lv.id.bonne.animalpen.interaction.cooldown.CooldownEntry;
import lv.id.bonne.animalpen.interaction.function.FunctionKey;
import lv.id.bonne.animalpen.interaction.ingredient.CustomIngredient;
import lv.id.bonne.animalpen.interaction.model.AnimalInteraction;
import lv.id.bonne.animalpen.interaction.model.AnimalInteractionBuilder;
import lv.id.bonne.animalpen.interaction.textentry.TextEntry;
import lv.id.bonne.animalpen.interaction.textentry.TextEntryVisibility;
import lv.id.bonne.animalpen.registries.AnimalPenFunctionRegistry;
import lv.id.bonne.animalpen.registries.AnimalPenTags;
import lv.id.bonne.animalpen.util.AnimalPenCompoundTags;
import lv.id.bonne.animalpen.util.AnimalPenItemHelper;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;


/**
 * Animal Interaction Data Provider.
 */
public class AnimalInteractionProvider implements DataProvider
{
    public AnimalInteractionProvider(DataGenerator generator)
    {
        this.generator = generator;
    }


    @Override
    public void run(HashCache cache) throws IOException
    {
        Path basePath = this.generator.getOutputFolder().resolve("data").
            resolve("minecraft").
            resolve("animal_interactions");

        // Default minecraft animals with custom implementations
        this.generateAxolotl(basePath, cache);
        this.generateBee(basePath, cache);
        this.generateChicken(basePath, cache);
        this.generateCow(basePath, cache);
        this.generateGoat(basePath, cache);
        this.generateMooshroom(basePath, cache);
        this.generateSheep(basePath, cache);
        this.generateTurtle(basePath, cache);

        // Fishes
        this.generateFish(basePath, cache, "cod", Items.COD_BUCKET);
        this.generateFish(basePath, cache, "pufferfish", Items.PUFFERFISH_BUCKET);
        this.generateFish(basePath, cache, "salmon", Items.SALMON_BUCKET);
        this.generateFish(basePath, cache, "tropical_fish", Items.TROPICAL_FISH_BUCKET);

        // Only food animals
        this.generateWithFood(basePath, cache, "cat",
            CustomIngredient.of(Items.COD, Items.SALMON));
        this.generateWithFood(basePath, cache, "dolphin",
            CustomIngredient.of(AnimalPenItemHelper.itemTag("fishes")));
        this.generateWithFood(basePath, cache, "fox",
            CustomIngredient.of(AnimalPenItemHelper.itemTag("fox_food")));
        this.generateWithFood(basePath, cache, "glow_squid",
            CustomIngredient.of(AnimalPenItemHelper.itemTag("fishes")));
        this.generateWithFood(basePath, cache, "hoglin",
            CustomIngredient.of(Items.CRIMSON_FUNGUS));
        this.generateWithFood(basePath, cache, "llama",
            CustomIngredient.of(Items.WHEAT, Items.HAY_BLOCK));
        this.generateWithFood(basePath, cache, "ocelot",
            CustomIngredient.of(Items.COD, Items.SALMON));
        this.generateWithFood(basePath, cache, "panda",
            CustomIngredient.of(Items.BAMBOO));
        this.generateWithFood(basePath, cache, "pig",
            CustomIngredient.of(Items.CARROT, Items.POTATO, Items.BEETROOT));
        this.generateWithFood(basePath, cache, "rabbit",
            CustomIngredient.of(Items.CARROT, Items.GOLDEN_CARROT, Items.DANDELION));
        this.generateWithFood(basePath, cache, "squid",
            CustomIngredient.of(AnimalPenItemHelper.itemTag("fishes")));
        this.generateWithFood(basePath, cache, "strider",
            CustomIngredient.of(Items.WARPED_FUNGUS));
        this.generateWithFood(basePath, cache, "trader_llama",
            CustomIngredient.of(Items.WHEAT, Items.HAY_BLOCK));

        this.generateWithFood(basePath, cache, "wolf",
            CustomIngredient.merge(CustomIngredient.of(AnimalPenItemHelper.itemTag("meat")),
                CustomIngredient.of(Items.COD,
                    Items.COOKED_COD,
                    Items.SALMON,
                    Items.COOKED_SALMON,
                    Items.TROPICAL_FISH,
                    Items.PUFFERFISH,
                    Items.RABBIT_STEW)));

        CustomIngredient horseFood = CustomIngredient.of(Items.WHEAT,
            Items.SUGAR,
            Items.HAY_BLOCK,
            Items.APPLE,
            Items.GOLDEN_CARROT,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE);
        this.generateWithFood(basePath, cache, "donkey", horseFood);
        this.generateWithFood(basePath, cache, "horse", horseFood);
        this.generateWithFood(basePath, cache, "mule", horseFood);
        this.generateWithFood(basePath, cache, "skeleton_horse", horseFood);
        this.generateWithFood(basePath, cache, "zombie_horse", horseFood);
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
        return AnimalInteractionBuilder.create("feeding").
            ingredient(food).
            consume(true).
            perEntity(true).
            even(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL_DATA,
                Operator.GTE,
                AnimalPenCompoundTags.TAG_AMOUNT,
                2)).
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


    public void generateWithFood(Path basePath, HashCache cache, String animalName, CustomIngredient foodItem)
        throws IOException
    {
        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, List.of(this.generateFood(foodItem))).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve(animalName + ".json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateAxolotl(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(AnimalPenItemHelper.itemTag("axolotl_tempt_items"))));
        // Water Pickup
        interactions.add(AnimalInteractionBuilder.create("water_bucket_pickup").
            ingredient(CustomIngredient.of(Items.WATER_BUCKET)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.WATER_BUCKET_PICKUP.get())).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.AXOLOTL_BUCKET))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("axolotl.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateChicken(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.WHEAT_SEEDS,
            Items.MELON_SEEDS,
            Items.PUMPKIN_SEEDS,
            Items.BEETROOT_SEEDS)));

        // Egg Dropping
        interactions.add(AnimalInteractionBuilder.create("eggs").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bucket/egg")).
            perEntity(true).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            dropLimit(90).
            sound(ResourceLocation.tryParse("entity.chicken.egg")).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.EGG))).
            textLines(TextEntry.cooldown("display.animal_pen.egg_cooldown", CustomIngredient.of(Items.EGG))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("chicken.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateBee(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(AnimalPenItemHelper.itemTag("small_flowers"))));

        // Shears
        interactions.add(AnimalInteractionBuilder.create("shearing").
            ingredient(CustomIngredient.merge(CustomIngredient.of(Items.SHEARS),
                CustomIngredient.of(AnimalPenTags.FORGE_SHEARS),
                CustomIngredient.of(AnimalPenTags.COMMON_SHEARS))).
            lootTable(AnimalPen.resourceOf("animal_interactions/shear/honeycomb")).
            damage(1).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL_DATA,
                Operator.GTE,
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                5)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                -5)).
            sound(ResourceLocation.tryParse("block.beehive.shear")).
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
            lootTable(AnimalPen.resourceOf("animal_interactions/glass_bottle/honey_bottle")).
            consume(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL_DATA,
                Operator.GTE,
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                5)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                -5)).
            sound(ResourceLocation.tryParse("item.bottle.fill")).
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
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL_DATA,
                Operator.LT,
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL,
                10)).
            finishFunctions(FunctionKey.of(AnimalPenFunctionRegistry.INCREMENT_KEY.get(),
                AnimalPenCompoundTags.TAG_POLLEN_LEVEL)).
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

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("bee.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateCow(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.WHEAT)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket")).
            consume(true).
            sound(ResourceLocation.tryParse("entity.cow.milk")).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            textLines(TextEntry.cooldown("display.animal_pen.milk_cooldown", CustomIngredient.of(Items.MILK_BUCKET))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("cow.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateMooshroom(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.WHEAT)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket")).
            consume(true).
            sound(ResourceLocation.tryParse("entity.cow.milk")).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            build());
        // Soup pickup
        interactions.add(AnimalInteractionBuilder.create("stew").
            ingredient(CustomIngredient.of(Items.BOWL)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bowl/mushroom_stew")).
            consume(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.HAS, "EffectId", false)).
            sound(ResourceLocation.tryParse("entity.mooshroom.milk")).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MUSHROOM_STEW))).
            build());
        interactions.add(AnimalInteractionBuilder.create("stew").
            ingredient(CustomIngredient.of(Items.BOWL)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bowl/suspicious_stew")).
            consume(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.HAS, "EffectId", true)).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.MATCH, "Type", "brown")).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_REMOVE_EFFECT.get())).
            sound(ResourceLocation.tryParse("entity.mooshroom.suspicious_milk")).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.SUSPICIOUS_STEW))).
            build());
        // Flowers
        interactions.add(AnimalInteractionBuilder.create("flower").
            ingredient(CustomIngredient.of(AnimalPenItemHelper.itemTag("small_flowers"))).
            consume(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.HAS, "EffectId", false)).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.MATCH, "Type", "brown")).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_SET_EFFECT.get())).
            sound(ResourceLocation.tryParse("entity.mooshroom.eat")).
            textLines(TextEntry.ready("display.animal_pen.apply_ready", CustomIngredient.of(Items.SUSPICIOUS_STEW))).
            build());
        interactions.add(AnimalInteractionBuilder.create("flower").
            ingredient(CustomIngredient.of(AnimalPenItemHelper.itemTag("small_flowers"))).
            consume(true).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.HAS, "EffectId", true)).
            conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.MATCH, "Type", "brown")).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.MOOSHROOM_FAILED_EFFECT.get())).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("mooshroom.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateGoat(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.WHEAT)));
        // Milk Pickup
        interactions.add(AnimalInteractionBuilder.create("milk").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bucket/milk_bucket")).
            consume(true).
            sound(ResourceLocation.tryParse("entity.goat.milk")).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.MILK_BUCKET))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("goat.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateSheep(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(18);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.WHEAT)));

        // Shearing
        for (DyeColor value : DyeColor.values())
        {
            interactions.add(AnimalInteractionBuilder.create("shearing").
                ingredient(CustomIngredient.merge(CustomIngredient.of(Items.SHEARS),
                    CustomIngredient.of(AnimalPenTags.FORGE_SHEARS),
                    CustomIngredient.of(AnimalPenTags.COMMON_SHEARS))).
                lootTable(AnimalPen.resourceOf("animal_interactions/shear/wool/" + value.getName())).
                conditions(ConditionEntry.of(AnimalPenCompoundTags.TAG_ANIMAL, Operator.EQ, "Color", value.getId())).
                perEntity(true).
                damage(1).
                cooldown(new CooldownEntry.Randomized(1200, 6000)).
                sound(ResourceLocation.tryParse("entity.sheep.shear")).
                redstoneBit(2).
                dropLimit(320).
                runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.SHEEP_SET_SHEARED.get(), null, true)).
                finishFunctions(FunctionKey.of(AnimalPenFunctionRegistry.SHEEP_SET_SHEARED.get(), null, false)).
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
            consume(true).
            sound(ResourceLocation.tryParse("item.dye.use")).
            dropLimit(320).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.SHEEP_CHANGE_COLOR.get())).
            textLines(TextEntry.ready("display.animal_pen.color_ready",
                CustomIngredient.of(AnimalPenItemHelper.ITEM_BY_DYE.values().stream().map(ItemStack::new)))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("sheep.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateTurtle(Path basePath, HashCache cache) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.SEAGRASS),
            FunctionKey.of(AnimalPenFunctionRegistry.TURTLE_DROP_SCUTE.get())));

        // Egg Dropping
        interactions.add(AnimalInteractionBuilder.create("eggs").
            ingredient(CustomIngredient.of(Items.BUCKET)).
            lootTable(AnimalPen.resourceOf("animal_interactions/bucket/turtle_egg")).
            perEntity(true).
            cooldown(new CooldownEntry.Linear(6000, -20, 200)).
            dropLimit(90).
            sound(ResourceLocation.tryParse("entity.turtle.lay_egg")).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(Items.TURTLE_EGG))).
            textLines(TextEntry.cooldown("display.animal_pen.egg_cooldown", CustomIngredient.of(Items.TURTLE_EGG))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve("turtle.json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


    private void generateFish(Path basePath, HashCache cache, String fish, Item resultItem) throws IOException
    {
        List<AnimalInteraction> interactions = new ArrayList<>(2);

        // Food
        interactions.add(this.generateFood(CustomIngredient.of(Items.KELP, Items.SEAGRASS)));
        // Water Pickup
        interactions.add(AnimalInteractionBuilder.create("water_bucket_pickup").
            ingredient(CustomIngredient.of(Items.WATER_BUCKET)).
            runFunctions(FunctionKey.of(AnimalPenFunctionRegistry.WATER_BUCKET_PICKUP.get())).
            redstoneBit(2).
            textLines(TextEntry.ready("display.animal_pen.full_ready", CustomIngredient.of(resultItem))).
            build());

        JsonElement json = Codec.list(AnimalInteraction.CODEC).
            encodeStart(JsonOps.INSTANCE, interactions).
            getOrThrow(false, IllegalStateException::new);

        Path file = basePath.resolve(fish + ".json");
        Files.createDirectories(file.getParent());
        DataProvider.save(GSON, cache, json, file);
    }


// ---------------------------------------------------------------------
// Section: Variables
// ---------------------------------------------------------------------


    private final DataGenerator generator;

    private static final Gson GSON = new GsonBuilder().
        registerTypeAdapter(ResourceLocation.class, new ResourceLocation.Serializer()).
        setPrettyPrinting().
        disableHtmlEscaping().
        create();
}