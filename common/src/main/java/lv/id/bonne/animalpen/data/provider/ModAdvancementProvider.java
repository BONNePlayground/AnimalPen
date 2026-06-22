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
import net.minecraft.advancements.predicates.ItemPredicate;
import net.minecraft.advancements.triggers.InventoryChangeTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
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
            Pair.of(EntityTypes.ARMADILLO, Items.ARMADILLO_SPAWN_EGG),
            Pair.of(EntityTypes.CAMEL, Items.CAMEL_SPAWN_EGG),
            Pair.of(EntityTypes.CAT, Items.CAT_SPAWN_EGG),
            Pair.of(EntityTypes.CHICKEN, Items.CHICKEN_SPAWN_EGG),
            Pair.of(EntityTypes.COW, Items.COW_SPAWN_EGG),
            Pair.of(EntityTypes.DONKEY, Items.DONKEY_SPAWN_EGG),
            Pair.of(EntityTypes.FOX, Items.FOX_SPAWN_EGG),
            Pair.of(EntityTypes.FROG, Items.FROG_SPAWN_EGG),
            Pair.of(EntityTypes.GOAT, Items.GOAT_SPAWN_EGG),
            Pair.of(EntityTypes.HOGLIN, Items.HOGLIN_SPAWN_EGG),
            Pair.of(EntityTypes.HORSE, Items.HORSE_SPAWN_EGG),
            Pair.of(EntityTypes.LLAMA, Items.LLAMA_SPAWN_EGG),
            Pair.of(EntityTypes.MOOSHROOM, Items.MOOSHROOM_SPAWN_EGG),
            Pair.of(EntityTypes.MULE, Items.MULE_SPAWN_EGG),
            Pair.of(EntityTypes.OCELOT, Items.OCELOT_SPAWN_EGG),
            Pair.of(EntityTypes.PANDA, Items.PANDA_SPAWN_EGG),
            Pair.of(EntityTypes.PIG, Items.PIG_SPAWN_EGG),
            Pair.of(EntityTypes.POLAR_BEAR, Items.POLAR_BEAR_SPAWN_EGG),
            Pair.of(EntityTypes.RABBIT, Items.RABBIT_SPAWN_EGG),
            Pair.of(EntityTypes.SHEEP, Items.SHEEP_SPAWN_EGG),
            Pair.of(EntityTypes.SKELETON_HORSE, Items.SKELETON_HORSE_SPAWN_EGG),
            Pair.of(EntityTypes.SNIFFER, Items.SNIFFER_SPAWN_EGG),
            Pair.of(EntityTypes.STRIDER, Items.STRIDER_SPAWN_EGG),
            Pair.of(EntityTypes.TRADER_LLAMA, Items.TRADER_LLAMA_SPAWN_EGG),
            Pair.of(EntityTypes.WOLF, Items.WOLF_SPAWN_EGG),
            Pair.of(EntityTypes.ZOMBIE_HORSE, Items.ZOMBIE_HORSE_SPAWN_EGG)
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

            if (animal == EntityTypes.CHICKEN || animal == EntityTypes.COW || animal == EntityTypes.MOOSHROOM || animal == EntityTypes.GOAT || animal == EntityTypes.SNIFFER)
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

            if (animal == EntityTypes.MOOSHROOM)
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
                                BlockItemTags.SMALL_FLOWERS.item())),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_flower"));
            }

            if (animal == EntityTypes.SHEEP)
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
                            Items.DYE.white(),
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
                                Items.DYE.white(), Items.DYE.orange(), Items.DYE.magenta(), Items.DYE.lightBlue(),
                                Items.DYE.yellow(), Items.DYE.lime(), Items.DYE.pink(), Items.DYE.gray(), Items.DYE.lightGray(),
                                Items.DYE.cyan(), Items.DYE.purple(), Items.DYE.blue(), Items.DYE.brown(), Items.DYE.green(),
                                Items.DYE.red(), Items.DYE.black())),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_dye"));
            }

            if (animal == EntityTypes.FROG)
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

            if (animal == EntityTypes.SNIFFER)
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

            if (animal == EntityTypes.ARMADILLO)
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
            Pair.of(EntityTypes.AXOLOTL, Items.AXOLOTL_SPAWN_EGG),
            Pair.of(EntityTypes.COD, Items.COD_SPAWN_EGG),
            Pair.of(EntityTypes.PUFFERFISH, Items.PUFFERFISH_SPAWN_EGG),
            Pair.of(EntityTypes.SALMON, Items.SALMON_SPAWN_EGG),
            Pair.of(EntityTypes.TROPICAL_FISH, Items.TROPICAL_FISH_SPAWN_EGG),
            Pair.of(EntityTypes.DOLPHIN, Items.DOLPHIN_SPAWN_EGG),
            Pair.of(EntityTypes.SQUID, Items.SQUID_SPAWN_EGG),
            Pair.of(EntityTypes.GLOW_SQUID, Items.GLOW_SQUID_SPAWN_EGG),
            Pair.of(EntityTypes.TURTLE, Items.TURTLE_SPAWN_EGG),
            Pair.of(EntityTypes.FROG, Items.FROG_SPAWN_EGG),
            Pair.of(EntityTypes.TADPOLE, Items.TADPOLE_SPAWN_EGG)
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

            if (animal == EntityTypes.TURTLE)
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

            if (animal == EntityTypes.AXOLOTL || animal == EntityTypes.COD || animal == EntityTypes.PUFFERFISH ||
                animal == EntityTypes.TROPICAL_FISH || animal == EntityTypes.SALMON || animal == EntityTypes.TADPOLE)
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

            if (animal == EntityTypes.FROG)
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
            Pair.of(EntityTypes.BEE, Items.BEE_SPAWN_EGG),
            Pair.of(EntityTypes.BAT, Items.BAT_SPAWN_EGG),
            Pair.of(EntityTypes.PARROT, Items.PARROT_SPAWN_EGG),
            Pair.of(EntityTypes.HAPPY_GHAST, Items.HAPPY_GHAST_SPAWN_EGG),
            Pair.of(EntityTypes.ALLAY, Items.ALLAY_SPAWN_EGG)
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

            if (animal == EntityTypes.BEE)
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
