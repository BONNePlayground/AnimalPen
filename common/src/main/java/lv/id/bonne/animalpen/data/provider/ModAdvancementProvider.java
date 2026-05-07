//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import com.mojang.datafixers.util.Pair;
import java.util.List;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalItemUseTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalVariantChangeTrigger;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;


public interface ModAdvancementProvider
{
    AdvancementHolder generatePlatformAdvancement(Consumer<AdvancementHolder> consumer,
        Advancement.Builder builder,
        Identifier resourceLocation);

    default void buildModAdvancements(Consumer<AdvancementHolder> consumer, HolderLookup.Provider provider)
    {
        AdvancementHolder root = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                display(
                    AnimalPenBlockRegistry.ANIMAL_PENS.values().iterator().next().get().asItem(),
                    Component.translatable("advancements.animal_pen.root.title"),
                    Component.translatable("advancements.animal_pen.root.description"),
                    Identifier.tryParse("minecraft:gui/advancements/backgrounds/husbandry"),
                    AdvancementType.TASK,
                    false, // no toast
                    false, // no chat
                    true   // hidden until earned
                ).
                addCriterion("has_any_catching_item",
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(
                            provider.lookupOrThrow(Registries.ITEM),
                            AnimalPensItemRegistry.ANIMAL_CAGE.get(),
                            AnimalPensItemRegistry.ANIMAL_CONTAINER.get(),
                            AnimalPensItemRegistry.BIRD_CATCHER.get()).build())
                ),
            AnimalPen.resourceOf("root"));

        AdvancementHolder firstAnimal = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.ANIMAL_CAGE.get(),
                    Component.translatable("advancements.animal_pen.first_catch.title"),
                    Component.translatable("advancements.animal_pen.first_catch.description"),
                    null,
                    AdvancementType.TASK,
                    true,  // show toast
                    true,  // announce to chat
                    false  // not hidden
                ).
                addCriterion("caught_animal",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(provider, AnimalPensItemRegistry.ANIMAL_CAGE.get())
                ),
            AnimalPen.resourceOf("animal_pen/first_catch"));

        List<Pair<EntityType<?>, Item>> animals = List.of(
            Pair.of(EntityType.ARMADILLO, Items.ARMADILLO_SPAWN_EGG),
            Pair.of(EntityType.CAMEL, Items.CAMEL_SPAWN_EGG),
            Pair.of(EntityType.CAT, Items.CAT_SPAWN_EGG),
            Pair.of(EntityType.CHICKEN, Items.CHICKEN_SPAWN_EGG),
            Pair.of(EntityType.COW, Items.COW_SPAWN_EGG),
            Pair.of(EntityType.DONKEY, Items.DONKEY_SPAWN_EGG),
            Pair.of(EntityType.FOX, Items.FOX_SPAWN_EGG),
            Pair.of(EntityType.FROG, Items.FROG_SPAWN_EGG),
            Pair.of(EntityType.GOAT, Items.GOAT_SPAWN_EGG),
            Pair.of(EntityType.HOGLIN, Items.HOGLIN_SPAWN_EGG),
            Pair.of(EntityType.HORSE, Items.HORSE_SPAWN_EGG),
            Pair.of(EntityType.LLAMA, Items.LLAMA_SPAWN_EGG),
            Pair.of(EntityType.MOOSHROOM, Items.MOOSHROOM_SPAWN_EGG),
            Pair.of(EntityType.MULE, Items.MULE_SPAWN_EGG),
            Pair.of(EntityType.OCELOT, Items.OCELOT_SPAWN_EGG),
            Pair.of(EntityType.PANDA, Items.PANDA_SPAWN_EGG),
            Pair.of(EntityType.PIG, Items.PIG_SPAWN_EGG),
            Pair.of(EntityType.POLAR_BEAR, Items.POLAR_BEAR_SPAWN_EGG),
            Pair.of(EntityType.RABBIT, Items.RABBIT_SPAWN_EGG),
            Pair.of(EntityType.SHEEP, Items.SHEEP_SPAWN_EGG),
            Pair.of(EntityType.SKELETON_HORSE, Items.SKELETON_HORSE_SPAWN_EGG),
            Pair.of(EntityType.SNIFFER, Items.SNIFFER_SPAWN_EGG),
            Pair.of(EntityType.STRIDER, Items.STRIDER_SPAWN_EGG),
            Pair.of(EntityType.TRADER_LLAMA, Items.TRADER_LLAMA_SPAWN_EGG),
            Pair.of(EntityType.WOLF, Items.WOLF_SPAWN_EGG),
            Pair.of(EntityType.ZOMBIE_HORSE, Items.ZOMBIE_HORSE_SPAWN_EGG)
        );

        animals.forEach(animalPair ->
        {
            EntityType<?> animal = animalPair.getFirst();

            AdvancementHolder advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstAnimal).
                    display(
                        animalPair.getSecond(),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        AdvancementType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(provider, animal,
                            AnimalPensItemRegistry.ANIMAL_CAGE.get())),
                AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_catch"));

            if (animal == EntityType.CHICKEN || animal == EntityType.COW || animal == EntityType.MOOSHROOM || animal == EntityType.GOAT || animal == EntityType.SNIFFER)
            {
                // Interact with bucket
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.BUCKET,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_bucket.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_bucket.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.BUCKET)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_bucket"));
            }

            if (animal == EntityType.MOOSHROOM)
            {
                // Interact with bowl
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.BOWL,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_bowl.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_bowl.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.BOWL)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_bowl"));

                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.DANDELION,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_flower.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_flower.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                ItemTags.SMALL_FLOWERS)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_flower"));
            }

            if (animal == EntityType.SHEEP)
            {
                // Shearing
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.SHEARS,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_shear.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_shear.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.SHEARS)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_shear"));

                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.WHITE_DYE,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_dye.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_dye.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.WHITE_DYE, Items.ORANGE_DYE, Items.MAGENTA_DYE, Items.LIGHT_BLUE_DYE,
                                Items.YELLOW_DYE, Items.LIME_DYE, Items.PINK_DYE, Items.GRAY_DYE, Items.LIGHT_GRAY_DYE,
                                Items.CYAN_DYE, Items.PURPLE_DYE, Items.BLUE_DYE, Items.BROWN_DYE, Items.GREEN_DYE,
                                Items.RED_DYE, Items.BLACK_DYE)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_dye"));
            }

            if (animal == EntityType.FROG)
            {
                // Froglight
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.OCHRE_FROGLIGHT,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_froglight.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_froglight.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.MAGMA_BLOCK)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_froglight"));
            }

            if (animal == EntityType.SNIFFER)
            {
                // Shearing
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.BOWL,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_bowl.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_bowl.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.BOWL)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_bowl"));
            }

            if (animal == EntityType.ARMADILLO)
            {
                // Brush
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.BRUSH,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_brush.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_brush.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.BRUSH)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_brush"));
            }
        });

        // First aquatic catch
        AdvancementHolder firstAquatic = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.ANIMAL_CONTAINER.get(),
                    Component.translatable("advancements.animal_pen.aquatic_keeper.title"),
                    Component.translatable("advancements.animal_pen.aquatic_keeper.description"),
                    null,
                    AdvancementType.TASK,
                    true,
                    false,
                    false
                ).
                addCriterion("caught_aquatic",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(provider, AnimalPensItemRegistry.ANIMAL_CONTAINER.get())

                ),
            AnimalPen.resourceOf("animal_pen/aquatic_keeper"));

        List<Pair<EntityType<?>, Item>> aquatics = List.of(
            Pair.of(EntityType.AXOLOTL, Items.AXOLOTL_SPAWN_EGG),
            Pair.of(EntityType.COD, Items.COD_SPAWN_EGG),
            Pair.of(EntityType.PUFFERFISH, Items.PUFFERFISH_SPAWN_EGG),
            Pair.of(EntityType.SALMON, Items.SALMON_SPAWN_EGG),
            Pair.of(EntityType.TROPICAL_FISH, Items.TROPICAL_FISH_SPAWN_EGG),
            Pair.of(EntityType.DOLPHIN, Items.DOLPHIN_SPAWN_EGG),
            Pair.of(EntityType.SQUID, Items.SQUID_SPAWN_EGG),
            Pair.of(EntityType.GLOW_SQUID, Items.GLOW_SQUID_SPAWN_EGG),
            Pair.of(EntityType.TURTLE, Items.TURTLE_SPAWN_EGG),
            Pair.of(EntityType.FROG, Items.FROG_SPAWN_EGG),
            Pair.of(EntityType.TADPOLE, Items.TADPOLE_SPAWN_EGG)
        );

        aquatics.forEach(animalPair ->
        {
            EntityType<?> animal = animalPair.getFirst();

            AdvancementHolder advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstAquatic).
                    display(
                        animalPair.getSecond(),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        AdvancementType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(provider, animal,
                            AnimalPensItemRegistry.ANIMAL_CONTAINER.get())),
                AnimalPen.resourceOf("animal_pen/water_container/" + animal.getDescriptionId() + "_catch"));

            if (animal == EntityType.TURTLE)
            {
                // Interact with bucket
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.BUCKET,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_bucket.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_bucket.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.BUCKET)),
                    AnimalPen.resourceOf("animal_pen/water_container/" + animal.getDescriptionId() + "_bucket"));
            }

            if (animal == EntityType.AXOLOTL || animal == EntityType.COD || animal == EntityType.PUFFERFISH ||
                animal == EntityType.TROPICAL_FISH || animal == EntityType.SALMON || animal == EntityType.TADPOLE)
            {
                // Interact with water bucket
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.WATER_BUCKET,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_water_bucket.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_water_bucket.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.WATER_BUCKET)),
                    AnimalPen.resourceOf("animal_pen/water_container/" + animal.getDescriptionId() + "_water_bucket"));
            }

            if (animal == EntityType.FROG)
            {
                // Froglight
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.OCHRE_FROGLIGHT,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_froglight.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_froglight.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.MAGMA_BLOCK)),
                    AnimalPen.resourceOf("animal_pen/water_container/" + animal.getDescriptionId() + "_froglight"));
            }
        });

        // First bird catch
        AdvancementHolder firstFlyer = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.BIRD_CATCHER.get(),
                    Component.translatable("advancements.animal_pen.bird_watcher.title"),
                    Component.translatable("advancements.animal_pen.bird_watcher.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false
                ).
                addCriterion("caught_bird",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(provider, AnimalPensItemRegistry.BIRD_CATCHER.get())
                ),
            AnimalPen.resourceOf("animal_pen/bird_watcher"));

        List<Pair<EntityType<?>, Item>> flyers = List.of(
            Pair.of(EntityType.BEE, Items.BEE_SPAWN_EGG),
            Pair.of(EntityType.BAT, Items.BAT_SPAWN_EGG),
            Pair.of(EntityType.PARROT, Items.PARROT_SPAWN_EGG),
            Pair.of(EntityType.HAPPY_GHAST, Items.HAPPY_GHAST_SPAWN_EGG),
            Pair.of(EntityType.ALLAY, Items.ALLAY_SPAWN_EGG)
        );

        flyers.forEach(animalPair ->
        {
            EntityType<?> animal = animalPair.getFirst();

            AdvancementHolder advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstFlyer).
                    display(
                        animalPair.getSecond(),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        AdvancementType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(provider, animal,
                            AnimalPensItemRegistry.BIRD_CATCHER.get())),
                AnimalPen.resourceOf("animal_pen/bird_catcher/" + animal.getDescriptionId() + "_catch"));

            if (animal == EntityType.BEE)
            {
                // Shearing
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.SHEARS,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_shear.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_shear.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.SHEARS)),
                    AnimalPen.resourceOf("animal_pen/bird_catcher/" + animal.getDescriptionId() + "_shear"));

                // Bottle
                this.generatePlatformAdvancement(consumer,
                    Advancement.Builder.advancement().
                        parent(advancement).
                        display(
                            Items.GLASS_BOTTLE,
                            Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + "_bottle.title"),
                            Component.translatable(
                                "advancements.animal_pen." + animal.getDescriptionId() + "_bottle.description"),
                            null,
                            AdvancementType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(provider, animal,
                                Items.GLASS_BOTTLE)),
                    AnimalPen.resourceOf("animal_pen/bird_catcher/" + animal.getDescriptionId() + "_bottle"));
            }
        });

        // Release Animal Trigger
        this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    Items.LEAD,
                    Component.translatable("advancements.animal_pen.release.title"),
                    Component.translatable("advancements.animal_pen.release.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false
                ).
                addCriterion("release",
                    AnimalItemUseTrigger.TriggerInstance.releaseAnimal()
                ),
            AnimalPen.resourceOf("animal_pen/release"));

        // Change Animal Variant trigger
        this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    Items.MAP,
                    Component.translatable("advancements.animal_pen.variant_change.title"),
                    Component.translatable("advancements.animal_pen.variant_change.description"),
                    null,
                    AdvancementType.TASK,
                    true, true, false
                ).
                addCriterion("variant_change",
                    AnimalVariantChangeTrigger.TriggerInstance.changeVariant()
                ),
            AnimalPen.resourceOf("animal_pen/variant_change"));
    }
}
