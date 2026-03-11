//
// Created by BONNe
// Copyright - 2025
//


package lv.id.bonne.animalpen.data.provider;


import java.util.List;
import java.util.function.Consumer;

import lv.id.bonne.animalpen.AnimalPen;
import lv.id.bonne.animalpen.advancements.critereon.AnimalItemUseTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalInteractTrigger;
import lv.id.bonne.animalpen.advancements.critereon.AnimalVariantChangeTrigger;
import lv.id.bonne.animalpen.registries.AnimalPenBlockRegistry;
import lv.id.bonne.animalpen.registries.AnimalPensItemRegistry;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.FrameType;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SpawnEggItem;


public interface ModAdvancementProvider
{
    Advancement generatePlatformAdvancement(Consumer<Advancement> consumer,
        Advancement.Builder builder,
        ResourceLocation resourceLocation);

    default void buildModAdvancements(Consumer<Advancement> consumer)
    {
        Advancement root = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                display(
                    AnimalPenBlockRegistry.ANIMAL_PENS.values().iterator().next().get().asItem(),
                    Component.translatable("advancements.animal_pen.root.title"),
                    Component.translatable("advancements.animal_pen.root.description"),
                    new ResourceLocation("textures/gui/advancements/backgrounds/husbandry.png"),
                    FrameType.TASK,
                    false, // no toast
                    false, // no chat
                    true   // hidden until earned
                ).
                addCriterion("has_any_catching_item",
                    InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(
                            AnimalPensItemRegistry.ANIMAL_CAGE.get(),
                            AnimalPensItemRegistry.ANIMAL_CONTAINER.get(),
                            AnimalPensItemRegistry.BIRD_CATCHER.get()).build())
                ),
            AnimalPen.resourceOf("root"));

        Advancement firstAnimal = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.ANIMAL_CAGE.get(),
                    Component.translatable("advancements.animal_pen.first_catch.title"),
                    Component.translatable("advancements.animal_pen.first_catch.description"),
                    null,
                    FrameType.TASK,
                    true,  // show toast
                    true,  // announce to chat
                    false  // not hidden
                ).
                addCriterion("caught_animal",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(AnimalPensItemRegistry.ANIMAL_CAGE.get())
                ),
            AnimalPen.resourceOf("animal_pen/first_catch"));

        List<EntityType<?>> animals = List.of(
            EntityType.CAT,
            EntityType.CHICKEN,
            EntityType.COW,
            EntityType.DONKEY,
            EntityType.FOX,
            EntityType.FROG,
            EntityType.GOAT,
            EntityType.HOGLIN,
            EntityType.HORSE,
            EntityType.LLAMA,
            EntityType.MOOSHROOM,
            EntityType.MULE,
            EntityType.OCELOT,
            EntityType.PANDA,
            EntityType.PIG,
            EntityType.POLAR_BEAR,
            EntityType.RABBIT,
            EntityType.SHEEP,
            EntityType.SKELETON_HORSE,
            EntityType.STRIDER,
            EntityType.TRADER_LLAMA,
            EntityType.WOLF,
            EntityType.ZOMBIE_HORSE
        );

        animals.forEach(animal ->
        {
            Advancement advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstAnimal).
                    display(
                        SpawnEggItem.byId(animal),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        FrameType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(animal,
                            AnimalPensItemRegistry.ANIMAL_CAGE.get())),
                AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_catch"));

            if (animal == EntityType.CHICKEN || animal == EntityType.COW || animal == EntityType.MOOSHROOM || animal == EntityType.GOAT)
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
                                Items.MAGMA_BLOCK)),
                    AnimalPen.resourceOf("animal_pen/animal_cage/" + animal.getDescriptionId() + "_froglight"));
            }
        });

        // First aquatic catch
        Advancement firstAquatic = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.ANIMAL_CONTAINER.get(),
                    Component.translatable("advancements.animal_pen.aquatic_keeper.title"),
                    Component.translatable("advancements.animal_pen.aquatic_keeper.description"),
                    null,
                    FrameType.TASK,
                    true,
                    false,
                    false
                ).
                addCriterion("caught_aquatic",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(AnimalPensItemRegistry.ANIMAL_CONTAINER.get())

                ),
            AnimalPen.resourceOf("animal_pen/aquatic_keeper"));

        List<EntityType<?>> aquatics = List.of(
            EntityType.AXOLOTL,
            EntityType.COD,
            EntityType.PUFFERFISH,
            EntityType.SALMON,
            EntityType.TROPICAL_FISH,
            EntityType.DOLPHIN,
            EntityType.SQUID,
            EntityType.GLOW_SQUID,
            EntityType.TURTLE,
            EntityType.FROG,
            EntityType.TADPOLE
        );

        aquatics.forEach(animal ->
        {
            Advancement advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstAquatic).
                    display(
                        SpawnEggItem.byId(animal),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        FrameType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
                                Items.MAGMA_BLOCK)),
                    AnimalPen.resourceOf("animal_pen/water_container/" + animal.getDescriptionId() + "_froglight"));
            }
        });

        // First bird catch
        Advancement firstFlyer = this.generatePlatformAdvancement(consumer,
            Advancement.Builder.advancement().
                parent(root).
                display(
                    AnimalPensItemRegistry.BIRD_CATCHER.get(),
                    Component.translatable("advancements.animal_pen.bird_watcher.title"),
                    Component.translatable("advancements.animal_pen.bird_watcher.description"),
                    null,
                    FrameType.TASK,
                    true, true, false
                ).
                addCriterion("caught_bird",
                    AnimalItemUseTrigger.TriggerInstance.caughtWithItem(AnimalPensItemRegistry.BIRD_CATCHER.get())
                ),
            AnimalPen.resourceOf("animal_pen/bird_watcher"));

        List<EntityType<?>> flyers = List.of(EntityType.BEE,
            EntityType.BAT,
            EntityType.PARROT,
            EntityType.ALLAY);

        flyers.forEach(animal ->
        {
            Advancement advancement = this.generatePlatformAdvancement(consumer,
                Advancement.Builder.advancement().
                    parent(firstFlyer).
                    display(
                        SpawnEggItem.byId(animal),
                        Component.translatable("advancements.animal_pen." + animal.getDescriptionId() + ".title"),
                        Component.translatable(
                            "advancements.animal_pen." + animal.getDescriptionId() + ".description"),
                        null,
                        FrameType.TASK,
                        true,  // show toast
                        false,  // announce to chat
                        false  // not hidden
                    ).
                    addCriterion(animal.getDescriptionId(),
                        AnimalItemUseTrigger.TriggerInstance.caughtAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                            FrameType.TASK,
                            true,  // show toast
                            false,  // announce to chat
                            false  // not hidden
                        ).
                        addCriterion(animal.getDescriptionId(),
                            AnimalInteractTrigger.TriggerInstance.interactAnimalWithItem(animal,
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
                    FrameType.TASK,
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
                    FrameType.TASK,
                    true, true, false
                ).
                addCriterion("variant_change",
                    AnimalVariantChangeTrigger.TriggerInstance.changeVariant()
                ),
            AnimalPen.resourceOf("animal_pen/variant_change"));
    }
}
