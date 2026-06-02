//
// Created by BONNe
// Copyright - 2026
//


package lv.id.bonne.animalpen.config.screen;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lv.id.bonne.animalpen.AnimalPen;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;


public class AnimalPenConfigScreen
{
    public static Screen createConfigScreen(Screen parent)
    {
        ConfigBuilder builder = ConfigBuilder.create().
            setParentScreen(parent).
            setTitle(Component.translatable("title.animal_pen.config"));

        buildClientConfig(builder);
        buildServerConfig(builder);

        builder.setSavingRunnable(() -> {
            try
            {
                AnimalPen.CONFIG_MANAGER.writeConfig(true);
            }
            catch (IOException e)
            {
                AnimalPen.LOGGER.error("Failed to save config", e);
            }
        });

        return builder.build();
    }


    private static void buildClientConfig(ConfigBuilder builder)
    {
        ConfigCategory general = builder.getOrCreateCategory(
            Component.translatable("category.animal_pen.general"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.show_cooldowns_while_crouching"),
                AnimalPen.config().isShowCooldownsOnCrouch()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.show_cooldowns_while_crouching.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setShowCooldownsOnCrouch(newValue)).
            build());
        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.show_all_interactions_above"),
                AnimalPen.config().isShowAllInteractions()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.show_all_interactions_above.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setShowAllInteractions(newValue)).
            build());
        general.addEntry(entryBuilder.
            startFloatField(Component.translatable("option.animal_pen.growth_multiplier"),
                AnimalPen.config().getAnimalPenMobSize()).
            setDefaultValue(0.001f).
            setTooltip(tooltips("option.animal_pen.growth_multiplier.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setGrowthMultiplier(newValue)).
            build());

        SubCategoryBuilder animalPen =
            entryBuilder.startSubCategory(Component.translatable("category.animal_pen.animal_pen"));
        animalPen.add(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.animal_pen_mob_can_grow"),
                AnimalPen.config().isGrowAnimalPenMob()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.animal_pen_mob_can_grow.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setGrowAnimalPenMob(newValue)).
            build());
        animalPen.add(entryBuilder.
            startFloatField(Component.translatable("option.animal_pen.animal_pen_mob_size"),
                AnimalPen.config().getAnimalPenMobSize()).
            setDefaultValue(0.33f).
            setTooltip(tooltips("option.animal_pen.animal_pen_mob_size.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setAnimalPenMobSize(newValue)).
            build());
        general.addEntry(animalPen.build());

        SubCategoryBuilder aquarium = entryBuilder.startSubCategory(
            Component.translatable("category.animal_pen.aquarium"));
        aquarium.add(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.aquarium_mob_can_grow"),
                AnimalPen.config().isGrowAquariumMob()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.aquarium_mob_can_grow.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setGrowAquariumMob(newValue)).
            build());
        aquarium.add(entryBuilder.
            startFloatField(Component.translatable("option.animal_pen.aquarium_mob_size"),
                AnimalPen.config().getAquariumMobSize()).
            setDefaultValue(0.33f).
            setTooltip(tooltips("option.animal_pen.aquarium_mob_size.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setAquariumMobSize(newValue)).
            build());
        general.addEntry(aquarium.build());

        SubCategoryBuilder aviary = entryBuilder.startSubCategory(
            Component.translatable("category.animal_pen.aviary"));
        aviary.add(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.aviary_mob_can_grow"),
                AnimalPen.config().isGrowAviaryMob()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.aviary_mob_can_grow.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setGrowAviaryMob(newValue)).
            build());
        aviary.add(entryBuilder.
            startFloatField(Component.translatable("option.animal_pen.aviary_mob_size"),
                AnimalPen.config().getAviaryMobSize()).
            setDefaultValue(0.33f).
            setTooltip(tooltips("option.animal_pen.aviary_mob_size.tooltip")).
            setSaveConsumer(newValue ->
                AnimalPen.config().setAviaryMobSize(newValue)).
            build());
        general.addEntry(aviary.build());
    }


    private static void buildServerConfig(ConfigBuilder builder)
    {
        ConfigCategory general = builder.getOrCreateCategory(
            Component.translatable("category.animal_pen.server"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.trigger_advancements"),
                AnimalPen.config().isTriggerAdvancements()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.trigger_advancements.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setTriggerAdvancements(newValue)).
            build());
        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.increase_statistics"),
                AnimalPen.config().isIncreaseStatistics()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.increase_statistics.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setIncreaseStatistics(newValue)).
            build());

        general.addEntry(entryBuilder.
            startIntField(Component.translatable("option.animal_pen.attack_cooldown"),
                AnimalPen.config().getAttackCooldown()).
            setDefaultValue(5).
            setTooltip(tooltips("option.animal_pen.attack_cooldown.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setAttackCooldown(newValue)).
            build());

        general.addEntry(entryBuilder.
            startLongField(Component.translatable("option.animal_pen.animal_limit_in_pen"),
                AnimalPen.config().getMaximalAnimalCount()).
            setDefaultValue(Integer.MAX_VALUE).
            setTooltip(tooltips("option.animal_pen.animal_limit_in_pen.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setMaximalAnimalCount(newValue)).
            build());

        general.addEntry(entryBuilder.
            startIntField(Component.translatable("option.animal_pen.max_stored_animal_variants"),
                AnimalPen.config().getMaxStoredVariants()).
            setDefaultValue(16).
            setTooltip(tooltips("option.animal_pen.max_stored_animal_variants.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setMaxStoredAnimalVariants(newValue)).
            build());

        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.block_dispenser_interactions"),
                AnimalPen.config().isBlockDispenserInteractions()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.block_dispenser_interactions.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setBlockDispenserInteractions(newValue)).
            build());

        general.addEntry(entryBuilder.
            startBooleanToggle(Component.translatable("option.animal_pen.debug"),
                AnimalPen.config().isDebug()).
            setDefaultValue(false).
            setTooltip(tooltips("option.animal_pen.debug.tooltip")).
            setRequirement(LOCAL_OR_OP_REQUIREMENT).
            setSaveConsumer(newValue ->
                AnimalPen.config().setDebug(newValue)).
            build());
    }


    private static boolean isLocalSide()
    {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.level == null)
        {
            return true;
        }

        // Check if single-player/local world
        if (minecraft.hasSingleplayerServer())
        {
            return true;
        }

        return false;
    }


    private static Optional<Component[]> tooltips(String key)
    {
        List<Component> lines = new ArrayList<>(10);
        
        if (I18n.exists(key))
        {
            
            lines.add(Component.translatable(key));
        }
        
        for (int i = 0; i < 10; i++)
        {
            String subLine = key + "[" + i + "]";
            
            if (I18n.exists(subLine))
            {
                lines.add(Component.translatable(subLine));
            }
        }
        
        if (lines.isEmpty()) return Optional.empty();
        
        return Optional.of(lines.toArray(new Component[0]));
    }


    private static final Requirement LOCAL_OR_OP_REQUIREMENT = AnimalPenConfigScreen::isLocalSide;
}
